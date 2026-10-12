package com.talentflow.interview.infrastructure.mail;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.talentflow.interview.application.InterviewInvitationSender;

/**
 * Plain-text invitation in English and German, with the time in the company's time zone.
 */
@Component
public class MailInterviewInvitationSender implements InterviewInvitationSender {

    private final JavaMailSender mailSender;
    private final String from;
    private final ZoneId zone;

    public MailInterviewInvitationSender(JavaMailSender mailSender, @Value("${talentflow.mail.from}") String from,
            @Value("${talentflow.time-zone}") String zone) {
        this.mailSender = mailSender;
        this.from = from;
        this.zone = ZoneId.of(zone);
    }

    @Override
    public void send(String email, String fullName, String jobTitle, Instant scheduledAt, int durationMinutes,
            String meetingLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Interview invitation: " + jobTitle + " / Einladung zum Interview");
        message.setText(body(fullName, jobTitle, scheduledAt, durationMinutes, meetingLink));
        mailSender.send(message);
    }

    String body(String fullName, String jobTitle, Instant scheduledAt, int durationMinutes, String meetingLink) {
        var time = scheduledAt.atZone(zone);
        String english = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy 'at' HH:mm z", Locale.ENGLISH).format(time);
        String german = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy 'um' HH:mm 'Uhr' z", Locale.GERMAN).format(time);
        String link = meetingLink == null ? "" : "\nMeeting link / Link: " + meetingLink + "\n";
        return """
                Hello %1$s,

                we would like to invite you to an interview for "%2$s" on %3$s (%4$d minutes).

                ---

                Hallo %1$s,

                wir laden Sie herzlich zu einem Interview für "%2$s" am %5$s ein (%4$d Minuten).
                %6$s
                Best regards / Viele Grüße
                The TalentFlow team
                """.formatted(fullName, jobTitle, english, durationMinutes, german, link);
    }
}
