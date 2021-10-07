package edu.iu.uits.lms.coursereviewform.repository;

import edu.iu.uits.lms.coursereviewform.model.QualtricsLaunch;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

@Component("QualtricsLaunchRepository")
public interface QualtricsLaunchRepository extends PagingAndSortingRepository<QualtricsLaunch, Long> {
}
