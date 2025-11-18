package com.job.radar.utils;

import org.telegram.telegrambots.meta.api.objects.Update;

public class PaymentUtils {
    public static boolean isSuccessfulPayment(Update update) {
        return update.hasMessage() && update.getMessage().hasSuccessfulPayment();
    }
}
