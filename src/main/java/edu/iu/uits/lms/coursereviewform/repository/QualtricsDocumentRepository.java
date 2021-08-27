package edu.iu.uits.lms.coursereviewform.repository;

import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

@Component("QualtricsDocumentRepository")
public interface QualtricsDocumentRepository extends PagingAndSortingRepository<QualtricsDocument, Long> {
}
