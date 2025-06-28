package com.admission_crm.lead_management.Entity.CoreEntities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Entity
@Table(name = "institutions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, length = 10, name = "institute_code")
    private String instituteCode;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 15)
    private String phone;

    @Column(length = 100)
    private String email;

    private String website;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "university_id")
    private String universityId;

    @ElementCollection
    @CollectionTable(name = "institution_admins", joinColumns = @JoinColumn(name = "institution_id"))
    @Column(name = "admin_id")
    private List<String> instituteAdmin = new ArrayList<>();

    @Column(name = "max_counselors")
    private Integer maxCounselors = 5;

    @Column(name = "current_counselors")
    private Integer currentCounselors = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ElementCollection
    @CollectionTable(name = "institution_counselors", joinColumns = @JoinColumn(name = "institution_id"))
    @Column(name = "counselor_id")
    private List<String> counselors = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "institution_departments", joinColumns = @JoinColumn(name = "institution_id"))
    @Column(name = "department_id")
    private List<String> departments = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "institution_courses", joinColumns = @JoinColumn(name = "institution_id"))
    @Column(name = "course_id")
    private List<String> courses = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "institution_leads", joinColumns = @JoinColumn(name = "institution_id"))
    @Column(name = "lead_id")
    private List<String> leads = new ArrayList<>();

    // Persistent queue using ordered list
    @ElementCollection
    @CollectionTable(name = "institution_queued_leads", joinColumns = @JoinColumn(name = "institution_id"))
    @OrderColumn(name = "queue_position")
    @Column(name = "lead_id")
    private List<String> queuedLeadsList = new ArrayList<>();

    // Transient Deque for runtime operations
    @Transient
    private transient Deque<String> queuedLeadsCache;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================== QUEUE OPERATIONS ========================

    /**
     * Get the queue as a Deque for operations
     * Lazy initialization from persistent list
     */
    public Deque<String> getQueuedLeads() {
        if (queuedLeadsCache == null) {
            queuedLeadsCache = new ArrayDeque<>();
            if (queuedLeadsList != null) {
                queuedLeadsCache.addAll(queuedLeadsList);
            }
        }
        return queuedLeadsCache;
    }

    /**
     * Set the queue and sync with persistent storage
     */
    public void setQueuedLeads(Deque<String> queuedLeads) {
        this.queuedLeadsCache = queuedLeads;
        syncQueueToList();
    }

    /**
     * Add a lead to the end of the queue (FIFO)
     */
    public void addToQueue(String leadId) {
        if (leadId == null || leadId.trim().isEmpty()) {
            return;
        }

        // Avoid duplicates
        if (!getQueuedLeads().contains(leadId)) {
            getQueuedLeads().addLast(leadId);
            syncQueueToList();
        }
    }

    /**
     * Add a lead to the front of the queue (priority)
     */
    public void addToQueuePriority(String leadId) {
        if (leadId == null || leadId.trim().isEmpty()) {
            return;
        }

        // Remove if exists and add to front
        getQueuedLeads().remove(leadId);
        getQueuedLeads().addFirst(leadId);
        syncQueueToList();
    }

    /**
     * Remove and return the next lead from the queue
     */
    public String removeFromQueue() {
        String leadId = getQueuedLeads().pollFirst();
        if (leadId != null) {
            syncQueueToList();
        }
        return leadId;
    }

    /**
     * Peek at the next lead without removing
     */
    public String peekQueue() {
        return getQueuedLeads().peekFirst();
    }

    /**
     * Remove a specific lead from the queue
     */
    public boolean removeFromQueue(String leadId) {
        boolean removed = getQueuedLeads().remove(leadId);
        if (removed) {
            syncQueueToList();
        }
        return removed;
    }

    /**
     * Get the current queue size
     */
    public int getQueueSize() {
        return getQueuedLeads().size();
    }

    /**
     * Check if queue is empty
     */
    public boolean isQueueEmpty() {
        return getQueuedLeads().isEmpty();
    }

    /**
     * Get the position of a lead in the queue (0-based)
     */
    public int getQueuePosition(String leadId) {
        List<String> queueList = new ArrayList<>(getQueuedLeads());
        return queueList.indexOf(leadId);
    }

    /**
     * Move a lead to a specific position in the queue
     */
    public boolean moveInQueue(String leadId, int newPosition) {
        List<String> queueList = new ArrayList<>(getQueuedLeads());

        if (queueList.remove(leadId)) {
            // Ensure position is within bounds
            newPosition = Math.max(0, Math.min(newPosition, queueList.size()));
            queueList.add(newPosition, leadId);

            // Update both cache and persistent storage
            queuedLeadsCache = new ArrayDeque<>(queueList);
            syncQueueToList();
            return true;
        }
        return false;
    }

    /**
     * Clear the entire queue
     */
    public void clearQueue() {
        getQueuedLeads().clear();
        syncQueueToList();
    }

    /**
     * Get a copy of the queue as a list (for display/iteration)
     */
    public List<String> getQueueAsList() {
        return new ArrayList<>(getQueuedLeads());
    }

    // ======================== PRIVATE HELPER METHODS ========================

    /**
     * Sync the transient Deque cache to the persistent List
     */
    private void syncQueueToList() {
        if (queuedLeadsCache != null) {
            queuedLeadsList = new ArrayList<>(queuedLeadsCache);
        }
    }

    /**
     * Initialize the cache from persistent storage after loading from DB
     */
    @PostLoad
    public void initializeQueueCache() {
        if (queuedLeadsList != null && !queuedLeadsList.isEmpty()) {
            queuedLeadsCache = new ArrayDeque<>(queuedLeadsList);
        }
    }

    /**
     * Ensure cache is synced before saving to DB
     */
    @PrePersist
    @PreUpdate
    public void syncBeforeSave() {
        syncQueueToList();
    }

    // ======================== UTILITY METHODS ========================

    /**
     * Add counselor to institution
     */
    public void addCounselor(String counselorId) {
        if (counselorId != null && !counselors.contains(counselorId)) {
            counselors.add(counselorId);
            currentCounselors = counselors.size();
        }
    }

    /**
     * Remove counselor from institution
     */
    public void removeCounselor(String counselorId) {
        if (counselors.remove(counselorId)) {
            currentCounselors = counselors.size();
        }
    }

    /**
     * Check if institution can accept more counselors
     */
    public boolean canAddMoreCounselors() {
        return currentCounselors < maxCounselors;
    }

    /**
     * Get available counselor slots
     */
    public int getAvailableCounselorSlots() {
        return Math.max(0, maxCounselors - currentCounselors);
    }
}