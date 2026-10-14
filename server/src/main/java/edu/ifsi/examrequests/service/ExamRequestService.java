package edu.ifsi.examrequests.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.ifsi.examrequests.model.Course;
import edu.ifsi.examrequests.model.ExamRequest;
import edu.ifsi.examrequests.model.NewExamRequest;
import edu.ifsi.examrequests.model.ValidatedRequest;
import edu.ifsi.examrequests.repository.CourseRepository;
import edu.ifsi.examrequests.repository.ExamRequestRepository;

@Service
public class ExamRequestService {

    private final ExamRequestRepository examRequestRepository;
    private final CourseRepository courseRepository;
    private final ExamRequestValidator validator;

    public ExamRequestService(ExamRequestRepository examRequestRepository, CourseRepository courseRepository,
            ExamRequestValidator validator) {
        this.examRequestRepository = examRequestRepository;
        this.courseRepository = courseRepository;
        this.validator = validator;
    }

    public List<Course> getActiveCourses() {
        return courseRepository.findAllActive();
    }

    public List<ExamRequest> getAllRequests() {
        return examRequestRepository.findAll();
    }

    // Throws ValidationException if the input is invalid; nothing is saved in that case.
    public ExamRequest createRequest(NewExamRequest input) {
        ValidatedRequest valid = validator.validate(input);
        int requestId = examRequestRepository.insert(valid);
        return examRequestRepository.findById(requestId).orElseThrow();
    }
}
