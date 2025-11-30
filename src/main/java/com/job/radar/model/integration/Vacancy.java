package com.job.radar.model.integration;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Vacancy {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("area")
    private Area area;

    @SerializedName("salary")
    private Salary salary;

    @SerializedName("employer")
    private Employer employer;

    @SerializedName("published_at")
    private String publishedAt;

    @SerializedName("alternate_url")
    private String alternateUrl;

    @SerializedName("experience")
    private Experience experience;

    @SerializedName("employment")
    private Employment employment;

    @SerializedName("snippet")
    private Snippet snippet;
}
