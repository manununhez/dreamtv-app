package com.manuelnunhez.dreamtv.data.networking.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by manuel on 6/26/17.
 */

public class SubtitleSchema {
    @SerializedName("version_number")
    public int versionNumber;
    @SerializedName("subtitles")
    public List<SubtitleTextSchema> subtitles;
    @SerializedName("sub_format")
    public String subFormat;
    @SerializedName("title")
    public String videoTitleTranslated;
    @SerializedName("description")
    public String videoDescriptionTranslated;
    @SerializedName("video_title")
    public String videoTitleOriginal;
    @SerializedName("video_description")
    public String videoDescriptionOriginal;


    public static class SubtitleTextSchema {
        private static final int ONE_SEC_IN_MS = 1000;
        @SerializedName("text")
        private String text;
        @SerializedName("position")
        private int position;
        @SerializedName("start")
        private int start; //msecs
        @SerializedName("end")
        private int end; //msecs

        public int getStartInSecs() {
            return start / ONE_SEC_IN_MS;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEndInSecs() {
            return end / ONE_SEC_IN_MS;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }
}
