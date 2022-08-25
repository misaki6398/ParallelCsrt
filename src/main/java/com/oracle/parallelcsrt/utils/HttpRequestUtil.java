package com.oracle.parallelcsrt.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

import com.oracle.parallelcsrt.models.HttpResponseModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpRequestUtil {
    private static final Logger logger = LogManager.getLogger(HttpRequestUtil.class);

    public static HttpResponseModel post(String targetUrl) throws IOException {
        URL url = new URL(null, targetUrl, new sun.net.www.protocol.https.Handler());
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        // connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        String responseLine = "";
        StringBuilder response;
        connection.connect();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader((connection.getInputStream()), StandardCharsets.UTF_8))) {

            response = new StringBuilder();
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine);
                response.append(System.lineSeparator());
            }
        }
        HttpResponseModel responseModel = new HttpResponseModel();
        responseModel.setResponseString(response.toString());
        responseModel.setStatusCode(connection.getResponseCode());
        connection.disconnect();
        return responseModel;

    }

    public static HttpResponseModel post(String targetUrl, String jsonString) throws IOException {
        URL url = new URL(null, targetUrl,  new sun.net.www.protocol.https.Handler());
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "basic ZmNjbWFkbW46cGFzc3dvcmQx");
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        String responseLine = "";
        StringBuilder response;

        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        }

        connection.connect();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader((connection.getInputStream()), StandardCharsets.UTF_8))) {

            response = new StringBuilder();
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine);
                response.append(System.lineSeparator());
            }
        }
        HttpResponseModel responseModel = new HttpResponseModel();
        responseModel.setResponseString(response.toString());
        responseModel.setStatusCode(connection.getResponseCode());
        connection.disconnect();
        return responseModel;

    }


    /**
     * Check web api response json is valid or not and check response code is 200 ok
     * 
     * @param functionName
     * @param response
     * @return
     */
    public static boolean checkResponseOk(String functionName, HttpResponseModel response) {
        if (response.getResponseString().equals("")) {
            logger.error(String.format("%s response Json was blank, might be caused error", functionName));
            return false;
        }
        if (JsonUtil.isJsonValid(response.getResponseString()) && response.getStatusCode() == 200) {
            logger.debug(String.format("%s response is Valid", functionName));
            logger.debug(String.format("StatusCode: %s, response: %s", response.getStatusCode(),
                    response.getResponseString()));
            return true;
        } else {
            logger.error("%s response Json was not valid please check", functionName);
            logger.error(String.format("StatusCode: %s, response: %s", response.getStatusCode(),
                    response.getResponseString()));
            return false;
        }
    }
}
