package com.coolioasjulio.configuration;

public class DoubleValidator implements SettingValidator {
    @Override
    public boolean isValid(String text) {
        try {
            if ("".equals(text)) {
                return true;
            }
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
