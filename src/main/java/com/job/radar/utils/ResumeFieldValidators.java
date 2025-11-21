package com.job.radar.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ResumeFieldValidators {
    public static boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
