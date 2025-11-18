package com.job.radar.model.dto;

import com.job.radar.model.enums.resume.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private String profession;
    private String specialization;
    private String desiredPosition;
    private String industry;
    private CareerLevel careerLevel;
    private Integer minSalary;
    private String currency;
    private ExperienceLevel totalExperience;
    private ExperienceLevel relevantExperience;
    private EducationLevel educationLevel;
    private String institution;
    private String specialty;
    private List<String> certificates;
    private List<WorkSchedule> workSchedules;
    private List<EmploymentType> employmentTypes;
    private List<String> professionalSkills;
    private List<String> softSkills;
    private List<String> tools;
    private Map<String, String> languages;
    private String achievements;
    private List<String> keywords;
    private List<String> excludeKeywords;
    private List<String> companyTypes;
    private List<String> preferredIndustries;
    private WorkFormat workFormat;
    private BusinessTripReadiness businessTrips;
    private Boolean autoApplyEnabled;
    private String coverLetter;
}
