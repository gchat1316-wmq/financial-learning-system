package com.investment.repository;

import com.investment.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    Optional<UserProgress> findByUserIdAndNodeId(Long userId, Long nodeId);

    List<UserProgress> findByUserId(Long userId);

    List<UserProgress> findByUserIdAndNextReviewDateBefore(Long userId, LocalDateTime date);

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.userId = :userId AND up.masteryLevel >= 4")
    int countMasteredNodes(@Param("userId") Long userId);

    @Query("SELECT up FROM UserProgress up WHERE up.userId = :userId ORDER BY up.masteryLevel ASC LIMIT :limit")
    List<UserProgress> findWeakestNodes(@Param("userId") Long userId, @Param("limit") int limit);
}
