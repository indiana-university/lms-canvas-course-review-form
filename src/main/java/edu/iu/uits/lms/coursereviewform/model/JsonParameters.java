package edu.iu.uits.lms.coursereviewform.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class JsonParameters {
// [base_url]?course_id=2023963&course_title=Qualtrics%20Test%20Course&
// last_opened_by=leward&userid1=leward&userid1_name=Lynn%20Ward&userid2=&
// userid2_name=&userid3=&userid3_name=&userid4=&userid4_name=&userid5=&userid5_name=

    @SerializedName("course_id")
    String courseId;

    @SerializedName("course_title")
    String courseTitle;

    @SerializedName("last_opened_by")
    String lastOpenedBy;

    @SerializedName("userid1")
    String userId1;
    @SerializedName("userid1_name")
    String userId1Name;

    @SerializedName("userid2")
    String userId2;
    @SerializedName("userid2_name")
    String userId2Name;

    @SerializedName("userid3")
    String userId3;
    @SerializedName("userid3_name")
    String userId3Name;

    @SerializedName("userid4")
    String userId4;
    @SerializedName("userid4_name")
    String userId4Name;

    @SerializedName("userid5")
    String userId5;
    @SerializedName("userid5_name")
    String userId5Name;
}
