package com.job.radar.service.bot;

import com.job.radar.service.UpdateProcessorService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Profile("dev")
@Component
public class DevBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;
    private final UpdateProcessorService updateProcessor;

    @SuppressWarnings("deprecation")
    public DevBot(@Value("${telegram.bot.token}") String botToken,
                  @Value("${telegram.bot.username}") String botUsername,
                  UpdateProcessorService updateProcessor) {
        super();
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.updateProcessor = updateProcessor;
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateProcessor.processUpdate(update, response -> {
            try {
                execute(response);
            } catch (TelegramApiException e) {
                log.error("onUpdateReceived error.", e);
            }
        });
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @PostConstruct
    public void init() {
        log.info("=== JobRadarBot STARTED ===");
        log.info("Bot username: {}", getBotUsername());
        log.info("Bot token: {}", "..." + getBotToken().substring(5, 10) + "...");
        log.info("===========================");
    }
}
