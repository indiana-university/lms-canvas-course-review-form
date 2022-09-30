//package edu.iu.uits.lms.coursereviewform.services;
//
//import edu.iu.uits.lms.coursereviewform.model.QualtricsCourse;
//import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
//import edu.iu.uits.lms.coursereviewform.model.QualtricsLaunch;
//import edu.iu.uits.lms.coursereviewform.model.QualtricsSubmission;
//import edu.iu.uits.lms.coursereviewform.repository.QualtricsCourseRepository;
//import edu.iu.uits.lms.coursereviewform.repository.QualtricsDocumentRepository;
//import edu.iu.uits.lms.coursereviewform.repository.QualtricsLaunchRepository;
//import edu.iu.uits.lms.coursereviewform.repository.QualtricsSubmissionRepository;
//import edu.iu.uits.lms.coursereviewform.service.QualtricsService;
//import org.joda.time.DateTime;
//import org.junit.Test;
//import org.junit.jupiter.api.Assertions;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//
//@RunWith(MockitoJUnitRunner.class)
//public class QualtricsServiceTest {
//    @InjectMocks
//    private QualtricsService qualtricsService;
//
//    @Mock
//    private QualtricsDocumentRepository qualtricsDocumentRepository;
//
//    @Mock
//    private QualtricsCourseRepository qualtricsCourseRepository;
//
//    @Mock
//    private QualtricsLaunchRepository qualtricsLaunchRepository;
//
//    @Mock
//    private QualtricsSubmissionRepository qualtricsSubmissionRepository;
//
//    @Test
//    public void getCourseFromADocumentTest() {
//        QualtricsDocument qualtricsDocument = new QualtricsDocument();
//        qualtricsDocument.setId(1L);
//        qualtricsDocument.setName("Test document");
//        qualtricsDocument.setBaseUrl("https://www.iub.edu");
//
//        List<QualtricsCourse> qualtricsCourses = new ArrayList<>();
//
//        QualtricsCourse qualtricsCourse1 = new QualtricsCourse();
//        qualtricsCourse1.setCourseId("1234");
//        qualtricsCourse1.setCourseTitle("Course title 1");
//        qualtricsCourse1.setOpen(false);
//
//        qualtricsCourses.add(qualtricsCourse1);
//
//        QualtricsCourse qualtricsCourse2 = new QualtricsCourse();
//        qualtricsCourse2.setCourseId("5678");
//        qualtricsCourse2.setCourseTitle("Course title 1");
//        qualtricsCourse2.setOpen(false);
//
//        qualtricsCourses.add(qualtricsCourse2);
//
//        QualtricsCourse qualtricsCourse3 = new QualtricsCourse();
//        qualtricsCourse3.setCourseId("9012");
//        qualtricsCourse3.setCourseTitle("Course title 1");
//        qualtricsCourse3.setOpen(false);
//
//        qualtricsCourses.add(qualtricsCourse3);
//
//        qualtricsDocument.setQualtricsCourses(qualtricsCourses);
//
//        QualtricsCourse foundCourse = null;
//
//        foundCourse = qualtricsService.getCourse(qualtricsDocument, "0000");
//        Assertions.assertNull(foundCourse);
//
//        foundCourse = qualtricsService.getCourse(qualtricsDocument, "1234");
//        Assertions.assertNotNull(foundCourse);
//        Assertions.assertEquals("1234", foundCourse.getCourseId());
//
//        foundCourse = qualtricsService.getCourse(qualtricsDocument, "5678");
//        Assertions.assertNotNull(foundCourse);
//        Assertions.assertEquals("5678", foundCourse.getCourseId());
//
//        foundCourse = qualtricsService.getCourse(qualtricsDocument, "9012");
//        Assertions.assertNotNull(foundCourse);
//        Assertions.assertEquals("9012", foundCourse.getCourseId());
//    }
//
//    @Test
//    public void getAscendingOrderedUniqueLaunchesTest() {
//        QualtricsCourse qualtricsCourse1 = new QualtricsCourse();
//        qualtricsCourse1.setCourseId("1234");
//        qualtricsCourse1.setCourseTitle("Course title 1");
//        qualtricsCourse1.setOpen(false);
//
//        List<QualtricsLaunch> qualtricsLaunches = new ArrayList<>();
//
//        DateTime now = DateTime.now();
//
//        QualtricsLaunch qualtricsLaunch1 = new QualtricsLaunch();
//        qualtricsLaunch1.setId(1L);
//        qualtricsLaunch1.setUserId("user2");
//        qualtricsLaunch1.setCreatedOn(now.toDate());
//
//        qualtricsLaunches.add(qualtricsLaunch1);
//
//        QualtricsLaunch qualtricsLaunch2 = new QualtricsLaunch();
//        qualtricsLaunch2.setId(2L);
//        qualtricsLaunch2.setUserId("user2");
//        qualtricsLaunch2.setCreatedOn(now.plusSeconds(10).toDate());
//
//        qualtricsLaunches.add(qualtricsLaunch2);
//
//        QualtricsLaunch qualtricsLaunch3 = new QualtricsLaunch();
//        qualtricsLaunch3.setId(3L);
//        qualtricsLaunch3.setUserId("user1");
//        qualtricsLaunch3.setCreatedOn(now.plusSeconds(20).toDate());
//
//        qualtricsLaunches.add(qualtricsLaunch3);
//
//        QualtricsLaunch qualtricsLaunch4 = new QualtricsLaunch();
//        qualtricsLaunch4.setId(4L);
//        qualtricsLaunch4.setUserId("user1");
//        qualtricsLaunch4.setCreatedOn(now.plusSeconds(30).toDate());
//
//        qualtricsLaunches.add(qualtricsLaunch4);
//
//        qualtricsCourse1.setQualtricsLaunches(qualtricsLaunches);
//
//        List<QualtricsLaunch> foundQualtricsLaunches = qualtricsService.getAscendingOrderedUniqueLaunches(qualtricsCourse1);
//
//        Assertions.assertNotNull(foundQualtricsLaunches);
//        Assertions.assertEquals(2, foundQualtricsLaunches.size());
//
//        Assertions.assertEquals(Long.valueOf(1), foundQualtricsLaunches.get(0).getId());
//        Assertions.assertEquals(Long.valueOf(3), foundQualtricsLaunches.get(1).getId());
//    }
//
//    @Test
//    public void launchCourseDocumentTest() {
//        QualtricsCourse qualtricsCourse1 = new QualtricsCourse();
//        qualtricsCourse1.setOpen(false);
//
//        Mockito.when(qualtricsCourseRepository.save(any(QualtricsCourse.class))).thenAnswer(i -> i.getArguments()[0]);
//
//        QualtricsCourse returnedQualtricsCourse = qualtricsService.launchCourseDocument("userId", "User Name", qualtricsCourse1);
//
//        Assertions.assertNotNull(returnedQualtricsCourse);
//        Assertions.assertNotNull(returnedQualtricsCourse.getQualtricsLaunches());
//        Assertions.assertEquals(1, returnedQualtricsCourse.getQualtricsLaunches().size());
//        Assertions.assertEquals(true, returnedQualtricsCourse.getOpen());
//
//    }
//
//    @Test
//    public void getMostRecentLaunchTest() {
//        QualtricsCourse qualtricsCourse1 = new QualtricsCourse();
//
//        List<QualtricsLaunch> qualtricsLaunches = new ArrayList<>();
//
//        DateTime now = DateTime.now();
//
//        QualtricsLaunch qualtricsLaunch1 = new QualtricsLaunch();
//        qualtricsLaunch1.setId(1L);
//        qualtricsLaunch1.setUserId("user1");
//        qualtricsLaunch1.setCreatedOn(now.toDate());
//        qualtricsLaunches.add(qualtricsLaunch1);
//
//        QualtricsLaunch qualtricsLaunch2 = new QualtricsLaunch();
//        qualtricsLaunch2.setId(2L);
//        qualtricsLaunch2.setUserId("user2");
//        qualtricsLaunch2.setCreatedOn(now.plusSeconds(10).toDate());
//        qualtricsLaunches.add(qualtricsLaunch2);
//
//        QualtricsLaunch qualtricsLaunch3 = new QualtricsLaunch();
//        qualtricsLaunch3.setId(3L);
//        qualtricsLaunch3.setUserId("user1");
//        qualtricsLaunch3.setCreatedOn(now.plusSeconds(20).toDate());
//        qualtricsLaunches.add(qualtricsLaunch3);
//
//        qualtricsCourse1.setQualtricsLaunches(qualtricsLaunches);
//
//        QualtricsLaunch foundQualtricsLaunch = qualtricsService.getLastLaunch(qualtricsCourse1);
//
//        Assertions.assertNotNull(foundQualtricsLaunch);
//        Assertions.assertEquals(Long.valueOf(3), foundQualtricsLaunch.getId());
//    }
//
//    @Test
//    public void getMostRecentSubmissionTest() {
//        QualtricsCourse qualtricsCourse1 = new QualtricsCourse();
//
//        List<QualtricsSubmission> qualtricsSubmissions = new ArrayList<>();
//
//        DateTime now = DateTime.now();
//
//        QualtricsSubmission qualtricsSubmission1 = new QualtricsSubmission();
//        qualtricsSubmission1.setId(1L);
//        qualtricsSubmission1.setCreatedOn(now.toDate());
//        qualtricsSubmissions.add(qualtricsSubmission1);
//
//        QualtricsSubmission qualtricsSubmission2 = new QualtricsSubmission();
//        qualtricsSubmission2.setId(2L);
//        qualtricsSubmission2.setCreatedOn(now.plusSeconds(10).toDate());
//        qualtricsSubmissions.add(qualtricsSubmission2);
//
//        QualtricsSubmission qualtricsSubmission3 = new QualtricsSubmission();
//        qualtricsSubmission3.setId(3L);
//        qualtricsSubmission3.setCreatedOn(now.plusSeconds(20).toDate());
//        qualtricsSubmissions.add(qualtricsSubmission3);
//
//        qualtricsCourse1.setQualtricsSubmissions(qualtricsSubmissions);
//
//        QualtricsSubmission foundQualtricsSubmission = qualtricsService.getMostRecentSubmission(qualtricsCourse1);
//
//        Assertions.assertNotNull(foundQualtricsSubmission);
//        Assertions.assertEquals(Long.valueOf(3), foundQualtricsSubmission.getId());
//    }
//}