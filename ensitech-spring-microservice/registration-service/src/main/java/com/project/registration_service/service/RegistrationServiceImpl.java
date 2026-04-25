package com.project.registration_service.service;

import com.project.registration_service.dao.repository.RegistrationRepository;
import com.project.registration_service.domain.Registration;
import com.project.registration_service.dto.*;
import com.project.registration_service.enumeration.AcademicYearStatus;
import com.project.registration_service.feign.AcademicRestClient;
import com.project.registration_service.feign.SpecialityRestClient;
import com.project.registration_service.feign.StudentRestClient;
import com.project.registration_service.mapper.RegistrationMapper;
import com.project.registration_service.mapper.StudentRegistrationMapper;
import com.project.registration_service.model.AcademicYear;
import com.project.registration_service.model.Speciality;
import com.project.registration_service.model.Student;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class RegistrationServiceImpl implements RegistrationService{
    private static final Logger log = LoggerFactory.getLogger(RegistrationServiceImpl.class);
    private final RegistrationRepository registrationRepository;
    private final RegistrationMapper registrationMapper;
    private final StudentRestClient studentRestClient;
    private final AcademicRestClient academicRestClient;
    private final EntityManager entityManager;
    private final SpecialityRestClient specialityRestClient;

    public RegistrationServiceImpl(RegistrationRepository registrationRepository, RegistrationMapper registrationMapper, StudentRestClient studentRestClient, AcademicRestClient academicRestClient, EntityManager entityManager, SpecialityRestClient specialityRestClient) {
        this.registrationRepository = registrationRepository;
        this.registrationMapper = registrationMapper;
        this.studentRestClient = studentRestClient;
        this.academicRestClient = academicRestClient;
        this.entityManager = entityManager;
        this.specialityRestClient = specialityRestClient;
    }

    @Override
    public RegistrationDTO getRegistration(Long id) {
        Optional<Registration> registration = registrationRepository.findById(id);
        if(registration.isPresent()){
            return this.registrationMapper.toDto(registration.get());
        }
         return null;
    }

    /**
     * Récupérer une inscription par id
     */
    public RegDTO getById(Long id) {
       return this.registrationRepository.findById(id)
               .map(StudentRegistrationMapper::toDtoR)
               .orElseThrow(()-> new NoSuchElementException("There nothing about "+ id));
    }

    public Registration getPlainRegById(Long id) {
       return this.registrationRepository.findById(id)
               .orElseThrow(()-> new NotFoundException("There nothing about "+ id));
    }


    @Override
    public List<RegistrationDTO> getAllRegistrations() {
        List<Registration> registrations = this.registrationRepository.findAll();
        return this.registrationMapper.toDtoList(registrations);
    }

    @Override
    public RegistrationDTO createRegistration(RegistrationDTO registrationDTO) {
           if(this.registrationRepository.existsByRegistrationNumber(registrationDTO.registrationNumber())){
               throw new IllegalArgumentException("Registration number already exists");
           }
           Registration registration = this.registrationMapper.toEntity(registrationDTO);
           this.registrationRepository.save(registration);
        return registrationDTO;

    }

    @Override
    public RegistrationDTO updateRegistration(Long id, RegistrationDTO registrationDTO) {

        Registration existingYear = this.registrationRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Already exists!"));

        existingYear.setRegistrationNumber(registrationDTO.registrationNumber());
        existingYear.setLevel(registrationDTO.level());



        Registration registration = this.registrationRepository.save(existingYear);

        return registrationMapper.toDto(registration);
    }

    @Override
    public RegDTO updateRegs(Long id, UpdateRegDTO updateRegDTO) {

        Registration existingRegistration = this.registrationRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Registration does not exists!"));

        existingRegistration.setLevel(updateRegDTO.level());
//        && !updateRegDTO.matricule().equals(existingRegistration.getMatricule()
        if (updateRegDTO.matricule() != null) {
            Student student = this.studentRestClient.getStudentByMatricule(updateRegDTO.matricule());
            if (student == null) {
                throw new IllegalArgumentException("Le matricule fourni pour la mise à jour n'existe pas.");
            }
            existingRegistration.setMatricule(updateRegDTO.matricule());
            existingRegistration.setStudent(student);
            log.info("Student chargé : {} {}", student.getFirstName(), student.getLastName());
        }

        if(updateRegDTO.specialityLabel() != null ){
            Speciality speciality = this.specialityRestClient.getSpecialityByLabel(updateRegDTO.specialityLabel());
            log.info(updateRegDTO.toString(), speciality.toString());
            if(speciality == null){
                throw new IllegalArgumentException("Cette specialité n'existe pas");
            }
            existingRegistration.setSpecialityLabel(updateRegDTO.specialityLabel());
            log.info("Speciality mise à jour : {}", speciality.getLabel());


        }
        System.out.println("*****************Speciality label mise à jour : {} "+ existingRegistration.getSpecialityLabel());
        Registration registration = this.registrationRepository.save(existingRegistration);

        // Réinjections de student car JPA ne persiste pas ce champ
        registration.setStudent(existingRegistration.getStudent());
        return StudentRegistrationMapper.toDtoR(registration);
    }

    @Override
    public void deleteRegistration(Long id) {
        if(this.registrationRepository.findById(id).isPresent()){
            this.registrationRepository.deleteById(id);
        }
        else {
            throw new RuntimeException("Not deleted!");
        }

    }

    @Override
    @Transactional
    public RegDTO processRegistration(CreateRegistrationDTO createRegistrationDTO) {

        // Check 1: Fetch student, academic year and speciality
        Student student = studentRestClient.getStudentByMatricule(createRegistrationDTO.matricule());
        if (student == null) {
            throw new IllegalArgumentException("Student not found for the given matricule.");
        }

        AcademicYear academicYear = academicRestClient.getAcademicYearByLabel(createRegistrationDTO.academicYearLabel());
        if (academicYear == null) {
            throw new IllegalArgumentException("Aucune n'année academique ne correspond à ce label !");
        }
        if(this.specialityRestClient.getSpecialityByLabel(createRegistrationDTO.specialityLabel()) == null){
            throw new IllegalArgumentException("Speciality not found for the given label !");
        }

        // Check 2: Student already registered for this academic year
        if (this.getRegsByMatriculeAndAYLabel(student.getMatricule(), academicYear.getLabel()) != null) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous inscrire deux fois!");
        }

        // Check 3: Academic year status
        if (academicYear.getStatus() != AcademicYearStatus.EN_COURS) {
            log.error("STATUS of academic year: " + academicYear.getStatus().name() + " is not correct for registration.");
            throw new IllegalArgumentException("L'année académique n'est pas ouverte pour les inscriptions.");
        }

        // Check 4: Registration period
        boolean isWithinRegistrationPeriod = academicYear.getPeriods().stream()
                .filter(period -> "INSCRIPTION_PERIOD".equals(period.getTypePeriod()))
                .anyMatch(period -> (LocalDate.now().isAfter(period.getStartedAt()) || LocalDate.now().isEqual(period.getStartedAt()))
                        && (LocalDate.now().isBefore(period.getEndedAt()) || LocalDate.now().isEqual(period.getEndedAt())));

        if (!isWithinRegistrationPeriod) {
            throw new DateTimeException("La date d'inscription ne correspond pas à la période d'inscription !");
        }

        // Process registration
        Registration registration = StudentRegistrationMapper.toEntity(createRegistrationDTO);
        registration.setStudent(student);
        registration.setDateOfRegistration(LocalDateTime.now());

        // Check 5 : registration number does not exist

        if(registration.getRegistrationNumber() == null){
//            Long nextRegNumber = (Long) entityManager.createNativeQuery("SELECT NEXTVAL('registration_number_seq')").getSingleResult();
//            registration.setRegistrationNumber(nextRegNumber);
            Long lastNumber = registrationRepository.findTopByOrderByRegistrationNumberDesc() != null
                    ? registrationRepository.findTopByOrderByRegistrationNumberDesc().getRegistrationNumber()
                    : 0L;

            registration.setRegistrationNumber(lastNumber+1);
        }
        // Persist and flush
        this.registrationRepository.save(registration);

        return StudentRegistrationMapper.toDtoR(registration);
    }

    @Override
    public List<RegDTO> getRegistrationListByMatricule(String matricule) {
        List<Registration> registrations = this.registrationRepository.findByMatricule(matricule);
        Student student = studentRestClient.getStudentByMatricule(matricule);
        registrations.forEach(registration -> registration.setStudent(student));

        if(registrations.isEmpty()){
            throw new NotFoundException("NOT FOUND !");
        }
        return  StudentRegistrationMapper.toRegDTOList(registrations);
    }

    @Override
    public List<RegDTO> getAllRegs() {
        List<Registration> registrations = this.registrationRepository.findAll();
        return  StudentRegistrationMapper.toRegDTOList(registrations);
    }

    @Override
    public List<RegDTO> getRegistrationsBySpecialityLabel(String label) {
        return StudentRegistrationMapper.toRegDTOList(this.registrationRepository.findAllBySpecialityLabel(label));
    }

    @Override
    public RegDTO getLatestRegistrationByMatricule(String matricule) {
        // 1. Valider et récupérer l'étudiant d'abord (Fail-Fast)
        Student student = studentRestClient.getStudentByMatricule(matricule);
        if (student == null) {
            throw new NotFoundException("Aucun étudiant trouvé avec le matricule : " + matricule);
        }

        // 2. Chercher la dernière inscription
        Optional<Registration> latestRegistrationOptional = registrationRepository.findTopByMatriculeOrderByDateOfRegistrationDesc(matricule);

        // 3. Vérifier explicitement si l'Optional est vide
        if (latestRegistrationOptional.isEmpty()) {
            throw new NotFoundException("Aucune inscription trouvée pour le matricule : " + matricule);
        }

        // 4. Si on arrive ici, l'inscription existe. On la récupère.
        Registration latestRegistration = latestRegistrationOptional.get();

        // 5. On enrichit l'entité avec l'objet Student déjà récupéré
        latestRegistration.setStudent(student);

        // 6. On retourne le DTO
        return StudentRegistrationMapper.toDtoR(latestRegistration);
    }
    @Override
    public List<RegDTO> getRegistrationsByLabel(String label) {
        List<Registration> registrations = this.registrationRepository.findByAcademicYearLabel(label);
        return  StudentRegistrationMapper.toRegDTOList(registrations);
    }

    public RegDTO getRegsByMatriculeAndAYLabel(String matricule, String label){
        Optional<Registration> registrationOptional = this.registrationRepository
                .findByMatriculeAndAcademicYearLabel(matricule, label);
        // Utilisez Optional.map() pour un mappage conditionnel et sûr
        return registrationOptional.map(StudentRegistrationMapper::toDtoR).orElse(null);
    }

}
