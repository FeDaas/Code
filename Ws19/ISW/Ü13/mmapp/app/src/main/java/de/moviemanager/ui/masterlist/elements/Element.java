package de.moviemanager.ui.masterlist.elements;

public class Element extends TypifiedBase{
    private final String content;
    private boolean visible;

    Element(Type type, String content) {
        super(type);
        this.content = content;
        this.visible = true;
    }

    public String getContent() {
        return content;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return this.visible;
    }
}



