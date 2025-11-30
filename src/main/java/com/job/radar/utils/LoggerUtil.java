package com.job.radar.utils;

import com.job.radar.model.integration.Vacancy;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class LoggerUtil {
    public static void log(Vacancy vacancy) {
        log.info("--------------------------------------");
        log.info("Salary: {}", vacancy.getSalary());
        log.info("Employment: {}", vacancy.getEmployment());
        log.info("Experience: {}", vacancy.getExperience());
        log.info("Employer: {}", vacancy.getEmployer());
        log.info("--------------------------------------");
    }
}
