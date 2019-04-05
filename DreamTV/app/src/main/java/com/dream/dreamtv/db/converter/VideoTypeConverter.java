package com.dream.dreamtv.db.converter;

import com.dream.dreamtv.model.Video;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import androidx.room.TypeConverter;

public class VideoTypeConverter {

    @TypeConverter
    public static Video stringToVideo(String data) {
        Gson gson = new Gson();

        if (data == null) {
            return new Video();
        }

        Type listType = new TypeToken<Video>() {
        }.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String videoToString(Video someObjects) {
        Gson gson = new Gson();

        return gson.toJson(someObjects);
    }
}
