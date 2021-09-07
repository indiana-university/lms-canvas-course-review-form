package edu.iu.uits.lms.coursereviewform.rest;

import edu.iu.uits.lms.coursereviewform.model.QualtricsCourse;
import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import edu.iu.uits.lms.coursereviewform.model.QualtricsLaunch;
import edu.iu.uits.lms.coursereviewform.model.QualtricsSubmission;
import edu.iu.uits.lms.coursereviewform.service.QualtricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping({"/rest/db/qualtricscoursereviewform"})
@Slf4j
public class QualtricsCourseReviewFormDbRestService {
    @Autowired
    private QualtricsService qualtricsService;

    @PostMapping("/documents/")
    public QualtricsDocument createDocument(@RequestBody QualtricsDocument qualtricsDocument) {
        if (qualtricsDocument == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing request body");
        }

        return qualtricsService.saveDocument(qualtricsDocument);
    }

    @PutMapping("/documents/{id}")
    public QualtricsDocument updateDocument(@PathVariable Long id, @RequestBody QualtricsDocument qualtricsDocument) {
        QualtricsDocument qualtricsUpdatedDocument = qualtricsService.getDocument(id);

        if (qualtricsDocument == null || qualtricsUpdatedDocument == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing request body or non existent document");
        }

        if (qualtricsDocument.getName() != null) {
            qualtricsUpdatedDocument.setName(qualtricsDocument.getName());
        }

        if (qualtricsDocument.getBaseUrl() != null) {
            qualtricsUpdatedDocument.setBaseUrl(qualtricsDocument.getBaseUrl());
        }

        if (qualtricsDocument.getToken() != null) {
            qualtricsUpdatedDocument.setToken(qualtricsDocument.getToken());
        }

        return qualtricsService.saveDocument(qualtricsUpdatedDocument);
    }

    @GetMapping("/documents/all")
    public List<QualtricsDocument> getAllDocuments() {
        return qualtricsService.getAllDocuments();
    }

    @GetMapping("/documents/id/{id}")
    public QualtricsDocument getDocumentById(@PathVariable Long id) {
        return qualtricsService.getDocument(id);
    }

    @GetMapping("/courses/all")
    public List<QualtricsCourse> getAllCourses() {
        return qualtricsService.getAllCourses();
    }

    @GetMapping("/courses/id/{id}")
    public QualtricsCourse getCouseById(@PathVariable Long id) {
        return qualtricsService.getCourse(id);
    }

    @PutMapping("/courses/{id}/close")
    public QualtricsCourse closeCourse(@PathVariable Long id) {
        QualtricsCourse qualtricsUpdatedCourse = qualtricsService.getCourse(id);

        if (qualtricsUpdatedCourse == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course not found");
        }

        qualtricsUpdatedCourse.setOpen(false);

        log.info("Closed couse {}", id);
        return qualtricsService.saveCourse(qualtricsUpdatedCourse);
    }

    @PutMapping("/courses/{id}")
    public QualtricsCourse updateCourse(@PathVariable Long id, @RequestBody QualtricsCourse qualtricsCourse) {
        QualtricsCourse qualtricsUpdatedCourse = qualtricsService.getCourse(id);

        if (qualtricsCourse == null || qualtricsUpdatedCourse == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing request body or non existent course");
        }

        if (qualtricsCourse.getCourseId() != null) {
            qualtricsUpdatedCourse.setCourseId(qualtricsCourse.getCourseId());
        }

        if (qualtricsCourse.getCourseTitle() != null) {
            qualtricsUpdatedCourse.setCourseTitle(qualtricsCourse.getCourseTitle());
        }

        if (qualtricsCourse.getOpen() != null) {
            qualtricsUpdatedCourse.setOpen(qualtricsCourse.getOpen());
        }

        return qualtricsService.saveCourse(qualtricsUpdatedCourse);
    }

    @GetMapping("/launches/all")
    public List<QualtricsLaunch> getAllLaunches() {
        return qualtricsService.getAllLaunches();
    }

    @GetMapping("/launches/id/{id}")
    public QualtricsLaunch getLaunchById(@PathVariable Long id) {
        return qualtricsService.getLaunch(id);
    }

    @GetMapping("/documents/{documentid}/launches/courseid/{canvascourseid}")
    public List<QualtricsLaunch> getLaunchesByDocumentAndCanvasCourseId(@PathVariable("documentid") Long documentId,
                                                                        @PathVariable("canvascourseid") String canvasCourseId) {
        QualtricsDocument qualtricsDocument = qualtricsService.getDocument(documentId);

        if (qualtricsDocument != null) {
            QualtricsCourse qualtricsCourse = qualtricsService.getCourse(qualtricsDocument, canvasCourseId);

            if (qualtricsCourse != null) {
                return qualtricsCourse.getQualtricsLaunches();
            }
        }

        return null;
    }

    @PostMapping("/submissions/")
    public QualtricsSubmission createSubmission(@RequestBody QualtricsSubmission qualtricsSubmission) {
        if (qualtricsSubmission == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing request body");
        }

        return qualtricsService.saveSubmission(qualtricsSubmission);
    }

    @PutMapping("/submissions/{id}")
    public QualtricsSubmission updateSubmission(@PathVariable Long id, @RequestBody QualtricsSubmission qualtricsSubmission) {
        QualtricsSubmission qualtricsUpdatedSubmission = qualtricsService.getSubmission(id);

        if (qualtricsSubmission == null || qualtricsUpdatedSubmission == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing request body or non existent submission");
        }

        if (qualtricsSubmission.getUserId() == null) {
            qualtricsUpdatedSubmission.setUserId(qualtricsSubmission.getUserId());
        }

        if (qualtricsSubmission.getResponseId() != null) {
            qualtricsUpdatedSubmission.setResponseId(qualtricsSubmission.getResponseId());
        }

        return qualtricsService.saveSubmission(qualtricsUpdatedSubmission);
    }

    @GetMapping("/submissions/all")
    public List<QualtricsSubmission> getAllSubmissions() {
        return qualtricsService.getAllSubmissions();
    }

    @GetMapping("/submissions/id/{id}")
    public QualtricsSubmission getSubmissionById(@PathVariable Long id) {
        return qualtricsService.getSubmission(id);
    }

    @GetMapping("/documents/{documentid}/submissions/courseid/{canvascourseid}")
    public List<QualtricsSubmission> getSubmissionsByDocumentAndCanvasCourseId(@PathVariable("documentid") Long documentId,
                                                                               @PathVariable("canvascourseid") String canvasCourseId) {
        QualtricsDocument qualtricsDocument = qualtricsService.getDocument(documentId);

        if (qualtricsDocument != null) {
            QualtricsCourse qualtricsCourse = qualtricsService.getCourse(qualtricsDocument, canvasCourseId);

            if (qualtricsCourse != null) {
                return qualtricsCourse.getQualtricsSubmissions();
            }
        }

        return null;
    }
}
