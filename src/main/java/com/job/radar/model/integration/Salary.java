package com.job.radar.model.integration;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Salary {
    @SerializedName("currency")
    private String currency;

    @SerializedName("from")
    private Integer from;

    @SerializedName("gross")
    private Boolean isGross;

    @SerializedName("to")
    private Integer to;
}
