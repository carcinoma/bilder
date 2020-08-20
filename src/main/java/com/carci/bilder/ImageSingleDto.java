package com.carci.bilder;

import lombok.Builder;

import java.util.List;

/**
 * Created by carcinoma on 18.11.17.
 */
@Builder
class ImageSingleDto {

    public Long id;
    public String name;
    public String path;
    public String snapDate;
    public String cameraName;
    public List<String> tags;

}
