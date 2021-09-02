package edu.iu.uits.lms.coursereviewform.repository;

import edu.iu.uits.lms.coursereviewform.model.QualtricsCourse;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

@Component("QualtricsCourseRepository")
public interface QualtricsCourseRepository extends PagingAndSortingRepository<QualtricsCourse, Long> {
}
