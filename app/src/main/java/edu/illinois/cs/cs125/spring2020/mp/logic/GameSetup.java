package edu.illinois.cs.cs125.spring2020.mp.logic;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Provides static methods to convert game information to JSON payloads
 * that can be POSTed to the server's /games/create endpoint to create a multiplayer game.
 */
public class GameSetup {

    /** Constructor Method. */
    public GameSetup() {

    }

    /**
     * Creates a JSON object representing the configuration of a multiplayer area mode game.
     * Refer to our API documentation for the structure of the output JSON.
     * The configuration is valid if there is at least one invitee and a positive (larger than zero) cell size.
     * @param invitees all players involved in the game (never null)
     * @param area the area boundaries
     * @param cellSize the desired cell size in meters
     * @return a JSON object usable by the /games/create endpoint or null if the configuration is invalid
     */
    public static JsonObject areaMode(final List<Invitee> invitees,
                                      final LatLngBounds area,
                                      final int cellSize) {
        // at least one invitee for valid configuration
        if (invitees.size() < 1) {
            return null;
        }
        // cell size (int) must be positive for valid configuration
        if (cellSize < 1) {
            return null;
        }

        // complete information about the game
        JsonObject games = new JsonObject();
        // game mode property (String)
        games.addProperty("mode", "area");
        // cell size property (integer)
        games.addProperty("cellSize", cellSize);
        // add cell bounds as individual properties (doubles)
        games.addProperty("areaNorth", area.northeast.latitude);
        games.addProperty("areaEast", area.northeast.longitude);
        games.addProperty("areaSouth", area.southwest.latitude);
        games.addProperty("areaWest", area.southwest.longitude);

        // invitee info array (array of objects)
        JsonArray inviteeArray = new JsonArray();
        // loops through invitees list to extract relevant info
        for (Invitee i : invitees) {
            // create JSON object, representing one invitee
            JsonObject invitee = new JsonObject();
            // add email and team properties for the given invitee
            invitee.addProperty("email", i.getEmail());
            invitee.addProperty("team", i.getTeamId());
            // add completed invitee-JSON-object to the invitee info array
            inviteeArray.add(invitee);
        }
        // add invitee info array to games object
        games.add("invitees", inviteeArray);

        // return & check output
        System.out.println(games);
        return games;
    }

    /**
     * Creates a JSON object representing the configuration of a multiplayer target mode game.
     * Refer to our API documentation for the structure of the output JSON.
     * The configuration is valid if there is at least one invitee, at least one target,
     * and a positive (larger than zero) proximity threshold.
     * If the configuration is invalid, this function returns null.
     * @param invitees all players involved in the game (never null)
     * @param targets the positions of all targets (never null)
     * @param proximityThreshold the proximity threshold in meters
     * @return a JSON object usable by the /games/create endpoint or null if the configuration is invalid
     */
    public static JsonObject targetMode(final List<Invitee> invitees,
                                        final List<LatLng> targets,
                                        final int proximityThreshold) {
        // at least one invitee for valid configuration
        if (invitees.size() < 1) {
            return null;
        }
        // at least one target for valid configuration
        if (targets.size() < 1) {
            return null;
        }
        // proximity threshold (int) must be positive for valid configuration
        if (proximityThreshold < 1) {
            return null;
        }

        // complete information about the game
        JsonObject games = new JsonObject();
        // game mode property (String)
        games.addProperty("mode", "target");
        // proximity threshold property (integer)
        games.addProperty("proximityThreshold", proximityThreshold);

        // add target array (array of objects)
        JsonArray targetArray = new JsonArray();
        // loops through targets list to format relevant info into JSON object
        for (LatLng i : targets) {
            // create JSON object, representing one target
            JsonObject target = new JsonObject();
            // add latitude and longitude properties for the given target
            target.addProperty("latitude", i.latitude);
            target.addProperty("longitude", i.longitude);
            // add completed target-JSON-object to the target array
            targetArray.add(target);
        }
        // add target array to games object
        games.add("targets", targetArray);

        // invitee info array (array of objects)
        JsonArray inviteeArray = new JsonArray();
        // loops through invitees list to format relevant info into JSON object
        for (Invitee i : invitees) {
            // create JSON object, representing one invitee
            JsonObject invitee = new JsonObject();
            // add email and team properties for the given invitee
            invitee.addProperty("email", i.getEmail());
            invitee.addProperty("team", i.getTeamId());
            // add completed invitee-JSON-object to the invitee info array
            inviteeArray.add(invitee);
        }
        // add invitee info array to games object
        games.add("invitees", inviteeArray);

        // return & check output
        System.out.println(games);
        return games;
    }
}
