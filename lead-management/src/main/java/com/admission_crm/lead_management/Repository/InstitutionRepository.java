package com.admission_crm.lead_management.Repository;

import com.admission_crm.lead_management.Entity.CoreEntities.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, String> {

    Optional<Institution> findByInstituteCode(String instituteCode);

    List<Institution> findByUniversityId(String universityId);

    List<Institution> findByIsActive(Boolean isActive);

    @Query("SELECT i FROM Institution i WHERE i.name LIKE %:name%")
    List<Institution> findByNameContaining(@Param("name") String name);

    @Query("SELECT i FROM Institution i WHERE :counselorId MEMBER OF i.counselors")
    List<Institution> findByCounselorId(@Param("counselorId") String counselorId);

    @Query("SELECT i FROM Institution i WHERE :adminId MEMBER OF i.instituteAdmin")
    List<Institution> findByAdminId(@Param("adminId") String adminId);
}
