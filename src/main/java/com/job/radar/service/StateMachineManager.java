package com.job.radar.service;

import com.job.radar.model.enums.statemachine.event.FormEvent;
import com.job.radar.model.enums.statemachine.event.MenuEvent;
import com.job.radar.model.enums.statemachine.event.ResumeEvent;
import com.job.radar.model.enums.statemachine.state.FormState;
import com.job.radar.model.enums.statemachine.state.MenuState;
import com.job.radar.model.enums.statemachine.state.ResumeState;
import jakarta.annotation.Resource;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StateMachineManager {
    @Resource(name = "menuStateMachine")
    private StateMachineFactory<MenuState, MenuEvent> menuStateMachineFactory;

    @Resource(name = "resumeStateMachine")
    private StateMachineFactory<ResumeState, ResumeEvent> resumeStateMachineFactory;

    @Resource(name = "formStateMachine")
    private StateMachineFactory<FormState, FormEvent> formStateMachineFactory;

    private final Map<Long, StateMachine<MenuState, MenuEvent>> menuMachines = new ConcurrentHashMap<>();
    private final Map<Long, StateMachine<ResumeState, ResumeEvent>> resumeMachines = new ConcurrentHashMap<>();
    private final Map<Long, StateMachine<FormState, FormEvent>> formMachines = new ConcurrentHashMap<>();


    // Menu State Machine методы
    @SuppressWarnings("deprecation")
    public StateMachine<MenuState, MenuEvent> getMenuStateMachine(Long chatId) {
        return menuMachines.computeIfAbsent(chatId, k -> {
            StateMachine<MenuState, MenuEvent> machine = menuStateMachineFactory.getStateMachine();
            machine.start();
            return machine;
        });
    }

    public MenuState getCurrentMenuState(Long chatId) {
        return getMenuStateMachine(chatId).getState().getId();
    }

    // Resume State Machine методы
    @SuppressWarnings("deprecation")
    public StateMachine<ResumeState, ResumeEvent> getResumeStateMachine(Long chatId) {
        return resumeMachines.computeIfAbsent(chatId, k -> {
            StateMachine<ResumeState, ResumeEvent> machine = resumeStateMachineFactory.getStateMachine();
            machine.start();
            return machine;
        });
    }

    public ResumeState getCurrentResumeState(Long chatId) {
        StateMachine<ResumeState, ResumeEvent> machine = resumeMachines.get(chatId);
        return machine != null ? machine.getState().getId() : null;
    }

    // Form State Machine методы
    @SuppressWarnings("deprecation")
    public StateMachine<FormState, FormEvent> getFormStateMachine(Long chatId) {
        return formMachines.computeIfAbsent(chatId, k -> {
            StateMachine<FormState, FormEvent> machine = formStateMachineFactory.getStateMachine();
            machine.start();
            return machine;
        });
    }

    public FormState getCurrentFormState(Long chatId) {
        StateMachine<FormState, FormEvent> machine = formMachines.get(chatId);
        return machine != null ? machine.getState().getId() : null;
    }

    public boolean isInFormFlow(Long chatId) {
        FormState currentState = getCurrentFormState(chatId);
        return currentState != null &&
                currentState != FormState.FORM_IDLE &&
                currentState != FormState.FORM_COMPLETED &&
                currentState != FormState.FORM_CANCELLED;
    }

    // Методы очистки
    @SuppressWarnings("deprecation")
    public void cleanupFormMachine(Long chatId) {
        StateMachine<FormState, FormEvent> machine = formMachines.get(chatId);
        if (machine != null) {
            machine.stop();
            formMachines.remove(chatId);
        }
    }

    @SuppressWarnings("deprecation")
    public void cleanupResumeMachine(Long chatId) {
        StateMachine<ResumeState, ResumeEvent> machine = resumeMachines.get(chatId);
        if (machine != null) {
            machine.stop();
            resumeMachines.remove(chatId);
        }
    }

    public void cleanupUserSession(Long chatId) {
        // Очищаем все State Machines пользователя
        cleanupStateMachine(menuMachines, chatId);
        cleanupStateMachine(resumeMachines, chatId);
        cleanupStateMachine(formMachines, chatId);
    }

    @SuppressWarnings("deprecation")
    private <S, E> void cleanupStateMachine(Map<Long, StateMachine<S, E>> machines, Long chatId) {
        StateMachine<S, E> machine = machines.get(chatId);
        if (machine != null) {
            machine.stop();
            machines.remove(chatId);
        }
    }
}