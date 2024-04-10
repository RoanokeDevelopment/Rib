package dev.roanoke.rib.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.roanoke.rib.Rib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class CobblemonTools {
    public static GymTeam getTeamById(String teamID) {

        String url = Rib.config.getCobblemonToolsURL() + "api/v1/teams/" + teamID;
        Rib.LOGGER.info("Trying to get Cobblemon Tools Team from URL: " + url);

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            con.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();
                    return response.toString();
                } else {
                    throw new IOException("GET request failed, response code: " + responseCode);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });


// Wait for the request to complete and get the response string
        String responseString = future.join();
        System.out.println("Response: " + responseString);

// Parse the response JSON data
        JsonObject responseJson = JsonParser.parseString(responseString).getAsJsonObject();
        if (responseJson.get("name").getAsString() != null) {
            GymTeam gymTeam = Rib.gymManager.readTeamFromJson(responseJson);
            return gymTeam;
        } else {
            Rib.LOGGER.info("Response Json didn't have name key");
            return null;
        }


    }
}
