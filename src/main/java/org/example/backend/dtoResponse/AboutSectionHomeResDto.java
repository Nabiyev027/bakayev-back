package org.example.backend.dtoResponse;
import lombok.Data;

import java.util.UUID;

@Data
public class AboutSectionHomeResDto {
    private UUID id;
    private String imgUrl;
    private String videoUrl;
    private String videoThumbnailUrl;
    private String desc1;
    private String desc2;
    private Integer successfulStudents;
    private Double averageScore;
    private Integer yearsExperience;
    private Integer successRate;

}
