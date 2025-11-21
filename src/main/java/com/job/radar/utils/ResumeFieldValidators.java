package com.job.radar.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ResumeFieldValidators {
    public static boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static boolean isValidName(String name) {
        return name == null || name.trim().length() < 2;
    }
}
