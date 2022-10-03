package edu.iu.uits.lms.coursereviewform.services;

import com.google.gson.Gson;
import com.nimbusds.jose.shaded.json.JSONObject;
import edu.iu.uits.lms.common.session.CourseSessionService;
import edu.iu.uits.lms.coursereviewform.model.JsonParameters;
import edu.iu.uits.lms.coursereviewform.model.QualtricsCourse;
import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import edu.iu.uits.lms.coursereviewform.model.QualtricsLaunch;
import edu.iu.uits.lms.coursereviewform.model.QualtricsSubmission;
import edu.iu.uits.lms.coursereviewform.service.QualtricsService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.config.LtiClientTestConfig;
import edu.iu.uits.lms.coursereviewform.config.ToolConfig;
import edu.iu.uits.lms.coursereviewform.controller.ToolController;
import edu.iu.uits.lms.lti.config.TestUtils;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ox.ctl.lti13.lti.Claims;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(value = ToolController.class, properties = {"oauth.tokenprovider.url=http://foo"})
@Import({ToolConfig.class, LtiClientTestConfig.class})
//@RunWith(SpringRunner.class)
//@WebMvcTest(ToolController.class)
//@Import(ToolConfig.class)
//@ActiveProfiles("none")
public class AppLaunchSecurityTest {
   @Autowired
   private MockMvc mvc;

   @MockBean
   private QualtricsService qualtricsService;

//   @BeforeEach
//   public void init() throws InterruptedException {
//      Mockito.when(courseSessionService.getAttributeFromSession(any(HttpSession.class), any(), eq(BasicLTIConstants.LIS_PERSON_NAME_FULL), eq(String.class))).thenReturn("User Fullname");
//      Mockito.when(courseSessionService.getAttributeFromSession(any(HttpSession.class), any(), eq(BasicLTIConstants.CONTEXT_TITLE), eq(String.class))).thenReturn("Test course name");
//   }

