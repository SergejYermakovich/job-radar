package com.job.radar.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.Update;

@UtilityClass
public class PaymentUtils {
    public static boolean isSuccessfulPayment(Update update) {
        return update.hasMessage() && update.getMessage().hasSuccessfulPayment();
    }
}
