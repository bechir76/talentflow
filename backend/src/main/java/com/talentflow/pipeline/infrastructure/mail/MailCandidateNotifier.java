package com.talentflow.pipeline.infrastructure.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.pipeline.application.CandidateNotifier;

/**
 * Sends a short plain-text email in English and German. It never contains AI results or internal notes.
 */
@Component
public class MailCandidateNotifier implements CandidateNotifier {

    private final JavaMailSender mailSender;
    private final String from;

    public MailCandidateNotifier(JavaMailSender mailSender, @Value("${talentflow.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void stageChanged(String email, String fullName, String jobTitle, ApplicationStage newStage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Update on your application: " + jobTitle + " / Neuigkeiten zu Ihrer Bewerbung");
        message.setText(body(fullName, jobTitle, newStage));
        mailSender.send(message);
    }

    static String body(String fullName, String jobTitle, ApplicationStage stage) {
        return """
                Hello %1$s,

                %2$s

                You can follow your application at any time in your candidate portal.

                ---

                Hallo %1$s,

                %3$s

                Den Stand Ihrer Bewerbung sehen Sie jederzeit in Ihrem Bewerberportal.

                Best regards / Viele Grüße
                The TalentFlow team
                """.formatted(fullName, english(jobTitle, stage), german(jobTitle, stage));
    }

    private static String english(String job, ApplicationStage stage) {
        return switch (stage) {
            case APPLIED -> "your application for \"" + job + "\" is back in our review queue.";
            case SCREENING -> "we are now reviewing your application for \"" + job + "\" in detail.";
            case INTERVIEW -> "good news: we would like to invite you to an interview for \"" + job
                    + "\". We will contact you shortly with suggested dates.";
            case OFFER -> "we are happy to tell you that we are preparing an offer for \"" + job + "\".";
            case HIRED -> "welcome on board! Your hiring for \"" + job + "\" is confirmed.";
            case REJECTED -> "thank you for your interest in \"" + job + "\". After careful consideration we have "
                    + "decided not to continue with your application. We wish you every success.";
        };
    }

    private static String german(String job, ApplicationStage stage) {
        return switch (stage) {
            case APPLIED -> "Ihre Bewerbung für \"" + job + "\" befindet sich wieder in unserer Prüfung.";
            case SCREENING -> "wir prüfen Ihre Bewerbung für \"" + job + "\" jetzt im Detail.";
            case INTERVIEW -> "gute Nachrichten: Wir möchten Sie zu einem Interview für \"" + job
                    + "\" einladen. Wir melden uns in Kürze mit Terminvorschlägen.";
            case OFFER -> "wir freuen uns, Ihnen mitzuteilen, dass wir ein Angebot für \"" + job + "\" vorbereiten.";
            case HIRED -> "willkommen an Bord! Ihre Einstellung für \"" + job + "\" ist bestätigt.";
            case REJECTED -> "vielen Dank für Ihr Interesse an \"" + job + "\". Nach sorgfältiger Prüfung haben wir "
                    + "entschieden, Ihre Bewerbung nicht weiterzuverfolgen. Wir wünschen Ihnen viel Erfolg.";
        };
    }
}
