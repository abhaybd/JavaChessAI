package com.coolioasjulio.jchess;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public class IntegerDocumentFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        StringBuilder sb = createBuilder(fb);
        sb.insert(offset, string);

        if (isValid(sb.toString())) {
            super.insertString(fb, offset, string, attr);
        }
    }

    private StringBuilder createBuilder(FilterBypass fb) throws BadLocationException {
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        return sb;
    }

    private boolean isValid(String text) {
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

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        StringBuilder sb = createBuilder(fb);
        sb.replace(offset, offset + length, text);

        if (isValid(sb.toString())) {
            super.replace(fb, offset, length, text, attrs);
        }
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        StringBuilder sb = createBuilder(fb);
        sb.delete(offset, offset + length);

        if (isValid(sb.toString())) {
            super.remove(fb, offset, length);
        }
    }
}