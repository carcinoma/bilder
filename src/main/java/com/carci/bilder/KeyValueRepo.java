package com.carci.bilder;

import org.springframework.data.repository.CrudRepository;

/**
 * Created by carcinoma on 12.11.17.
 */
interface KeyValueRepo extends CrudRepository<KeyValue, Long> {
    KeyValue findByKey(String key);
}
