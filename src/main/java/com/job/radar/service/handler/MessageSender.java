package com.job.radar.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Slf4j
@Service
public class MessageSender extends DefaultAbsSender {

    protected MessageSender(@Value("${telegram.bot.token}") String botToken) {
        super(new DefaultBotOptions(), botToken);
    }
}
