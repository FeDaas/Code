package de.moviemanager.ui.masterlist.elements;

import static de.moviemanager.ui.masterlist.elements.Type.CONTENT;

public class ContentElement<T> extends Element{
    private final String meta;
    private T contentModel;
    private HeaderElement<T> header;

    public ContentElement(String content, String meta) {
        super(CONTENT, content);
        this.meta = meta;
    }

    public void setHeader(HeaderElement<T> header) {
        this.header = header;
    }

    public HeaderElement<T> getHeader() {
        return this.header;
    }

    public void attachContentModel(T obj) {
        this.contentModel = obj;
    }

    public T retrieveContentModel() {
        return this.contentModel;
    }

    public String getTitle() {
        return getContent();
    }

    public String getMeta() {
        return meta;
    }
}