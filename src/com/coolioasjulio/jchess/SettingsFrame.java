package com.coolioasjulio.jchess;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import com.coolioasjulio.configuration.ConfigurationMenu;
import com.coolioasjulio.configuration.Setting;

class SettingsFrame extends JDialog {
    private static final long serialVersionUID = 1L;

    private List<Object> inputs;
    private List<Setting<?>> settings;

    public SettingsFrame(JFrame parent) {
        super(parent);
        this.setLayout(new GridBagLayout());
        // this.getInsets().set(10, 10, 10, 10);
        GridBagConstraints c = new GridBagConstraints();
        c.ipadx = 20;
        c.insets = new Insets(5, 10, 5, 10);

        Set<ConfigurationMenu> configMenus = ConfigurationMenu.getConfigMenusCopy();

        configMenus = configMenus.stream().filter(Objects::nonNull).collect(Collectors.toSet());

        inputs = new LinkedList<>();
        this.settings = configMenus.stream().flatMap(e -> e.getSettings().stream()).collect(Collectors.toList());

        int index = 0;
        for (ConfigurationMenu menu : configMenus) {
            configConstraints(c, 0, index++, 2, 1);
            c.anchor = GridBagConstraints.CENTER;
            c.fill = GridBagConstraints.BOTH;
            this.add(new JLabel(String.format("--------%s--------", menu.getName()), JLabel.CENTER), c);

            for (Setting<?> setting : menu.getSettings()) {
                configConstraints(c, 0, index, 1, 1);
                c.anchor = GridBagConstraints.EAST;
                c.fill = GridBagConstraints.NONE;
                this.add(new JLabel(setting.getName()), c);

                configConstraints(c, 1, index, 1, 1);
                c.anchor = GridBagConstraints.CENTER;
                c.fill = GridBagConstraints.BOTH;
                switch (setting.getInputType()) {
                    case CHOICE:
                        JComboBox<String> combo = new JComboBox<>(setting.getChoicesNames());
                        this.add(combo, c);
                        combo.setSelectedItem(setting.getCurrentSetting().toString());
                        inputs.add(combo);
                        break;
                    case DOUBLE:
                    case INTEGER:
                    case STRING:
                    default:
                        this.add(configureInput(new InputFilter(setting.getValidator()),
                                setting.getCurrentSetting().toString()), c);
                        break;
                }

                index++;
            }
        }

        configConstraints(c, 0, index, 2, 1);
        c.fill = GridBagConstraints.BOTH;
        JButton apply = new JButton("Apply Settings");
        apply.addActionListener(e -> applySettings());
        this.add(apply, c);
    }

    private void applySettings() {
        for (int i = 0; i < settings.size(); i++) {
            Setting<?> setting = settings.get(i);
            JTextField input;
            String text;
            switch (setting.getInputType()) {
                case DOUBLE:
                    input = (JTextField) inputs.get(i);
                    text = input.getText();
                    if ("".equals(text)) {
                        continue;
                    }
                    setting.updateUntypedValue(Double.parseDouble(text));
                    break;
                case INTEGER:
                    input = (JTextField) inputs.get(i);
                    text = input.getText();
                    if ("".equals(text)) {
                        continue;
                    }
                    setting.updateUntypedValue(Integer.parseInt(text));
                    break;
                case STRING:
                    input = (JTextField) inputs.get(i);
                    setting.updateUntypedValue(input.getText());
                    break;
                case CHOICE:
                default:
                    @SuppressWarnings("unchecked")
                    JComboBox<String> combo = (JComboBox<String>) inputs.get(i);
                    setting.updateUntypedValue(setting.getChoices()[combo.getSelectedIndex()]);
                    break;
            }
        }
        this.setVisible(false);
        this.dispose();
    }

    private JTextField configureInput(DocumentFilter filter, String startValue) {
        JTextField input = new JTextField();
        ((PlainDocument) input.getDocument()).setDocumentFilter(filter);
        input.setText(startValue);
        inputs.add(input);
        return input;
    }

    private void configConstraints(GridBagConstraints c, int x, int y, int width, int height) {
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.gridheight = height;
    }
}