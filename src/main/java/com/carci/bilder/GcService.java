package com.carci.bilder;

import lombok.extern.java.Log;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Log
public class GcService {

    @Async
    public void gc() {

        log.info("Perform GC");
        try {
            for(int i=0;i<5;i++) {
                Thread.sleep(5000);
                log.info("GC " + (i +1));
                System.gc();
            }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

    }

}
