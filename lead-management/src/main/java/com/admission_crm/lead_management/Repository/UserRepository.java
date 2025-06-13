package com.admission_crm.lead_management.Repository;

import com.admission_crm.lead_management.Entity.CoreEntities.Role;
import com.admission_crm.lead_management.Entity.CoreEntities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    public List<User> findByRole(Role role);

    public Optional<User> findByEmail(String email);
}
