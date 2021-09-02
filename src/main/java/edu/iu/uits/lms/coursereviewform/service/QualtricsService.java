package edu.iu.uits.lms.coursereviewform.service;

import edu.iu.uits.lms.coursereviewform.model.QualtricsCourse;
import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import edu.iu.uits.lms.coursereviewform.model.QualtricsLaunch;
import edu.iu.uits.lms.coursereviewform.model.QualtricsSubmission;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsCourseRepository;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsDocumentRepository;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsLaunchRepository;
import edu.iu.uits.lms.coursereviewform.repository.QualtricsSubmissionRepository;
import lombok.NonNull;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QualtricsService {
    @Autowired
    private QualtricsDocumentRepository qualtricsDocumentRepository;

    @Autowired
    private QualtricsCourseRepository qualtricsCourseRepository;

    @Autowired
    private QualtricsLaunchRepository qualtricsLaunchRepository;

    @Autowired
    private QualtricsSubmissionRepository qualtricsSubmissionRepository;

    /**
     * Gets a Qualtrics Document
     * @param id - The database id of the document
     * @return The found Qualtrics Document
     */
    public QualtricsDocument getDocument(long id) {
        return qualtricsDocumentRepository.findById(id).orElse(null);
    }

    /**
     * Gets all Qualtrics Documents
     * @return All Qualtrics Documents
     */
    public List<QualtricsDocument> getAllDocuments() {
        return IterableUtils.toList(qualtricsDocumentRepository.findAll());
    }

    /**
     * Saves a Qualtrics Document
     * @param qualtricsDocument Qualtrics Document to save
     * @return The saved Qualtrics Document
     */
    public QualtricsDocument saveDocument(QualtricsDocument qualtricsDocument) {
        return qualtricsDocumentRepository.save(qualtricsDocument);
    }

    /**
     * Gets a Qualtrics Course
     * @param id Database id of the Qualtrics Course
     * @return The Qualtrics Course
     */
    public QualtricsCourse getCourse(long id) {
        return qualtricsCourseRepository.findById(id).orElse(null);
    }

    /**
     * Searches a Qualtrics Document for a Qualtric course with the given canvas course id
     * @param qualtricsDocument Document to search
     * @param courseId The canvas courseId to find
     * @return
     */
    public QualtricsCourse getCourse(@NonNull QualtricsDocument qualtricsDocument, @NonNull String courseId) {
        List<QualtricsCourse> qualtricsCourses = qualtricsDocument.getQualtricsCourses();

        if (qualtricsCourses != null) {
            for (QualtricsCourse qualtricsCourse : qualtricsCourses) {
                if (courseId.equals(qualtricsCourse.getCourseId())) {
                    return qualtricsCourse;
                }
            }
        }

        return null;
    }

    /**
     * Return the course for a given document with the given courseId. If it doesn't exist, create one
     * using the course title as well.
     * @param qualtricsDocument Qualtrics Docuent to search for the given canvas course id record.
     * @param courseId The canvas course id to search for
     * @param courseTitle The course title to use if we end up creating a new one
     * @return
     */
    public QualtricsCourse createOrGetExistingCourse(@NonNull QualtricsDocument qualtricsDocument, @NonNull String courseId, @NonNull String courseTitle) {
        QualtricsCourse qualtricsCourse = getCourse(qualtricsDocument, courseId);

        if (qualtricsCourse != null) {
            return qualtricsCourse;
        } else {
            qualtricsCourse = new QualtricsCourse();
            qualtricsCourse.setQualtricsDocument(qualtricsDocument);
            qualtricsCourse.setCourseId(courseId);
            qualtricsCourse.setCourseTitle(courseTitle);

            return qualtricsCourseRepository.save(qualtricsCourse);
        }
    }

    /**
     * Get all Qualtrics Courses
     * @return All Qualtrics Courses
     */
    public List<QualtricsCourse> getAllCourses() {
        return IterableUtils.toList(qualtricsCourseRepository.findAll());
    }

    /**
     * Save the given Qualtrics Course
     * @param qualtricsCourse Qualtrics Course to save
     * @return The saved Qualtrics Course
     */
    public QualtricsCourse saveCourse(@NonNull QualtricsCourse qualtricsCourse) {
        return qualtricsCourseRepository.save(qualtricsCourse);
    }

    /**
     * Return a list of Qualtrics Launches sorted in ascending order by createdOn date
     * of only giving unique lanuches (userIds)
     * @param qualtricsCourse The course to grab the Qualtrics Launches
     * @return A list of unique userId Qualtrics Launches sorted in ascending order by createdOn date
     */
    public List<QualtricsLaunch> getAscendingOrderedUniqueLaunches(@NonNull QualtricsCourse qualtricsCourse) {
        List<QualtricsLaunch> lastLaunches = new ArrayList<>();

        Map<String, String> uniqueUserIdLaunches = new HashMap<>();

        List<QualtricsLaunch> sortedQualtricsLaunches = qualtricsCourse.getQualtricsLaunches();

        if (sortedQualtricsLaunches != null) {
            sortedQualtricsLaunches = sortedQualtricsLaunches.stream().
                    sorted(Comparator.comparing(QualtricsLaunch::getCreatedOn)).
                    collect(Collectors.toList());

            for (int i = 0; i < sortedQualtricsLaunches.size() && i < 5; i++) {
                QualtricsLaunch qualtricsLaunch = sortedQualtricsLaunches.get(i);
                String userId = qualtricsLaunch.getUserId();

                if (qualtricsLaunch != null && userId != null && ! uniqueUserIdLaunches.containsKey(userId)) {
                    uniqueUserIdLaunches.put(userId, null);
                    lastLaunches.add(qualtricsLaunch);
                }
            }
        }

        return lastLaunches;
    }

    /**
     * Launches a Course Document. This creates a Qualtrics Launch and persists it to the database.
     * @param userId UserId who is launching
     * @param userFullName The launching user's full name
     * @param qualtricsCourse The Qualtrics course that is launching
     * @return The Qualtrics Course with the new Qualtrics Launch
     */
    public QualtricsCourse launchCourseDocument(@NonNull String userId, @NonNull String userFullName, @NonNull QualtricsCourse qualtricsCourse) {

        QualtricsLaunch qualtricsLaunch = new QualtricsLaunch();
        qualtricsLaunch.setQualtricsCourse(qualtricsCourse);
        qualtricsLaunch.setUserId(userId);
        qualtricsLaunch.setUserFullName(userFullName);

        qualtricsCourse.setOpen(true);

        List<QualtricsLaunch> qualtricsLaunches = qualtricsCourse.getQualtricsLaunches();

        if (qualtricsLaunches == null) {
            qualtricsLaunches = new ArrayList<>();
            qualtricsCourse.setQualtricsLaunches(qualtricsLaunches);
        }

        qualtricsLaunches.add(qualtricsLaunch);

        qualtricsCourse = qualtricsCourseRepository.save(qualtricsCourse);

        return qualtricsCourse;
    }

    /**
     * Gets the last Qualtrics Submission for a given Qualtrics Course
     * @param qualtricsCourse Qualtrics Course to use to get the Qualtrics Submissions
     * @return The latest (by createdOn date) Qualtrics Submission
     */
    public QualtricsSubmission getMostRecentSubmission(@NonNull QualtricsCourse qualtricsCourse) {
        List<QualtricsSubmission> reverseSortedSubmissions = qualtricsCourse.getQualtricsSubmissions();

        if (reverseSortedSubmissions == null || reverseSortedSubmissions.isEmpty()) {
            return null;
        } else {
            reverseSortedSubmissions = reverseSortedSubmissions.stream().
                    sorted(Comparator.comparing(QualtricsSubmission::getCreatedOn).reversed()).
                    collect(Collectors.toList());

            return reverseSortedSubmissions.get(0);
        }
    }

    /**
     * Gets the Qualtrics Launch with the given database id.
     * @param launchId The database id of the Qualtrics Launch to retrieve
     * @return The found Qualtrics Launch
     */
    public QualtricsLaunch getLaunch(long launchId) {
        return qualtricsLaunchRepository.findById(launchId).orElse(null);
    }

    /**
     * Get all Qualtrics Launches
     * @return All the Qualtrics Launches
     */
    public List<QualtricsLaunch> getAllLaunches() {
        return IterableUtils.toList(qualtricsLaunchRepository.findAll());
    }

    /**
     * Get the Qualtrics Submission for the given database is
     * @param submissionId The database id of the Qualtrics Submission to retreive
     * @return The Qualtrics Submission
     */
    public QualtricsSubmission getSubmission(long submissionId) {
        return qualtricsSubmissionRepository.findById(submissionId).orElse(null);
    }

    /**
     * Get all Qualtrics Submissions
     * @return All Qualtrics Submissions
     */
    public List<QualtricsSubmission> getAllSubmissions() {
        return IterableUtils.toList(qualtricsSubmissionRepository.findAll());
    }

    /**
     * Save a given Qualtrics Submission
     * @param qualtricsSubmission Submission to save
     * @return Saved Qualtrics Submission
     */
    public QualtricsSubmission saveSubmission(@NonNull QualtricsSubmission qualtricsSubmission) {
        return qualtricsSubmissionRepository.save(qualtricsSubmission);
    }
}
