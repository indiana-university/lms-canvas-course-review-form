package edu.iu.uits.lms.coursereviewform.services;

import canvas.client.generated.api.CoursesApi;
import canvas.client.generated.api.UsersApi;
import canvas.client.generated.model.Course;
import com.google.gson.Gson;
import edu.iu.uits.lms.coursereviewform.model.JsonParameters;
import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import edu.iu.uits.lms.coursereviewform.model.QualtricsLaunch;
import edu.iu.uits.lms.coursereviewform.model.QualtricsSubmission;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsDocumentRepository;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsLaunchRepository;
import edu.iu.uits.lms.lti.security.LtiAuthenticationProvider;
import edu.iu.uits.lms.lti.security.LtiAuthenticationToken;
import edu.iu.uits.lms.coursereviewform.config.ToolConfig;
import edu.iu.uits.lms.coursereviewform.controller.ToolController;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(ToolController.class)
@Import(ToolConfig.class)
@ActiveProfiles("none")
public class AppLaunchSecurityTest {

   @Autowired
   private MockMvc mvc;

   @MockBean
   private CoursesApi coursesApi;

   @MockBean
   private UsersApi usersApi;

   @MockBean
   private QualtricsDocumentRepository qualtricsDocumentRepository;

   @MockBean
   private QualtricsLaunchRepository qualtricsLaunchRepository;

   @Before
   public void init() throws InterruptedException {
      QualtricsDocument qualtricsDocument1 = new QualtricsDocument();
      qualtricsDocument1.setId(1L);
      qualtricsDocument1.setName("Test document");
      qualtricsDocument1.setBaseUrl("https://www.iub.edu");
      qualtricsDocument1.setOpen(false);

      Mockito.when(qualtricsDocumentRepository.findById(1L)).thenReturn(java.util.Optional.of(qualtricsDocument1));

      QualtricsDocument qualtricsDocument2 = new QualtricsDocument();
      qualtricsDocument2.setId(2L);
      qualtricsDocument2.setName("Test document 2");
      qualtricsDocument2.setBaseUrl("https://www.iub.edu");
      qualtricsDocument2.setOpen(true);

      Mockito.when(qualtricsDocumentRepository.findById(2L)).thenReturn(java.util.Optional.of(qualtricsDocument2));

      Course course = new Course();
      course.setName("Test course name");

      Mockito.when(coursesApi.getCourse(any())).thenReturn(course);
   }

   @Test
   public void appNoAuthnLaunch() throws Exception {
      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/app/index/1234/1")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
   }

   @Test
   public void appAuthnWrongContextLaunch() throws Exception {
      LtiAuthenticationToken token = new LtiAuthenticationToken("userId",
            "asdf", "systemId",
            AuthorityUtils.createAuthorityList(LtiAuthenticationProvider.LTI_USER_ROLE, "authority"),
            "unit_test");

      SecurityContextHolder.getContext().setAuthentication(token);

      //This is a secured endpoint and should not not allow access without authn
      ResultActions mockMvcAction = mvc.perform(get("/app/index/1234/1")
              .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
              .contentType(MediaType.APPLICATION_JSON));

      mockMvcAction.andExpect(status().isInternalServerError());
      mockMvcAction.andExpect(MockMvcResultMatchers.view().name ("error"));
      mockMvcAction.andExpect(MockMvcResultMatchers.model().attributeExists("error"));
   }

   @Test
   public void appAuthnFirstLaunch() throws Exception {
      LtiAuthenticationToken token = new LtiAuthenticationToken("userId",
            "1234", "systemId",
            AuthorityUtils.createAuthorityList(LtiAuthenticationProvider.LTI_USER_ROLE, "authority"),
            "unit_test");

      SecurityContextHolder.getContext().setAuthentication(token);

      JsonParameters jsonParameters = new JsonParameters();
      jsonParameters.setCourseId("1234");
      jsonParameters.setCourseTitle("Test course name");
      jsonParameters.setLastOpenedBy("userId");
      jsonParameters.setUserId1("userId");

      Gson gson = new Gson();
      String jsonString = gson.toJson(jsonParameters);

      String expectedRedirectUrl = "https://www.iub.edu?Q_EED=" +
              new String(Base64.encodeBase64(jsonString.getBytes()));

      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/app/index/1234/1")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(expectedRedirectUrl));
   }

