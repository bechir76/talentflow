package com.talentflow.ai.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Removes direct identifiers from CV text before it is sent to an AI provider (GDPR data minimisation).
 * It replaces the candidate's known name, email and phone, plus anything that looks like an email,
 * an international or national phone number, or a profile link. Best effort: it reduces, but cannot
 * fully guarantee, the personal data that leaves the system.
 */
public final class PersonalDataRedactor {

    public static final String NAME = "[name]";
    public static final String EMAIL = "[email]";
    public static final String PHONE = "[phone]";
    public static final String LINK = "[link]";

    private static final int MIN_NAME_PART_LENGTH = 2;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}");
    private static final Pattern LINK_PATTERN =
            Pattern.compile("(?i)\\b(?:https?://|www\\.)\\S+|\\b(?:linkedin|github|xing)\\.com/\\S+");
    // "+49 151 2345678", "0049 30 123456", "(030) 1234 5678", "0151-2345678". Year ranges such as
    // "2016 - 2018" do not match because they neither start with + / 00 / 0 nor contain enough digits.
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(?<![\\w+])(?:\\+|00)\\d[\\d\\s().\\-/]{6,}\\d|(?<![\\w+])\\(?0\\d{2,5}\\)?[\\s/-]?\\d{3,}(?:[\\s-]?\\d{2,})*");

    public String redact(String text, String fullName, String email, String phone) {
        String result = text;
        if (email != null && !email.isBlank()) {
            result = replaceLiteral(result, email.strip(), EMAIL);
        }
        result = EMAIL_PATTERN.matcher(result).replaceAll(EMAIL);
        result = LINK_PATTERN.matcher(result).replaceAll(LINK);
        if (phone != null && !phone.isBlank()) {
            result = replaceLiteral(result, phone.strip(), PHONE);
        }
        result = PHONE_PATTERN.matcher(result).replaceAll(PHONE);
        if (fullName != null && !fullName.isBlank()) {
            result = replaceLiteral(result, fullName.strip(), NAME);
            for (String part : Arrays.stream(fullName.split("\\s+")).filter(p -> p.length() >= MIN_NAME_PART_LENGTH).toList()) {
                result = replaceWord(result, part, NAME);
            }
        }
        return result;
    }

    private static String replaceLiteral(String text, String value, String replacement) {
        return Pattern.compile(Pattern.quote(value), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                .matcher(text).replaceAll(replacement);
    }

    private static String replaceWord(String text, String word, String replacement) {
        String boundary = "(?<![\\p{L}\\p{N}])";
        String end = "(?![\\p{L}\\p{N}])";
        return Pattern.compile(boundary + Pattern.quote(word.toLowerCase(Locale.ROOT)) + end,
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(text).replaceAll(replacement);
    }
}
