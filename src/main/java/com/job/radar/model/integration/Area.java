package com.job.radar.model.integration;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Area {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;
}
