package com.project.ensitech.training_service.model.dto.registrationDto;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

public class RegistrationDTO {
    private Long registrationNumber;
    private String level;
    private String matricule;
    private String specialityLabel;
    private String academicYearLabel;

}
