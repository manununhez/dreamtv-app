package com.manuelnunhez.dreamtv.data.networking;

import com.google.gson.Gson;
import com.manuelnunhez.dreamtv.data.model.Category;
import com.manuelnunhez.dreamtv.data.model.ErrorReason;
import com.manuelnunhez.dreamtv.data.model.Subtitle;
import com.manuelnunhez.dreamtv.data.model.Subtitle.SubtitleText;
import com.manuelnunhez.dreamtv.data.model.Task;
import com.manuelnunhez.dreamtv.data.model.TasksList;
import com.manuelnunhez.dreamtv.data.model.User;
import com.manuelnunhez.dreamtv.data.model.UserTask;
import com.manuelnunhez.dreamtv.data.model.UserTaskError;
import com.manuelnunhez.dreamtv.data.model.Video;
import com.manuelnunhez.dreamtv.data.model.VideoTest;
import com.manuelnunhez.dreamtv.data.model.VideoTopic;
import com.manuelnunhez.dreamtv.data.networking.model.ErrorReasonSchema;
import com.manuelnunhez.dreamtv.data.networking.model.SubtitleSchema;
import com.manuelnunhez.dreamtv.data.networking.model.SubtitleSchema.SubtitleTextSchema;
import com.manuelnunhez.dreamtv.data.networking.model.TaskSchema;
import com.manuelnunhez.dreamtv.data.networking.model.TasksListSchema;
import com.manuelnunhez.dreamtv.data.networking.model.UserSchema;
import com.manuelnunhez.dreamtv.data.networking.model.UserTaskErrorSchema;
import com.manuelnunhez.dreamtv.data.networking.model.UserTaskSchema;
import com.manuelnunhez.dreamtv.data.networking.model.VideoSchema;
import com.manuelnunhez.dreamtv.data.networking.model.VideoTestSchema;
import com.manuelnunhez.dreamtv.data.networking.model.VideoTopicSchema;

import java.util.ArrayList;
import java.util.List;

/*******************
 * DATA MAPPER - SCHEMA TO Model
 *******************/

class DataMapper {

    static User getUserFromSchema(UserSchema userSchema) {
        return new User(userSchema.email, userSchema.password, userSchema.subLanguage, userSchema.audioLanguage,
                userSchema.interfaceMode);
    }

    static VideoTopic[] getTopicFromSchema(VideoTopicSchema[] videoTopicSchemas) {
        VideoTopic[] videoTopics = new VideoTopic[videoTopicSchemas.length];

        for (int i = 0; i < videoTopicSchemas.length; i++) {
            videoTopics[i] = new VideoTopic(videoTopicSchemas[i].imageName, videoTopicSchemas[i].language,
                    videoTopicSchemas[i].name);
        }

        return videoTopics;
    }

    static TasksList[] getTasksListFromSchema(TasksListSchema[] tasksListSchemas) {
        TasksList[] tasksLists = new TasksList[tasksListSchemas.length];
        for (int i = 0; i < tasksListSchemas.length; i++) {
            tasksLists[i] = new TasksList(getTasksFromSchema(tasksListSchemas[i].tasks),
                    new Category(tasksListSchemas[i].category.orderIndex,
                            tasksListSchemas[i].category.name, tasksListSchemas[i].category.visible));
        }

        return tasksLists;
    }

    static ErrorReason[] getReasonsFromSchema(ErrorReasonSchema[] errorReasonSchemas) {
        ErrorReason[] errorReasons = new ErrorReason[errorReasonSchemas.length];

        for (int i = 0; i < errorReasonSchemas.length; i++) {
            errorReasons[i] = new ErrorReason(errorReasonSchemas[i].reasonCode, errorReasonSchemas[i].name,
                    errorReasonSchemas[i].description, errorReasonSchemas[i].language);
        }

        return errorReasons;
    }

