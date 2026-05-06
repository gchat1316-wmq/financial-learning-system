package com.investment.repository;

import com.investment.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    List<UserAnswer> findByUserId(Long userId);

    @Query("SELECT COUNT(ua), SUM(CASE WHEN ua.isCorrect = true THEN 1 ELSE 0 END) FROM UserAnswer ua WHERE ua.userId = :userId")
    List<Object[]> countByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT DAYOFWEEK(ua.answeredAt), COUNT(DISTINCT DATE(ua.answeredAt)) " +
            "FROM user_answers ua " +
            "WHERE ua.userId = :userId " +
            "AND ua.answeredAt >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY) " +
            "GROUP BY DAYOFWEEK(ua.answeredAt)", nativeQuery = true)
    List<Object[]> getHeatmapData(@Param("userId") Long userId);
}
