package com.dream.dreamtv.beans;

/**
 * Created by gbogarin on 12/10/2015.
 * Clase base para el json que viene por response
 */
public class JsonResponseBaseBean<T> {
    public Boolean success;
    public T data;

    @Override
    public String toString() {
        return "JsonResponseBaseBean{" +
                "success=" + success +
                ", data=" + data +
                '}';
    }
}