package com.admission_crm.lead_management.Controller;

import com.admission_crm.lead_management.Entity.CoreEntities.University;
import com.admission_crm.lead_management.Exception.InvalidRequestException;
import com.admission_crm.lead_management.Exception.ResourceNotFoundException;
import com.admission_crm.lead_management.Payload.Request.UniversityCreateRequest;
import com.admission_crm.lead_management.Payload.Request.UniversityUpdateRequest;
import com.admission_crm.lead_management.Payload.Response.ApiResponse;
import com.admission_crm.lead_management.Payload.Response.UniversityResponseDTO;
import com.admission_crm.lead_management.Service.UniversityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/universities")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UniversityController {

    private final UniversityService universityService;

    @PostMapping
    public ResponseEntity<?> createUniversity(@Valid @RequestBody UniversityCreateRequest universityCreateRequest) {
        try {
            University createdUniversity = universityService.createUniversity(universityCreateRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("University created successfully", createdUniversity));
        } catch (InvalidRequestException e) {
            log.warn("Invalid university creation request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid university data", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating university: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create university", "An unexpected error occurred"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUniversityById(@PathVariable String id) {
        try {
            log.info("REST request to get University by ID: {}", id);
            University university = universityService.getUniversityById(id);
            return ResponseEntity.ok(ApiResponse.success("University retrieved successfully", university));
        } catch (ResourceNotFoundException e) {
            log.warn("University not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("University not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving university: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve university", "An unexpected error occurred"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUniversities() {
        try {
            log.info("REST request to get all Universities");
            List<University> universities = universityService.getAllUniversities();
            return ResponseEntity.ok(ApiResponse.success("Universities retrieved successfully", universities));
        } catch (Exception e) {
            log.error("Error retrieving universities: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve universities", "An unexpected error occurred"));
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getAllUniversitiesPaginated(Pageable pageable) {
        try {
            log.info("REST request to get all Universities with pagination");
            Page<University> universities = universityService.getAllUniversities(pageable);
            return ResponseEntity.ok(ApiResponse.success("Universities retrieved successfully", universities));
        } catch (Exception e) {
            log.error("Error retrieving universities with pagination: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve universities", "An unexpected error occurred"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUniversity(@PathVariable String id,
                                              @Valid @RequestBody UniversityUpdateRequest universityDetails) {
        try {
            UniversityResponseDTO updatedUniversity = universityService.updateUniversity(id, universityDetails);
            return ResponseEntity.ok(ApiResponse.success("University updated successfully", updatedUniversity));
        } catch (ResourceNotFoundException e) {
            log.warn("University not found for update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("University not found", e.getMessage()));
        } catch (InvalidRequestException e) {
            log.warn("Invalid university update data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid university data", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating university: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update university", "An unexpected error occurred"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUniversity(@PathVariable String id) {
        try {
            universityService.deleteUniversity(id);
            return ResponseEntity.ok(ApiResponse.success("University deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            log.warn("University not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("University not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting university: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete university", "An unexpected error occurred"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUniversitiesByName(@RequestParam String name) {
        try {
            log.info("REST request to search Universities by name: {}", name);
            List<University> universities = universityService.searchUniversitiesByName(name);
            return ResponseEntity.ok(ApiResponse.success("Universities search completed", universities));
        } catch (Exception e) {
            log.error("Error searching universities: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search universities", "An unexpected error occurred"));
        }
    }

    @PostMapping("/{universityId}/admins/{adminId}")
    public ResponseEntity<?> addAdmin(@PathVariable String universityId,
                                      @PathVariable String adminId) {
        try {
            University updatedUniversity = universityService.addAdmin(universityId, adminId);
            return ResponseEntity.ok(ApiResponse.success("Admin added successfully", updatedUniversity));
        } catch (ResourceNotFoundException e) {
            log.warn("University not found for admin addition: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("University not found", e.getMessage()));
        } catch (InvalidRequestException e) {
            log.warn("Invalid admin addition request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid request", e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding admin: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add admin", "An unexpected error occurred"));
        }
    }

    @DeleteMapping("/{universityId}/admins/{adminId}")
    public ResponseEntity<?> removeAdmin(@PathVariable String universityId,
                                         @PathVariable String adminId) {
        try {
            University updatedUniversity = universityService.removeAdmin(universityId, adminId);
            return ResponseEntity.ok(ApiResponse.success("Admin removed successfully", updatedUniversity));
        } catch (ResourceNotFoundException e) {
            log.warn("University not found for admin removal: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("University not found", e.getMessage()));
        } catch (InvalidRequestException e) {
            log.warn("Invalid admin removal request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid request", e.getMessage()));
        } catch (Exception e) {
            log.error("Error removing admin: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to remove admin", "An unexpected error occurred"));
        }
    }

    @DeleteMapping("/{universityId}/institutions/{institutionId}")
    public ResponseEntity<?> removeInstitution(@PathVariable String universityId,
                                               @PathVariable String institutionId) {
        try {
            UniversityResponseDTO updatedUniversity = universityService.removeInstitution(universityId, institutionId);
            return ResponseEntity.ok(ApiResponse.success("Institution removed successfully", updatedUniversity));
        } catch (ResourceNotFoundException e) {
            log.warn("University not found for institution removal: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("University not found", e.getMessage()));
        } catch (InvalidRequestException e) {
            log.warn("Invalid institution removal request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid request", e.getMessage()));
        } catch (Exception e) {
            log.error("Error removing institution: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to remove institution", "An unexpected error occurred"));
        }
    }
}