package com.carci.bilder;

import lombok.Builder;

import java.util.List;

/**
 * Created by carcinoma on 18.11.17.
 */
@Builder
class ImageDto {

    public String thumb;
    public String img;
    public Long id;
    public List<String> tags;

}
