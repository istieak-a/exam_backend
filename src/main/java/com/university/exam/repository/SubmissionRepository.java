package com.university.exam.repository;

import com.university.exam.model.ExamSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<ExamSubmission, Long> {

    List<ExamSubmission> findByExamId(Long examId);

    List<ExamSubmission> findByExamIdIn(Collection<Long> examIds);

    Page<ExamSubmission> findByExamIdIn(Collection<Long> examIds, Pageable pageable);

    List<ExamSubmission> findByStudentId(Long studentId);

    Page<ExamSubmission> findByStudentId(Long studentId, Pageable pageable);

    Optional<ExamSubmission> findByExamIdAndStudentId(Long examId, Long studentId);

    long countByExamId(Long examId);

    boolean existsByExamId(Long examId);
}
