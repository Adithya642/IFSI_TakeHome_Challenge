package edu.ifsi.examrequests.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import edu.ifsi.examrequests.model.Course;
import edu.ifsi.examrequests.model.ExamRequest;
import edu.ifsi.examrequests.model.NewExamRequest;
import edu.ifsi.examrequests.service.ExamRequestService;

@RestController
@RequestMapping("/api")
public class ExamRequestController {

    private final ExamRequestService examRequestService;

    public ExamRequestController(ExamRequestService examRequestService) {
        this.examRequestService = examRequestService;
    }

    @GetMapping("/courses")
    public List<Course> getCourses() {
        return examRequestService.getActiveCourses();
    }

    @GetMapping("/requests")
    public List<ExamRequest> getRequests() {
        return examRequestService.getAllRequests();
    }

    // Validation errors are turned into a 400 by ApiExceptionHandler.
    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ExamRequest createRequest(@RequestBody NewExamRequest body) {
        return examRequestService.createRequest(body);
    }
}
