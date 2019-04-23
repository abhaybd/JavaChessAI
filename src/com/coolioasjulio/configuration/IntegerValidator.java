package com.coolioasjulio.configuration;

public class IntegerValidator implements SettingValidator {

    @Override
    public boolean isValid(String text) {
        try {
            if ("".equals(text)) {
                return true;
            }
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
