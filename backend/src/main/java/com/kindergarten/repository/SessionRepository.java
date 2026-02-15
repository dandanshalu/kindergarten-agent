package com.kindergarten.repository;

import com.kindergarten.entity.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Page<Session> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    void deleteByUserIdAndId(Long userId, Long id);

    boolean existsByIdAndUserId(Long id, Long userId);
}