    static Task[] getTasksFromSchema(TaskSchema[] taskSchemas) {
        Task[] tasks = new Task[taskSchemas.length];

        for (int i = 0; i < taskSchemas.length; i++) {
            tasks[i] = new Task(taskSchemas[i].taskId, taskSchemas[i].videoId, taskSchemas[i].videoTitleTranslated,
                    taskSchemas[i].videoDescriptionTranslated, taskSchemas[i].subLanguage, taskSchemas[i].type, getVideoFromSchema(taskSchemas[i].video),
                    getUserTasksFromSchema(taskSchemas[i].userTasks));
        }
        return tasks;
    }

    private static UserTask[] getUserTasksFromSchema(UserTaskSchema[] userTaskSchemas) {
        UserTask[] userTasks = new UserTask[userTaskSchemas.length];
        for (int i = 0; i < userTaskSchemas.length; i++) {
            userTasks[i] = getUserTaskFromSchema(userTaskSchemas[i]);
        }

        return userTasks;
    }

    static UserTask getUserTaskFromSchema(UserTaskSchema userTaskSchema) {
        return new UserTask(userTaskSchema.id, userTaskSchema.userId, userTaskSchema.taskId, userTaskSchema.completed,
                userTaskSchema.rating, userTaskSchema.timeWatched, userTaskSchema.subVersion, getUserTaskErrorsFromSchema(userTaskSchema.userTaskErrorList));
    }

    static UserTaskError[] getUserTaskErrorsFromSchema(UserTaskErrorSchema[] userTaskErrorSchemas) {
        UserTaskError[] userTaskErrors = new UserTaskError[userTaskErrorSchemas.length];

        for (int i = 0; i < userTaskErrorSchemas.length; i++) {
            userTaskErrors[i] = new UserTaskError(userTaskErrorSchemas[i].reasonCode,
                    userTaskErrorSchemas[i].subtitlePosition, userTaskErrorSchemas[i].comment);
        }

        return userTaskErrors;
    }

    static Video getVideoFromSchema(VideoSchema videoSchema) {
        return new Video(videoSchema.videoId, videoSchema.audioLanguage, videoSchema.speakerName,
                videoSchema.title, videoSchema.description, videoSchema.duration, videoSchema.thumbnail,
                videoSchema.team, videoSchema.project, videoSchema.videoUrl);
    }

    static Subtitle getSubtitleFromSchema(SubtitleSchema subtitleSchema) {
        return new Subtitle(subtitleSchema.versionNumber, getSubtitleTextFromSchema(subtitleSchema.subtitles), subtitleSchema.subFormat, subtitleSchema.videoTitleTranslated,
                subtitleSchema.videoDescriptionTranslated, subtitleSchema.videoTitleOriginal, subtitleSchema.videoDescriptionOriginal);
    }

    static List<SubtitleText> getSubtitleTextFromSchema(List<SubtitleTextSchema> subtitleTextSchema) {
        List<SubtitleText> subtitleTextList = new ArrayList<>();
        for (int i = 0; i < subtitleTextSchema.size(); i++) {
            SubtitleTextSchema textSchema = subtitleTextSchema.get(i);
            int subtitlePosition = i + 1;
            subtitleTextList.add(new SubtitleText(textSchema.getText(), subtitlePosition,
                    textSchema.getStart(), textSchema.getEnd()));
        }

        return subtitleTextList;
    }

    static VideoTest[] getVideoTestFromSchema(VideoTestSchema[] data) {
        VideoTest[] videoTests = new VideoTest[data.length];
        for (int i = 0; i < data.length; i++) {
            videoTests[i] = new VideoTest(data[i].id, data[i].videoId, data[i].subVersion,
                    data[i].subLanguage);
        }
        return videoTests;
    }


    static String getErrorReasonListSchemaFromModel(List<ErrorReason> errorReasonList) {
        List<ErrorReasonSchema> errorReasonSchemas = new ArrayList<>();
        for (ErrorReason errorReason : errorReasonList) {
            errorReasonSchemas.add(new ErrorReasonSchema(errorReason.getReasonCode(), errorReason.getName(),
                    errorReason.getDescription(),
                    errorReason.getLanguage()));
        }
        return new Gson().toJson(errorReasonSchemas);
    }

}
