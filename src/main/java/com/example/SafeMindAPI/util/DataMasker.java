package com.example.SafeMindAPI.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataMasker {
    public static String maskSensitiveData(String input) {
        if (input == null) return null;

        String masked = input;

        masked = maskEmails(masked);
        masked = maskCVC(masked);
        masked = maskIBAN(masked);
        masked = maskVAT(masked);
        masked = maskPhoneNumbers(masked);
        masked = maskNineDigitNumber(masked);
        masked = maskPostalCode(masked);
        masked = maskDates(masked);
        masked = maskCardExpiry(masked);
        masked = maskBalance(masked);
        masked = maskName(masked);
        masked = maskCreditCard(masked);
        masked = maskAddress(masked);

        return masked;
    }

    private static String maskNineDigitNumber(String input) {

        // ID Card, NIF/TIN/VAT
        return input.replaceAll("\\b\\d{9}\\b", "*********");
    }

    public static String maskPhoneNumbers(String input) {

        Pattern pattern = Pattern.compile("(\\b\\+351|351)?\\s*9\\d{2}[\\s.-]?\\d{3}[\\s.-]?\\d{3}\\b");

        return pattern.matcher(input).replaceAll(matchResult -> {
            String raw = matchResult.group();
            String cleaned = raw.replaceAll("[^\\d]", "");

            if (cleaned.length() == 9 && cleaned.startsWith("9")) {
                return "9** *** *" + cleaned.charAt(8);
            }

            if (cleaned.length() == 12 && cleaned.startsWith("351")) {
                return "351 9** *** *" + cleaned.charAt(11);
            }

            return raw;
        });
    }

    private static String maskEmails(String input) {
        Pattern pattern = Pattern.compile("\\b([\\w._%+-]+)@([\\w.-]+)\\.([a-zA-Z]{2,})\\b");
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String username = matcher.group(1);
            String domain = matcher.group(2);
            String tld = matcher.group(3);

            String maskedUsername = username.length() >= 2
                    ? username.charAt(0) + "*".repeat(username.length() - 2) + username.charAt(username.length() - 1)
                    : username.charAt(0) + "*";

            String maskedDomain = domain.length() >= 2
                    ? domain.charAt(0) + "*".repeat(domain.length() - 1)
                    : domain.charAt(0) + "*";

            String maskedTld = tld.length() >= 2
                    ? tld.charAt(0) + "*".repeat(tld.length() - 1)
                    : tld.charAt(0) + "*";

            String maskedEmail = maskedUsername + "@" + maskedDomain + "." + maskedTld;
            matcher.appendReplacement(sb, maskedEmail);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String maskIBAN(String input) {

        // Portugal – 25 characters
        Pattern ptPattern = Pattern.compile("\\bPT50([\\.\\s-]?\\d){21}\\b", Pattern.CASE_INSENSITIVE);
        input = ptPattern.matcher(input).replaceAll(matchResult -> {
            String raw = matchResult.group().replaceAll("[^\\d]", "");
            if (raw.length() != 23) return matchResult.group();
            return "PT50" + ".****.****.***********.**";
        });

        // Angola – 25 characters
        Pattern aoPattern = Pattern.compile("\\bAO(?:[\\.\\s-]?\\d){23}\\b", Pattern.CASE_INSENSITIVE);
        input = aoPattern.matcher(input).replaceAll(matchResult -> {
            String raw = matchResult.group().replaceAll("[^\\d]", "");
            if (raw.length() != 23) return matchResult.group();
            return "AO" + raw.substring(0, 2) + ".****.****.***********.**";
        });

        // Cabo Verde – 23 characters
        Pattern cvPattern = Pattern.compile("\\bCV(?:[\\.\\s-]?\\d){21}\\b", Pattern.CASE_INSENSITIVE);
        input = cvPattern.matcher(input).replaceAll(matchResult -> {
            String raw = matchResult.group().replaceAll("[^\\d]", "");
            if (raw.length() != 21) return matchResult.group();
            return "CV" + raw.substring(0, 2) + ".****.****.***********";
        });

        // São Tome e Principe – 23 characters
        Pattern stPattern = Pattern.compile("\\bST(?:[\\.\\s-]?\\d){21}\\b", Pattern.CASE_INSENSITIVE);
        input = stPattern.matcher(input).replaceAll(matchResult -> {
            String raw = matchResult.group().replaceAll("[^\\d]", "");
            if (raw.length() != 21) return matchResult.group();
            return "ST" + raw.substring(0, 2) + ".****.****.***********";
        });

        return input;
    }

    public static String maskVAT(String input) {

        // Portugal (9 digits)
        // Sao Tome e Principe (9 digits)
        // Cabo Verde (10 digits)
        // Angola (14 digits)

        Pattern pattern = Pattern.compile("\\b\\d{9,14}\\b");

        return pattern.matcher(input).replaceAll(matchResult -> {
            String raw = matchResult.group();
            String cleaned = raw.replaceAll("[^\\d]", "");

            switch (cleaned.length()) {
                case 9 -> { // Portugal, Sao Tome e Principe
                    if (cleaned.matches("[125]\\d{8}")) return "*** *** " + cleaned.substring(6);
                }
                case 10 -> { // Cabo Verde
                    return "** *** ** " + cleaned.substring(7);
                }
                case 14 -> { // Angola
                    return "*** **** *** " + cleaned.substring(10);
                }
            }


            return raw;
        });
    }

    public static String maskCreditCard(String input) {
        Pattern pattern = Pattern.compile("\\b(?:\\d{4}[-.\\s]?){3}\\d{4}\\b");

        return pattern.matcher(input).replaceAll(matchResult -> {
            String raw = matchResult.group();
            String cleaned = raw.replaceAll("[^\\d]", "");

            if (cleaned.length() != 16) return raw;

            return "**** **** **** " + cleaned.substring(12);
        });
    }


    private static String maskCardExpiry(String input) {
        return input.replaceAll("\\b(0[1-9]|1[0-2])[\\/\\-](\\d{2}|\\d{4})\\b", "**/**");
    }

    private static String maskDates(String input) {
        input = input.replaceAll("\\b\\d{4}[/-]?\\d{2}[/-]?\\d{2}\\b", "****-**-**");

        input = input.replaceAll("\\b\\d{2}[/-]?\\d{4}[/-]?\\d{2}\\b", "**-****-**");

        input = input.replaceAll("\\b\\d{2}[/-]?\\d{2}[/-]?\\d{4}\\b", "**-**-****");

        return input;
    }

    public static String maskCVC(String input) {
        return input.replaceAll("\\b(?i)(cvv|cvc)[\\s:]*(\\d{3,4}\\b)", "$1: ***");
    }

    public static String maskPostalCode(String input) {
        return input.replaceAll("\\b(\\d{4})[\\s/-]?(\\d{3})\\b", "****-$2");
    }

    private static String maskAddress(String input) {

        // Mask street names
        Pattern pattern = Pattern.compile("\\b(?i)(rua|avenida|praça|praca|travessa)\\s+([\\p{L}\\s]+)\\b");
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String type = matcher.group(1);
            String name = matcher.group(2);

            StringBuilder maskedName = new StringBuilder();
            maskedName.append(name.charAt(0));

            for (int i = 1; i < name.length(); i++) {
                char c = name.charAt(i);
                if (c == ' ') {
                    maskedName.append(' ');
                } else {
                    maskedName.append('*');
                }
            }

            matcher.appendReplacement(sb, type + " " + maskedName.toString());
        }
        matcher.appendTail(sb);
        input = sb.toString();

        return input;
    }

    public static String maskBalance(String input) {
        Pattern pattern = Pattern.compile(
                "(?i)(<saldo>|<balance>|\"saldo\"\\s*[:>]\\s*|\"balance\"\\s*[:>]\\s*)(\\d+)(\\.(\\d+))?(</saldo>|</balance>)?",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String prefix = matcher.group(1);         // ex: <Balance> or "saldo" :>
            String integerPart = matcher.group(2);    // ex: 5200
            String decimalPart = matcher.group(4);    // ex: 75
            String suffix = matcher.group(5) != null ? matcher.group(5) : "";  // ex: </balance>

            String maskedInt = "*".repeat(Math.max(1, integerPart.length()));
            String maskedDec = decimalPart != null ? "." + "*".repeat(decimalPart.length()) : "";

            String maskedValue = prefix + maskedInt + maskedDec + suffix;
            matcher.appendReplacement(sb, Matcher.quoteReplacement(maskedValue));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskName(String input) {
        Pattern pattern = Pattern.compile(
                "(?i)(?:<(?<xmlTag>nome|name|banco|bank|cidade|city|distrito|district|estado|state)>(?<xmlValue>[^<]+)</\\k<xmlTag>>" +     // XML
                        "|\"(?<jsonKey>nome|name|banco|bank|cidade|city|distrito|district|estado|state)\"\\s*[:>]\\s*\"(?<jsonValue>[^\"]+)\")"      // JSON
        );

        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String value = matcher.group("xmlValue") != null ? matcher.group("xmlValue") : matcher.group("jsonValue");

            StringBuilder masked = new StringBuilder();
            for (String word : value.split(" ")) {
                if (!word.isEmpty()) {
                    masked.append(word.charAt(0));
                    masked.append("*".repeat(Math.max(0, word.length() - 1)));
                    masked.append(" ");
                }
            }
            String maskedValue = masked.toString().trim();

            if (matcher.group("xmlTag") != null) {
                String tag = matcher.group("xmlTag");
                matcher.appendReplacement(sb, "<" + tag + ">" + maskedValue + "</" + tag + ">");
            } else {
                String key = matcher.group("jsonKey");
                matcher.appendReplacement(sb, "\"" + key + "\": \"" + maskedValue + "\"");
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }
}
