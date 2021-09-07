package edu.iu.uits.lms.coursereviewform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "QUALTRICS_COURSE")
@SequenceGenerator(name = "QUALTRICS_COURSE_ID_SEQ", sequenceName = "QUALTRICS_COURSE_ID_SEQ", allocationSize = 1)
@Data
@NoArgsConstructor
public class QualtricsCourse implements Serializable {
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "QUALTRICS_COURSE_ID_SEQ")
    private Long id;

    @JsonIgnore
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "DOCUMENT_ID", referencedColumnName = "ID", nullable = false)
    private QualtricsDocument qualtricsDocument;

    @JsonIgnore
    @OneToMany(mappedBy = "qualtricsCourse", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<QualtricsLaunch> qualtricsLaunches;

    @JsonIgnore
    @OneToMany(mappedBy = "qualtricsCourse", fetch = FetchType.LAZY)
    private List<QualtricsSubmission> qualtricsSubmissions;

    @Column(name = "COURSE_ID")
    private String courseId;

    @Column(name = "COURSE_TITLE")
    private String courseTitle;

    private Boolean open;

    @Column(name = "CREATED_ON")
    private Date createdOn;

    @PreUpdate
    @PrePersist
    public void updateStuff() {
        if (open == null) {
            open = false;
        }

        if (createdOn == null) {
            createdOn = new Date();
        }
    }
}