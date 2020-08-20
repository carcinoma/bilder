package com.carci.bilder;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Level;

import static com.carci.bilder.ImageService.LAST_IMAGE_ADDED;

/**
 * Created by carcinoma on 12.11.17.
 */
@Component
@Log
public class ImageProcessor {

    public static final int IMAGE_MAX_PRESENTATION_WIDTH = 1600;
    public static final int IMAGE_THUMBNAIL_WIDTH = 200;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private GcService gcService;

    @Value("${imagesPath}")
    String imagesPath;

    @Value("${cacheDir}")
    String cacheDir;

    private String thumbDir = "";
    private String fullSizeDir = "";

    @PostConstruct
    void init() {

        thumbDir = cacheDir + "thumbnails";
        fullSizeDir = cacheDir + "full-size";

        if(!new File(thumbDir).exists()) {
            new File(thumbDir).mkdir();
        }
        if(!new File(fullSizeDir).exists()) {
            new File(fullSizeDir).mkdir();
        }

    }

    @Async
    public void processFile(Path completeImagePath, boolean lastImage) {

        try {

            String folderName = completeImagePath.getParent().toString();
            Metadata metadata = ImageMetadataReader.readMetadata(completeImagePath.toFile());

            ExifSubIFDDirectory sdirectory
                    = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

            ExifIFD0Directory directory
                    = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            Image image = imageService.getByPath(folderName, completeImagePath.getFileName().toString());

            if(image == null) {
                image = new Image();
                image.setName(completeImagePath.getFileName().toString());

                String folderAbrev = folderName.replace(imagesPath, "");
                if(folderAbrev.startsWith("/")) {
                    folderAbrev = folderAbrev.substring(1);
                }
                String md5Path=folderAbrev + "/" + completeImagePath.getFileName().toString();
                image.setThumbUuid(getMd5(md5Path));
                image.setFolderName(folderAbrev);
            }

            generateThumbnails(completeImagePath.toFile(), getOrientation(directory), image.getThumbUuid());

            image.setSnapDate(getSnapDate(sdirectory, completeImagePath));
            image.setCameraName(getCamera(directory));
            image.setRealPath(folderName);

            imageService.addImage(image);

            if(lastImage) {
                imageService.setValue(LAST_IMAGE_ADDED, Long.toString(System.currentTimeMillis()));
                log.info("Last image, perform GC");
                //gcService.gc();
            }

        } catch (Throwable e) {

            log.log(Level.WARNING, "Error while processing " + completeImagePath, e);

        } finally {

            String currentImage = completeImagePath.toString().replace(imagesPath, "");
            if(currentImage.startsWith("/")) {
                currentImage = currentImage.substring(1);
            }
            progressService.pushProgress(currentImage);

        }

    }

    private String getMd5(String thing) throws NoSuchAlgorithmException {
        return bytesToHex(MessageDigest.getInstance("MD5").digest(thing.getBytes()));
    }

    private Date getSnapDate(ExifSubIFDDirectory sdirectory, Path imagePath) throws IOException {

        Date snapDate = null;
        if(sdirectory != null) {
            snapDate = sdirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        }

        if(snapDate == null) {
            snapDate = new Date(Files.readAttributes(imagePath, BasicFileAttributes.class).creationTime().toMillis());
        }
        return snapDate;
    }

    private String getCamera(ExifIFD0Directory directory) {

        String camera = null;

        if(directory != null) {

            String make = directory.getString(ExifSubIFDDirectory.TAG_MAKE);
            String model = directory.getString(ExifSubIFDDirectory.TAG_MODEL);

            if (!StringUtils.isEmpty(make) && !StringUtils.isEmpty(model)) {
                camera = String.format("%s - %s", make, model);
            } else if (!StringUtils.isEmpty(make)) {
                camera = make;
            } else if (!StringUtils.isEmpty(model)) {
                camera = model;
            }

            if(camera == null && directory.getString(ExifSubIFDDirectory.TAG_SOFTWARE) != null) {
                camera = directory.getString(ExifSubIFDDirectory.TAG_SOFTWARE);
            }

        }

        if(camera == null) {
            camera = "Unbekannt";
        }

        return camera;
    }


    private String getOrientation(ExifIFD0Directory directory) {

        String orientation = null;
        if(directory != null) {
            orientation = directory.getString(ExifSubIFDDirectory.TAG_ORIENTATION);
        }

        if(orientation == null) {
            orientation = "";
        }

        return orientation;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private void generateThumbnails(File originalImage, String orientation, Object thumbUuid) throws IOException {

        File thumbFileName = new File(String.format("%s/%s.jpg", thumbDir, thumbUuid));
        File fullSizeFileName = new File(String.format("%s/%s.jpg", fullSizeDir, thumbUuid));

        log.finest("generate thumb: " + thumbFileName);
        log.finest("generate full-size: " + fullSizeFileName);


        if(thumbFileName.exists()) {
            thumbFileName.delete();

        }

        if(fullSizeFileName.exists()) {
            fullSizeFileName.delete();
        }

        long startTime = System.currentTimeMillis();
        BufferedImage img = ImageIO.read(originalImage);
        startTime = tick(startTime, "Image load");

        if(!orientation.equals("1")) {

            switch (orientation) {
                case "8":
                    img = Scalr.rotate(img, Scalr.Rotation.CW_270);
                    break;
                case "3":
                    img = Scalr.rotate(img, Scalr.Rotation.CW_180);
                    break;
                case "6":
                    img = Scalr.rotate(img, Scalr.Rotation.CW_90);
                    break;
            }

        }

        startTime = tick(startTime, "orientation");
        int thumbMin = Math.min(img.getHeight(), IMAGE_THUMBNAIL_WIDTH);
        BufferedImage thumbImg = Scalr.resize(img, Scalr.Method.BALANCED, Scalr.Mode.FIT_TO_WIDTH,
                thumbMin, thumbMin, Scalr.OP_ANTIALIAS);
        ImageIO.write(thumbImg, "jpg", thumbFileName);
        startTime = tick(startTime, "thumb-scaled");

        if(img.getHeight() > IMAGE_MAX_PRESENTATION_WIDTH) {

            BufferedImage scaledImg = Scalr.resize(img, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH,
                    IMAGE_MAX_PRESENTATION_WIDTH, IMAGE_MAX_PRESENTATION_WIDTH, Scalr.OP_ANTIALIAS);
            ImageIO.write(scaledImg, "jpg", fullSizeFileName);
            startTime = tick(startTime, "full-size-scaled");

        } else {

            Files.createSymbolicLink(fullSizeFileName.toPath(), originalImage.toPath());
            startTime = tick(startTime, "full-size-link");

        }

    }


    private long tick(long lastTick, String message) {

        long thisTick = System.currentTimeMillis();
        log.finest(message + ": " + (System.currentTimeMillis() - lastTick) + "ms");
        return thisTick;

    }

}
