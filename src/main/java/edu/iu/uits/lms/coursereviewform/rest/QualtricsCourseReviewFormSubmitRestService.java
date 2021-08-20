package edu.iu.uits.lms.coursereviewform.rest;

import canvas.client.generated.api.CoursesApi;
import canvas.client.generated.model.Course;
import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import edu.iu.uits.lms.coursereviewform.model.QualtricsLaunch;
import edu.iu.uits.lms.coursereviewform.model.QualtricsRestSubmission;
import edu.iu.uits.lms.coursereviewform.model.QualtricsSubmission;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsDocumentRepository;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsLaunchRepository;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsSubmissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/rest/submit"})
@Slf4j
public class QualtricsCourseReviewFormSubmitRestService {
    @Autowired
    QualtricsDocumentRepository qualtricsDocumentRepository;

    @Autowired
    private QualtricsLaunchRepository qualtricsLaunchRepository;

    @Autowired
    private QualtricsSubmissionRepository qualtricsSubmissionRepository;

    @Autowired
    private CoursesApi coursesApi;

    @PostMapping("/fromqualtrics")
    public void submit(HttpServletRequest request, @RequestHeader Map<String, String> headers) {
            // @RequestBody QualtricsRestSubmission qualtricsRestSubmission) {
//        if (qualtricsRestSubmission == null) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing job information");
//        }

        String tokenHeader = headers.get("x-api-token");

        if (tokenHeader == null) {
            log.info("auth header not found");
            return;
        }

        QualtricsDocument qualtricsDocument = qualtricsDocumentRepository.getByToken(tokenHeader);

        if (qualtricsDocument == null) {
            log.info("Could not find document by token");
        } else {
            log.info("Document found as = {}", qualtricsDocument);
        }



//        log.info("key / value");
//        for(String key : headers.keySet()) {
//            log.info("{} / {}", key, headers.get(key));
//        }
//
//        log.info("request parameters");
//        Map<String, String[]> parameterMap = request.getParameterMap();
//
//        for(String key : parameterMap.keySet()) {
//            log.info("{} / {}", key, headers.get(key));
//        }


//        final String courseId = qualtricsRestSubmission.getCourseId();
//        Course course = null;
//
//        if (courseId != null) {
//            course = coursesApi.getCourse(courseId);
//        }
//
//        QualtricsSubmission qualtricsSubmission = new QualtricsSubmission();
//        qualtricsSubmission.setCourseId(courseId);
//
//        if (course != null && course.getName() != null) {
//            qualtricsSubmission.setCourseTitle(course.getName());
//        }
//
//        qualtricsSubmission.setUserId(qualtricsRestSubmission.getLastSubmittedBy());
//        qualtricsSubmission.setResponseId(qualtricsRestSubmission.getResponseId());
//
//        List<QualtricsLaunch> qualtricsLaunches = qualtricsLaunchRepository.getByCourseId(courseId);

        // TODO
//        qualtricsSubmission.setQualtricsDocument();

    }

    @GetMapping("/gimmie")
    public String getMe() {
        return "You have arrived";
    }

}
