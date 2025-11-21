package com.job.radar.service.processor.field;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

@Service
public class FullNameProcessor implements ResumeFieldProcessor {
    @Override
    public BotApiMethod<?> process(long chatId, String text) {
        return null;
    }
}
