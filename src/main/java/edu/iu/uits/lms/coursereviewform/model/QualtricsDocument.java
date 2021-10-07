package edu.iu.uits.lms.coursereviewform.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.iu.uits.lms.common.date.DateFormatUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "QUALTRICS_DOCUMENT")
@SequenceGenerator(name = "QUALTRICS_DOCUMENT_ID_SEQ", sequenceName = "QUALTRICS_DOCUMENT_ID_SEQ", allocationSize = 1)
@Data
@NoArgsConstructor
public class QualtricsDocument implements Serializable {
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "QUALTRICS_DOCUMENT_ID_SEQ")
    private Long id;

    @JsonIgnore
    @OneToMany(mappedBy = "qualtricsDocument", fetch = FetchType.LAZY)
    private List<QualtricsCourse> qualtricsCourses;

    @Column(name = "NAME")
    private String name;

    @Column(name = "BASE_URL")
    private String baseUrl;

    @Column(name = "TOKEN")
    private String token;

    @Column(name = "CREATED_ON")
    @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
    private Date createdOn;

    @PreUpdate
    @PrePersist
    public void updateCreatedOn() {
        if (createdOn == null) {
            createdOn = new Date();
        }
    }
}