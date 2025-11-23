package com.job.radar.service;

import com.job.radar.model.entity.Resume;
import com.job.radar.model.entity.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.job.radar.utils.FieldNames.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;

    public Optional<Resume> findByChatId(Long chatId) {
        return resumeRepository.findByChatId(chatId);
    }

    @Transactional
    public Resume save(Resume resume) {
        return resumeRepository.save(resume);
    }

    @Transactional
    public void deleteByChatId(Long chatId) {
        resumeRepository.deleteByChatId(chatId);
    }

    @Transactional
    public Resume createOrUpdate(Long chatId, String field, String value) {
        Optional<Resume> existingResume = findByChatId(chatId);
        Resume resume = existingResume.orElse(Resume.builder().chatId(chatId).build());

        switch (field) {
            case FULL_NAME -> resume.setFullName(value);
            case EMAIL -> resume.setEmail(value);
            case PHONE -> resume.setPhone(value);
            case CITY -> resume.setCity(value);
            case POSITION -> resume.setPosition(value);
            case EXPERIENCE -> resume.setExperience(value);
            case SKILLS -> resume.setSkills(value);
            case EMPLOYMENT_TYPE -> resume.setEmploymentType(value);
            case EDUCATION -> resume.setEducation(value);
            case LANGUAGES -> resume.setLanguages(value);
            case PORTFOLIO -> resume.setPortfolio(value);
            case ABOUT -> resume.setAbout(value);
            default -> log.warn("Unknown field: {}", field);
        }

        return save(resume);
    }

    @Transactional
    public Resume createOrUpdate(Long chatId, String field, Integer value) {
        Optional<Resume> existingResume = findByChatId(chatId);
        Resume resume = existingResume.orElse(Resume.builder().chatId(chatId).build());

        switch (field) {
            case AGE -> resume.setAge(value);
            case SALARY -> resume.setSalary(value);
            default -> log.warn("Unknown field: {}", field);
        }

        return save(resume);
    }
}

