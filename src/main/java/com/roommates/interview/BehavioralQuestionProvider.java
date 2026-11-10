package com.roommates.interview;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BehavioralQuestionProvider implements QuestionProvider {
    private static final Logger log = LoggerFactory.getLogger(BehavioralQuestionProvider.class);
    private static final List<String> QUESTIONS= List.of(
            "Takım çalışmasıyla ilgili bir deneyiminizi paylaşır mısınız?",
            "Zor bir durumu nasıl yönettiğinizi anlatır mısınız?",
            "Bir projede liderlik rolü üstlendiğiniz bir zamanı anlatır mısınız?",
            "Hedeflerinize ulaşmak için nasıl motive oluyorsunuz?",
            "Bir hata yaptığınızda nasıl bir yaklaşım sergilersiniz?"
        );

    public BehavioralQuestionProvider() {
        log.info("Davranışsal soru bankası yüklendi");
    }
    @Override
    public String nextQuestion(String topic) {
        int randomIndex = ThreadLocalRandom.current().nextInt(QUESTIONS.size());
        String question = QUESTIONS.get(randomIndex);
        return topic + " bağlamında: " + question;
    }
}
