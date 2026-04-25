package com.project.registration_service.dto;

import java.util.Date;

public record StudentDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        Date birthday,
        String gender
) {}
