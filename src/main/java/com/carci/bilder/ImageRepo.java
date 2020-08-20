package com.carci.bilder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.stream.Stream;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

/**
 * Created by carcinoma on 12.11.17.
 */
interface ImageRepo extends JpaRepository<Image, Long> {
    Image findByNameAndRealPath(String name, String realPath);

    @Query(value = "select i from Image i")
    Stream<Image> streamAll();

}
