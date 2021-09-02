package edu.iu.uits.lms.coursereviewform.rest;

import edu.iu.uits.lms.coursereviewform.model.QualtricsCourse;
import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import edu.iu.uits.lms.coursereviewform.model.QualtricsRestSubmission;
import edu.iu.uits.lms.coursereviewform.model.QualtricsSubmission;
import edu.iu.uits.lms.coursereviewform.service.QualtricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
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
    private QualtricsService qualtricsService;

    @PostMapping("/fromqualtrics/{document_id}")
    public void submit(@PathVariable("document_id") Long documentId, @RequestHeader Map<String, String> headers, @RequestBody QualtricsRestSubmission qualtricsRestSubmission) {
        if (qualtricsRestSubmission == null || qualtricsRestSubmission.getCourseId() == null ||
                qualtricsRestSubmission.getLastSubmittedBy() == null ||
                qualtricsRestSubmission.getResponseId() == null ||
                documentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing information");
        }

        String tokenHeader = headers.get("x-api-token");

        if (tokenHeader == null) {
            log.error("Auth header not found");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Auth header not found");
        }

        QualtricsDocument qualtricsDocument = qualtricsService.getDocument(documentId);

        if (qualtricsDocument == null) {
            log.error("Could not find document by token = {}", tokenHeader);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing job information - token");
        }

        if (! tokenHeader.equals(qualtricsDocument.getToken())) {
            log.error("The provided token {} doesn't match expected token value {}", tokenHeader, qualtricsDocument.getToken());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided token doesn't match expected token value");
        }

        final String courseId = qualtricsRestSubmission.getCourseId();

        QualtricsCourse qualtricsCourse = qualtricsService.getCourse(qualtricsDocument, courseId);

        if (qualtricsCourse == null) {
            log.error("Cannot find course with id {} for document id {}", courseId, qualtricsDocument.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot find course for document");
        }

        QualtricsSubmission qualtricsSubmission = new QualtricsSubmission();
        qualtricsSubmission.setQualtricsCourse(qualtricsCourse);
        qualtricsSubmission.setUserId(qualtricsRestSubmission.getLastSubmittedBy());
        qualtricsSubmission.setResponseId(qualtricsRestSubmission.getResponseId());

        qualtricsSubmission = qualtricsService.saveSubmission(qualtricsSubmission);

        qualtricsCourse.setOpen(false);
        qualtricsCourse = qualtricsService.saveCourse(qualtricsCourse);

        log.info("Saved submission id {} from user {} for documentId {}",
                qualtricsSubmission.getId(), qualtricsSubmission.getUserId(), qualtricsDocument.getId());
    }
}
