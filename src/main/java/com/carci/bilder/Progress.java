package com.carci.bilder;

import lombok.Data;

/**
 * Created by carcinoma on 14.11.17.
 */
@Data
public class Progress {
    private int current;
    private int total;
    private String currentFile;
}
