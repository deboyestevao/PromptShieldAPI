package com.example.PromptShieldAPI.util;

import com.example.PromptShieldAPI.service.MaskingResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataMasker {
    public static MaskingResult maskSensitiveData(String input) {
        String masked = input;
        MaskingResult result;

        result = maskEmails(masked);
        masked = result.getMaskedText();

        result = maskCVC(masked);
        masked = result.getMaskedText();

        result = maskIBAN(masked);
        masked = result.getMaskedText();

        result = maskVAT(masked);
        masked = result.getMaskedText();

        result = maskPhoneNumbers(masked);
        masked = result.getMaskedText();

        result = maskCreditCard(masked);
        masked = result.getMaskedText();

        result = maskNineDigitNumber(masked);
        masked = result.getMaskedText();

        result = maskPostalCode(masked);
        masked = result.getMaskedText();

        result = maskDates(masked);
        masked = result.getMaskedText();

        result = maskCardExpiry(masked);
        masked = result.getMaskedText();

        result = maskBalance(masked);
        masked = result.getMaskedText();

        result = maskName(masked);
        masked = result.getMaskedText();

        result = maskAddress(masked);
        masked = result.getMaskedText();

        result = maskAnyNumber(masked);
        masked = result.getMaskedText();

        return new MaskingResult(masked);
    }

    private static MaskingResult maskNineDigitNumber(String input) {
        String regex = "\\b(\\d)\\d{2}\\s?\\d{3}\\s?\\d{3}\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String original = matcher.group();
            // Preserva os espaços originais
            String masked = original.replaceAll("(\\d)(\\d{2})(\\s?)(\\d{3})(\\s?)(\\d{2})(\\d)", "$1**$3***$5**$7");
            matcher.appendReplacement(result, masked);
        }

        matcher.appendTail(result);

        return new MaskingResult(result.toString());
    }

    private static MaskingResult maskPhoneNumbers(String input)  {

        Pattern pattern = Pattern.compile("(\\b\\+351|351)?\\s*9\\d{2}[\\s.-]?\\d{3}[\\s.-]?\\d{3}\\b");
        Matcher matcher = pattern.matcher(input);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String raw = matcher.group();
            String cleaned = raw.replaceAll("[^\\d]", "");

            String masked;
            if (cleaned.length() == 9 && cleaned.startsWith("9")) {
                // Sempre adiciona espaços no mascaramento para números de 9 dígitos
                masked = " 9** *** **" + cleaned.charAt(8);
            } else if (cleaned.length() == 12 && cleaned.startsWith("351")) {
                masked = " 351 9** *** **" + cleaned.charAt(11);
            } else {
                masked = raw;
            }

            matcher.appendReplacement(result, masked);
        }

        matcher.appendTail(result);

        return new MaskingResult(result.toString());
    }

    private static MaskingResult maskEmails(String input)  {
        Pattern pattern = Pattern.compile("\\b([\\w._%+-]+)@([\\w.-]+)\\.([a-zA-Z]{2,})\\b");
        Matcher matcher = pattern.matcher(input);

        StringBuffer result = new StringBuffer();

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
            matcher.appendReplacement(result, maskedEmail);
        }
        matcher.appendTail(result);

        return new MaskingResult(result.toString());
    }

    private static MaskingResult maskIBAN(String input) {
        // Portugal – 25 characters (PT50 + 21 digits)
        Pattern ptPattern = Pattern.compile("\\bPT50([\\.\\s-]?\\d){21}\\b", Pattern.CASE_INSENSITIVE);
        Matcher ptMatcher = ptPattern.matcher(input);
        StringBuffer ptBuffer = new StringBuffer();

        while (ptMatcher.find()) {
            String raw = ptMatcher.group().replaceAll("[^\\d]", "");
            if (raw.length() == 23) {
                ptMatcher.appendReplacement(ptBuffer, "PT50.****.****.***********.**");
            } else {
                ptMatcher.appendReplacement(ptBuffer, ptMatcher.group());
            }
        }
        ptMatcher.appendTail(ptBuffer);
        input = ptBuffer.toString();

        // Angola – 25 characters
        Pattern aoPattern = Pattern.compile("\\bAO06([\\.\\s-]?\\d){21}\\b", Pattern.CASE_INSENSITIVE);
        Matcher aoMatcher = aoPattern.matcher(input);
        StringBuffer aoBuffer = new StringBuffer();

        while (aoMatcher.find()) {
            String raw = aoMatcher.group().replaceAll("[^\\d]", "");
            if (raw.length() == 23) {
                aoMatcher.appendReplacement(aoBuffer, "AO06.****.****.***********.**");
            } else {
                aoMatcher.appendReplacement(aoBuffer, aoMatcher.group());
            }
        }
        aoMatcher.appendTail(aoBuffer);
        input = aoBuffer.toString();

        // Moçambique – 25 characters
        Pattern mzPattern = Pattern.compile("\\bMZ59([\\.\\s-]?\\d){21}\\b", Pattern.CASE_INSENSITIVE);
        Matcher mzMatcher = mzPattern.matcher(input);
        StringBuffer mzBuffer = new StringBuffer();

        while (mzMatcher.find()) {
            String raw = mzMatcher.group().replaceAll("[^\\d]", "");
            if (raw.length() == 23) {
                mzMatcher.appendReplacement(mzBuffer, "MZ59.****.****.***********.**");
            } else {
                mzMatcher.appendReplacement(mzBuffer, mzMatcher.group());
            }
        }
        mzMatcher.appendTail(mzBuffer);

        return new MaskingResult(mzBuffer.toString());
    }

    private static MaskingResult maskVAT(String input)  {
        // Portugal VAT numbers (9 digits starting with 1, 2, 3, 5, 6, 8, 9)
        Pattern ptPattern = Pattern.compile("\\bPT\\s*(\\d{9})\\b", Pattern.CASE_INSENSITIVE);
        Matcher ptMatcher = ptPattern.matcher(input);
        StringBuffer ptBuffer = new StringBuffer();

        while (ptMatcher.find()) {
            String vatNumber = ptMatcher.group(1);
            String masked = "PT " + vatNumber.substring(0, 3) + "***" + vatNumber.substring(6);
            ptMatcher.appendReplacement(ptBuffer, masked);
        }
        ptMatcher.appendTail(ptBuffer);
        input = ptBuffer.toString();

        // Angola VAT numbers (9 digits)
        Pattern aoPattern = Pattern.compile("\\bAO\\s*(\\d{9})\\b", Pattern.CASE_INSENSITIVE);
        Matcher aoMatcher = aoPattern.matcher(input);
        StringBuffer aoBuffer = new StringBuffer();

        while (aoMatcher.find()) {
            String vatNumber = aoMatcher.group(1);
            String masked = "AO " + vatNumber.substring(0, 3) + "***" + vatNumber.substring(6);
            aoMatcher.appendReplacement(aoBuffer, masked);
        }
        aoMatcher.appendTail(aoBuffer);

        return new MaskingResult(aoBuffer.toString());
    }

    private static MaskingResult maskCreditCard(String input) {
        // Captura números de 16 dígitos com ou sem espaços
        Pattern pattern = Pattern.compile("\\b(\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4})\\b");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String cardNumber = matcher.group(1);
            // Remove espaços e hífens para obter apenas os dígitos
            String cleanNumber = cardNumber.replaceAll("[\\s-]", "");
            
            if (cleanNumber.length() == 16) {
                String masked = cleanNumber.substring(0, 4) + " **** **** " + cleanNumber.substring(12);
                matcher.appendReplacement(result, masked);
            } else {
                matcher.appendReplacement(result, cardNumber);
            }
        }

        matcher.appendTail(result);
        return new MaskingResult(result.toString());
    }


    private static MaskingResult maskCardExpiry(String input) {
        Pattern pattern = Pattern.compile("\\b(0[1-9]|1[0-2])/([0-9]{2})\\b");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String masked = "**/**";
            matcher.appendReplacement(result, masked);
        }

        matcher.appendTail(result);
        return new MaskingResult(result.toString());
    }

    private static MaskingResult maskDates(String input) {
        // DD/MM/YYYY or DD-MM-YYYY
        Pattern pattern = Pattern.compile("\\b(0[1-9]|[12]\\d|3[01])[/-](0[1-9]|1[0-2])[/-](19|20)\\d{2}\\b");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String masked = "**/**/****";
            matcher.appendReplacement(result, masked);
        }

        matcher.appendTail(result);
        return new MaskingResult(result.toString());
    }

    private static MaskingResult maskCVC(String input) {
        Pattern pattern = Pattern.compile("\\b\\d{3,4}\\b");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String cvc = matcher.group();
            if (cvc.length() >= 3 && cvc.length() <= 4) {
                String masked = "*".repeat(cvc.length());
                matcher.appendReplacement(result, masked);
        }
        }

        matcher.appendTail(result);
        return new MaskingResult(result.toString());
    }

    private static MaskingResult maskPostalCode(String input) {
        // Portuguese postal codes (4 digits + hyphen + 3 digits)
        Pattern pattern = Pattern.compile("\\b\\d{4}-\\d{3}\\b");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String masked = "****-***";
            matcher.appendReplacement(result, masked);
        }

        matcher.appendTail(result);
        return new MaskingResult(result.toString());
    }

    private static MaskingResult maskAddress(String input) {
        // Simple address masking - mask house numbers
        Pattern pattern = Pattern.compile("\\b(?:Rua|Avenida|Travessa|Largo|Praça)\\s+[^,]+,\\s*(\\d+)\\b");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String masked = matcher.group().replaceAll("(\\d+)", "***");
            matcher.appendReplacement(result, masked);
        }

        matcher.appendTail(result);
        return new MaskingResult(result.toString());
    }

    private static MaskingResult maskBalance(String input) {
        // Currency amounts
        Pattern pattern = Pattern.compile("\\b\\d+[.,]\\d{2}\\s*(?:EUR|USD|AOA|MZN)\\b");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String masked = "***€";
            matcher.appendReplacement(result, masked);
        }

        matcher.appendTail(result);
        return new MaskingResult(result.toString());
    }

    private static MaskingResult maskName(String input) {
        // Names (2+ words, each starting with capital letter)
        Pattern pattern = Pattern.compile("\\b[A-Z][a-z]+\\s+[A-Z][a-z]+\\b");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String name = matcher.group();
            String[] parts = name.split("\\s+");
            String masked = parts[0].charAt(0) + "*".repeat(parts[0].length() - 1) + " " +
                          parts[1].charAt(0) + "*".repeat(parts[1].length() - 1);
            matcher.appendReplacement(result, masked);
            }

        matcher.appendTail(result);
        return new MaskingResult(result.toString());
    }

    private static MaskingResult maskAnyNumber(String input) {
        Pattern pattern = Pattern.compile("\\b\\d+\\b");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String masked = "*".repeat(matcher.group().length());
            matcher.appendReplacement(result, masked);
        }

        matcher.appendTail(result);
        return new MaskingResult(result.toString());
    }
}
