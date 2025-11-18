package com.job.radar.service.bot;

import com.job.radar.service.UpdateProcessorService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Profile("prod")
@Component
public class ProdBot extends TelegramWebhookBot {
    
    private final String botToken;
    private final String botUsername;
    private final String botPath;
    private final UpdateProcessorService updateProcessor;
    
    @SuppressWarnings("deprecation")
    public ProdBot(@Value("${telegram.bot.token}") String botToken,
                   @Value("${telegram.bot.username}") String botUsername,
                   @Value("${telegram.bot.path:/webhook}") String botPath,
                   UpdateProcessorService updateProcessor) {
        super();
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.botPath = botPath;
        this.updateProcessor = updateProcessor;
    }
    
    @PostConstruct
    public void init() {
        log.info("=== ProdBot INITIALIZED ===");
        log.info("Bot username: {}", getBotUsername());
        log.info("Bot path: {}", getBotPath());
        log.info("Bot token: {}", botToken != null && botToken.length() > 10 ? botToken.substring(5, 10) + "..." : "not set");
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return updateProcessor.processUpdate(update);
    }

    @Override
    public String getBotPath() {
        return botPath;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
    
    @Override
    public String getBotToken() {
        return botToken;
    }
}
