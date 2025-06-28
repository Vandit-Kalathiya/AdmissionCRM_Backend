package com.admission_crm.lead_management.Repository;

import com.admission_crm.lead_management.Entity.CoreEntities.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UniversityRepository extends JpaRepository<University, String> {

    @Query("SELECT u FROM University u WHERE u.name LIKE %:name%")
    List<University> findByNameContaining(@Param("name") String name);

    @Query("SELECT u FROM University u WHERE :adminId MEMBER OF u.admins")
    List<University> findByAdminId(@Param("adminId") String adminId);

    @Query("SELECT u FROM University u WHERE :institutionId MEMBER OF u.institutions")
    University findByInstitutionId(@Param("institutionId") String institutionId);
}
