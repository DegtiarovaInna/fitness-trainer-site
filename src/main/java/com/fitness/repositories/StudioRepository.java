package com.fitness.repositories;

import com.fitness.models.Studio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudioRepository extends JpaRepository<Studio, Long> {
    boolean existsByName(String name);
}
