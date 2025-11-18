package com.job.radar.config.statemachine;

import com.job.radar.model.enums.statemachine.event.ResumeEvent;
import com.job.radar.model.enums.statemachine.state.ResumeState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachineFactory(name = "resumeStateMachine")
public class ResumeStateMachineConfig extends StateMachineConfigurerAdapter<ResumeState, ResumeEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<ResumeState, ResumeEvent> states) throws Exception {
        states
                .withStates()
                .initial(ResumeState.RESUME_VIEW)
                .state(ResumeState.RESUME_CREATE)
                .state(ResumeState.RESUME_EDIT)
                .state(ResumeState.RESUME_DELETE_CONFIRM)
                .end(ResumeState.RESUME_COMPLETED)
                .end(ResumeState.RESUME_CANCELLED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ResumeState, ResumeEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .source(ResumeState.RESUME_VIEW).target(ResumeState.RESUME_CREATE).event(ResumeEvent.CREATE_RESUME)
                .and()
                .withExternal()
                .source(ResumeState.RESUME_VIEW).target(ResumeState.RESUME_EDIT).event(ResumeEvent.EDIT_RESUME)
                .and()
                .withExternal()
                .source(ResumeState.RESUME_VIEW).target(ResumeState.RESUME_DELETE_CONFIRM).event(ResumeEvent.DELETE_RESUME)
                .and()
                .withExternal()
                .source(ResumeState.RESUME_CREATE).target(ResumeState.RESUME_VIEW).event(ResumeEvent.COMPLETE)
                .and()
                .withExternal()
                .source(ResumeState.RESUME_EDIT).target(ResumeState.RESUME_VIEW).event(ResumeEvent.COMPLETE)
                .and()
                .withExternal()
                .source(ResumeState.RESUME_DELETE_CONFIRM).target(ResumeState.RESUME_VIEW).event(ResumeEvent.CONFIRM_DELETE)
                .and()
                .withExternal()
                .source(ResumeState.RESUME_DELETE_CONFIRM).target(ResumeState.RESUME_VIEW).event(ResumeEvent.CANCEL_DELETE)
                .and()
                .withExternal()
                .source(ResumeState.RESUME_CREATE).target(ResumeState.RESUME_CANCELLED).event(ResumeEvent.CANCEL)
                .and()
                .withExternal()
                .source(ResumeState.RESUME_EDIT).target(ResumeState.RESUME_CANCELLED).event(ResumeEvent.CANCEL);
    }
}
