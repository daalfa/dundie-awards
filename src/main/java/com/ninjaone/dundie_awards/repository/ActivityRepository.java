package com.ninjaone.dundie_awards.repository;

import com.ninjaone.dundie_awards.model.Activity;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByOrderByOccuredAtDesc(Limit of);
}
