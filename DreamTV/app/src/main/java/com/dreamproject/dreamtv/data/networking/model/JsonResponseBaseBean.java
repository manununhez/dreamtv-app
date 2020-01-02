package com.dreamproject.dreamtv.data.networking.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by gbogarin on 12/10/2015.
 * Clase base para el json que viene por response
 */
public class JsonResponseBaseBean<T> {
    @SerializedName("success")
    public Boolean success;
    @SerializedName("data")
    public T data;
    @SerializedName("message")
    public String message;

    @Override
    public String toString() {
        return "JsonResponseBaseBean{" +
                "success=" + success +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}