package com.milind.mcp.common.model;

/** A single content block inside a {@code tools/call} result. Only "text" is used here. */
public class ToolContent {

    private String type = "text";
    private String text;

    public ToolContent() {
    }

    public static ToolContent text(String text) {
        ToolContent content = new ToolContent();
        content.text = text;
        return content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
