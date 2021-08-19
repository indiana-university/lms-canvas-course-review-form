package edu.iu.uits.lms.coursereviewform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JoinColumn(name = "DOCUMENT_ID", referencedColumnName = "ID", nullable = false)
    private QualtricsDocument qualtricsDocument;

    @Column(name = "COURSE_ID")
    private String courseId;

    @Column(name = "COURSE_TITLE")
    private String courseTitle;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "RESPONSE_ID")
    private String responseId;

    @Column(name = "CREATED_ON")
    private Date createdOn;

    @PreUpdate
    @PrePersist
    public void updateCreatedOn() {
        if (createdOn == null) {
            createdOn = new Date();
        }
    }
}