package com.job.radar.service.processor;

import com.job.radar.model.enums.resume.UpdateType;
import com.job.radar.utils.PaymentUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class UpdateTypeProcessor {
    public UpdateType getUpdateType(Update update) {
        if (PaymentUtils.isSuccessfulPayment(update)) {
            return UpdateType.SUCCESSFUL_PAYMENT;
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            return UpdateType.MESSAGE;
        } else if (update.hasCallbackQuery()) {
            return UpdateType.CALLBACK_QUERY;
        } else if (update.hasPreCheckoutQuery()) {
            return UpdateType.PRE_CHECKOUT_QUERY;
        } else if (update.hasMessage() && update.getMessage().hasVoice()) {
            return UpdateType.VOICE_MESSAGE;
        } else {
            return UpdateType.DEFAULT;
        }
    }
}
