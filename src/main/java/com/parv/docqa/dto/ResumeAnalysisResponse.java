package com.parv.docqa.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResumeAnalysisResponse {
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private String matchScore;
    private String summary;
    private List<String> recommendations;
}
