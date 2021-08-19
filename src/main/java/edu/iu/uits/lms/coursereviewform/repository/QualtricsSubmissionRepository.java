package edu.iu.uits.lms.coursereviewform.repository;

import edu.iu.uits.lms.coursereviewform.model.QualtricsSubmission;
import lombok.NonNull;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import java.util.List;


@Component("QualtricsSubmissionRepository")
public interface QualtricsSubmissionRepository extends PagingAndSortingRepository<QualtricsSubmission, Long> {
    List<QualtricsSubmission> getByCourseId(@NonNull String courseId);
}
