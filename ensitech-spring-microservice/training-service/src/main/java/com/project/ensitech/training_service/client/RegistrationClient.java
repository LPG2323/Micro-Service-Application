package com.project.ensitech.training_service.client;

import com.project.ensitech.training_service.model.dto.registrationDto.RegistrationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "registration-service")
public interface RegistrationClient {
//    @GetMapping("/api/registrations/by-specialities/{speciality}")
//    RegistrationDTO getRegistrationBySpec();

    @GetMapping("api/registrations/by-speciality/{specialityLabel}")
    List<RegistrationDTO> getRegistrationsBySpec(@PathVariable String specialityLabel);
}
