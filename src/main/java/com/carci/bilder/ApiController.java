package com.carci.bilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by carcinoma on 12.11.17.
 */
@Controller
@CrossOrigin(origins = "*")
@RequestMapping("/api")
class ApiController {

    @Autowired
    ImageService imageService;

    @Autowired
    ProgressService progressService;

    @Autowired
    Scanner scanner;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @RequestMapping(value = "/tree.json", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
    @Cacheable("byFolderName")
	public Map<String, Map<String, ImageDto>> getTree() {

        Map<String, Map<String, ImageDto>> folders = new LinkedHashMap<>();

        imageService.getByFolder().forEach((folder, images) -> {
            folders.put(folder, fillImages(images));
        });

        return folders;

    }

	@RequestMapping("/progress.json")
	@ResponseBody
	public Progress getProgress() {
		return progressService.getProgress();
	}

	@RequestMapping("/by-date.json")
	@ResponseBody
    @Cacheable("byDate")
	public Map<String, Map<String, ImageDto>> getByDate() {

        Map<String, Map<String, ImageDto>> snapDates = new LinkedHashMap<>();

        imageService.getByDate().forEach((snapDate, images) -> {
            snapDates.put(snapDate, fillImages(images));
        });

        return snapDates;

	}

	@RequestMapping("/by-cammodel.json")
	@ResponseBody
    @Cacheable("byCamera")
	public Map<String, Map<String, ImageDto>> getByCamera() {

        Map<String, Map<String, ImageDto>> cameras = new LinkedHashMap<>();

        imageService.getByCamera().forEach((camera, images) -> {
            cameras.put(camera, fillImages(images));
        });

        return cameras;

	}

    @RequestMapping("/update-images.sh")
    @ResponseBody
    public void updateImages() {
        scanner.scan();
    }

    @RequestMapping(value = "/tags")
    @ResponseBody
    @Cacheable("byTag")
    public Map<String, Map<String, ImageDto>> getTags() {

        Map<String, Map<String, ImageDto>> tags = new LinkedHashMap<>();

        imageService.getTags().forEach((tag) -> {
            tags.put(tag.getName(), fillImages(tag.getImages()));
        });

        return tags;

    }

    @RequestMapping(value = "/tags", method = RequestMethod.POST)
    @ResponseBody
    @CacheEvict(cacheNames = {"byTag","image","byCamera","byFolderName","byDate"}, allEntries = true)
    public void addTag(@RequestBody Tag tag) {
        imageService.createTag(tag.getName());
    }

    @RequestMapping(value = "/tags/{tag}", method = RequestMethod.POST)
    @ResponseBody
    @CacheEvict(cacheNames = {"byTag","image","byCamera","byFolderName","byDate"}, allEntries = true)
    public void addImageToTag(@PathVariable("tag") String tag, @RequestBody List<Image> images) {
        for(Image image : images) {
            imageService.addImageToTag(image, tag);
        }
    }

    @RequestMapping(value = "/tags/{tag}", method = RequestMethod.DELETE)
    @ResponseBody
    @CacheEvict(cacheNames = {"byTag","image","byCamera","byFolderName","byDate"}, allEntries = true)
    public void deleteTag(@PathVariable("tag") String tag) {
        imageService.deleteTag(tag);
    }

    @RequestMapping(value = "/images/{id}/tags", method = RequestMethod.PUT)
    @ResponseBody
    @CacheEvict(cacheNames = {"byTag","image","byCamera","byFolderName","byDate"}, allEntries = true)
    public void setImageTags(@PathVariable("id") Long imageId, @RequestBody List<String> tags) {
        imageService.setImageTags(imageId, tags);
    }

    @RequestMapping(value = "/images/{id}")
    @ResponseBody
    @Cacheable(value = "image")
    public ImageSingleDto getImage(@PathVariable("id") Long imageId) {

        Image image = imageService.getById(imageId);

        List<String> tags = new ArrayList<>();
        image.getTags().forEach((tag) -> {
            tags.add(tag.getName());
        });

        return ImageSingleDto.builder()
                .id(image.getId())
                .name(image.getName())
                .path(image.getFolderName())
                .cameraName(image.getCameraName())
                .snapDate(sdf.format(image.getSnapDate()))
                .tags(tags)
                .build();

    }

    private Map<String, ImageDto> fillImages(List<Image> images) {
        Map<String, ImageDto> result = new LinkedHashMap<>(images.size());

        for(Image image : images) {
            result.put(image.getName(), ImageDto.builder()
                    .id(image.getId())
                    .thumb("thumbnails/" + image.getThumbUuid() + ".jpg")
                    .img("full-size/" + image.getThumbUuid() + ".jpg")
                    .tags(
                            image.getTags().stream().map(t -> t.getName() ).collect(Collectors.toList())
                        )
                    .build()
            );
        }
        return result;
    }

}
