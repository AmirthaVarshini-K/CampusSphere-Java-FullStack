package com.campussphere.repository;

import com.campussphere.entity.Role;
import com.campussphere.entity.RoleCode;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @EntityGraph(attributePaths = {"permissions"})
    Optional<Role> findByCodeAndDeletedFalse(RoleCode code);

    boolean existsByCodeAndDeletedFalse(RoleCode code);
}
