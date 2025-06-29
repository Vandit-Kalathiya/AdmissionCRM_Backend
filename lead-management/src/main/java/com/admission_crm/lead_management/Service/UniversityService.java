package com.admission_crm.lead_management.Service;

import com.admission_crm.lead_management.Entity.CoreEntities.University;
import com.admission_crm.lead_management.Exception.InvalidRequestException;
import com.admission_crm.lead_management.Exception.ResourceNotFoundException;
import com.admission_crm.lead_management.Payload.Request.UniversityCreateRequest;
import com.admission_crm.lead_management.Payload.Request.UniversityUpdateRequest;
import com.admission_crm.lead_management.Payload.Response.UniversityResponseDTO;
import com.admission_crm.lead_management.Repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UniversityService {

    private final UniversityRepository universityRepository;

    public University createUniversity(UniversityCreateRequest universityCreateRequest) {
        try {
            log.info("Creating university: {}", universityCreateRequest.getName());

            // Validate required fields
            if (universityCreateRequest.getName() == null || universityCreateRequest.getName().trim().isEmpty()) {
                throw new InvalidRequestException("University name is required");
            }

            University university = new University();
            university.setName(universityCreateRequest.getName());
            university.setAddress(universityCreateRequest.getAddress());
            university.setPhone(universityCreateRequest.getPhone());
            university.setEmail(universityCreateRequest.getEmail());
            university.setWebsite(universityCreateRequest.getWebsite());
            university.setLogoUrl(universityCreateRequest.getLogoUrl());

            University savedUniversity = universityRepository.save(university);
            log.info("University created successfully with ID: {}", savedUniversity.getId());
            return savedUniversity;

        } catch (InvalidRequestException e) {
            log.error("Error creating university: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating university: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create university", e);
        }
    }

    @Transactional(readOnly = true)
    public University getUniversityById(String id) {
        try {
            log.info("Fetching university with ID: {}", id);
            return universityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("University not found with ID: " + id));
        } catch (ResourceNotFoundException e) {
            log.error("University not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching university with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch university", e);
        }
    }

    @Transactional(readOnly = true)
    public List<University> getAllUniversities() {
        try {
            log.info("Fetching all universities");
            return universityRepository.findAll();
        } catch (Exception e) {
            log.error("Error fetching all universities: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch universities", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<University> getAllUniversities(Pageable pageable) {
        try {
            log.info("Fetching universities with pagination");
            return universityRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("Error fetching universities with pagination: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch universities", e);
        }
    }

    public UniversityResponseDTO updateUniversity(String id, UniversityUpdateRequest universityDetails) {
        try {
            log.info("Updating university with ID: {}", id);

            University existingUniversity = getUniversityById(id);

            // Update fields
            if (universityDetails.getName() != null) {
                existingUniversity.setName(universityDetails.getName());
            }
            if (universityDetails.getAddress() != null) {
                existingUniversity.setAddress(universityDetails.getAddress());
            }
            if (universityDetails.getPhone() != null) {
                existingUniversity.setPhone(universityDetails.getPhone());
            }
            if (universityDetails.getEmail() != null) {
                existingUniversity.setEmail(universityDetails.getEmail());
            }
            if (universityDetails.getWebsite() != null) {
                existingUniversity.setWebsite(universityDetails.getWebsite());
            }
            if (universityDetails.getLogoUrl() != null) {
                existingUniversity.setLogoUrl(universityDetails.getLogoUrl());
            }

            University updatedUniversity = universityRepository.save(existingUniversity);
            log.info("University updated successfully with ID: {}", updatedUniversity.getId());

            return getUniversityResponseDTO(updatedUniversity);

        } catch (ResourceNotFoundException e) {
            log.error("Error updating university: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating university with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update university", e);
        }
    }

    public void deleteUniversity(String id) {
        try {
            log.info("Deleting university with ID: {}", id);

            University university = getUniversityById(id);
            universityRepository.delete(university);

            log.info("University deleted successfully with ID: {}", id);

        } catch (ResourceNotFoundException e) {
            log.error("Error deleting university: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting university with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete university", e);
        }
    }

    @Transactional(readOnly = true)
    public List<University> searchUniversitiesByName(String name) {
        try {
            log.info("Searching universities by name: {}", name);
            return universityRepository.findByNameContaining(name);
        } catch (Exception e) {
            log.error("Error searching universities by name {}: {}", name, e.getMessage(), e);
            throw new RuntimeException("Failed to search universities", e);
        }
    }

    public University addAdmin(String universityId, String adminId) {
        try {
            log.info("Adding admin {} to university {}", adminId, universityId);

            if (adminId == null || adminId.trim().isEmpty()) {
                throw new InvalidRequestException("Admin ID cannot be null or empty");
            }

            University university = getUniversityById(universityId);

            if (!university.getAdmins().contains(adminId)) {
                university.getAdmins().add(adminId);
            }

            University updatedUniversity = universityRepository.save(university);
            log.info("Admin added successfully");
            return updatedUniversity;

        } catch (ResourceNotFoundException | InvalidRequestException e) {
            log.error("Error adding admin: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error adding admin: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add admin", e);
        }
    }

    public University removeAdmin(String universityId, String adminId) {
        try {
            log.info("Removing admin {} from university {}", adminId, universityId);

            if (adminId == null || adminId.trim().isEmpty()) {
                throw new InvalidRequestException("Admin ID cannot be null or empty");
            }

            University university = getUniversityById(universityId);
            university.getAdmins().remove(adminId);

            University updatedUniversity = universityRepository.save(university);
            log.info("Admin removed successfully");
            return updatedUniversity;

        } catch (ResourceNotFoundException | InvalidRequestException e) {
            log.error("Error removing admin: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error removing admin: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to remove admin", e);
        }
    }

    public University addInstitution(String universityId, String institutionId) {
        try {
            log.info("Adding institution {} to university {}", institutionId, universityId);

            if (institutionId == null || institutionId.trim().isEmpty()) {
                throw new InvalidRequestException("Institution ID cannot be null or empty");
            }

            University university = getUniversityById(universityId);

            if (!university.getInstitutions().contains(institutionId)) {
                university.getInstitutions().add(institutionId);
            }

            University updatedUniversity = universityRepository.save(university);
            log.info("Institution added successfully");
            return updatedUniversity;

        } catch (ResourceNotFoundException | InvalidRequestException e) {
            log.error("Error adding institution: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error adding institution: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add institution", e);
        }
    }

    public UniversityResponseDTO removeInstitution(String universityId, String institutionId) {
        try {
            log.info("Removing institution {} from university {}", institutionId, universityId);

            if (institutionId == null || institutionId.trim().isEmpty()) {
                throw new InvalidRequestException("Institution ID cannot be null or empty");
            }

            University university = getUniversityById(universityId);
            university.getInstitutions().remove(institutionId);

            University updatedUniversity = universityRepository.save(university);
            log.info("Institution removed successfully");

            return getUniversityResponseDTO(updatedUniversity);

        } catch (ResourceNotFoundException | InvalidRequestException e) {
            log.error("Error removing institution: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error removing institution: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to remove institution", e);
        }
    }

    @NotNull
    private static UniversityResponseDTO getUniversityResponseDTO(University updatedUniversity) {
        UniversityResponseDTO updatedUniversityDTO = new UniversityResponseDTO();
        updatedUniversityDTO.setId(updatedUniversity.getId());
        updatedUniversityDTO.setName(updatedUniversity.getName());
        updatedUniversityDTO.setInstitutions(updatedUniversity.getInstitutions());
        updatedUniversityDTO.setAdmins(updatedUniversity.getAdmins());
        updatedUniversityDTO.setAddress(updatedUniversity.getAddress());
        updatedUniversityDTO.setPhone(updatedUniversity.getPhone());
        updatedUniversityDTO.setEmail(updatedUniversity.getEmail());
        updatedUniversityDTO.setWebsite(updatedUniversity.getWebsite());
        updatedUniversityDTO.setLogoUrl(updatedUniversity.getLogoUrl());
        updatedUniversityDTO.setCreatedAt(updatedUniversity.getCreatedAt());
        updatedUniversityDTO.setUpdatedAt(updatedUniversity.getUpdatedAt());
        return updatedUniversityDTO;
    }
}
