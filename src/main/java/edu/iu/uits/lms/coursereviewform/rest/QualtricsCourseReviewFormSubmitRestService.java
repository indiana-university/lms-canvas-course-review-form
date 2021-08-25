package edu.iu.uits.lms.coursereviewform.rest;

import canvas.client.generated.api.CoursesApi;
import canvas.client.generated.model.Course;
import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import edu.iu.uits.lms.coursereviewform.model.QualtricsRestSubmission;
import edu.iu.uits.lms.coursereviewform.model.QualtricsSubmission;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsDocumentRepository;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsSubmissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping({"/rest/submit"})
@Slf4j
public class QualtricsCourseReviewFormSubmitRestService {
    @Autowired
    QualtricsDocumentRepository qualtricsDocumentRepository;

    @Autowired
    private QualtricsSubmissionRepository qualtricsSubmissionRepository;

    @Autowired
    @Qualifier("coursesApiViaAnonymous")
    private CoursesApi coursesApi;

    @PostMapping("/fromqualtrics")
    public void submit(@RequestHeader Map<String, String> headers, @RequestBody QualtricsRestSubmission qualtricsRestSubmission) {
        if (qualtricsRestSubmission == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing job information");
        }

        String tokenHeader = headers.get("x-api-token");

        if (tokenHeader == null) {
            log.error("Auth header not found");
            return;
        }

        QualtricsDocument qualtricsDocument = qualtricsDocumentRepository.getByToken(tokenHeader);

        if (qualtricsDocument == null) {
            log.error("Could not find document by token = {}", tokenHeader);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing job information - token");
        }

        final String courseId = qualtricsRestSubmission.getCourseId();
        Course course = null;

        if (courseId != null && ! courseId.isEmpty()) {
            course = coursesApi.getCourse(courseId);
        }

        QualtricsSubmission qualtricsSubmission = new QualtricsSubmission();
        qualtricsSubmission.setQualtricsDocument(qualtricsDocument);
        qualtricsSubmission.setCourseId(courseId);

        if (course != null && course.getName() != null) {
            qualtricsSubmission.setCourseTitle(course.getName());
        }

        qualtricsSubmission.setUserId(qualtricsRestSubmission.getLastSubmittedBy());
        qualtricsSubmission.setResponseId(qualtricsRestSubmission.getResponseId());

        qualtricsSubmission = qualtricsSubmissionRepository.save(qualtricsSubmission);

        qualtricsDocument.setOpen(false);

        qualtricsDocumentRepository.save(qualtricsDocument);

        log.info("Saved submission id {} from user {} for documentId {}",
                qualtricsSubmission.getId(), qualtricsSubmission.getUserId(), qualtricsDocument.getId());
    }
}
