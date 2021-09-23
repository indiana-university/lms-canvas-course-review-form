package edu.iu.uits.lms.coursereviewform.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.iu.uits.lms.common.date.DateFormatUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "QUALTRICS_SUBMISSION")
@SequenceGenerator(name = "QUALTRICS_SUBMISSION_ID_SEQ", sequenceName = "QUALTRICS_SUBMISSION_ID_SEQ", allocationSize = 1)
@Data
@NoArgsConstructor
public class QualtricsSubmission implements Serializable {
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "QUALTRICS_SUBMISSION_ID_SEQ")
    private Long id;

    @JsonIgnore
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "QUALTRICS_COURSE_ID", referencedColumnName = "ID", nullable = false)
    private QualtricsCourse qualtricsCourse;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "RESPONSE_ID")
    private String responseId;

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