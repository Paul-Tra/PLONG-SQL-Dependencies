package sample;

import javafx.scene.paint.Color;

public class Style {
    /* Transaction's rectangle colors */
    private Color strokeColor;
    private Color backgroundColor;
    private Color textColor;

    /* Relation colors */
    private Color selectedDependencyColor;
    private Color classicDependencyColor;

    private String pattern = "rw"; // Read-Write

    public Style() {
        /* Initial default colors */
        this.strokeColor = Color.BLACK;
        this.backgroundColor = Color.WHITE;
        this.textColor = Color.BLUE;

        this.classicDependencyColor = Color.BLACK;
        this.selectedDependencyColor = Color.SALMON;
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getSelectedDependencyColor() {
        return selectedDependencyColor;
    }

    public void setSelectedDependencyColor(Color selectedDependencyColor) {
        this.selectedDependencyColor = selectedDependencyColor;
    }

    public Color getClassicDependencyColor() {
        return classicDependencyColor;
    }

    public void setClassicDependencyColor(Color classicDependencyColor) {
        this.classicDependencyColor = classicDependencyColor;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
