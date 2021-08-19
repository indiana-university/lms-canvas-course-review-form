package edu.iu.uits.lms.coursereviewform.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class QualtricsRestSubmission {
    @JsonProperty("course_id")
    private String courseId;

    @JsonProperty("response_id")
    private String responseId;

    @JsonProperty("last_submitted_by")
    private String lastSubmittedBy;
}
