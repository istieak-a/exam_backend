package com.university.exam.repository;

import com.university.exam.model.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByTeacherId(Long teacherId);

    Page<Exam> findByTeacherId(Long teacherId, Pageable pageable);

    List<Exam> findByStatusIn(Collection<Exam.ExamStatus> statuses);

    Page<Exam> findByStatusIn(Collection<Exam.ExamStatus> statuses, Pageable pageable);

    List<Exam> findByStatus(Exam.ExamStatus status);

    List<Exam> findByCourseIgnoreCase(String course);

    List<Exam> findByExamType(Exam.ExamType examType);

    @Query("SELECT e FROM Exam e WHERE e.startDateTime <= :now AND e.endDateTime >= :now " +
           "AND e.status IN (com.university.exam.model.Exam$ExamStatus.ACTIVE, " +
           "                  com.university.exam.model.Exam$ExamStatus.PUBLISHED) " +
           "ORDER BY e.startDateTime DESC")
    List<Exam> findActive(@Param("now") long now);
}
