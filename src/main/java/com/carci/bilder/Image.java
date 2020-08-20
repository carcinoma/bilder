package com.carci.bilder;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by carcinoma on 12.11.17.
 */
@Entity
@Data
@Table(indexes = {
        @Index(name = "IDX_IMAGE_1", columnList = "name,realPath"),
        @Index(name = "IDX_IMAGE_folderName", columnList = "folderName,name"),
        @Index(name = "IDX_IMAGE_snapDate", columnList = "snapDate,name"),
        @Index(name = "IDX_IMAGE_cameraName", columnList = "cameraName,name")
})
public class Image {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String realPath;
    private String folderName;
    private Date snapDate;
    private String cameraName;
    private String thumbUuid;

    @ManyToMany(cascade = CascadeType.PERSIST, mappedBy = "images", fetch = FetchType.EAGER)
    private List<Tag> tags;

}
