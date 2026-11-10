package com.roommates.interview;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

@Configuration
public class InterviewConfig {
    @Bean
    @Primary
    public QuestionProvider technicalQuestionProvider() {
        return new TechnicalQuestionProvider();
    }

    @Bean
    @Lazy
    @Qualifier("behavioral")
    public QuestionProvider behavioralQuestionProvider() {
        return new BehavioralQuestionProvider();
    }
}
