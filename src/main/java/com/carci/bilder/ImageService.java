package com.carci.bilder;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by carcinoma on 12.11.17.
 */
@Service
@Log
@Transactional
public class ImageService {

    public static final String LAST_IMAGE_ADDED = "last-image-added";
    @Autowired
    ImageRepo imageRepo;

    @Autowired
    KeyValueRepo keyValueRepo;

    @Autowired
    TagRepo tagRepo;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static Boolean kvSync = false;

    @CacheEvict(cacheNames = {"image","byCamera","byFolderName","byDate"}, allEntries = true)
    public void addImage(Image image) {
        imageRepo.save(image);
    }

    public Image getByPath(String path, String fileName) {
        return imageRepo.findByNameAndRealPath(fileName, path);
    }

    public List<Image> getAll() {
        return imageRepo.findAll();
    }

    public Map<String, List<Image>> getByFolder() {

        Iterable<Image> allImages = imageRepo.findAll(Sort.by("folderName").and(Sort.by("name")));

        Map<String, List<Image>> folders = new LinkedHashMap<>();

        allImages.forEach((image) -> {
            List<Image> images = folders.get(image.getFolderName());
            if(images == null) {
                images = new ArrayList<>();
                folders.put(image.getFolderName(), images);
            }
            images.add(image);
        });
        return folders;

    }

    public Map<String, List<Image>> getByDate() {

        Iterable<Image> allImages = imageRepo.findAll(Sort.by("snapDate").descending().and(Sort.by("name")));

        Map<String, List<Image>> snapDates = new LinkedHashMap<>();

        allImages.forEach((image) -> {
            String snapDate = sdf.format(image.getSnapDate());
            List<Image> images = snapDates.get(snapDate);
            if(images == null) {
                images = new ArrayList<>();
                snapDates.put(snapDate, images);
            }
            images.add(image);
        });
        return snapDates;

    }

    public Map<String, List<Image>> getByCamera() {

        Iterable<Image> allImages = imageRepo.findAll(Sort.by("cameraName").and(Sort.by("name")));

        Map<String, List<Image>> cameras = new LinkedHashMap<>();

        allImages.forEach((image) -> {
            List<Image> images = cameras.get(image.getCameraName());
            if(images == null) {
                images = new ArrayList<>();
                cameras.put(image.getCameraName(), images);
            }
            images.add(image);
        });
        return cameras;

    }

    public boolean hasAnyData() {
        return imageRepo.count() > 0;
    }

    public Date getLastAdded() {
        KeyValue lastImageAdded = keyValueRepo.findByKey(LAST_IMAGE_ADDED);
        if(lastImageAdded != null) {
            return new Date(Long.parseLong(lastImageAdded.getValue()));
        }
        return new Date(0);
    }

    public void setValue(String key, String value) {

        KeyValue kv = keyValueRepo.findByKey(key);

        if (kv == null) {
            kv = new KeyValue();
            kv.setKey(key);
        }

        kv.setValue(value);
        keyValueRepo.save(kv);

    }

    public List<Tag> getTags() {
        return tagRepo.findAll(Sort.by("name").and(Sort.by("images.name")));
    }

    public Tag createTag(String tag) {

        Tag storedTag = tagRepo.findByName(tag);
        if(storedTag == null) {
            storedTag = tagRepo.save(Tag.builder().name(tag).images(new ArrayList<>()).build());
        }
        return storedTag;

    }

    public Image getById(Long imageId) {
        return imageRepo.getOne(imageId);
    }

    public void addImageToTag(Image image, String tag) {

        Image storedImage = imageRepo.findById(image.getId()).get();
        Tag storedTag = tagRepo.findByName(tag);

        if(!storedTag.getImages().contains(storedImage)) {
            storedTag.getImages().add(storedImage);
            tagRepo.save(storedTag);
        }

    }

    public void setImageTags(Long imageId, List<String> tags) {

        Image storedImage = imageRepo.getOne(imageId);


        List<Tag> storedImageTags = storedImage.getTags();

        // save the current tag association
        tags.forEach((tag) -> {
            Tag tagByName = tagRepo.getByName(tag);
            if(!tagByName.getImages().contains(storedImage)) {
                tagByName.getImages().add(storedImage);
                tagRepo.save(tagByName);
            }
            storedImageTags.remove(tagByName);
        });

        // remove image from the rest of the tags
        storedImageTags.forEach((tag) -> {

            if(tag.getImages().contains(storedImage)) {
                tag.getImages().remove(storedImage);
                tagRepo.save(tag);
            }

        });

    }

    public void deleteTag(String tag) {

        Tag byName = tagRepo.getByName(tag);
        byName.setImages(new ArrayList<>());
        byName = tagRepo.save(byName);
        tagRepo.delete(byName);

    }

    @CacheEvict(cacheNames = {"image","byCamera","byFolderName","byDate"}, allEntries = true)
    public void removeAll(List<Image> removableImages) {

        tagRepo.findAll().stream().forEach((tag) -> {

            removableImages.stream().forEach((image) -> {
                if(tag.getImages().contains(image)) {
                    tag.getImages().remove(image);
                }
            });

            tagRepo.save(tag);

        });

        removableImages.stream().forEach((image) -> {
            Image storedImage = imageRepo.getOne(image.getId());
            imageRepo.delete(storedImage);
        });

    }

    @CacheEvict(cacheNames = {"image","byCamera","byFolderName","byDate"}, allEntries = true)
    public void remove(Image image) {

        Image storedImage = imageRepo.getOne(image.getId());

        storedImage.getTags().stream().forEach((tag) -> {

            if(tag.getImages().contains(image)) {
                tag.getImages().remove(image);
            }
            tagRepo.save(tag);

        });

        imageRepo.delete(storedImage);

    }
}
