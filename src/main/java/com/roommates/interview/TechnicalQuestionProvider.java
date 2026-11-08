package com.roommates.interview;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class TechnicalQuestionProvider implements QuestionProvider {
    private static final List<String> QUESTIONS=List.of(
        "Java Nedir?",
        "Spring Boot ile ilgili deneyiminiz nedir?",
        "REST API'lerle ilgili deneyiminiz nedir?",
        "SQL veri tabanları ile ilgili deneyiminiz nedir?",
        "NoSQL veri tabanları ile ilgili deneyiminiz nedir?"
    );
    @Override 
    public String nextQuestion(String topic) {

        int randomIndex = ThreadLocalRandom.current().nextInt(QUESTIONS.size());
        String question = QUESTIONS.get(randomIndex);
        return topic + " bağlamında: " + question;
    }

}
