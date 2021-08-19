package edu.iu.uits.lms.coursereviewform.rest;

import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import edu.iu.uits.lms.coursereviewform.model.QualtricsLaunch;
import edu.iu.uits.lms.coursereviewform.model.QualtricsSubmission;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsDocumentRepository;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsLaunchRepository;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsSubmissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
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
    private QualtricsDocumentRepository qualtricsDocumentRepository;

    @Autowired
    private QualtricsLaunchRepository qualtricsLaunchRepository;

    @Autowired
    private QualtricsSubmissionRepository qualtricsSubmissionRepository;

    @PostMapping("/documents/")
    public QualtricsDocument createDocument(@RequestBody QualtricsDocument qualtricsDocument) {
        if (qualtricsDocument == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing job information");
        }

        return qualtricsDocumentRepository.save(qualtricsDocument);
    }

    @PutMapping("/documents/{id}")
    public QualtricsDocument updateDocument(@PathVariable Long id, @RequestBody QualtricsDocument qualtricsDocument) {
        QualtricsDocument qualtricsUpdatedDocument = qualtricsDocumentRepository.findById(id).orElse(null);

        if (qualtricsDocument == null || qualtricsUpdatedDocument == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing job information");
        }

        if (qualtricsDocument.getName() != null) {
            qualtricsUpdatedDocument.setName(qualtricsDocument.getName());
        }

        if (qualtricsDocument.getBaseUrl() != null) {
            qualtricsUpdatedDocument.setBaseUrl(qualtricsDocument.getBaseUrl());
        }

        if (qualtricsDocument.getOpen() != null) {
            qualtricsUpdatedDocument.setOpen(qualtricsDocument.getOpen());
        }

        if (qualtricsDocument.getToken() != null) {
            qualtricsUpdatedDocument.setToken(qualtricsDocument.getToken());
        }

        return qualtricsDocumentRepository.save(qualtricsUpdatedDocument);
    }

    @GetMapping("/documents/all")
    public List<QualtricsDocument> getAllDocuments() {
        log.info("in getAllDocuments()");
        List <QualtricsDocument> qualtricsDocuments = IterableUtils.toList(qualtricsDocumentRepository.findAll());

        log.info("leaving getAllDocuments()");

        return qualtricsDocuments;
    }

    @GetMapping("/documents/id/{id}")
    public QualtricsDocument getDocumentById(@PathVariable Long id) {
        return qualtricsDocumentRepository.findById(id).orElse(null);
    }

    @GetMapping("/launches/all")
    public List<QualtricsLaunch> getAllLaunches() {
        return IterableUtils.toList(qualtricsLaunchRepository.findAll());
    }

    @GetMapping("/launches/id/{id}")
    public QualtricsLaunch getLaunchById(@PathVariable Long id) {
        return qualtricsLaunchRepository.findById(id).orElse(null);
    }

    @GetMapping("/launches/courseid/{courseid}")
    public List<QualtricsLaunch> getLaunchesByCourseId(@PathVariable String courseid) {
        return qualtricsLaunchRepository.getByCourseId(courseid);
    }

    @PostMapping("/submissions/")
    public QualtricsSubmission createSubmission(@RequestBody QualtricsSubmission qualtricsSubmission) {
        if (qualtricsSubmission == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing job information");
        }

        return qualtricsSubmissionRepository.save(qualtricsSubmission);
    }

    @PutMapping("/submissions/{id}")
    public QualtricsSubmission updateSubmission(@PathVariable Long id, @RequestBody QualtricsSubmission qualtricsSubmission) {
        QualtricsSubmission qualtricsUpdatedSubmission = qualtricsSubmissionRepository.findById(id).orElse(null);

        if (qualtricsSubmission == null || qualtricsUpdatedSubmission == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing job information");
        }

        if (qualtricsSubmission.getCourseId() != null) {
            qualtricsUpdatedSubmission.setCourseId(qualtricsSubmission.getCourseId());
        }

        if (qualtricsSubmission.getCourseTitle() != null) {
            qualtricsUpdatedSubmission.setCourseTitle(qualtricsSubmission.getCourseTitle());
        }

        if (qualtricsSubmission.getUserId() == null) {
            qualtricsUpdatedSubmission.setUserId(qualtricsSubmission.getUserId());
        }

        if (qualtricsSubmission.getResponseId() != null) {
            qualtricsUpdatedSubmission.setResponseId(qualtricsSubmission.getResponseId());
        }

        return qualtricsSubmissionRepository.save(qualtricsUpdatedSubmission);
    }

    @GetMapping("/submissions/all")
    public List<QualtricsSubmission> getAllSubmissions() {
        return IterableUtils.toList(qualtricsSubmissionRepository.findAll());
    }

    @GetMapping("/submissions/id/{id}")
    public QualtricsSubmission getSubmissionById(@PathVariable Long id) {
        return qualtricsSubmissionRepository.findById(id).orElse(null);
    }

    @GetMapping("/submissions/courseid/{courseid}")
    public List<QualtricsSubmission> getSubmissionsByCourseId(@PathVariable String courseid) {
        return qualtricsSubmissionRepository.getByCourseId(courseid);
    }
}