   @Test
   public void appNoAuthnLaunch() throws Exception {
      //This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/app/index/1234/1")
                      .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isForbidden());
   }

   @Test
   public void appAuthnWrongContextLaunch() throws Exception {
      OidcAuthenticationToken token = buildTokenForUnitTests("5678");

      SecurityContextHolder.getContext().setAuthentication(token);

      //This is a secured endpoint and should not allow access without authn
      ResultActions mockMvcAction = mvc.perform(get("/app/index/1234/1")
              .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
              .contentType(MediaType.APPLICATION_JSON));

      mockMvcAction.andExpect(status().isInternalServerError());
      mockMvcAction.andExpect(MockMvcResultMatchers.view().name("error"));
      mockMvcAction.andExpect(MockMvcResultMatchers.model().attributeExists("error"));
   }

   @Test
   public void appAuthnFirstLaunch() throws Exception {
      OidcAuthenticationToken token = buildTokenForUnitTests("1234");

      SecurityContextHolder.getContext().setAuthentication(token);

      QualtricsDocument qualtricsDocument = new QualtricsDocument();
      qualtricsDocument.setId(1L);
      qualtricsDocument.setName("Test document");
      qualtricsDocument.setBaseUrl("https://www.iub.edu");

      QualtricsCourse qualtricsCourse = new QualtricsCourse();
      qualtricsCourse.setCourseId("1234");
      qualtricsCourse.setCourseTitle("Course title 1");
      qualtricsCourse.setOpen(false);

      QualtricsLaunch qualtricsLaunch = new QualtricsLaunch();
      qualtricsLaunch.setUserId("theUserLoginId");
      qualtricsLaunch.setUserFullName("User Fullname");

      qualtricsDocument.setQualtricsCourses(Arrays.asList(qualtricsCourse));

      Mockito.when(qualtricsService.getDocument(1L)).thenReturn(qualtricsDocument);
      Mockito.when(qualtricsService.createOrGetExistingCourse(eq(qualtricsDocument), eq("1234"), any(String.class))).thenReturn(qualtricsCourse);
      Mockito.when(qualtricsService.launchCourseDocument(any(), any(), eq(qualtricsCourse))).thenReturn(qualtricsCourse);
      Mockito.when(qualtricsService.getAscendingOrderedUniqueLaunches(qualtricsCourse)).thenReturn(Arrays.asList(qualtricsLaunch));

//      Mockito.when(courseSessionService.getAttributeFromSession(any(HttpSession.class), any(), eq(BasicLTIConstants.LIS_PERSON_NAME_FULL), eq(String.class))).thenReturn("User Fullname");
//      Mockito.when(courseSessionService.getAttributeFromSession(any(HttpSession.class), any(), eq(BasicLTIConstants.CONTEXT_TITLE), eq(String.class))).thenReturn("Test course name");

      JsonParameters jsonParameters = new JsonParameters();
      jsonParameters.setCourseId("1234");
      jsonParameters.setCourseTitle("Test course name");
      jsonParameters.setLastOpenedBy("theUserLoginId");
      jsonParameters.setUserId1("theUserLoginId");
      jsonParameters.setUserId1Name("User Fullname");

      Gson gson = new Gson();
      String jsonString = gson.toJson(jsonParameters);

      UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString("https://www.iub.edu");
      uriComponentsBuilder.queryParam("Q_EED", Base64.encodeBase64URLSafeString(jsonString.getBytes()));

      //This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/app/index/1234/1")
                      .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl(uriComponentsBuilder.toUriString()));
   }

   @Test
   public void appAuthnVerifyLast5LaunchesLaunch() throws Exception {
      OidcAuthenticationToken token = buildTokenForUnitTests("1234");

      SecurityContextHolder.getContext().setAuthentication(token);

      QualtricsDocument qualtricsDocument = new QualtricsDocument();
      qualtricsDocument.setId(1L);
      qualtricsDocument.setName("Test document");
      qualtricsDocument.setBaseUrl("https://www.iub.edu");

      QualtricsCourse qualtricsCourse = new QualtricsCourse();
      qualtricsCourse.setCourseId("1234");
      qualtricsCourse.setCourseTitle("Course title 1");
      qualtricsCourse.setOpen(false);

      List<QualtricsLaunch> qualtricsLaunches = new ArrayList<>();

      QualtricsLaunch qualtricsLaunch1 = new QualtricsLaunch();
      qualtricsLaunch1.setUserId("user1");
      qualtricsLaunch1.setUserFullName("User Fullname");
      qualtricsLaunch1.setCreatedOn(new Date());

      qualtricsLaunches.add(qualtricsLaunch1);

      QualtricsLaunch qualtricsLaunch2 = new QualtricsLaunch();
      qualtricsLaunch2.setUserId("user2");
      qualtricsLaunch2.setUserFullName("User Fullname");
      qualtricsLaunch2.setCreatedOn(new Date());

      qualtricsLaunches.add(qualtricsLaunch2);

      QualtricsLaunch qualtricsLaunch3 = new QualtricsLaunch();
      qualtricsLaunch3.setUserId("user3");
      qualtricsLaunch3.setUserFullName("User Fullname");
      qualtricsLaunch3.setCreatedOn(new Date());

      qualtricsLaunches.add(qualtricsLaunch3);

      QualtricsLaunch qualtricsLaunch4 = new QualtricsLaunch();
      qualtricsLaunch4.setUserId("user4");
      qualtricsLaunch4.setUserFullName("User Fullname");
      qualtricsLaunch4.setCreatedOn(new Date());

      qualtricsLaunches.add(qualtricsLaunch4);

      QualtricsLaunch qualtricsLaunch5 = new QualtricsLaunch();
      qualtricsLaunch5.setUserId("theUserLoginId");
      qualtricsLaunch5.setUserFullName("User Fullname");
      qualtricsLaunch5.setCreatedOn(new Date());

      qualtricsLaunches.add(qualtricsLaunch5);

      qualtricsCourse.setQualtricsLaunches(qualtricsLaunches);

      QualtricsSubmission qualtricsSubmission = new QualtricsSubmission();
      qualtricsSubmission.setUserId("user4");
      qualtricsSubmission.setResponseId("responseId1");
      qualtricsSubmission.setCreatedOn(new Date());

      qualtricsCourse.setQualtricsSubmissions(Arrays.asList(qualtricsSubmission));

      Mockito.when(qualtricsService.getDocument(1L)).thenReturn(qualtricsDocument);
      Mockito.when(qualtricsService.createOrGetExistingCourse(eq(qualtricsDocument), eq("1234"), any(String.class))).thenReturn(qualtricsCourse);
      Mockito.when(qualtricsService.launchCourseDocument(any(), any(), eq(qualtricsCourse))).thenReturn(qualtricsCourse);
      Mockito.when(qualtricsService.getAscendingOrderedUniqueLaunches(qualtricsCourse)).thenReturn(qualtricsCourse.getQualtricsLaunches());
      Mockito.when(qualtricsService.getMostRecentSubmission(qualtricsCourse)).thenReturn(qualtricsCourse.getQualtricsSubmissions().get(0));

      JsonParameters jsonParameters = new JsonParameters();
      jsonParameters.setCourseId("1234");
      jsonParameters.setCourseTitle("Test course name");
      jsonParameters.setLastOpenedBy("theUserLoginId");
      jsonParameters.setUserId1("user1");
      jsonParameters.setUserId1Name("User Fullname");
      jsonParameters.setUserId2("user2");
      jsonParameters.setUserId2Name("User Fullname");
      jsonParameters.setUserId3("user3");
      jsonParameters.setUserId3Name("User Fullname");
      jsonParameters.setUserId4("user4");
      jsonParameters.setUserId4Name("User Fullname");
      jsonParameters.setUserId5("theUserLoginId");
      jsonParameters.setUserId5Name("User Fullname");

      Gson gson = new Gson();
      String jsonString = gson.toJson(jsonParameters);

      UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString("https://www.iub.edu");
      uriComponentsBuilder.queryParam("Q_R", "responseId1");
      uriComponentsBuilder.queryParam("Q_R_DEL", "1");
      uriComponentsBuilder.queryParam("Q_EED", Base64.encodeBase64URLSafeString(jsonString.getBytes()));

      //This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/app/index/1234/1")
                      .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl(uriComponentsBuilder.toUriString()));
   }

   @Test
   public void appAuthnDocumentOpenLaunch() throws Exception {
      OidcAuthenticationToken token = buildTokenForUnitTests("1234");

      SecurityContextHolder.getContext().setAuthentication(token);

      QualtricsDocument qualtricsDocument = new QualtricsDocument();
      qualtricsDocument.setId(1L);
      qualtricsDocument.setName("Test document");
      qualtricsDocument.setBaseUrl("https://www.iub.edu");

      QualtricsCourse qualtricsCourse = new QualtricsCourse();
      qualtricsCourse.setCourseId("1234");
      qualtricsCourse.setCourseTitle("Course title 1");
      qualtricsCourse.setOpen(true);

      QualtricsLaunch qualtricsLaunch = new QualtricsLaunch();
      qualtricsLaunch.setUserId("theUserLoginId");
      qualtricsLaunch.setUserFullName("User Fullname");

      Mockito.when(qualtricsService.getDocument(1L)).thenReturn(qualtricsDocument);
      Mockito.when(qualtricsService.createOrGetExistingCourse(eq(qualtricsDocument), eq("1234"), any(String.class))).thenReturn(qualtricsCourse);
      Mockito.when(qualtricsService.getAscendingOrderedUniqueLaunches(qualtricsCourse)).thenReturn(Arrays.asList(qualtricsLaunch));

      //This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/app/index/1234/1")
                      .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andExpect(view().name("inuse"));
   }

   @Test
   public void appAuthnDocumentNotFoundLaunch() throws Exception {
      OidcAuthenticationToken token = buildTokenForUnitTests("1234");

      SecurityContextHolder.getContext().setAuthentication(token);

      //This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/app/index/1234/33")
                      .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andExpect(view().name("notfound"));
   }

   @Test
   public void randomUrlNoAuth() throws Exception {
      //This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/asdf/foobar")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
   }

   @Test
   public void randomUrlWithAuth() throws Exception {
      OidcAuthenticationToken token = buildTokenForUnitTests("1234");

      SecurityContextHolder.getContext().setAuthentication(token);

      //This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/asdf/foobar")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
   }

   private OidcAuthenticationToken buildTokenForUnitTests(String courseId) {

      Map<String, Object> extraAttributes = new HashMap<>();
      JSONObject customMap = new JSONObject();
      customMap.put(LTIConstants.CUSTOM_CANVAS_COURSE_ID_KEY, courseId);
      customMap.put(LTIConstants.CUSTOM_CANVAS_USER_LOGIN_ID_KEY, "theUserLoginId");

      extraAttributes.put(LTIConstants.CLAIMS_FULL_NAME_KEY, "User Fullname");

      Map<String, Object> contextClaimsMap = new JSONObject();
      contextClaimsMap.put(LTIConstants.CLAIMS_CONTEXT_TITLE_KEY, "Test course name");

      extraAttributes.put(Claims.CONTEXT, contextClaimsMap);

      OidcAuthenticationToken token = TestUtils.buildToken("theUserLoginId", LTIConstants.BASE_USER_AUTHORITY,
              extraAttributes, customMap);

      return token;
   }
}
