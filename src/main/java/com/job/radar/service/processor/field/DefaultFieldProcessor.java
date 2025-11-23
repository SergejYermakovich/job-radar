package com.job.radar.service.processor.field;

import com.job.radar.model.enums.statemachine.event.FormEvent;
import com.job.radar.model.enums.statemachine.state.FormState;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

@Service
public class DefaultFieldProcessor implements ResumeFieldProcessor {
    @Override
    public BotApiMethod<?> process(Long chatId,
                                   String text,
                                   StateMachine<FormState, FormEvent> formMachine
    ) {
        return null;
    }
}
