package com.dream.dreamtv.data.model;

public final class Category {
    private Category() {
        // This class is not publicly instantiable
    }

    public enum Type {
        ALL("all"),
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
