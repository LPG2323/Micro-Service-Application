package com.project.ensitech.training_service.repository;

import com.project.ensitech.training_service.model.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTitleContainingIgnoreCase(String title);
    List<Course> findByTeacherId(Long teacherId);
    // List<Course> findByCourseTitle(String title);
    // permet de vérifier si le titre est déjà utilisé par un autre cours .
    boolean existsByTitle(String title);
    //permet de vérifier si le titre est déjà utilisé par un autre cours que celui qu’on met à jour.
    boolean existsByTitleAndIdNot(String title, Long id);
}
