package com.carci.bilder;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by carcinoma on 12.11.17.
 */
@Component
@Log
public class DirWatcher implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    ImageProcessor imageProcessor;

    @Autowired
    ImageService imageService;

    private WatchService watcher;

    @Value("${imagesPath}")
    String imagesPath;

    @Autowired
    Scanner scanner;

    @Autowired
    TaskExecutor taskExecutor;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {

        taskExecutor.execute(() ->
                {
                    try {
                        watch(Paths.get(imagesPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

    }

    @PostConstruct
    public void init() throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
    }

    @PreDestroy
    public void cleanup() {
        try {
            watcher.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error closing watcher service", e);
        }
    }

    public void watch(Path path) throws IOException {
        log.info("Starting Recursive Watcher for " + path.toString());

        final Map<WatchKey, Path> keys = new HashMap<>();

        Consumer<Path> register = (Path p) -> {
            if (!p.toFile().exists() || !p.toFile().isDirectory()) {
                throw new RuntimeException("folder " + p + " does not exist or is not a directory");
            }
            try {
                Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        log.info("registering " + dir + " in watcher service");
                        WatchKey watchKey = dir.register(watcher, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE});
                        keys.put(watchKey, dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("Error registering path " + p);
            }
        };

        register.accept(path);

        while (true) {
            final WatchKey key;
            try {
                key = watcher.take(); // wait for a key to be available
            } catch (InterruptedException | ClosedWatchServiceException ex) {
                return;
            }

            final Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey " + key + " not recognized!");
                continue;
            }

            key.pollEvents().stream()
                    .filter((WatchEvent<?> e) -> (e.kind() != OVERFLOW))
                    .forEach((WatchEvent<?> event) -> {

                        Path p = ((WatchEvent<Path>) event).context();
                        final Path absPath = dir.resolve(p);
                        if (absPath.toFile().isDirectory()) {

                            register.accept(absPath);

                        } else {

                            try {
                                if (event.kind() == ENTRY_CREATE) {
                                    addImage(absPath);
                                } else if (event.kind() == ENTRY_DELETE) {
                                    deleteImage(absPath);
                                }
                            } catch (Exception e) {
                                log.log(Level.SEVERE, "Error while handling " + absPath, e);
                            }


                        }
                    });

            boolean valid = key.reset(); // IMPORTANT: The key must be reset after processed
            if (!valid) {
                break;
            }
        }
    }

    private void deleteImage(Path file) {

        log.info("Detected deleted file " + file);

        if (file.toString().toLowerCase().endsWith(".jpg")) {

            Image image = imageService.getByPath(file.getParent().toString(), file.getFileName().toString());
            imageService.remove(image);
            scanner.deleteFile(image);

        } else {
            log.info("Ignore new file " + file + ". No image.");
        }

    }

    private void addImage(Path file) {

        log.info("Detected new file " + file);

        if (file.toString().toLowerCase().endsWith(".jpg")) {
            waitForStableFileSize(file);
            imageProcessor.processFile(file, false);
        } else {
            log.info("Ignore new file " + file + ". No image.");
        }

    }


    private void waitForStableFileSize(Path path) {

        File file = path.toFile();
        long currentLength = file.length();
        while(true) {

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // ignore
            }

            if(currentLength == file.length()) {
                return;
            }
            currentLength = file.length();

            log.info("Await " + file + "'s size to settle...");

        }

    }

}
