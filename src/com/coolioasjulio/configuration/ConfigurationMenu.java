package com.coolioasjulio.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ConfigurationMenu {

    private static Set<ConfigurationMenu> configMenus = new HashSet<>();

    public static void addConfigMenu(ConfigurationMenu menu) {
        configMenus.add(menu);
    }

    public static boolean removeConfigMenu(ConfigurationMenu menu) {
        return configMenus.remove(menu);
    }

    public static Set<ConfigurationMenu> getConfigMenusCopy() {
        return new HashSet<>(configMenus);
    }

    private String name;
    private List<Setting<?>> settings;

    public ConfigurationMenu(String name, Setting<?>... settings) {
        this(name, Arrays.asList(settings));
    }

    public ConfigurationMenu(String name, List<Setting<?>> settings) {
        this.name = name;
        this.settings = settings;
    }

    public String getName() {
        return name;
    }

    public List<Setting<?>> getSettings() {
        return new ArrayList<>(settings);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConfigurationMenu)) {
            return false;
        }

        ConfigurationMenu menu = (ConfigurationMenu) o;
        return menu.name.equals(name) && menu.settings.equals(settings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, settings);
    }
}
