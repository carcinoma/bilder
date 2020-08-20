package com.carci.bilder;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by carcinoma on 12.11.17.
 */
@Component
@Log
class Scanner implements ApplicationListener<ApplicationReadyEvent> {

	@Value("${imagesPath}")
    String imagesPath;

    @Value("${cacheDir}")
    String cacheDir;

	@Autowired
    ImageProcessor imageProcessor;

    @Autowired
    ImageService imageService;

	@Autowired
    ProgressService progressService;

    private static boolean running = false;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {

	    if(!imageService.hasAnyData()) {
	        log.info("detected first startup, trigger scan");
            scan();
        } else {
            progressService.setProgress(0, 0, "");
            //gcService.gc();
	    }

    }

    public void scan() {

        if(running) {
            log.info("Scan is already running, abort");
            return;
        }

	    try {

            log.info("about to scan " + imagesPath);

            Date scanBeginTime = imageService.getLastAdded();
            log.info("Scanning files created after: " + scanBeginTime);

            progressService.setProgress(1, 100, "Initialisierung");

            this.cleanup();

            List<Path> filesToProcess = scanDirectory(imagesPath, scanBeginTime);

            progressService.setProgress(filesToProcess.size(), 0, "Initialisierung");

            AtomicInteger count = new AtomicInteger(1);
            filesToProcess.forEach((file) -> {
                imageProcessor.processFile(file, (count.get() == filesToProcess.size()));
                count.incrementAndGet();
            });

        } finally {
            running = false;
        }

	}

	public void cleanup() {

        long start = System.currentTimeMillis();
        List<Image> removableImages = imageService.getAll().stream()
                .filter((image) -> !new File(image.getRealPath(), image.getName()).exists())
                .collect(Collectors.toList());

        log.info("Remove " + removableImages.size() + " images.");

        imageService.removeAll(removableImages);
        deleteFiles(removableImages);

        log.info("took: " + (System.currentTimeMillis() - start) + "ms");
    }

    private void deleteFiles(List<Image> removableImages) {

        removableImages.stream().forEach((image) -> {

            deleteFile(image);

        });

    }

    public void deleteFile(Image image) {

        String thumbDir = cacheDir + "thumbnails";
        String fullSizeDir = cacheDir + "full-size";

        File thumb = new File(String.format("%s/%s.jpg", thumbDir, image.getThumbUuid()));
        if (thumb.exists() && !thumb.delete()) {
            throw new IllegalStateException("unable to delete " + image);
        }

        File fullSize = new File(String.format("%s/%s.jpg", fullSizeDir, image.getThumbUuid()));
        if (fullSize.exists() && !fullSize.delete()) {
            throw new IllegalStateException("unable to delete " + image);
        }

    }

    private List<Path> scanDirectory(String path, Date scanBeginTime) {

        Path dir = Paths.get(path);
        List<Path> filesToProcess = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, new DirectoryStream.Filter<Path>() {

            @Override
            public boolean accept(Path entry) throws IOException {
                boolean isJpeg = entry.toString().toLowerCase().endsWith("jpg");
                boolean isValidDir = Files.isDirectory(entry) && !entry.getFileName().toString().equals("thumbs");
                boolean timeFits = Files.getLastModifiedTime(entry).toMillis() >= scanBeginTime.getTime();

                boolean result = timeFits && (isJpeg || isValidDir);

                return result;
            }
        })) {

            for (Path file: stream) {
                if ( Files.isDirectory(file) ) {
                    filesToProcess.addAll(scanDirectory(file.toString(), scanBeginTime));
                } else {
                    filesToProcess.add(file);
                }

            }
        } catch (IOException | DirectoryIteratorException x) {
            System.err.println(x);
        }

        return filesToProcess;
    }

}
