package com.dream.dreamtv.data.model;

public class VideoTopic {
    private final String imageName;
    private final String language;
    private final String name;

    public VideoTopic(String imageName, String language, String name) {
        this.imageName = imageName;
        this.language = language;
        this.name = name;
    }


    public String getImageName() {
        return imageName;
    }

    public String getLanguage() {
        return language;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "VideoTopic{" +
                "name='" + name + '\'' +
                ", language='" + language + '\'' +
                ", imageName='" + imageName + '\'' +
                '}';
    }
}
