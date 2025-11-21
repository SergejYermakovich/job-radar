package com.job.radar.service;

import com.job.radar.model.entity.Resume;
import com.job.radar.model.entity.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.job.radar.utils.FieldNames.EMAIL;
import static com.job.radar.utils.FieldNames.FULL_NAME;

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
            case "phone" -> resume.setPhone(value);
            case "city" -> resume.setCity(value);
            case "position" -> resume.setPosition(value);
            case "experience" -> resume.setExperience(value);
            case "skills" -> resume.setSkills(value);
            case "employmentType" -> resume.setEmploymentType(value);
            case "education" -> resume.setEducation(value);
            case "languages" -> resume.setLanguages(value);
            case "portfolio" -> resume.setPortfolio(value);
            case "about" -> resume.setAbout(value);
            default -> log.warn("Unknown field: {}", field);
        }

        return save(resume);
    }

    @Transactional
    public Resume createOrUpdate(Long chatId, String field, Integer value) {
        Optional<Resume> existingResume = findByChatId(chatId);
        Resume resume = existingResume.orElse(Resume.builder().chatId(chatId).build());

        switch (field) {
            case "age" -> resume.setAge(value);
            case "salary" -> resume.setSalary(value);
            default -> log.warn("Unknown field: {}", field);
        }

        return save(resume);
    }
}

