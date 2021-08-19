package edu.iu.uits.lms.coursereviewform.controller;

import canvas.client.generated.api.CoursesApi;
import canvas.client.generated.model.Course;
import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import edu.iu.uits.lms.coursereviewform.model.QualtricsLaunch;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsDocumentRepository;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsLaunchRepository;
import edu.iu.uits.lms.lti.controller.LtiAuthenticationTokenAwareController;
import edu.iu.uits.lms.lti.security.LtiAuthenticationProvider;
import edu.iu.uits.lms.lti.security.LtiAuthenticationToken;
import edu.iu.uits.lms.coursereviewform.config.ToolConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/app")
@Slf4j
public class ToolController extends LtiAuthenticationTokenAwareController {

   @Autowired
   private ToolConfig toolConfig = null;

   @Autowired
   private CoursesApi coursesApi;

   @Autowired
   private QualtricsDocumentRepository qualtricsDocumentRepository;

   @Autowired
   private QualtricsLaunchRepository qualtricsLaunchRepository;

   @RequestMapping("/index/{courseId}/{documentId}")
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView index(@PathVariable("courseId") String courseId, @PathVariable("documentId") String documentId,  Model model, HttpServletRequest request) {
      LtiAuthenticationToken token = getValidatedToken(courseId);

      log.info("documentId = {}", documentId);

      long longDocumentId = Long.parseLong(documentId);

      Optional<QualtricsDocument> optionalQualtricsDocument = qualtricsDocumentRepository.findById(longDocumentId);

      if (token != null && courseId != null && ! optionalQualtricsDocument.isEmpty()) {
         final String userId = (String) token.getPrincipal();

         QualtricsDocument qualtricsDocument = optionalQualtricsDocument.get();

         if (qualtricsDocument.getOpen()) {
            List<QualtricsLaunch> launches = qualtricsDocument.getQualtricsLaunchs();

            if (launches != null && ! launches.isEmpty()) {
               List<QualtricsLaunch> reverseSortedLaunches = launches.stream().
                       sorted(Comparator.comparing(QualtricsLaunch::getCreatedOn).reversed()).
                       collect(Collectors.toList());

               final String lastOpenedUserId = reverseSortedLaunches.get(0).getUserId();

               model.addAttribute("lastOpenedUserId", lastOpenedUserId);
               return new ModelAndView("inuse");
            }
         } else {
            if (verifyOkayToLaunchDocument(courseId, userId, optionalQualtricsDocument.get())) {
               return new ModelAndView("redirect:" + qualtricsDocument.getBaseUrl());
            }
         }
      }

      return new ModelAndView("notfound");
   }

   private boolean verifyOkayToLaunchDocument(String courseId, String userId, QualtricsDocument qualtricsDocument) {
      final Course course = coursesApi.getCourse(courseId);

      if (course != null && userId != null && qualtricsDocument != null && qualtricsDocument.getBaseUrl() != null) {
         log.info("qualtrics Document = {}", qualtricsDocument);

         QualtricsLaunch qualtricsLaunch = new QualtricsLaunch();
         qualtricsLaunch.setQualtricsDocument(qualtricsDocument);
         qualtricsLaunch.setCourseId(courseId);
         qualtricsLaunch.setCourseTitle(course.getName());
         qualtricsLaunch.setUserId(userId);

         qualtricsLaunch = qualtricsLaunchRepository.save(qualtricsLaunch);

         return true;
      }
      return false;
   }
}
