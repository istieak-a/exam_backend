package com.university.exam.service;

import com.university.exam.model.Exam;
import com.university.exam.model.ExamSubmission;
import com.university.exam.model.Question;
import com.university.exam.model.SubmissionAnswer;
import com.university.exam.model.User;
import com.university.exam.repository.ExamRepository;
import com.university.exam.repository.SubmissionRepository;
import com.university.exam.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    private static final List<Exam.ExamStatus> PUBLISHED_STATUSES =
            List.of(Exam.ExamStatus.PUBLISHED, Exam.ExamStatus.ACTIVE);

    public ExamService(ExamRepository examRepository,
                       SubmissionRepository submissionRepository,
                       UserRepository userRepository) {
        this.examRepository = examRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    private void validateExam(Exam exam) {
        Map<String, String> errors = new HashMap<>();

        if (exam.getTitle() == null || exam.getTitle().trim().isEmpty()) {
            errors.put("title", "Title is required");
        }
        if (exam.getCourse() == null || exam.getCourse().trim().isEmpty()) {
            errors.put("course", "Course is required");
        }
        if (exam.getExamType() == null) {
            errors.put("examType", "Exam type is required (MCQ or CQ)");
        }
        if (exam.getStartDateTime() == null) {
            errors.put("startDateTime", "Start date/time is required");
        }
        if (exam.getEndDateTime() == null) {
            errors.put("endDateTime", "End date/time is required");
        }
        if (exam.getQuestions() == null || exam.getQuestions().isEmpty()) {
            errors.put("questions", "At least one question is required");
        }

        if (exam.getTotalMarks() <= 0) {
            errors.put("totalMarks", "Total marks must be greater than 0");
        }
        if (exam.getPassingMarks() <= 0) {
            errors.put("passingMarks", "Passing marks must be greater than 0");
        }
        if (exam.getPassingMarks() > exam.getTotalMarks()) {
            errors.put("passingMarks", "Passing marks cannot exceed total marks");
        }

        if (exam.getDurationMinutes() <= 0) {
            errors.put("durationMinutes", "Duration must be greater than 0");
        }

        if (exam.getStartDateTime() != null && exam.getEndDateTime() != null) {
            if (exam.getStartDateTime() >= exam.getEndDateTime()) {
                errors.put("endDateTime", "End date/time must be after start date/time");
            }
        }

        if (exam.getQuestions() != null && !exam.getQuestions().isEmpty()) {
            int calculatedTotal = exam.getQuestions().stream()
                    .mapToInt(Question::getMarks)
                    .sum();

            if (calculatedTotal != exam.getTotalMarks()) {
                errors.put("totalMarks", "Total marks (" + exam.getTotalMarks() +
                    ") must equal sum of question marks (" + calculatedTotal + ")");
            }

            for (int i = 0; i < exam.getQuestions().size(); i++) {
                Question q = exam.getQuestions().get(i);
                if (q.getQuestionText() == null || q.getQuestionText().trim().isEmpty()) {
                    errors.put("questions[" + i + "].text", "Question text is required");
                }
                if (q.getMarks() <= 0) {
                    errors.put("questions[" + i + "].marks", "Question marks must be greater than 0");
                }

                if (q.getType() == Question.QuestionType.MCQ) {
                    if (q.getOptions() == null || q.getOptions().size() < 2) {
                        errors.put("questions[" + i + "].options", "MCQ must have at least 2 options");
                    }
                    if (q.getCorrectAnswer() == null || q.getCorrectAnswer().trim().isEmpty()) {
                        errors.put("questions[" + i + "].correctAnswer", "MCQ must have a correct answer");
                    }
                }

                if (q.getType() == Question.QuestionType.CQ) {
                    if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                        errors.put("questions[" + i + "].options", "CQ questions should not have options");
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    private Exam.ExamStatus calculateCurrentStatus(Exam exam) {
        if (exam.getStartDateTime() == null || exam.getEndDateTime() == null) {
            return exam.getStatus();
        }

        long now = System.currentTimeMillis();

        if (exam.getStatus() == Exam.ExamStatus.DRAFT) {
            return Exam.ExamStatus.DRAFT;
        }

        if (exam.getStatus() == Exam.ExamStatus.PUBLISHED || exam.getStatus() == Exam.ExamStatus.ACTIVE) {
            if (now >= exam.getStartDateTime() && now <= exam.getEndDateTime()) {
                return Exam.ExamStatus.ACTIVE;
            } else if (now > exam.getEndDateTime()) {
                return Exam.ExamStatus.COMPLETED;
            }
        }

        return exam.getStatus();
    }

    private Exam updateExamStatus(Exam exam) {
        Exam.ExamStatus newStatus = calculateCurrentStatus(exam);
        if (newStatus != exam.getStatus()) {
            exam.setStatus(newStatus);
            exam.setUpdatedAt(System.currentTimeMillis());
            return examRepository.save(exam);
        }
        return exam;
    }

    /**
     * Wire questions back to their parent exam and assign ordering. JPA assigns IDs on save.
     */
    private void assignQuestionMetadata(Exam exam) {
        if (exam.getQuestions() == null) {
            return;
        }
        for (int i = 0; i < exam.getQuestions().size(); i++) {
            Question question = exam.getQuestions().get(i);
            question.setExam(exam);
            if (question.getQuestionOrder() == 0) {
                question.setQuestionOrder(i + 1);
            }
        }
    }

    @Transactional
    public CompletableFuture<Exam> createExam(Exam exam, Long teacherId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<User> teacher = userRepository.findById(teacherId);
            if (teacher.isEmpty() || teacher.get().getRole() != User.UserRole.TEACHER) {
                throw new RuntimeException("Only teachers can create exams");
            }

            validateExam(exam);

            exam.setTeacherId(teacherId);
            exam.setTeacherName(teacher.get().getFullName());
            exam.setStatus(exam.getStatus() == null ? Exam.ExamStatus.PUBLISHED : exam.getStatus());
            long now = System.currentTimeMillis();
            if (exam.getCreatedAt() == 0L) {
                exam.setCreatedAt(now);
            }
            exam.setUpdatedAt(now);
            assignQuestionMetadata(exam);

            return examRepository.save(exam);
        });
    }

    @Transactional
    public CompletableFuture<Exam> updateExam(Long examId, Exam updatedExam, Long teacherId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Exam> existingExamOpt = examRepository.findById(examId);

            if (existingExamOpt.isEmpty()) {
                throw new RuntimeException("Exam not found");
            }

            Exam existing = existingExamOpt.get();
            if (!Objects.equals(existing.getTeacherId(), teacherId)) {
                throw new RuntimeException("Unauthorized: You can only update your own exams");
            }

            if (submissionRepository.existsByExamId(examId)) {
                throw new RuntimeException("Cannot update exam: Students have already submitted");
            }

            validateExam(updatedExam);

            existing.setTitle(updatedExam.getTitle());
            existing.setCourse(updatedExam.getCourse());
            existing.setDescription(updatedExam.getDescription());
            existing.setTotalMarks(updatedExam.getTotalMarks());
            existing.setPassingMarks(updatedExam.getPassingMarks());
            existing.setDurationMinutes(updatedExam.getDurationMinutes());
            existing.setStartDateTime(updatedExam.getStartDateTime());
            existing.setEndDateTime(updatedExam.getEndDateTime());
            existing.setExamType(updatedExam.getExamType());
            if (updatedExam.getStatus() != null) {
                existing.setStatus(updatedExam.getStatus());
            }
            existing.setUpdatedAt(System.currentTimeMillis());
            // Replace questions wholesale via the setter (clears+re-adds with parent linkage)
            existing.setQuestions(updatedExam.getQuestions());
            assignQuestionMetadata(existing);

            return examRepository.save(existing);
        });
    }

    @Transactional
    public CompletableFuture<Void> deleteExam(Long examId, Long teacherId) {
        return CompletableFuture.runAsync(() -> {
            Optional<Exam> exam = examRepository.findById(examId);

            if (exam.isEmpty()) {
                throw new RuntimeException("Exam not found");
            }

            if (!Objects.equals(exam.get().getTeacherId(), teacherId)) {
                throw new RuntimeException("Unauthorized: You can only delete your own exams");
            }

            if (submissionRepository.existsByExamId(examId)) {
                throw new RuntimeException("Cannot delete exam: Students have already submitted");
            }

            examRepository.deleteById(examId);
        });
    }

    public List<Exam> getPublishedExams() {
        return examRepository.findByStatusIn(PUBLISHED_STATUSES).stream()
                .map(this::updateExamStatus)
                .toList();
    }

    public Page<Exam> getPublishedExams(Pageable pageable) {
        return examRepository.findByStatusIn(PUBLISHED_STATUSES, pageable)
                .map(this::updateExamStatus);
    }

    public List<Exam> getTeacherExams(Long teacherId) {
        return examRepository.findByTeacherId(teacherId).stream()
                .map(this::updateExamStatus)
                .toList();
    }

    public Page<Exam> getTeacherExams(Long teacherId, Pageable pageable) {
        return examRepository.findByTeacherId(teacherId, pageable)
                .map(this::updateExamStatus);
    }

    public Optional<Exam> getExamById(Long examId, boolean isTeacher) {
        Optional<Exam> examOpt = examRepository.findById(examId);

        if (examOpt.isEmpty()) {
            return examOpt;
        }

        Exam exam = updateExamStatus(examOpt.get());

        if (!isTeacher) {
            // Hide correct answers for students by clearing them on the in-memory copy.
            // We do NOT persist this change.
            for (Question q : exam.getQuestions()) {
                q.setCorrectAnswer(null);
            }
        }

        return Optional.of(exam);
    }

    @Transactional
    public CompletableFuture<ExamSubmission> submitExam(
            Long examId, Long studentId, Map<String, String> answers) {

        return CompletableFuture.supplyAsync(() -> {
            Optional<ExamSubmission> existing =
                submissionRepository.findByExamIdAndStudentId(examId, studentId);
            existing.ifPresent(prev -> submissionRepository.deleteById(prev.getId()));

            Optional<Exam> examOpt = examRepository.findById(examId);
            if (examOpt.isEmpty()) {
                throw new RuntimeException("Exam not found");
            }

            Exam exam = examOpt.get();

            long now = System.currentTimeMillis();
            if (exam.getStartDateTime() != null && now < exam.getStartDateTime()) {
                throw new RuntimeException("Exam has not started yet");
            }
            if (exam.getEndDateTime() != null && now > exam.getEndDateTime()) {
                throw new RuntimeException("Exam has ended");
            }

            Optional<User> studentOpt = userRepository.findById(studentId);
            if (studentOpt.isEmpty()) {
                throw new RuntimeException("Student not found");
            }

            User student = studentOpt.get();

            ExamSubmission submission = new ExamSubmission();
            submission.setExamId(examId);
            submission.setExamTitle(exam.getTitle());
            submission.setExamType(exam.getExamType());
            submission.setMaxScore(exam.getTotalMarks());
            submission.setStudentId(studentId);
            submission.setStudentName(student.getFullName());
            submission.setSubmittedAt(now);

            int mcqScore = 0;
            boolean hasCQ = false;
            boolean hasMCQ = false;

            for (Question question : exam.getQuestions()) {
                String studentAnswer = answers != null ? answers.get(String.valueOf(question.getId())) : null;
                Integer awarded = null;

                if (question.getType() == Question.QuestionType.MCQ) {
                    hasMCQ = true;
                    boolean correct = studentAnswer != null
                            && question.getCorrectAnswer() != null
                            && studentAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim());
                    awarded = correct ? question.getMarks() : 0;
                    if (correct) {
                        mcqScore += question.getMarks();
                    }
                } else {
                    hasCQ = true;
                }

                SubmissionAnswer row = new SubmissionAnswer(submission, question.getId(), studentAnswer, awarded);
                submission.getSubmissionAnswers().add(row);
            }

            submission.setMcqScore(mcqScore);
            submission.setTotalScore(mcqScore);

            ExamSubmission.SubmissionStatus newStatus;
            if (!hasCQ) {
                newStatus = ExamSubmission.SubmissionStatus.FULLY_GRADED;
            } else if (hasMCQ) {
                newStatus = ExamSubmission.SubmissionStatus.GRADED_MCQ;
            } else {
                newStatus = ExamSubmission.SubmissionStatus.SUBMITTED;
            }
            submission.setStatus(newStatus);

            return submissionRepository.save(submission);
        });
    }

    @Transactional
    public CompletableFuture<ExamSubmission> gradeEssay(
            Long submissionId, int essayScore, Long teacherId) {

        return CompletableFuture.supplyAsync(() -> {
            Optional<ExamSubmission> submissionOpt = submissionRepository.findById(submissionId);
            if (submissionOpt.isEmpty()) {
                throw new RuntimeException("Submission not found");
            }

            ExamSubmission submission = submissionOpt.get();

            Optional<Exam> examOpt = examRepository.findById(submission.getExamId());
            if (examOpt.isEmpty() || !Objects.equals(examOpt.get().getTeacherId(), teacherId)) {
                throw new RuntimeException("Unauthorized: You can only grade your own exams");
            }

            submission.setEssayScore(essayScore);
            submission.setTotalScore(submission.getMcqScore() + essayScore);
            submission.setStatus(ExamSubmission.SubmissionStatus.FULLY_GRADED);

            return submissionRepository.save(submission);
        });
    }

    @Transactional
    public CompletableFuture<ExamSubmission> gradeCQSubmission(
            Long submissionId, Map<String, Integer> questionGrades, String feedback, Long teacherId) {

        return CompletableFuture.supplyAsync(() -> {
            Optional<ExamSubmission> submissionOpt = submissionRepository.findById(submissionId);
            if (submissionOpt.isEmpty()) {
                throw new RuntimeException("Submission not found");
            }

            ExamSubmission submission = submissionOpt.get();

            Optional<Exam> examOpt = examRepository.findById(submission.getExamId());
            if (examOpt.isEmpty() || !Objects.equals(examOpt.get().getTeacherId(), teacherId)) {
                throw new RuntimeException("Unauthorized: You can only grade your own exams");
            }

            int cqScore = questionGrades == null ? 0
                    : questionGrades.values().stream().filter(Objects::nonNull).mapToInt(Integer::intValue).sum();

            submission.setQuestionGrades(questionGrades);
            submission.setEssayScore(cqScore);
            submission.setTotalScore(submission.getMcqScore() + cqScore);
            submission.setStatus(ExamSubmission.SubmissionStatus.FULLY_GRADED);
            if (feedback != null && !feedback.isBlank()) {
                submission.setTeacherFeedback(feedback.strip());
            }

            return submissionRepository.save(submission);
        });
    }

    public List<ExamSubmission> getExamSubmissions(Long examId, Long teacherId) {
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (examOpt.isEmpty() || !Objects.equals(examOpt.get().getTeacherId(), teacherId)) {
            throw new RuntimeException("Unauthorized");
        }

        return submissionRepository.findByExamId(examId);
    }

    public List<ExamSubmission> getStudentSubmissions(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    public Page<ExamSubmission> getStudentSubmissions(Long studentId, Pageable pageable) {
        return submissionRepository.findByStudentId(studentId, pageable);
    }

    public Optional<ExamSubmission> getSubmission(Long submissionId) {
        return submissionRepository.findById(submissionId);
    }

    public List<ExamSubmission> getAllSubmissionsForTeacher(Long teacherId) {
        Set<Long> examIds = examRepository.findByTeacherId(teacherId).stream()
                .map(Exam::getId)
                .collect(Collectors.toSet());

        if (examIds.isEmpty()) {
            return List.of();
        }

        return submissionRepository.findByExamIdIn(examIds);
    }

    public Page<ExamSubmission> getAllSubmissionsForTeacher(Long teacherId, Pageable pageable) {
        Set<Long> examIds = examRepository.findByTeacherId(teacherId).stream()
                .map(Exam::getId)
                .collect(Collectors.toSet());

        if (examIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return submissionRepository.findByExamIdIn(examIds, pageable);
    }
}
