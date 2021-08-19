package edu.iu.uits.lms.coursereviewform.services;

import canvas.client.generated.api.CoursesApi;
import canvas.client.generated.model.Course;
import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsDocumentRepository;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsLaunchRepository;
import edu.iu.uits.lms.lti.security.LtiAuthenticationProvider;
import edu.iu.uits.lms.lti.security.LtiAuthenticationToken;
import edu.iu.uits.lms.coursereviewform.config.ToolConfig;
import edu.iu.uits.lms.coursereviewform.controller.ToolController;
import org.checkerframework.checker.units.qual.C;
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

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
   private QualtricsDocumentRepository qualtricsDocumentRepository;

   @MockBean
   private QualtricsLaunchRepository qualtricsLaunchRepository;

   @Before
   public void init() {
      QualtricsDocument qualtricsDocument = new QualtricsDocument();
      qualtricsDocument.setId(-1L);
      qualtricsDocument.setName("Test document");
      qualtricsDocument.setBaseUrl("https://www.iub.edu");
      qualtricsDocument.setOpen(false);
      
      Mockito.when(qualtricsDocumentRepository.findById(any())).thenReturn(java.util.Optional.of(qualtricsDocument));

      Course course = new Course();
      course.setName("Test course name");

      Mockito.when(coursesApi.getCourse(any())).thenReturn(course);
   }

   @Test
   public void appNoAuthnLaunch() throws Exception {
      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/app/index/1234")
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
      ResultActions mockMvcAction = mvc.perform(get("/app/index/1234")
              .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
              .contentType(MediaType.APPLICATION_JSON));

      mockMvcAction.andExpect(status().isInternalServerError());
      mockMvcAction.andExpect(MockMvcResultMatchers.view().name ("error"));
      mockMvcAction.andExpect(MockMvcResultMatchers.model().attributeExists("error"));
   }

   @Test
   public void appAuthnLaunch() throws Exception {
      LtiAuthenticationToken token = new LtiAuthenticationToken("userId",
            "1234", "systemId",
            AuthorityUtils.createAuthorityList(LtiAuthenticationProvider.LTI_USER_ROLE, "authority"),
            "unit_test");

      SecurityContextHolder.getContext().setAuthentication(token);

      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/app/index/1234")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is3xxRedirection());
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
