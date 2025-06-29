package com.admission_crm.lead_management.Payload;

import com.admission_crm.lead_management.Entity.CoreEntities.Institution;
import com.admission_crm.lead_management.Entity.CoreEntities.University;
import com.admission_crm.lead_management.Payload.Request.InstitutionCreateRequest;
import com.admission_crm.lead_management.Payload.Request.InstitutionUpdateRequest;
import com.admission_crm.lead_management.Payload.Request.UniversityCreateRequest;
import com.admission_crm.lead_management.Payload.Request.UniversityUpdateRequest;
import com.admission_crm.lead_management.Payload.Response.InstitutionResponseDTO;
import com.admission_crm.lead_management.Payload.Response.UniversityResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    // Institution Mappers
    public Institution toEntity(InstitutionCreateRequest dto) {
        Institution institution = new Institution();
        institution.setName(dto.getName());
        institution.setInstituteCode(dto.getInstituteCode());
        institution.setAddress(dto.getAddress());
        institution.setPhone(dto.getPhone());
        institution.setEmail(dto.getEmail());
        institution.setWebsite(dto.getWebsite());
        institution.setLogoUrl(dto.getLogoUrl());
        institution.setUniversityId(dto.getUniversityId());
        institution.setCurrentCounselors(0);
        institution.setIsActive(true);

        return institution;
    }

    public InstitutionResponseDTO toResponseDTO(Institution institution) {
        InstitutionResponseDTO dto = new InstitutionResponseDTO();
        dto.setId(institution.getId());
        dto.setName(institution.getName());
        dto.setInstituteCode(institution.getInstituteCode());
        dto.setAddress(institution.getAddress());
        dto.setPhone(institution.getPhone());
        dto.setEmail(institution.getEmail());
        dto.setWebsite(institution.getWebsite());
        dto.setLogoUrl(institution.getLogoUrl());
        dto.setUniversityId(institution.getUniversityId());
        dto.setInstituteAdmin(institution.getInstituteAdmin());
        dto.setMaxCounselors(institution.getMaxCounselors());
        dto.setCurrentCounselors(institution.getCurrentCounselors());
        dto.setIsActive(institution.getIsActive());
        dto.setCounselors(institution.getCounselors());
        dto.setDepartments(institution.getDepartments());
        dto.setCourses(institution.getCourses());
        dto.setLeads(institution.getLeads());
        dto.setQueueSize(institution.getQueueSize());
        dto.setAvailableCounselorSlots(institution.getAvailableCounselorSlots());
        dto.setCreatedAt(institution.getCreatedAt());
        dto.setUpdatedAt(institution.getUpdatedAt());
        return dto;
    }

    public void updateEntityFromDTO(Institution institution, InstitutionUpdateRequest dto) {
        if (dto.getName() != null) {
            institution.setName(dto.getName());
        }
        if (dto.getInstituteCode() != null) {
            institution.setInstituteCode(dto.getInstituteCode());
        }
        if (dto.getAddress() != null) {
            institution.setAddress(dto.getAddress());
        }
        if (dto.getPhone() != null) {
            institution.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            institution.setEmail(dto.getEmail());
        }
        if (dto.getWebsite() != null) {
            institution.setWebsite(dto.getWebsite());
        }
        if (dto.getLogoUrl() != null) {
            institution.setLogoUrl(dto.getLogoUrl());
        }
        if (dto.getUniversityId() != null) {
            institution.setUniversityId(dto.getUniversityId());
        }
        if (dto.getMaxCounselors() != null) {
            institution.setMaxCounselors(dto.getMaxCounselors());
        }
        if (dto.getIsActive() != null) {
            institution.setIsActive(dto.getIsActive());
        }
    }

    // University Mappers
    public University toEntity(UniversityCreateRequest dto) {
        University university = new University();
        university.setName(dto.getName());
        university.setAddress(dto.getAddress());
        university.setPhone(dto.getPhone());
        university.setEmail(dto.getEmail());
        university.setWebsite(dto.getWebsite());
        university.setLogoUrl(dto.getLogoUrl());

        return university;
    }

    public UniversityResponseDTO toResponseDTO(University university) {
        UniversityResponseDTO dto = new UniversityResponseDTO();
        dto.setId(university.getId());
        dto.setName(university.getName());
        dto.setAddress(university.getAddress());
        dto.setPhone(university.getPhone());
        dto.setEmail(university.getEmail());
        dto.setWebsite(university.getWebsite());
        dto.setLogoUrl(university.getLogoUrl());
        dto.setAdmins(university.getAdmins());
        dto.setInstitutions(university.getInstitutions());
        dto.setTotalInstitutions(university.getInstitutions().size());
        dto.setTotalAdmins(university.getAdmins().size());
        dto.setCreatedAt(university.getCreatedAt());
        dto.setUpdatedAt(university.getUpdatedAt());
        return dto;
    }

    public void updateEntityFromDTO(University university, UniversityUpdateRequest dto) {
        if (dto.getName() != null) {
            university.setName(dto.getName());
        }
        if (dto.getAddress() != null) {
            university.setAddress(dto.getAddress());
        }
        if (dto.getPhone() != null) {
            university.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            university.setEmail(dto.getEmail());
        }
        if (dto.getWebsite() != null) {
            university.setWebsite(dto.getWebsite());
        }
        if (dto.getLogoUrl() != null) {
            university.setLogoUrl(dto.getLogoUrl());
        }
    }
}
