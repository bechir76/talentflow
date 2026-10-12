package com.talentflow.pipeline.infrastructure.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.talentflow.application.domain.ApplicationStage;

class MailCandidateNotifierTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final MailCandidateNotifier notifier = new MailCandidateNotifier(mailSender, "TalentFlow <no-reply@talentflow.dev>");

    @Test
    void sendsBilingualInterviewInvitation() {
        notifier.stageChanged("clara@example.org", "Clara Candidate", "Java Developer", ApplicationStage.INTERVIEW);

        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        assertThat(message.getValue().getTo()).containsExactly("clara@example.org");
        assertThat(message.getValue().getSubject()).contains("Java Developer");
        assertThat(message.getValue().getText())
                .contains("Hello Clara Candidate", "invite you to an interview", "Hallo Clara Candidate", "Interview");
    }

    @Test
    void rejectionIsPoliteAndContainsNoInternalDetails() {
        String body = MailCandidateNotifier.body("Clara", "Java Developer", ApplicationStage.REJECTED);

        assertThat(body).contains("thank you for your interest", "vielen Dank")
                .doesNotContainIgnoringCase("score").doesNotContainIgnoringCase("AI");
    }
}
