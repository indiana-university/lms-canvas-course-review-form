package edu.iu.uits.lms.coursereviewform.controller;

import canvas.client.generated.api.CoursesApi;
import canvas.client.generated.api.UsersApi;
import canvas.client.generated.model.Course;
import canvas.client.generated.model.User;
import com.google.gson.Gson;
import edu.iu.uits.lms.coursereviewform.model.JsonParameters;
import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import edu.iu.uits.lms.coursereviewform.model.QualtricsLaunch;
import edu.iu.uits.lms.coursereviewform.model.QualtricsSubmission;
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

      // Do we have all the starting information needed to prepare a qualtrics survey launch?
      if (token != null && courseId != null && ! optionalQualtricsDocument.isEmpty()) {
         final String userId = (String) token.getPrincipal();

         QualtricsDocument qualtricsDocument = optionalQualtricsDocument.get();

         // Does someone else have this document open?
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
         } else { // nobody else has this document open. Let's open it and launch
            final Course course = coursesApi.getCourse(courseId);
            if (verifyOkayAndSetStateToLaunchDocument(course, userId, optionalQualtricsDocument.get())) {

               // Set up this object w/ parameters needed for qualtrics so we can
               // base64 these parameters easily and add it to the end of the launch URL
               JsonParameters jsonParameters = new JsonParameters();
               jsonParameters.setCourseId(courseId);
               jsonParameters.setCourseTitle(course.getName());

               List<QualtricsLaunch> reverseSortedLaunches = optionalQualtricsDocument.get().
                       getQualtricsLaunchs().stream().
                       sorted(Comparator.comparing(QualtricsLaunch::getCreatedOn).reversed()).
                       collect(Collectors.toList());

               jsonParameters.setLastOpenedBy(userId);

               // add the current launch to this list. We won't be persisting this so
               // it's okay to just make a disposable one w/ the bare minimum need for parameter
               // generation
               QualtricsLaunch lastQualtricsLaunch = new QualtricsLaunch();
               lastQualtricsLaunch.setUserId(jsonParameters.getLastOpenedBy());

               reverseSortedLaunches.add(0, lastQualtricsLaunch);

               // set the userX values in the JSON object by using Java reflection
               for (int i = 0; i < 5 && i < reverseSortedLaunches.size(); i++) {
                  QualtricsLaunch qualtricsLaunch = reverseSortedLaunches.get(i);

                  String localUserId = qualtricsLaunch.getUserId();

                  User localUser = usersApi.getUserBySisLoginId(localUserId);

                  String localUsername = null;

                  // in case (for whatever reason) the name in the db canvas can't find
                  if (localUser != null) {
                     localUsername = usersApi.getUserBySisLoginId(localUserId).getName();
                  }

                  try {
                     Method setUserIdMethod = jsonParameters.getClass().
                             getMethod("setUserId" + (i + 1), String.class);
                     Method setUsernameMethod = jsonParameters.getClass().
                             getMethod("setUserId" + (i + 1) + "Name", String.class);

                     setUserIdMethod.invoke(jsonParameters, localUserId);
                     setUsernameMethod.invoke(jsonParameters, localUsername);
                  } catch (Exception e) {
                     log.error("Could not invoke method ", e);
                     throw new RuntimeException("Could not invoke Java method");
                  }
               }

               // We need to get the most recent responseId (which we get on qualtrics survey submission)
               // so that we can add that to the launch URL. If this is a first launch
               // of this document or we never received any submissions for it, we won't
               // have a responseId
               List<QualtricsSubmission> reverseSortedSubmissions = optionalQualtricsDocument.get().
                       getQualtricsSubmissions().stream().
                       sorted(Comparator.comparing(QualtricsSubmission::getCreatedOn).reversed()).
                       collect(Collectors.toList());

               String lastResponseId = null;

               if (reverseSortedSubmissions != null && ! reverseSortedSubmissions.isEmpty()) {
                  lastResponseId = reverseSortedSubmissions.get(0).getResponseId();
               }

               // Make JSON of these now set parameters and then Base64 encode that JSON
               // so that we can add it to the launch URL
               Gson gson = new Gson();
               String jsonString = gson.toJson(jsonParameters);

               String addedParameters = "";

               // if we have a responseId, add this to the launch URL (Base64 encoded)
               if (lastResponseId != null) {
                  addedParameters = "Q_R=" +
                          new String(Base64.encodeBase64(lastResponseId.getBytes())) +
                          "&QDEL=1&";
               }

               // Now add the every launch parameters (Base64 encoded)
               addedParameters = addedParameters + "Q_EED=" +
                       new String(Base64.encodeBase64(jsonString.getBytes()));

               // launch the qualtrics document w/ all the generated parameters
               return new ModelAndView("redirect:" + qualtricsDocument.getBaseUrl() +
                       "?" + addedParameters);
            }
         }
      } // end of - all starting parameters exist for a qualtrics launch url generation attempt

      // if we reach here something was missing. Most likely a missing
      // document_id tool lti launch parameter.
      return new ModelAndView("notfound");
   }

   private boolean verifyOkayAndSetStateToLaunchDocument(Course course, String userId, QualtricsDocument qualtricsDocument) {
      if (course != null && userId != null && qualtricsDocument != null && qualtricsDocument.getBaseUrl() != null) {
         log.info("qualtrics Document = {}", qualtricsDocument);

         QualtricsLaunch qualtricsLaunch = new QualtricsLaunch();
         qualtricsLaunch.setQualtricsDocument(qualtricsDocument);
         qualtricsLaunch.setCourseId(course.getId());
         qualtricsLaunch.setCourseTitle(course.getName());
         qualtricsLaunch.setUserId(userId);

         qualtricsLaunch = qualtricsLaunchRepository.save(qualtricsLaunch);

         qualtricsDocument.setOpen(true);

         qualtricsDocumentRepository.save(qualtricsDocument);

         return true;
      }
      return false;
   }
}
