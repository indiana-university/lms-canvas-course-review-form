package edu.iu.uits.lms.coursereviewform.controller;

import com.google.gson.Gson;
import edu.iu.uits.lms.coursereviewform.model.JsonParameters;
import edu.iu.uits.lms.coursereviewform.model.QualtricsCourse;
import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import edu.iu.uits.lms.coursereviewform.model.QualtricsLaunch;
import edu.iu.uits.lms.coursereviewform.model.QualtricsSubmission;
import edu.iu.uits.lms.coursereviewform.service.QualtricsService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.controller.OidcTokenAwareController;
import edu.iu.uits.lms.coursereviewform.config.ToolConfig;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/app")
@Slf4j
public class ToolController extends OidcTokenAwareController {

   @Autowired
   private ToolConfig toolConfig = null;

   @Autowired
   private QualtricsService qualtricsService;

   private static final String DOCUMENT_ID_CLAIM_NAME = "document_id";

   @RequestMapping("/launch")
   @Secured(LTIConstants.BASE_USER_ROLE)
   public ModelAndView launch(Model model, SecurityContextHolderAwareRequestWrapper request) {
      log.info("In launch");
      OidcAuthenticationToken token = getTokenWithoutContext();

      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      String courseId    = oidcTokenUtils.getCourseId();
      String documentId  = oidcTokenUtils.getCustomValue(DOCUMENT_ID_CLAIM_NAME);

      return index(courseId, documentId, model, request);
   }

      /**
       *
       * @param courseId Canvas numerical id like 1234
       * @param documentId Qualtrics database id
       * @param model
       * @param request
       * @return
       */
   @RequestMapping("/index/{courseId}/{documentId}")
   @Secured(LTIConstants.BASE_USER_ROLE)
   public ModelAndView index(@PathVariable("courseId") String courseId, @PathVariable("documentId") String documentId, Model model, HttpServletRequest request) {
      OidcAuthenticationToken token = getValidatedToken(courseId);
      log.info("documentId = {}", documentId);

      // if not set, documentId (document_id in lti launch) will come in as string "null"
      if (documentId == null || documentId.equals("null") || documentId.trim().length() == 0) {
         return new ModelAndView("notfound");
      }

      long longDocumentId = Long.parseLong(documentId);

      QualtricsDocument qualtricsDocument = qualtricsService.getDocument(longDocumentId);

      // Do we have all the starting information needed to prepare a qualtrics survey launch?
      if (token != null && courseId != null && qualtricsDocument != null) {
         OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

         final String userFullName = oidcTokenUtils.getPersonFullName();
         final String courseTitle  = oidcTokenUtils.getContextValue(LTIConstants.CLAIMS_CONTEXT_TITLE_KEY);
         final String userLoginId  = oidcTokenUtils.getUserLoginId();

         QualtricsCourse qualtricsCourse = qualtricsService.createOrGetExistingCourse(qualtricsDocument, courseId, courseTitle);

         // Does someone else have this document open?
         if (qualtricsCourse.getOpen()) {
            QualtricsLaunch lastOpenedQualtricsLaunch = qualtricsService.getLastLaunch(qualtricsCourse);

            if (lastOpenedQualtricsLaunch == null) {
               lastOpenedQualtricsLaunch = new QualtricsLaunch();
               lastOpenedQualtricsLaunch.setUserId("none");
               lastOpenedQualtricsLaunch.setUserFullName("None");
            }

            model.addAttribute("lastOpenedUserId", lastOpenedQualtricsLaunch.getUserId());
            model.addAttribute("lastOpenedUserFullName", lastOpenedQualtricsLaunch.getUserFullName());
            return new ModelAndView("inuse");
         } else { // nobody else has this document course open. Let's open it and launch
            qualtricsCourse = qualtricsService.launchCourseDocument(userLoginId, userFullName, qualtricsCourse);

            if (qualtricsCourse != null) {

               // Set up this object w/ parameters needed for qualtrics so we can
               // base64 these parameters easily and add it to the end of the launch URL
               JsonParameters jsonParameters = new JsonParameters();
               jsonParameters.setCourseId(courseId);
               jsonParameters.setCourseTitle(courseTitle);

               List<QualtricsLaunch> sortedAscendingByCreateDateUniqueLaunches = qualtricsService.getAscendingOrderedUniqueLaunches(qualtricsCourse);

               if (sortedAscendingByCreateDateUniqueLaunches == null) {
                  sortedAscendingByCreateDateUniqueLaunches = new ArrayList<>();
               }

               jsonParameters.setLastOpenedBy(userLoginId);

               // set the userX values in the JSON object by using Java reflection
               for (int i = 0; i < 5 && i < sortedAscendingByCreateDateUniqueLaunches.size(); i++) {
                  QualtricsLaunch qualtricsLaunch = sortedAscendingByCreateDateUniqueLaunches.get(i);

                  try {
                     Method setUserIdMethod = jsonParameters.getClass().
                             getMethod("setUserId" + (i + 1), String.class);
                     Method setUsernameMethod = jsonParameters.getClass().
                             getMethod("setUserId" + (i + 1) + "Name", String.class);

                     setUserIdMethod.invoke(jsonParameters, qualtricsLaunch.getUserId());
                     setUsernameMethod.invoke(jsonParameters, qualtricsLaunch.getUserFullName());
                  } catch (Exception e) {
                     log.error("Could not invoke method ", e);
                     throw new RuntimeException("Could not invoke Java method");
                  }
               }

               // We need to get the most recent responseId (which we get on qualtrics survey submission)
               // so that we can add that to the launch URL. If this is a first launch
               // of this document or we never received any submissions for it, we won't
               // have a responseId
               QualtricsSubmission mostRecentQualtricsSubmission = qualtricsService.getMostRecentSubmission(qualtricsCourse);

               String lastResponseId = mostRecentQualtricsSubmission != null ? mostRecentQualtricsSubmission.getResponseId() : null;

               // Make JSON of these now set parameters and then Base64 encode that JSON
               // so that we can add it to the launch URL
               Gson gson = new Gson();
               String jsonString = gson.toJson(jsonParameters);

               UriComponentsBuilder uriComponentsBuilder =
                       UriComponentsBuilder.fromUriString(qualtricsDocument.getBaseUrl());

               // if we have a responseId, add this to the launch URL (Base64 encoded)
               if (lastResponseId != null) {
                  uriComponentsBuilder.queryParam("Q_R", lastResponseId);
                  uriComponentsBuilder.queryParam("Q_R_DEL", "1");
               }

               // Now add the every launch parameters (Base64 encoded)
               uriComponentsBuilder.queryParam("Q_EED", Base64.encodeBase64URLSafeString(jsonString.getBytes()));

               // launch the qualtrics document w/ all the generated parameters
               return new ModelAndView("redirect:" + uriComponentsBuilder.toUriString());
            }
         }
      } // end of - all starting parameters exist for a qualtrics launch url generation attempt

      // if we reach here something was missing. Most likely a missing
      // document_id tool lti launch parameter.
      return new ModelAndView("notfound");
   }
}
