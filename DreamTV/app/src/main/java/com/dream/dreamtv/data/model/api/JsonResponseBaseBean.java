package com.dream.dreamtv.data.model.api;

/**
 * Created by gbogarin on 12/10/2015.
 * Clase base para el json que viene por response
 */
public class JsonResponseBaseBean<T> {
    public Boolean success;
    public T data;
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