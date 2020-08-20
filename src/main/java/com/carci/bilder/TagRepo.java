package com.carci.bilder;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by carcinoma on 18.11.17.
 */
public interface TagRepo extends JpaRepository<Tag, Long> {
    Tag findByName(String tag);
    Tag getByName(String tag);
}
