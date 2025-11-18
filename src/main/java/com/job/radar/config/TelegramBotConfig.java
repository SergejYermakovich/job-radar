package com.job.radar.config;

import com.job.radar.service.bot.DevBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Конфигурация для явной регистрации бота в Telegram API.
 * 
 * Несмотря на использование telegrambots-spring-boot-starter,
 * автоматическая регистрация может не работать в Spring Boot 3.x,
 * поэтому требуется явная регистрация через этот класс.
 */
@Slf4j
@Configuration
public class TelegramBotConfig {

    @Bean
    @Profile("dev")
    public CommandLineRunner registerBot(DevBot devBot) {
        return args -> {
            try {
                TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
                telegramBotsApi.registerBot(devBot);
                log.info("Bot is now listening for updates...");
            } catch (TelegramApiException e) {
                throw new RuntimeException("Failed to register bot", e);
            }
        };
    }

    @Bean
    public DefaultBotOptions defaultBotOptions() {
        return new DefaultBotOptions();
    }
}

