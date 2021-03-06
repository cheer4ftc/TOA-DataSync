package org.theorangealliance.datasync.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import org.theorangealliance.datasync.json.ErrorJSON;
import org.theorangealliance.datasync.logging.TOALogger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Kyle Flynn on 11/28/2017.
 */
public class TOAEndpoint implements Runnable {

    private final String BASE_URL = "https://theorangealliance.org/apiv2/";
//    private final String BASE_URL = "http://localhost:8080/apiv2/";
    private String endpoint;
    private String requestType;
    private String apiKey;
    private String eventKey;
    private String bodyJSON;

    private Thread connection;
    private TOACompleteListener completeListener;

    public TOAEndpoint(String requestType, String endpoint) {
        this.requestType = requestType;
        this.endpoint = BASE_URL + endpoint;
        this.connection = new Thread(this, endpoint);
        this.apiKey = "";
        this.eventKey = "";
        this.bodyJSON = "";
    }

    public TOAEndpoint(String endpoint) {
        this("GET", endpoint);
    }

    public void setCredentials(String apiKey, String eventKey) {
        this.apiKey = apiKey;
        this.eventKey = eventKey;
    }

    public void setBody(TOARequestBody requestBody) {
        this.bodyJSON = getGson().toJson(requestBody);
    }

    public void execute(TOACompleteListener completeListener) {
        this.connection.start();
        this.completeListener = completeListener;
    }

    @Override
    public void run() {
            try {
                URL url = new URL(this.endpoint);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod(this.requestType);
                con.setRequestProperty("X-Application-Origin", "TOA-DataSync");
                con.setRequestProperty("X-TOA-Key", this.apiKey);
                con.setRequestProperty("X-TOA-Event", this.eventKey);

                if (bodyJSON.length() > 1) {
                    TOALogger.log(Level.INFO, "Making " + requestType + " with body " + bodyJSON);
                    con.setRequestProperty("Content-Type","application/json");
                    con.setDoOutput(true);
                    DataOutputStream stream = new DataOutputStream(con.getOutputStream());
                    stream.writeBytes(bodyJSON);
                    stream.flush();
                    stream.close();
                } else {
                    TOALogger.log(Level.INFO, "Making " + requestType + " to URL " + url.toString());
                }

                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    Platform.runLater(() -> {
                        if (this.completeListener != null) {
                            this.completeListener.onComplete(response.toString(), true);
                        }
                    });
                } else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    ErrorJSON error = getGson().fromJson(response.toString(), ErrorJSON.class);
                    TOALogger.log(Level.WARNING, "URL " + error.getUrl() + " returned " + error.getStatus() + ": " + error.getCode());

                    Platform.runLater(() -> {
                        if (this.completeListener != null) {
                            this.completeListener.onComplete(error.getStatus() + ": " + error.getCode(), false);
                        }
                    });
                }
            } catch (IOException e) {
                TOALogger.log(Level.WARNING, e.getMessage());
                Platform.runLater(() -> this.completeListener.onComplete(e.getLocalizedMessage(), false));
            }
    }

    public Gson getGson() {
        return new GsonBuilder().serializeNulls().create();
    }

    public interface TOACompleteListener {
        void onComplete(String response, boolean success);
    }

}
