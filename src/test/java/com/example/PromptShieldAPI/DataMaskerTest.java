package com.example.PromptShieldAPI;

import com.example.PromptShieldAPI.util.DataMasker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataMaskerTest {
    @Test
    void testMaskName() {
        String input = " <nome>Ricardo Pereira</nome>";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertTrue(masked.contains("R****** P******"));
    }

    @Test
    void testMaskNameFalse() {
        String input = "O nome do cliente é Ricardo Pereira";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertEquals(input, masked);
    }

    @Test
    void testMaskVAT() {
        String input = "O NIF é 245678912";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertTrue(masked.contains("*** *** 912"));
    }

    @Test
    void testMaskVATFalse() {
        String input = "O NIF é 123456789011111";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertEquals(input, masked);
    }

    @Test
    void testMaskDates() {
        String input = "Eu nasci em 1985-11-10";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertTrue(masked.contains("****-**-**"));
    }

    @Test
    void testMaskDatesFalse() {
        String input = "Eu nasci em 19891-111-101";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertEquals(input, masked);
    }

    @Test
    void testMaskAddress() {
        String input = "Avenida Martim Moniz";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertTrue(masked.contains("Avenida M***** ****"));
    }

    @Test
    void testMaskAddressFalse() {
        String input = "Avenidas Martim Moniz";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertEquals(input, masked);
    }

    @Test
    void testMaskPostalCode() {
        String input = "Este é o meu código postal 4000-067";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertTrue(masked.contains("****-***"));
    }

    @Test
    void testMaskPostalCodeFalse() {
        String input = "Este é o meu código postal 40005-067";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertEquals(input, masked);
    }

    @Test
    void testMaskCity() {
        String input = "<cidade>Lisboa</cidade>";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertTrue(masked.contains("L*****"));
    }

    @Test
    void testMaskCityFalse() {
        String input = "Lisboa";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertEquals(input, masked);
    }

    @Test
    void testMaskPhoneNumbers() {
        String input = "+351 935 678 901";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertTrue(masked.contains("351 9** *** **1"));
    }

    @Test
    void testMaskPhoneNumbersFalse() {
        String input = "+355 935 678 90";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertEquals(input, masked);
    }

    @Test
    void testMaskEmail() {
        String input = "O email é joana.lopes@exemplo.pt";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertTrue(masked.contains("@"));
    }

    @Test
    void testMaskEmailFalse() {
        String input = "O email é joana.lopesexemplo.pt";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertEquals(input, masked);
    }

    @Test
    void testMaskIBAN() {
        String input = "O meu iban é PT50 0023 0000 9876 5432 1098 7";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertTrue(masked.contains("PT50.****.****.***********.**"));
    }

    @Test
    void testMaskBalance() {
        String input = "<saldo>259.55</saldo>";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertTrue(masked.contains("***.**"));
    }

    @Test
    void testMaskBalanceFalse() {
        String input = "O meu saldo na Steam é de 259.55";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertEquals(input, masked);
    }

    @Test
    void testMaskCreditCard() {
        String input = "O meu cartão de credito é 4716 9876 5432 1098";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertTrue(masked.contains("**** **** **** 1098"));
    }

    @Test
    void testMaskCreditCardFalse() {
        String input = "O meu cartão de credito é 47165 9876 5432 1098";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertEquals(input, masked);
    }

    @Test
    void testMaskCardExpiry() {
        String input = "A data de validade do meu cartao é 05/28";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertTrue(masked.contains("**/**"));
    }

    @Test
    void testMaskCardExpiryFalse() {
        String input = "A data de validade do meu cartao é 223/332";
        String masked = DataMasker.maskSensitiveData(input).getMaskedText();
        assertEquals(input, masked);
    }
}