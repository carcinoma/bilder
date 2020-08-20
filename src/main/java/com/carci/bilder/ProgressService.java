package com.carci.bilder;

import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by carcinoma on 14.11.17.
 */
@Service
public class ProgressService {

    private int total = 0;
    private AtomicInteger current = new AtomicInteger(0);
    private String message = "";

    public void setProgress(int total, int current, String currentImage) {

        this.current.set(current);
        this.total = total;
        this.message = currentImage;

    }

    public void pushProgress(String s) {

        int newCurrent = current.addAndGet(1);
        message = s;

        if(newCurrent >= total) {
            message = "";
            current.set(0);
            total = 0;
        }

    }

    public Progress getProgress() {

        Progress progress = new Progress();
        progress.setCurrent(current.get());
        progress.setTotal(total);
        progress.setCurrentFile(message);
        return progress;

    }

}
