package com.job.radar.model.integration;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class VacancyResponse {
    @SerializedName("items")
    private List<Vacancy> vacancies;

    private int found;
    private int page;
    private int pages;

    @SerializedName("per_page")
    private int perPage;
}
