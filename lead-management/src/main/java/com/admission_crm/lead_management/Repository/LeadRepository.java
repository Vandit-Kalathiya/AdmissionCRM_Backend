package com.admission_crm.lead_management.Repository;

import com.admission_crm.lead_management.Entity.LeadManagement.Lead;
import com.admission_crm.lead_management.Entity.LeadManagement.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface LeadRepository extends JpaRepository<Lead, String> {

    // Basic CRUD enhancements
    Optional<Lead> findByEmail(String email);
    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);
    Page<Lead> findByInstitutionId(String institutionId, Pageable pageable);
    Page<Lead> findByAssignedCounselor(String counselorId, Pageable pageable);
    Page<Lead> findByInstitutionIdAndStatus(String institutionId, LeadStatus status, Pageable pageable);

    // Queue management queries
    List<Lead> findByInstitutionIdAndStatusOrderByCreatedAtAsc(String institutionId, LeadStatus status);

    @Query("SELECT l FROM Lead l WHERE l.institutionId = :institutionId AND l.status = :status ORDER BY l.priority DESC, l.leadScore DESC, l.createdAt ASC")
    List<Lead> findQueuedLeadsByPriority(@Param("institutionId") String institutionId, @Param("status") LeadStatus status);

    // Analytics queries
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.institutionId = :institutionId AND (:status IS NULL OR l.status = :status)")
    Long countByInstitutionIdAndStatus(@Param("institutionId") String institutionId, @Param("status") LeadStatus status);

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.assignedCounselor = :counselorId AND l.status IN :statuses")
    Long countActiveLeadsByCounselor(@Param("counselorId") String counselorId, @Param("statuses") List<LeadStatus> statuses);

    // Export and reporting queries
    List<Lead> findByInstitutionIdOrderByCreatedAtAsc(String institutionId);

    List<Lead> findByInstitutionIdAndCreatedAtAfter(String institutionId, LocalDateTime createdAfter);

    @Query("SELECT l FROM Lead l WHERE l.institutionId = :institutionId AND l.createdAt BETWEEN :startDate AND :endDate ORDER BY l.createdAt ASC")
    List<Lead> findByInstitutionIdAndCreatedAtBetween(@Param("institutionId") String institutionId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    // Cleanup and maintenance queries
    List<Lead> findByStatusInAndCompletedAtBefore(List<LeadStatus> statuses, LocalDateTime completedBefore);

    @Query("SELECT l FROM Lead l WHERE l.status IN :statuses AND l.completedAt < :completedBefore")
    List<Lead> findOldCompletedLeads(@Param("statuses") List<LeadStatus> statuses,
                                     @Param("completedBefore") LocalDateTime completedBefore);

    // Additional useful queries
    List<Lead> findByInstitutionIdAndStatusIn(String institutionId, List<LeadStatus> statuses);

    @Query("SELECT l FROM Lead l WHERE l.institutionId = :institutionId AND l.assignedCounselor = :counselorId AND l.status IN :statuses")
    List<Lead> findActiveLearsByCounselorAndInstitution(@Param("counselorId") String counselorId,
                                                        @Param("institutionId") String institutionId,
                                                        @Param("statuses") List<LeadStatus> statuses);

    // Source-based queries
    @Query("SELECT l.leadSource, COUNT(l) FROM Lead l WHERE l.institutionId = :institutionId GROUP BY l.leadSource")
    List<Object[]> getLeadCountBySource(@Param("institutionId") String institutionId);

    // Priority-based queries
    @Query("SELECT l.priority, COUNT(l) FROM Lead l WHERE l.institutionId = :institutionId GROUP BY l.priority")
    List<Object[]> getLeadCountByPriority(@Param("institutionId") String institutionId);

    // Status-based queries
    @Query("SELECT l.status, COUNT(l) FROM Lead l WHERE l.institutionId = :institutionId GROUP BY l.status")
    List<Object[]> getLeadCountByStatus(@Param("institutionId") String institutionId);

    // Date range queries
    @Query("SELECT DATE(l.createdAt) as date, COUNT(l) as count FROM Lead l WHERE l.institutionId = :institutionId AND l.createdAt >= :fromDate GROUP BY DATE(l.createdAt) ORDER BY DATE(l.createdAt)")
    List<Object[]> getDailyLeadCounts(@Param("institutionId") String institutionId, @Param("fromDate") LocalDateTime fromDate);

    // Counselor performance queries
    @Query("SELECT l.assignedCounselor, COUNT(l) FROM Lead l WHERE l.institutionId = :institutionId AND l.assignedCounselor IS NOT NULL GROUP BY l.assignedCounselor")
    List<Object[]> getLeadCountByCounselor(@Param("institutionId") String institutionId);

    @Query("SELECT l.assignedCounselor, COUNT(l) FROM Lead l WHERE l.institutionId = :institutionId AND l.status = :status AND l.assignedCounselor IS NOT NULL GROUP BY l.assignedCounselor")
    List<Object[]> getLeadCountByCounselorAndStatus(@Param("institutionId") String institutionId, @Param("status") LeadStatus status);

    // Lead score queries
    @Query("SELECT AVG(l.leadScore) FROM Lead l WHERE l.institutionId = :institutionId")
    Double getAverageLeadScore(@Param("institutionId") String institutionId);

    @Query("SELECT l FROM Lead l WHERE l.institutionId = :institutionId AND l.leadScore >= :minScore ORDER BY l.leadScore DESC")
    List<Lead> findHighScoreLeads(@Param("institutionId") String institutionId, @Param("minScore") Double minScore);

    // Conversion queries
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.institutionId = :institutionId AND l.status = 'CONVERTED' AND l.completedAt BETWEEN :startDate AND :endDate")
    Long getConversionCountBetweenDates(@Param("institutionId") String institutionId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    // Search functionality
    @Query("SELECT l FROM Lead l WHERE " +
            "(:searchTerm IS NULL OR LOWER(l.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(l.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(l.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "l.phone LIKE CONCAT('%', :searchTerm, '%')) AND " +
            "(:institutionId IS NULL OR l.institutionId = :institutionId) AND " +
            "(:status IS NULL OR l.status = :status)")
    Page<Lead> searchLeads(@Param("searchTerm") String searchTerm,
                           @Param("institutionId") String institutionId,
                           @Param("status") LeadStatus status,
                           Pageable pageable);

    // Advanced search with multiple filters
    @Query("SELECT l FROM Lead l WHERE " +
            "(:searchTerm IS NULL OR LOWER(l.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(l.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(l.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "l.phone LIKE CONCAT('%', :searchTerm, '%')) AND " +
            "(:institutionId IS NULL OR l.institutionId = :institutionId) AND " +
            "(:status IS NULL OR l.status = :status) AND " +
            "(:priority IS NULL OR l.priority = :priority) AND " +
            "(:source IS NULL OR l.leadSource = :source) AND " +
            "(:counselorId IS NULL OR l.assignedCounselor = :counselorId) AND " +
            "(:fromDate IS NULL OR l.createdAt >= :fromDate) AND " +
            "(:toDate IS NULL OR l.createdAt <= :toDate)")
    Page<Lead> advancedSearchLeads(@Param("searchTerm") String searchTerm,
                                   @Param("institutionId") String institutionId,
                                   @Param("status") LeadStatus status,
                                   @Param("priority") Lead.LeadPriority priority,
                                   @Param("source") Lead.LeadSource source,
                                   @Param("counselorId") String counselorId,
                                   @Param("fromDate") LocalDateTime fromDate,
                                   @Param("toDate") LocalDateTime toDate,
                                   Pageable pageable);
}