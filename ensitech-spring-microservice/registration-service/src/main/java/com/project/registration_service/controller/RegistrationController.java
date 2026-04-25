package com.project.registration_service.controller;

import com.project.registration_service.domain.Registration;
import com.project.registration_service.dto.*;
import com.project.registration_service.service.PdfService;
import com.project.registration_service.service.RegistrationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {
    private final RegistrationService registrationService;
    private final PdfService pdfService;

    public RegistrationController(RegistrationService registrationService, PdfService pdfService) {
        this.registrationService = registrationService;
        this.pdfService = pdfService;
    }

//    @GetMapping
//    public ResponseEntity<List<RegistrationDTO>> getAllRegistrations(){
//        return ResponseEntity.ok(this.registrationService.getAllRegistrations());
//    }

    @GetMapping
    public ResponseEntity<List<RegDTO>> getAllRegs(){
        return ResponseEntity.ok(this.registrationService.getAllRegs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(registrationService.getById(id));
    }

    /*@GetMapping("/{id}")
    public ResponseEntity<RegistrationDTO> getRegistration(@PathVariable("id") Long id){
        return ResponseEntity.ok(this.registrationService.getRegistration(id));
    }*/


    @PutMapping("test/{id}")
    public ResponseEntity<RegistrationDTO> update(@PathVariable Long id, @RequestBody RegistrationDTO registrationDTO){
        return ResponseEntity.ok(this.registrationService.updateRegistration(id,registrationDTO));
    }

    // @PutMapping("update/{id}")
    @PutMapping("/{id}")
    public ResponseEntity<RegDTO> updateReg(@PathVariable Long id, @RequestBody UpdateRegDTO updateRegDTO){
        return ResponseEntity.ok(this.registrationService.updateRegs(id,updateRegDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        this.registrationService.deleteRegistration(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<RegDTO> post(@Validated @RequestBody CreateRegistrationDTO createRegistrationDTO){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.registrationService.processRegistration(createRegistrationDTO));
//        return ResponseEntity.ok(this.registrationService.processRegistration(createRegistrationDTO));
    }

    @GetMapping("/by-student/{matricule}")
    public ResponseEntity<List<RegDTO>> getRegByMatricule(@Validated @PathVariable String matricule){
        return ResponseEntity.ok(this.registrationService.getRegistrationListByMatricule(matricule));
    }

    @GetMapping("/by-speciality/{specialityLabel}")
    public ResponseEntity<List<RegDTO>> getRegistrationsBySpec(@Validated @PathVariable String specialityLabel){
        return ResponseEntity.ok(this.registrationService.getRegistrationsBySpecialityLabel(specialityLabel));
    }

    /**
     * Récupère la dernière inscription (la plus récente) pour un étudiant donné.
     * @param matricule Le matricule de l'étudiant.
     * @return La dernière inscription ou 404 si aucune n'est trouvée.
     */
    @GetMapping("/by-student/{matricule}/latest")
    public ResponseEntity<RegDTO> getLatestRegistrationByMatricule(@PathVariable String matricule) {
        RegDTO latestRegistration = registrationService.getLatestRegistrationByMatricule(matricule);
        return ResponseEntity.ok(latestRegistration);
    }

//    @PostMapping
//    public ResponseEntity<RegistrationStudentDTO> create(@RequestBody CreateRegistrationDTO dto) {
//        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.create(dto));
//    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getRegistrationPdf(@PathVariable Long id) {
        Registration registration = this.registrationService.getPlainRegById(id);
        byte[] pdfBytes = pdfService.generateRegistrationPdf(registration);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=registration-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{id}/original-pdf")
    public ResponseEntity<byte[]> getOriginalPdf(@PathVariable Long id) {
        Registration registration = this.registrationService.getPlainRegById(id);
        byte[] pdfBytes = pdfService.generateRegistrationPdfWithoutQr(registration);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=original_registration_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
