package com.example.PromptShieldAPI.util;

import com.example.PromptShieldAPI.service.MaskingResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataMasker {
    public static MaskingResult maskSensitiveData(String input) {
        String masked = input;
        long totalCount = 0;
        MaskingResult result;

        result = maskEmails(masked);
        masked = result.getMaskedText();
        totalCount += result.getTotal();

        result = maskCVC(masked);
        masked = result.getMaskedText();
        totalCount += result.getTotal();

        result = maskIBAN(masked);
        masked = result.getMaskedText();
        totalCount += result.getTotal();

        result = maskVAT(masked);
        masked = result.getMaskedText();
        totalCount += result.getTotal();

        result = maskPhoneNumbers(masked);
        masked = result.getMaskedText();
        totalCount += result.getTotal();

        result = maskNineDigitNumber(masked);
        masked = result.getMaskedText();
        totalCount += result.getTotal();

        result = maskPostalCode(masked);
        masked = result.getMaskedText();
        totalCount += result.getTotal();

        result = maskDates(masked);
        masked = result.getMaskedText();
        totalCount += result.getTotal();

        result = maskCardExpiry(masked);
        masked = result.getMaskedText();
        totalCount += result.getTotal();

        result = maskBalance(masked);
        masked = result.getMaskedText();
        totalCount += result.getTotal();

        result = maskName(masked);
        masked = result.getMaskedText();
        totalCount += result.getTotal();

        result = maskCreditCard(masked);
        masked = result.getMaskedText();
        totalCount += result.getTotal();

        result = maskAddress(masked);
        masked = result.getMaskedText();
        totalCount += result.getTotal();

        return new MaskingResult(masked, totalCount);
    }

    private static MaskingResult maskNineDigitNumber(String input) {
        String regex = "\\b\\d{9}\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        MaskingResult maskingResult = new MaskingResult();
        StringBuffer result = new StringBuffer();
        Long count = 0L;

        while (matcher.find()) {
            count++;
            matcher.appendReplacement(result, "*********");
        }

        matcher.appendTail(result);

        maskingResult.setTotal(count);

        return new MaskingResult(result.toString(), maskingResult.getTotal());
    }

    private static MaskingResult maskPhoneNumbers(String input)  {

        Pattern pattern = Pattern.compile("(\\b\\+351|351)?\\s*9\\d{2}[\\s.-]?\\d{3}[\\s.-]?\\d{3}\\b");
        Matcher matcher = pattern.matcher(input);

        MaskingResult maskingResult = new MaskingResult();
        StringBuffer result = new StringBuffer();
        Long count = 0L;

        while (matcher.find()) {
            String raw = matcher.group();
            String cleaned = raw.replaceAll("[^\\d]", "");

            String masked;
            if (cleaned.length() == 9 && cleaned.startsWith("9")) {
                masked = "9** *** **" + cleaned.charAt(8);
                count++;
            } else if (cleaned.length() == 12 && cleaned.startsWith("351")) {
                masked = "351 9** *** **" + cleaned.charAt(11);
                count++;
            } else {
                masked = raw;
            }

            matcher.appendReplacement(result, masked);
        }

        matcher.appendTail(result);

        maskingResult.setTotal(count);

        return new MaskingResult(result.toString(), maskingResult.getTotal());
    }

    private static MaskingResult maskEmails(String input)  {
        Pattern pattern = Pattern.compile("\\b([\\w._%+-]+)@([\\w.-]+)\\.([a-zA-Z]{2,})\\b");
        Matcher matcher = pattern.matcher(input);

        MaskingResult maskingResult = new MaskingResult();
        StringBuffer result = new StringBuffer();
        Long count = 0L;

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
            count++;
            matcher.appendReplacement(result, maskedEmail);
        }
        matcher.appendTail(result);

        maskingResult.setTotal(count);

        return new MaskingResult(result.toString(), maskingResult.getTotal());
    }

    private static MaskingResult maskIBAN(String input) {
        MaskingResult maskingResult = new MaskingResult();
        Long count = 0L;

        // Portugal – 25 characters (PT50 + 21 digits)
        Pattern ptPattern = Pattern.compile("\\bPT50([\\.\\s-]?\\d){21}\\b", Pattern.CASE_INSENSITIVE);
        Matcher ptMatcher = ptPattern.matcher(input);
        StringBuffer ptBuffer = new StringBuffer();

        while (ptMatcher.find()) {
            String raw = ptMatcher.group().replaceAll("[^\\d]", "");
            if (raw.length() == 23) {
                ptMatcher.appendReplacement(ptBuffer, "PT50.****.****.***********.**");
                count++;
            } else {
                ptMatcher.appendReplacement(ptBuffer, ptMatcher.group());
            }
        }
        ptMatcher.appendTail(ptBuffer);
        input = ptBuffer.toString();

        // Angola – 25 characters
        Pattern aoPattern = Pattern.compile("\\bAO(?:[\\.\\s-]?\\d){23}\\b", Pattern.CASE_INSENSITIVE);
        Matcher aoMatcher = aoPattern.matcher(input);
        StringBuffer aoBuffer = new StringBuffer();

        while (aoMatcher.find()) {
            String raw = aoMatcher.group().replaceAll("[^\\d]", "");
            if (raw.length() == 23) {
                String masked = "AO" + raw.substring(0, 2) + ".****.****.***********.**";
                aoMatcher.appendReplacement(aoBuffer, masked);
                count++;
            } else {
                aoMatcher.appendReplacement(aoBuffer, aoMatcher.group());
            }
        }
        aoMatcher.appendTail(aoBuffer);
        input = aoBuffer.toString();

        // Cabo Verde – 23 characters
        Pattern cvPattern = Pattern.compile("\\bCV(?:[\\.\\s-]?\\d){21}\\b", Pattern.CASE_INSENSITIVE);
        Matcher cvMatcher = cvPattern.matcher(input);
        StringBuffer cvBuffer = new StringBuffer();

        while (cvMatcher.find()) {
            String raw = cvMatcher.group().replaceAll("[^\\d]", "");
            if (raw.length() == 21) {
                String masked = "CV" + raw.substring(0, 2) + ".****.****.***********";
                cvMatcher.appendReplacement(cvBuffer, masked);
                count++;
            } else {
                cvMatcher.appendReplacement(cvBuffer, cvMatcher.group());
            }
        }
        cvMatcher.appendTail(cvBuffer);
        input = cvBuffer.toString();

        // São Tomé e Príncipe – 23 characters
        Pattern stPattern = Pattern.compile("\\bST(?:[\\.\\s-]?\\d){21}\\b", Pattern.CASE_INSENSITIVE);
        Matcher stMatcher = stPattern.matcher(input);
        StringBuffer stBuffer = new StringBuffer();

        while (stMatcher.find()) {
            String raw = stMatcher.group().replaceAll("[^\\d]", "");
            if (raw.length() == 21) {
                String masked = "ST" + raw.substring(0, 2) + ".****.****.***********";
                stMatcher.appendReplacement(stBuffer, masked);
                count++;
            } else {
                stMatcher.appendReplacement(stBuffer, stMatcher.group());
            }
        }
        stMatcher.appendTail(stBuffer);
        input = stBuffer.toString();

        maskingResult.setTotal(count);

        return new MaskingResult(input, maskingResult.getTotal());
    }

    private static MaskingResult maskVAT(String input)  {

        MaskingResult maskingResult = new MaskingResult();
        Long count = 0L;

        Pattern pattern = Pattern.compile("\\b\\d{9,14}\\b");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String raw = matcher.group();
            String cleaned = raw.replaceAll("[^\\d]", "");
            String masked = raw;

            switch (cleaned.length()) {
                case 9 -> {
                    if (cleaned.matches("[125]\\d{8}")) {
                        masked = "*** *** " + cleaned.substring(6);
                        count++;
                    }
                }
                case 10 -> {
                    masked = "** *** ** " + cleaned.substring(7);
                    count++;
                }
                case 14 -> {
                    masked = "*** **** *** " + cleaned.substring(10);
                    count++;
                }
            }

            matcher.appendReplacement(result, masked);
        }

        matcher.appendTail(result);

        maskingResult.setTotal(count);

        return new MaskingResult(result.toString(), maskingResult.getTotal());
    }

    private static MaskingResult maskCreditCard(String input)  {

        MaskingResult maskingResult = new MaskingResult();
        Long count = 0L;

        Pattern pattern = Pattern.compile("\\b(?:\\d{4}[-.\\s]?){3}\\d{4}\\b");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String raw = matcher.group();
            String cleaned = raw.replaceAll("[^\\d]", "");

            if (cleaned.length() != 16) {
                matcher.appendReplacement(result, raw);
                continue;
            }

            String masked = "**** **** **** " + cleaned.substring(12);
            matcher.appendReplacement(result, masked);
            count++;
        }

        matcher.appendTail(result);

        maskingResult.setTotal(count);

        return new MaskingResult(result.toString(), maskingResult.getTotal());
    }


    private static MaskingResult maskCardExpiry(String input)  {

        Pattern pattern = Pattern.compile("\\b(0[1-9]|1[0-2])[\\/\\-](\\d{2}|\\d{4})\\b");
        Matcher matcher = pattern.matcher(input);

        MaskingResult maskingResult = new MaskingResult();
        StringBuffer result = new StringBuffer();
        Long count = 0L;

        while (matcher.find()) {
            matcher.appendReplacement(result, "**/**");
            count++;
        }

        matcher.appendTail(result);

        maskingResult.setTotal(count);

        return new MaskingResult(result.toString(), maskingResult.getTotal());
    }

    private static MaskingResult maskDates(String input) {
        MaskingResult maskingResult = new MaskingResult();
        Long count = 0L;
        StringBuffer result = new StringBuffer();

        // ISO: yyyy-MM-dd
        Pattern pattern1 = Pattern.compile("\\b\\d{4}[/-]?\\d{2}[/-]?\\d{2}\\b");
        Matcher matcher1 = pattern1.matcher(input);
        while (matcher1.find()) {
            matcher1.appendReplacement(result, "****-**-**");
            count++;
        }
        matcher1.appendTail(result);

        String temp = result.toString();
        result.setLength(0);

        // dd-yyyy-MM
        Pattern pattern2 = Pattern.compile("\\b\\d{2}[/-]?\\d{4}[/-]?\\d{2}\\b");
        Matcher matcher2 = pattern2.matcher(temp);
        while (matcher2.find()) {
            matcher2.appendReplacement(result, "**-****-**");
            count++;
        }
        matcher2.appendTail(result);

        temp = result.toString();
        result.setLength(0);

        // dd-MM-yyyy
        Pattern pattern3 = Pattern.compile("\\b\\d{2}[/-]?\\d{2}[/-]?\\d{4}\\b");
        Matcher matcher3 = pattern3.matcher(temp);
        while (matcher3.find()) {
            matcher3.appendReplacement(result, "**-**-****");
            count++;
        }
        matcher3.appendTail(result);

        maskingResult.setTotal(count);

        return new MaskingResult(result.toString(), maskingResult.getTotal());
    }

    private static MaskingResult maskCVC(String input)  {

        Pattern pattern = Pattern.compile("\\b(?i)(cvv|cvc)[\\s:]*(\\d{3,4})\\b");
        Matcher matcher = pattern.matcher(input);

        MaskingResult maskingResult = new MaskingResult();
        Long count = 0L;
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            count++;
            matcher.appendReplacement(result, matcher.group() + ": ***");
        }
        matcher.appendTail(result);

        maskingResult.setTotal(count);

        return new MaskingResult(result.toString(), maskingResult.getTotal());
    }

    private static MaskingResult maskPostalCode(String input)  {

        Pattern pattern = Pattern.compile("\\b(\\d{4})[\\s/-]?(\\d{3})\\b");
        Matcher matcher = pattern.matcher(input);

        MaskingResult maskingResult = new MaskingResult();
        Long count = 0L;
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            count++;
            matcher.appendReplacement(result, "****-***");
        }
        matcher.appendTail(result);

        maskingResult.setTotal(count);

        return new MaskingResult(result.toString(), maskingResult.getTotal());
    }

    private static MaskingResult maskAddress(String input) {

        // Mask street names
        Pattern pattern = Pattern.compile("\\b(?i)(rua|avenida|praça|praca|travessa)\\s+([\\p{L}\\s]+)\\b");
        Matcher matcher = pattern.matcher(input);

        MaskingResult maskingResult = new MaskingResult();
        Long count = 0L;
        StringBuffer result = new StringBuffer();

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

            count++;
            matcher.appendReplacement(result, type + " " + maskedName.toString());
        }
        matcher.appendTail(result);

        maskingResult.setTotal(count);

        return new MaskingResult(result.toString(), maskingResult.getTotal());
    }

    private static MaskingResult maskBalance(String input)  {
        Pattern pattern = Pattern.compile("(?i)(<saldo>|<balance>|\"saldo\"\\s*[:>]\\s*|\"balance\"\\s*[:>]\\s*)(\\d+)(\\.(\\d+))?(</saldo>|</balance>)?");
        Matcher matcher = pattern.matcher(input);

        MaskingResult maskingResult = new MaskingResult();
        Long count = 0L;
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String prefix = matcher.group(1);         // ex: <Balance> ou "saldo" :>
            String integerPart = matcher.group(2);    // ex: 5200
            String decimalPart = matcher.group(4);    // ex: 75
            String suffix = matcher.group(5) != null ? matcher.group(5) : "";  // ex: </balance>

            String maskedInt = integerPart != null ? "*".repeat(integerPart.length()) : "";
            String maskedDec = decimalPart != null ? "." + "*".repeat(decimalPart.length()) : "";

            String maskedValue = prefix + maskedInt + maskedDec + suffix;
            count++;
            matcher.appendReplacement(result, Matcher.quoteReplacement(maskedValue));
        }
        matcher.appendTail(result);

        maskingResult.setTotal(count);

        return new MaskingResult(result.toString(), maskingResult.getTotal());
    }

    private static MaskingResult maskName(String input)  {
        Pattern pattern = Pattern.compile(
                "(?i)(?:<(?<xmlTag>nome|name|banco|bank|cidade|city|distrito|district|estado|state)>(?<xmlValue>[^<]+)</\\k<xmlTag>>" +     // XML
                        "|\"(?<jsonKey>nome|name|banco|bank|cidade|city|distrito|district|estado|state)\"\\s*[:>]\\s*\"(?<jsonValue>[^\"]+)\")"      // JSON
        );
        Matcher matcher = pattern.matcher(input);

        MaskingResult maskingResult = new MaskingResult();
        Long count = 0L;
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String value = matcher.group("xmlValue") != null ? matcher.group("xmlValue") : matcher.group("jsonValue");

            StringBuilder masked = new StringBuilder();
            for (String word : value.split(" ")) {
                if (!word.isEmpty()) {
                    masked.append(word.charAt(0));
                    masked.append("*".repeat(word.length() - 1));
                    masked.append(" ");
                }
            }
            String maskedValue = masked.toString().trim();

            if (matcher.group("xmlTag") != null) {
                String tag = matcher.group("xmlTag");
                matcher.appendReplacement(result, "<" + tag + ">" + maskedValue + "</" + tag + ">");
            } else {
                String key = matcher.group("jsonKey");
                matcher.appendReplacement(result, "\"" + key + "\": \"" + maskedValue + "\"");
            }

            count++;
        }
        matcher.appendTail(result);

        maskingResult.setTotal(count);

        return new MaskingResult(result.toString(), maskingResult.getTotal());
    }
}