   @Test
   public void appAuthnVerifyLast5LaunchesLaunch() throws Exception {
      LtiAuthenticationToken token = new LtiAuthenticationToken("userId",
              "1234", "systemId",
              AuthorityUtils.createAuthorityList(LtiAuthenticationProvider.LTI_USER_ROLE, "authority"),
              "unit_test");

      SecurityContextHolder.getContext().setAuthentication(token);

      QualtricsDocument qualtricsDocument3 = new QualtricsDocument();
      qualtricsDocument3.setId(3L);
      qualtricsDocument3.setName("Test document 3");
      qualtricsDocument3.setBaseUrl("https://www.iub.edu");
      qualtricsDocument3.setOpen(false);

      List<QualtricsLaunch> qualtricsLaunches = new ArrayList<>();

      QualtricsLaunch qualtricsLaunch1 = new QualtricsLaunch();
      qualtricsLaunch1.setUserId("user1");
      qualtricsLaunch1.setCreatedOn(new Date());

      qualtricsLaunches.add(qualtricsLaunch1);

      // Add pause so dates are guaranteed to be different
      Thread.sleep(1000);

      QualtricsLaunch qualtricsLaunch2 = new QualtricsLaunch();
      qualtricsLaunch2.setUserId("user2");
      qualtricsLaunch2.setCreatedOn(new Date());

      qualtricsLaunches.add(qualtricsLaunch2);

      // Add pause so dates are guaranteed to be different
      Thread.sleep(1000);

      QualtricsLaunch qualtricsLaunch3 = new QualtricsLaunch();
      qualtricsLaunch3.setUserId("user3");
      qualtricsLaunch3.setCreatedOn(new Date());

      qualtricsLaunches.add(qualtricsLaunch3);

      // Add pause so dates are guaranteed to be different
      Thread.sleep(1000);

      QualtricsLaunch qualtricsLaunch4 = new QualtricsLaunch();
      qualtricsLaunch4.setUserId("user4");
      qualtricsLaunch4.setCreatedOn(new Date());

      qualtricsLaunches.add(qualtricsLaunch4);

      qualtricsDocument3.setQualtricsLaunchs(qualtricsLaunches);

      QualtricsSubmission qualtricsSubmission = new QualtricsSubmission();
      qualtricsSubmission.setUserId("user1");
      qualtricsSubmission.setResponseId("responseId1");
      qualtricsSubmission.setCreatedOn(new Date());

      qualtricsDocument3.setQualtricsSubmissions(Arrays.asList(qualtricsSubmission));

      Mockito.when(qualtricsDocumentRepository.findById(3L)).thenReturn(java.util.Optional.of(qualtricsDocument3));

      JsonParameters jsonParameters = new JsonParameters();
      jsonParameters.setCourseId("1234");
      jsonParameters.setCourseTitle("Test course name");
      jsonParameters.setLastOpenedBy("userId");
      jsonParameters.setUserId1("userId");
      jsonParameters.setUserId2("user4");
      jsonParameters.setUserId3("user3");
      jsonParameters.setUserId4("user2");
      jsonParameters.setUserId5("user1");

      Gson gson = new Gson();
      String jsonString = gson.toJson(jsonParameters);

      String expectedRedirectUrl = "https://www.iub.edu?" + "Q_R=" +
              new String(Base64.encodeBase64("responseId1".getBytes())) +
              "&QDEL=1" +
              "&Q_EED=" +
              new String(Base64.encodeBase64(jsonString.getBytes()));

      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/app/index/1234/3")
                      .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl(expectedRedirectUrl));
   }

   @Test
   public void appAuthnDocumentOpenLaunch() throws Exception {
      LtiAuthenticationToken token = new LtiAuthenticationToken("userId",
              "1234", "systemId",
              AuthorityUtils.createAuthorityList(LtiAuthenticationProvider.LTI_USER_ROLE, "authority"),
              "unit_test");

      SecurityContextHolder.getContext().setAuthentication(token);

      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/app/index/1234/2")
                      .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andExpect(view().name("inuse"));
   }

   @Test
   public void appAuthnDocumentNotFoundLaunch() throws Exception {
      LtiAuthenticationToken token = new LtiAuthenticationToken("userId",
              "1234", "systemId",
              AuthorityUtils.createAuthorityList(LtiAuthenticationProvider.LTI_USER_ROLE, "authority"),
              "unit_test");

      SecurityContextHolder.getContext().setAuthentication(token);

      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/app/index/1234/33")
                      .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andExpect(view().name("notfound"));
   }

   @Test
   public void randomUrlNoAuth() throws Exception {
      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/asdf/foobar")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
   }

   @Test
   public void randomUrlWithAuth() throws Exception {
      LtiAuthenticationToken token = new LtiAuthenticationToken("userId",
            "1234", "systemId",
            AuthorityUtils.createAuthorityList(LtiAuthenticationProvider.LTI_USER_ROLE, "authority"),
            "unit_test");
      SecurityContextHolder.getContext().setAuthentication(token);

      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/asdf/foobar")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
   }
}
