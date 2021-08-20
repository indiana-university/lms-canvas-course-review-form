package edu.iu.uits.lms.coursereviewform.controller;

import canvas.client.generated.api.CoursesApi;
import canvas.client.generated.api.UsersApi;
import canvas.client.generated.model.Course;
import com.google.gson.Gson;
import edu.iu.uits.lms.coursereviewform.model.JsonParameters;
import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import edu.iu.uits.lms.coursereviewform.model.QualtricsLaunch;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsDocumentRepository;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsLaunchRepository;
import edu.iu.uits.lms.lti.controller.LtiAuthenticationTokenAwareController;
import edu.iu.uits.lms.lti.security.LtiAuthenticationProvider;
import edu.iu.uits.lms.lti.security.LtiAuthenticationToken;
import edu.iu.uits.lms.coursereviewform.config.ToolConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
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
   private UsersApi usersApi;

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
            final Course course = coursesApi.getCourse(courseId);
            if (verifyOkayToLaunchDocument(course, userId, optionalQualtricsDocument.get())) {

// [base_url]?course_id=2023963&course_title=Qualtrics%20Test%20Course&
// last_opened_by=leward&userid1=leward&userid1_name=Lynn%20Ward&userid2=&
// userid2_name=&userid3=&userid3_name=&userid4=&userid4_name=&userid5=&userid5_name=

               JsonParameters jsonParameters = new JsonParameters();
               jsonParameters.setCourseId(courseId);
               jsonParameters.setCourseTitle(course.getName());

               List<QualtricsLaunch> reverseSortedLaunches = optionalQualtricsDocument.get().
                       getQualtricsLaunchs().stream().
                       sorted(Comparator.comparing(QualtricsLaunch::getCreatedOn).reversed()).
                       collect(Collectors.toList());

               jsonParameters.setLastOpenedBy(userId);

               // add the current launch to this list
               QualtricsLaunch lastQualtricsLaunch = new QualtricsLaunch();
               lastQualtricsLaunch.setUserId(jsonParameters.getLastOpenedBy());

               reverseSortedLaunches.add(0, lastQualtricsLaunch);

               for (int i = 0; i < 4 && i < reverseSortedLaunches.size(); i++) {
                  QualtricsLaunch qualtricsLaunch = reverseSortedLaunches.get(i);

                  String localUserId = qualtricsLaunch.getUserId();
                  String localUsername = usersApi.getUserBySisLoginId(localUserId).getName();

                  try {
                     Method setUserIdMethod = jsonParameters.getClass().
                             getMethod("setUserId" + (i + 1), String.class);
                     Method setUsernameMethod = jsonParameters.getClass().
                             getMethod("setUserId" + (i + 1) + "Name", String.class);

                     setUserIdMethod.invoke(jsonParameters, localUserId);
                     setUsernameMethod.invoke(jsonParameters, localUsername);
                  } catch (Exception e) {
                     log.error("Could not invoke method ", e);
                  }
               }


               Gson gson = new Gson();
               String jsonString = gson.toJson(jsonParameters);

               String base64EncodedParameters = new String(Base64.encodeBase64(jsonString.getBytes()));
               return new ModelAndView("redirect:" + qualtricsDocument.getBaseUrl() +
                       "?Q_EED=" + base64EncodedParameters);
            }
         }
      }

      return new ModelAndView("notfound");
   }

   private boolean verifyOkayToLaunchDocument(Course course, String userId, QualtricsDocument qualtricsDocument) {
      if (course != null && userId != null && qualtricsDocument != null && qualtricsDocument.getBaseUrl() != null) {
         log.info("qualtrics Document = {}", qualtricsDocument);

         QualtricsLaunch qualtricsLaunch = new QualtricsLaunch();
         qualtricsLaunch.setQualtricsDocument(qualtricsDocument);
         qualtricsLaunch.setCourseId(course.getId());
         qualtricsLaunch.setCourseTitle(course.getName());
         qualtricsLaunch.setUserId(userId);

         qualtricsLaunch = qualtricsLaunchRepository.save(qualtricsLaunch);

//         qualtricsDocument.setOpen(true);

         qualtricsDocumentRepository.save(qualtricsDocument);

         return true;
      }
      return false;
   }
}
