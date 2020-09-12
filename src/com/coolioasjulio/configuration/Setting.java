package com.coolioasjulio.configuration;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Setting<T> {
    public enum InputType {
        DOUBLE, INTEGER, STRING, CHOICE
    }

    private final String name;
    private final InputType inputType;
    private T[] choices;
    private final Consumer<T> callback;
    private final Supplier<T> currentSetting;
    private SettingValidator validator = new DefaultValidator();

    public Setting(String key, InputType inputType, Consumer<T> callback, Supplier<T> currentSetting) {
        if (inputType == InputType.CHOICE) {
            throw new IllegalArgumentException("Use the other constructor!");
        }

        this.inputType = inputType;
        this.name = key;
        this.callback = callback;
        this.currentSetting = currentSetting;

        switch (inputType) {
            case INTEGER:
                setValidator(new IntegerValidator());
                break;

            case DOUBLE:
                setValidator(new DoubleValidator());
                break;

            default:
                break;
        }
    }

    @SafeVarargs
    public Setting(String key, Consumer<T> callback, Supplier<T> currentSetting, T... choices) {
        if (choices == null || choices.length == 0) {
            throw new IllegalArgumentException("Length must be greater than 0!");
        }

        this.name = key;
        this.callback = callback;
        inputType = InputType.CHOICE;
        this.choices = choices;
        this.currentSetting = currentSetting;
    }

    public Setting<T> setValidator(SettingValidator validator) {
        this.validator = validator == null ? new DefaultValidator() : validator;
        return this;
    }

    public SettingValidator getValidator() {
        return validator;
    }

    public T getCurrentSetting() {
        return currentSetting.get();
    }

    public String getName() {
        return name;
    }

    public void updateValue(T value) {
        callback.accept(value);
    }

    @SuppressWarnings("unchecked")
    public void updateUntypedValue(Object value) {
        callback.accept((T) value);
    }

    public String[] getChoicesNames() {
        return Arrays.stream(choices).map(Object::toString).toArray(String[]::new);
    }

    public T[] getChoices() {
        return choices;
    }

    public InputType getInputType() {
        return inputType;
    }
}