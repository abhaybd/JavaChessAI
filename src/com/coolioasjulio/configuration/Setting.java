package com.coolioasjulio.configuration;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Setting<T> {
    public static enum InputType {
        FLOAT, INTEGER, STRING, CHOICE
    }

    private String name;
    private InputType inputType;
    private T[] choices;
    private Consumer<T> callback;
    private Supplier<T> currentSetting;

    public Setting(String key, InputType inputType, Consumer<T> callback, Supplier<T> currentSetting) {
        if (inputType == InputType.CHOICE) {
            throw new IllegalArgumentException("Use the other constructor!");
        }
        
        this.inputType = inputType;
        this.name = key;
        this.callback = callback;
        this.currentSetting = currentSetting;
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
    public <U> void updateUntypedValue(Object value) {
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