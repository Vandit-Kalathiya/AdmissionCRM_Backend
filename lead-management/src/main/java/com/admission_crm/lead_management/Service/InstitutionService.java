package com.admission_crm.lead_management.Service;

import com.admission_crm.lead_management.Entity.CoreEntities.Institution;
import com.admission_crm.lead_management.Entity.CoreEntities.University;
import com.admission_crm.lead_management.Exception.DuplicateLeadException;
import com.admission_crm.lead_management.Exception.ResourceNotFoundException;
import com.admission_crm.lead_management.Payload.EntityMapper;
import com.admission_crm.lead_management.Payload.Request.InstitutionCreateRequest;
import com.admission_crm.lead_management.Payload.Request.InstitutionUpdateRequest;
import com.admission_crm.lead_management.Payload.Response.InstitutionResponseDTO;
import com.admission_crm.lead_management.Repository.InstitutionRepository;
import com.admission_crm.lead_management.Repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final EntityMapper entityMapper;
    private final UniversityRepository universityRepository;

    public InstitutionResponseDTO createInstitution(InstitutionCreateRequest createDTO) {
        try {
            log.info("Creating institution: {}", createDTO.getName());

            // Check for duplicate institute code
            if (createDTO.getInstituteCode() != null &&
                    institutionRepository.findByInstituteCode(createDTO.getInstituteCode()).isPresent()) {
                throw new DuplicateLeadException("Institution with code " + createDTO.getInstituteCode() + " already exists");
            }

            Institution institution = entityMapper.toEntity(createDTO);
            Institution savedInstitution = institutionRepository.save(institution);

            University university = universityRepository.findById(createDTO.getUniversityId()).get();
            university.getInstitutions().add(savedInstitution.getId());

            universityRepository.save(university);

            log.info("Institution created successfully with ID: {}", savedInstitution.getId());
            return entityMapper.toResponseDTO(savedInstitution);

        } catch (DuplicateLeadException e) {
            log.error("Error creating institution: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating institution: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create institution", e);
        }
    }

    @Transactional(readOnly = true)
    public InstitutionResponseDTO getInstitutionById(String id) {
        try {
            log.info("Fetching institution with ID: {}", id);
            Institution institution = institutionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + id));
            return entityMapper.toResponseDTO(institution);
        } catch (ResourceNotFoundException e) {
            log.error("Institution not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching institution with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch institution", e);
        }
    }

    @Transactional(readOnly = true)
    public List<InstitutionResponseDTO> getAllInstitutions() {
        try {
            log.info("Fetching all institutions");
            List<Institution> institutions = institutionRepository.findAll();
            return institutions.stream()
                    .map(entityMapper::toResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all institutions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch institutions", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<InstitutionResponseDTO> getAllInstitutions(Pageable pageable) {
        try {
            log.info("Fetching institutions with pagination");
            Page<Institution> institutions = institutionRepository.findAll(pageable);
            return institutions.map(entityMapper::toResponseDTO);
        } catch (Exception e) {
            log.error("Error fetching institutions with pagination: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch institutions", e);
        }
    }

    public InstitutionResponseDTO updateInstitution(String id, InstitutionUpdateRequest updateDTO) {
        try {
            log.info("Updating institution with ID: {}", id);

            Institution existingInstitution = institutionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + id));

            // Check for duplicate institute code if changed
            if (updateDTO.getInstituteCode() != null &&
                    !updateDTO.getInstituteCode().equals(existingInstitution.getInstituteCode())) {
                Optional<Institution> duplicateInstitution = institutionRepository.findByInstituteCode(updateDTO.getInstituteCode());
                if (duplicateInstitution.isPresent() && !duplicateInstitution.get().getId().equals(id)) {
                    throw new DuplicateLeadException("Institution with code " + updateDTO.getInstituteCode() + " already exists");
                }
            }

            entityMapper.updateEntityFromDTO(existingInstitution, updateDTO);
            Institution updatedInstitution = institutionRepository.save(existingInstitution);

            log.info("Institution updated successfully with ID: {}", updatedInstitution.getId());
            return entityMapper.toResponseDTO(updatedInstitution);

        } catch (ResourceNotFoundException | DuplicateLeadException e) {
            log.error("Error updating institution: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating institution with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update institution", e);
        }
    }

    public void deleteInstitution(String id) {
        try {
            log.info("Deleting institution with ID: {}", id);

            Institution institution = institutionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + id));
            institutionRepository.delete(institution);

            log.info("Institution deleted successfully with ID: {}", id);

        } catch (ResourceNotFoundException e) {
            log.error("Error deleting institution: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting institution with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete institution", e);
        }
    }

    @Transactional(readOnly = true)
    public InstitutionResponseDTO getInstitutionByCode(String instituteCode) {
        try {
            log.info("Fetching institution with code: {}", instituteCode);
            Institution institution = institutionRepository.findByInstituteCode(instituteCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Institution not found with code: " + instituteCode));
            return entityMapper.toResponseDTO(institution);
        } catch (ResourceNotFoundException e) {
            log.error("Institution not found with code: {}", instituteCode);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching institution with code {}: {}", instituteCode, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch institution", e);
        }
    }

    @Transactional(readOnly = true)
    public List<InstitutionResponseDTO> getInstitutionsByUniversity(String universityId) {
        try {
            log.info("Fetching institutions for university: {}", universityId);
            List<Institution> institutions = institutionRepository.findByUniversityId(universityId);
            return institutions.stream()
                    .map(entityMapper::toResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching institutions for university {}: {}", universityId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch institutions", e);
        }
    }
}