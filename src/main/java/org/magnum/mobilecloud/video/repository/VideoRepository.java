package org.magnum.mobilecloud.video.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public interface VideoRepository extends CrudRepository<Video, Long> {

    Video findById(long id);
    List<Video> findByName(String title);
    List<Video> findByDurationLessThan(long Duration);
    //public Collection<Video> findAll();
    //public boolean existsById(long vid);
    //public boolean existsById(long id);  // this would cause error

    //public Video save(Video video);

}
