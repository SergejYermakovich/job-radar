package com.job.radar.service.processor.field;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

public interface ResumeFieldProcessor {
    BotApiMethod<?>  process(long chatId, String text);
}
