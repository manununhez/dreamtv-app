package com.dreamproject.dreamtv.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by manuel on 6/26/17.
 */

public class Subtitle implements Parcelable {

    public static final Creator<Subtitle> CREATOR = new Creator<Subtitle>() {
        @Override
        public Subtitle createFromParcel(Parcel in) {
            return new Subtitle(in);
        }

        @Override
        public Subtitle[] newArray(int size) {
            return new Subtitle[size];
        }
    };
    private int versionNumber;
    private List<SubtitleText> subtitles;
    private String subFormat;
    private String videoTitleTranslated;
    private String videoDescriptionTranslated;
    private String videoTitleOriginal;
    private String videoDescriptionOriginal;

    public Subtitle(int versionNumber, List<SubtitleText> subtitles, String subFormat,
                    String videoTitleTranslated, String videoDescriptionTranslated,
                    String videoTitleOriginal, String videoDescriptionOriginal) {
        this.versionNumber = versionNumber;
        this.subtitles = subtitles;
        this.subFormat = subFormat;
        this.videoTitleTranslated = videoTitleTranslated;
        this.videoDescriptionTranslated = videoDescriptionTranslated;
        this.videoTitleOriginal = videoTitleOriginal;
        this.videoDescriptionOriginal = videoDescriptionOriginal;
    }

    protected Subtitle(Parcel in) {
        versionNumber = in.readInt();
        subtitles = in.createTypedArrayList(SubtitleText.CREATOR);
        subFormat = in.readString();
        videoTitleTranslated = in.readString();
        videoDescriptionTranslated = in.readString();
        videoTitleOriginal = in.readString();
        videoDescriptionOriginal = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(versionNumber);
        dest.writeTypedList(subtitles);
        dest.writeString(subFormat);
        dest.writeString(videoTitleTranslated);
        dest.writeString(videoDescriptionTranslated);
        dest.writeString(videoTitleOriginal);
        dest.writeString(videoDescriptionOriginal);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public List<SubtitleText> getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(List<SubtitleText> subtitles) {
        this.subtitles = subtitles;
    }

    public String getSubFormat() {
        return subFormat;
    }

    public void setSubFormat(String subFormat) {
        this.subFormat = subFormat;
    }

    public String getVideoTitleTranslated() {
        return videoTitleTranslated;
    }

    public void setVideoTitleTranslated(String videoTitleTranslated) {
        this.videoTitleTranslated = videoTitleTranslated;
    }

    public String getVideoDescriptionTranslated() {
        return videoDescriptionTranslated;
    }

    public void setVideoDescriptionTranslated(String videoDescriptionTranslated) {
        this.videoDescriptionTranslated = videoDescriptionTranslated;
    }

    public String getVideoTitleOriginal() {
        return videoTitleOriginal;
    }

    public void setVideoTitleOriginal(String videoTitleOriginal) {
        this.videoTitleOriginal = videoTitleOriginal;
    }

    public String getVideoDescriptionOriginal() {
        return videoDescriptionOriginal;
    }

    public void setVideoDescriptionOriginal(String videoDescriptionOriginal) {
        this.videoDescriptionOriginal = videoDescriptionOriginal;
    }

    public SubtitleText getSyncSubtitleText(long l) {
        SubtitleText subtitle = null;
        for (SubtitleText subtitleTemp : this.subtitles) {
            if (l >= subtitleTemp.getStart() && l <= subtitleTemp.getEnd()) { //esta adentro del ciclo
                subtitle = subtitleTemp;
                break;
            } else if (l < subtitleTemp.getStart())
                break;

        }

        return subtitle;
    }


    @Override
    public String toString() {
        return "SubtitleResponse{" +
                "versionNumber=" + versionNumber +
                ", subtitles=" + subtitles +
                ", subFormat='" + subFormat + '\'' +
                ", videoTitleTranslated='" + videoTitleTranslated + '\'' +
                ", videoDescriptionTranslated='" + videoDescriptionTranslated + '\'' +
                ", videoTitleOriginal='" + videoTitleOriginal + '\'' +
                ", videoDescriptionOriginal='" + videoDescriptionOriginal + '\'' +
                '}';
    }


    public static class SubtitleText implements Parcelable {
        public static final Creator<SubtitleText> CREATOR = new Creator<SubtitleText>() {
            @Override
            public SubtitleText createFromParcel(Parcel in) {
                return new SubtitleText(in);
            }

            @Override
            public SubtitleText[] newArray(int size) {
                return new SubtitleText[size];
            }
        };
        private static final int ONE_SEC_IN_MS = 1000;
        private String text;
        private int position;
        private int start; //msecs
        private int end; //msecs

        public SubtitleText(String text, int position, int start, int end) {
            this.text = text;
            this.position = position;
            this.start = start;
            this.end = end;
        }

        protected SubtitleText(Parcel in) {
            text = in.readString();
            position = in.readInt();
            start = in.readInt();
            end = in.readInt();
        }

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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(text);
            dest.writeInt(position);
            dest.writeInt(start);
            dest.writeInt(end);
        }
    }
}
