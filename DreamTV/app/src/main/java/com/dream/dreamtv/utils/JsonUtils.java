package com.dream.dreamtv.utils;

import com.dream.dreamtv.data.networking.model.JsonResponseBaseBean;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import timber.log.Timber;


/**
 * Created by gbogarin on 12/10/2015.
 * Clase para trabajar con JSON
 */
public class JsonUtils {

    private JsonUtils() {
    }

    /**
     * Genera un String con estructura JSON a partir de los parámetros dados.
     *
     * @param <T>      La clase bean
     * @param jsonBean La clase a ser serializada
     * @return Un String con estructura JSON
     */
    public static <T> String getJsonRequest(
            T jsonBean) {

        Gson gson = new Gson();
        String jsonRequest = gson.toJson(jsonBean);
        Timber.d("JsonRequest generado: %s", jsonRequest);

        return jsonRequest;
    }

    /**
     * Genera un {@link JsonResponseBaseBean} a partir de un String con estructura JSON.
     *
     * @param jsonResponse Un String con estructura JSON
     * @param <T>          La clase que representa al JSON
     * @return Un {@link JsonResponseBaseBean}
     */
    @SuppressWarnings("unchecked")
    public static <T> JsonResponseBaseBean getJsonResponse(
            String jsonResponse, TypeToken<JsonResponseBaseBean<T>> typeToken) {
        Gson gson = new Gson();
        JsonResponseBaseBean<T> jsonObject;
        try {
            jsonObject = gson.fromJson(jsonResponse, typeToken.getType());
        } catch (JsonSyntaxException e) {
            Timber.d("No se pudo serializar el json: %s", e.getMessage());
            jsonObject = new JsonResponseBaseBean();
            jsonObject.success = false;
        }
        Timber.d("JsonResponse generado: %s", jsonObject.toString());

        return jsonObject;
    }

    /**
     * Genera un {@link JsonResponseBaseBean} a partir de un String con estructura JSON.
     *
     * @param jsonResponse Un String con estructura JSON
     * @param <T>          La clase que representa al JSON
     * @param logResponse  true si quiere imprimir el json response generado
     * @return Un {@link JsonResponseBaseBean}
     */
    @SuppressWarnings("unchecked")
    public static <T> JsonResponseBaseBean getJsonResponse(
            String jsonResponse, TypeToken<JsonResponseBaseBean<T>> typeToken, boolean logResponse) {
        Gson gson = new Gson();
        JsonResponseBaseBean<T> jsonObject;
        try {
            jsonObject = gson.fromJson(jsonResponse, typeToken.getType());
        } catch (JsonSyntaxException e) {
            Timber.d("No se pudo serializar el json: %s", e.getMessage());
            jsonObject = new JsonResponseBaseBean();
            jsonObject.success = false;
        }

        if (logResponse)
            Timber.d("JsonResponse generado: %s", jsonObject.toString());

        return jsonObject;
    }
}
