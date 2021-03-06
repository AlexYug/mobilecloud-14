package org.magnum.mobilecloud.video.repository;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends CrudRepository<Video, Long>{
	public Collection<Video> findByName(String name);
	public Collection<Video> findByDurationLessThan(long duration);
}