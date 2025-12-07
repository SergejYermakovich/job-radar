package com.job.radar.service;

import com.google.gson.Gson;
import com.job.radar.model.integration.VacancyResponse;
import com.job.radar.utils.HeadHunterApiConsts;
import lombok.RequiredArgsConstructor;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RequiredArgsConstructor
@Service
public class HeadHunterHttpService {
    private final OkHttpClient httpClient;
    private final Gson gson;

    public VacancyResponse searchVacancies(String technology) throws IOException {
        return searchVacancies(technology, 0);
    }

    public VacancyResponse searchVacancies(String technology, int page) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(HeadHunterApiConsts.BASE_URL + HeadHunterApiConsts.VACANCIES)
                .newBuilder()
                .addQueryParameter("text", technology)
                .addQueryParameter("per_page", "5"); // Показываем по 5 вакансий на страницу
        
        if (page > 0) {
            urlBuilder.addQueryParameter("page", String.valueOf(page));
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return gson.fromJson(response.body().string(), VacancyResponse.class);
        }
    }
}
