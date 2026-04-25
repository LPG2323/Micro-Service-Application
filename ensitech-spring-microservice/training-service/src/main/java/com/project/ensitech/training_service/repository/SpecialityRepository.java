package com.project.ensitech.training_service.repository;


import com.project.ensitech.training_service.model.entity.Speciality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpecialityRepository extends JpaRepository<Speciality, Long> {
    Optional<Speciality> findByLabel(String label);
    List<Speciality> findByLabelContainingIgnoreCase(String label);
    // List<Speciality> findByLabel(String label);
    // permet de vérifier si le titre est déjà utilisé par une autre spécialité .
    boolean existsByLabel(String label);
    //permet de vérifier si le titre est déjà utilisé par une autre spécialité que celui qu’on met à jour.
    boolean existsByLabelAndIdNot(String label, Long id);
}
