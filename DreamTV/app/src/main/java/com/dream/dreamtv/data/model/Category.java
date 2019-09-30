package com.dream.dreamtv.data.model;

import static com.dream.dreamtv.data.model.Category.Type.CONTINUE;
import static com.dream.dreamtv.data.model.Category.Type.FINISHED;
import static com.dream.dreamtv.data.model.Category.Type.MY_LIST;
import static com.dream.dreamtv.data.model.Category.Type.NEW;
import static com.dream.dreamtv.data.model.Category.Type.SETTINGS;
import static com.dream.dreamtv.data.model.Category.Type.TEST;
import static com.dream.dreamtv.data.model.Category.Type.TOPICS;

public class Category {
    private int orderIndex;
    private String name;
    private boolean visible;

    public Category(int orderIndex, String name, boolean visible) {
        this.orderIndex = orderIndex;
        this.name = name;
        this.visible = visible;
    }

    public Category.Type getCategoryType() {
        switch (this.name) {
            case "new":
                return NEW;
            case "test":
                return TEST;
            case "continue":
                return CONTINUE;
            case "finished":
                return FINISHED;
            case "myList":
                return MY_LIST;
            case "settings":
                return SETTINGS;
            case "topics":
                return TOPICS;
            default:
                throw new RuntimeException("Category not contemplated!");
        }
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public enum Type {
        ALL("all"),
        NEW("new"),
        CONTINUE("continue"),
        FINISHED("finished"),
        TEST("test"),
        MY_LIST("myList"),
        SETTINGS("settings"),
        TOPICS("topics");

        public final String value;

        Type(String value) {
            this.value = value;
        }
    }
}
