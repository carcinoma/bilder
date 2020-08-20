package com.carci.bilder;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * Created by carcinoma on 12.11.17.
 */
@Entity
@Data
public class KeyValue {

    @Id
    @GeneratedValue
    private Long id;

    private String key;
    private String value;

    @Version
    private Long version;

}
