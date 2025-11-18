package com.job.radar.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false, unique = true)
    private Long chatId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "age")
    private Integer age;

    @Column(name = "city")
    private String city;

    @Column(name = "position")
    private String position;

    @Column(name = "experience")
    private String experience;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    @Column(name = "salary")
    private Integer salary;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "education")
    private String education;

    @Column(name = "languages", columnDefinition = "TEXT")
    private String languages;

    @Column(name = "portfolio")
    private String portfolio;

    @Column(name = "about", columnDefinition = "TEXT")
    private String about;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

