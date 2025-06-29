package com.admission_crm.lead_management.Controller;

import com.admission_crm.lead_management.Exception.DuplicateLeadException;
import com.admission_crm.lead_management.Exception.ResourceNotFoundException;
import com.admission_crm.lead_management.Payload.Request.InstitutionCreateRequest;
import com.admission_crm.lead_management.Payload.Request.InstitutionUpdateRequest;
import com.admission_crm.lead_management.Payload.Response.ApiResponse;
import com.admission_crm.lead_management.Payload.Response.InstitutionResponseDTO;
import com.admission_crm.lead_management.Service.InstitutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/institutions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InstitutionController {

    private final InstitutionService institutionService;

    @PostMapping
    public ResponseEntity<?> createInstitution(@Valid @RequestBody InstitutionCreateRequest createRequest) {
        try {
            InstitutionResponseDTO createdInstitution = institutionService.createInstitution(createRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Institution created successfully", createdInstitution));
        } catch (DuplicateLeadException e) {
            log.warn("Duplicate institution creation attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Institution already exists", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating institution: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create institution", "An unexpected error occurred"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInstitutionById(@PathVariable String id) {
        try {
            log.info("REST request to get Institution by ID: {}", id);
            InstitutionResponseDTO institution = institutionService.getInstitutionById(id);
            return ResponseEntity.ok(ApiResponse.success("Institution retrieved successfully", institution));
        } catch (ResourceNotFoundException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving institution: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve institution", "An unexpected error occurred"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllInstitutions() {
        try {
            log.info("REST request to get all Institutions");
            List<InstitutionResponseDTO> institutions = institutionService.getAllInstitutions();
            return ResponseEntity.ok(ApiResponse.success("Institutions retrieved successfully", institutions));
        } catch (Exception e) {
            log.error("Error retrieving institutions: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve institutions", "An unexpected error occurred"));
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getAllInstitutionsPaginated(Pageable pageable) {
        try {
            log.info("REST request to get all Institutions with pagination");
            Page<InstitutionResponseDTO> institutions = institutionService.getAllInstitutions(pageable);
            return ResponseEntity.ok(ApiResponse.success("Institutions retrieved successfully", institutions));
        } catch (Exception e) {
            log.error("Error retrieving institutions with pagination: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve institutions", "An unexpected error occurred"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInstitution(@PathVariable String id,
                                               @Valid @RequestBody InstitutionUpdateRequest updateRequest,
                                               Authentication authentication) {
        try {
            log.info("REST request to update Institution with ID: {} by user: {}",
                    id, authentication.getName());
            InstitutionResponseDTO updatedInstitution = institutionService.updateInstitution(id, updateRequest);
            return ResponseEntity.ok(ApiResponse.success("Institution updated successfully", updatedInstitution));
        } catch (ResourceNotFoundException e) {
            log.warn("Institution not found for update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (DuplicateLeadException e) {
            log.warn("Duplicate institution code in update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Institution code already exists", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating institution: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update institution", "An unexpected error occurred"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInstitution(@PathVariable String id,
                                               Authentication authentication) {
        try {
            log.info("REST request to delete Institution with ID: {} by user: {}",
                    id, authentication.getName());
            institutionService.deleteInstitution(id);
            return ResponseEntity.ok(ApiResponse.success("Institution deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            log.warn("Institution not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting institution: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete institution", "An unexpected error occurred"));
        }
    }

    @GetMapping("/code/{instituteCode}")
    public ResponseEntity<?> getInstitutionByCode(@PathVariable String instituteCode) {
        try {
            log.info("REST request to get Institution by code: {}", instituteCode);
            InstitutionResponseDTO institution = institutionService.getInstitutionByCode(instituteCode);
            return ResponseEntity.ok(ApiResponse.success("Institution retrieved successfully", institution));
        } catch (ResourceNotFoundException e) {
            log.warn("Institution not found with code: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving institution by code: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve institution", "An unexpected error occurred"));
        }
    }

    @GetMapping("/university/{universityId}")
    public ResponseEntity<?> getInstitutionsByUniversity(@PathVariable String universityId) {
        try {
            log.info("REST request to get Institutions by university: {}", universityId);
            List<InstitutionResponseDTO> institutions = institutionService.getInstitutionsByUniversity(universityId);
            return ResponseEntity.ok(ApiResponse.success("University institutions retrieved successfully", institutions));
        } catch (Exception e) {
            log.error("Error retrieving institutions by university: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve institutions", "An unexpected error occurred"));
        }
    }
}