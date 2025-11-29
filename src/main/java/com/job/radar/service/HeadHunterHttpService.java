package com.job.radar.service;

import com.google.gson.Gson;
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

    public String searchVacancies(String technology) throws IOException {
        HttpUrl url = HttpUrl.parse("https://api.hh.ru/vacancies").newBuilder()
                .addQueryParameter("text", technology)
                .addQueryParameter("per_page", "20")
                .build();

        Request request = new Request.Builder().url(url).build();

        try (Response response = httpClient.newCall(request).execute()) {
            return gson.fromJson(response.body().string(), String.class);
        }
    }
}
