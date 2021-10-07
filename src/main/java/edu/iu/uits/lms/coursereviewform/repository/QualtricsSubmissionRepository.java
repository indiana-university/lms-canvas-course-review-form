package edu.iu.uits.lms.coursereviewform.repository;

import edu.iu.uits.lms.coursereviewform.model.QualtricsSubmission;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

@Component("QualtricsSubmissionRepository")
public interface QualtricsSubmissionRepository extends PagingAndSortingRepository<QualtricsSubmission, Long> {
}
