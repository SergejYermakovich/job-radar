package com.job.radar.model.integration;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import java.util.List;

@Data
public class Employer {

    @SerializedName("accredited_it_employer")
    private boolean accreditedItEmployer;

    @SerializedName("alternate_url")
    private String alternateUrl;

    @SerializedName("applicant_services")
    private ApplicantService applicantServices;

    @SerializedName("area")
    private Area area;

    @SerializedName("branded_description")
    private String brandedDescription;

    @SerializedName("branding")
    private Branding branding;

    @SerializedName("country_code")
    private String countryCode;

    @SerializedName("description")
    private String description;

    @SerializedName("id")
    private String id;

    @SerializedName("industries")
    private List<Industry> industries;

    @SerializedName("insider_interviews")
    private List<InsiderInterview> insiderInterviews;

    @SerializedName("logo_urls")
    private LogoUrls logoUrls;

    @SerializedName("name")
    private String name;

    @SerializedName("open_vacancies")
    private Integer openVacancies;

    @SerializedName("relations")
    private List<Object> relations;

    @SerializedName("site_url")
    private String siteUrl;

    @SerializedName("trusted")
    private boolean trusted;

    @SerializedName("type")
    private String type;

    @SerializedName("vacancies_url")
    private String vacanciesUrl;

    @Data
    public static class ApplicantService {
        @SerializedName("target_employer")
        private TargetEmployer targetEmployer;

        @Data
        public static class TargetEmployer {
            @SerializedName("count")
            private Integer count;
        }
    }

    @Data
    public static class Branding {
        @SerializedName("makeup")
        private Makeup makeup;

        @SerializedName("template_code")
        private String templateCode;

        @SerializedName("template_version_id")
        private String templateVersionId;

        @SerializedName("type")
        private String type;

        @Data
        public static class Makeup {
            @SerializedName("header_picture")
            private HeaderPicture headerPicture;

            @SerializedName("url")
            private String url;

            @Data
            public static class HeaderPicture {
                @SerializedName("mob_app_path")
                private String mobAppPath;

                @SerializedName("web_path")
                private String webPath;
            }
        }
    }

    @Data
    public static class Industry {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;
    }

    @Data
    public static class InsiderInterview {
        @SerializedName("id")
        private String id;

        @SerializedName("title")
        private String title;

        @SerializedName("url")
        private String url;
    }

    @Data
    public static class LogoUrls {
        @SerializedName("90")
        private String size90;

        @SerializedName("240")
        private String size240;

        @SerializedName("original")
        private String original;
    }
}
