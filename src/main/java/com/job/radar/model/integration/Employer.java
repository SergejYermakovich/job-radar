package com.job.radar.model.integration;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Employer {

    @SerializedName("accredited_it_employer")
    private boolean isAccreditedItEmployer;

    @SerializedName("name")
    private String name;
}
