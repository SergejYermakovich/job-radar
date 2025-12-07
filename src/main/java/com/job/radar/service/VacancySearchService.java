package com.job.radar.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для управления поиском вакансий и пагинацией
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VacancySearchService {
    
    // Хранит просмотренные ID вакансий для каждого пользователя
    private final Map<Long, Set<String>> viewedVacancyIds = new ConcurrentHashMap<>();
    
    // Хранит данные текущего поиска для каждого пользователя
    private final Map<Long, SearchSession> searchSessions = new ConcurrentHashMap<>();
    
    @Data
    public static class SearchSession {
        private String searchQuery;
        private List<String> allVacancyIds; // Все ID вакансий из текущего поиска
        private int currentPage;
        private int totalPages;
        private int totalFound;
        private int perPage;
        
        public SearchSession(String searchQuery, int currentPage, int totalPages, int totalFound, int perPage) {
            this.searchQuery = searchQuery;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalFound = totalFound;
            this.perPage = perPage;
            this.allVacancyIds = new ArrayList<>();
        }
    }
    
    /**
     * Получить просмотренные ID вакансий для пользователя
     */
    public Set<String> getViewedVacancyIds(Long chatId) {
        return viewedVacancyIds.computeIfAbsent(chatId, k -> new HashSet<>());
    }
    
    /**
     * Отметить вакансию как просмотренную
     */
    public void markVacancyAsViewed(Long chatId, String vacancyId) {
        viewedVacancyIds.computeIfAbsent(chatId, k -> new HashSet<>()).add(vacancyId);
    }
    
    /**
     * Отметить несколько вакансий как просмотренные
     */
    public void markVacanciesAsViewed(Long chatId, List<String> vacancyIds) {
        Set<String> viewed = viewedVacancyIds.computeIfAbsent(chatId, k -> new HashSet<>());
        viewed.addAll(vacancyIds);
    }
    
    /**
     * Сохранить сессию поиска
     */
    public void saveSearchSession(Long chatId, SearchSession session) {
        searchSessions.put(chatId, session);
    }
    
    /**
     * Получить текущую сессию поиска
     */
    public SearchSession getSearchSession(Long chatId) {
        return searchSessions.get(chatId);
    }
    
    /**
     * Очистить сессию поиска
     */
    public void clearSearchSession(Long chatId) {
        searchSessions.remove(chatId);
    }
    
    /**
     * Фильтровать вакансии, оставляя только новые (не просмотренные)
     */
    public List<String> filterNewVacancyIds(Long chatId, List<String> vacancyIds) {
        Set<String> viewed = getViewedVacancyIds(chatId);
        return vacancyIds.stream()
                .filter(id -> !viewed.contains(id))
                .toList();
    }
}

