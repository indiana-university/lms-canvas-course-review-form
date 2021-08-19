package edu.iu.uits.lms.coursereviewform.repository;

import edu.iu.uits.lms.coursereviewform.model.QualtricsLaunch;
import lombok.NonNull;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("QualtricsLaunchRepository")
public interface QualtricsLaunchRepository extends PagingAndSortingRepository<QualtricsLaunch, Long> {
    List<QualtricsLaunch> getByCourseId(@NonNull String courseId);
}
