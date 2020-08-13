package edu.illinois.cs.cs125.spring2020.mp.logic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static edu.illinois.cs.cs125.spring2020.mp.logic.GameStateID.ENDED;
import static edu.illinois.cs.cs125.spring2020.mp.logic.PlayerStateID.ACCEPTED;
import static edu.illinois.cs.cs125.spring2020.mp.logic.PlayerStateID.INVITED;
import static edu.illinois.cs.cs125.spring2020.mp.logic.PlayerStateID.PLAYING;

/**
 * Creates a game summary from JSON from the server.
 * Extracts summary information about a game from JSON provided by the server.
 * One GameSummary instance corresponds to one object from
 * the games array in the response from the server's /games endpoint.
 */

public class GameSummary {
    /** Instance variable representing the current state of the JSON game object. */
    private JsonObject jsonObject;

    /**
     * Constructor method for creating the grid object.
     * @param infoFromServer - one object from the array in the /games response
     */
    public GameSummary(final com.google.gson.JsonObject infoFromServer) {
        jsonObject = infoFromServer;
    }

    /**
     * Gets the unique, server-assigned ID of this game.
     * @return the game ID
     */
    public String getId() {
        return jsonObject.get("id").getAsString();
    }

    /**
     * Gets the mode of this game, either area or target.
     * @return the game mode
     */
    public String getMode() {
        return jsonObject.get("mode").getAsString();
    }

    /**
     * Gets the owner/creator of this game.
     * @return the email of the game's owner
     */
    public String getOwner() {
        return jsonObject.get("owner").getAsString();
    }

    /**
     * Gets the name of the user's team/role.
     * @param userEmail - the logged-in user's email
     * @param context - an Android context (for access to resources)
     * @return the human-readable team/role name of the user in this game
     */
    public String getPlayerRole(final String userEmail,
                                final android.content.Context context) {
        JsonArray players = jsonObject.get("players").getAsJsonArray();
        int team = TeamID.OBSERVER;
        String roleString = "";
        for (JsonElement i : players) {
            if (i.getAsJsonObject().get("email").getAsString().equals(userEmail)) {
                team = i.getAsJsonObject().get("team").getAsInt();
            }
        }
        if (team == TeamID.TEAM_RED) {
            roleString = "Red";
        } else if (team == TeamID.TEAM_YELLOW) {
            roleString = "Yellow";
        } else if (team == TeamID.TEAM_GREEN) {
            roleString = "Green";
        } else if (team == TeamID.TEAM_BLUE) {
            roleString = "Blue";
        } else if (team == TeamID.OBSERVER) {
            roleString = "Observer";
        }
        return roleString;
    }

    /**
     * Determines whether this game is an invitation to the user.
     * @param userEmail - the logged-in user's email
     * @return whether the user is invited to this game
     */
    public boolean isInvitation(final String userEmail) {
        JsonArray players = jsonObject.get("players").getAsJsonArray();
        boolean invited = false;
        // get the game state
        int gameState1 = jsonObject.get("state").getAsInt();
        if (gameState1 != ENDED) {
            for (JsonElement i : players) {
                if (i.getAsJsonObject().get("email").getAsString().equals(userEmail)) {
                    // get the player state
                    int playerState1 = i.getAsJsonObject().get("state").getAsInt();
                    if (playerState1 == INVITED) {
                        invited = true;
                    }
                }
            }
        }
        return invited;
    }

    /**
     * Determines whether the user is currently involved in this game.
     * For a game to be ongoing, it must not be over and the user must have accepted their invitation to it.
     * @param userEmail - the logged-in user's email
     * @return whether this game is ongoing for the user
     */
    public boolean isOngoing(final String userEmail) {
        JsonArray players = jsonObject.get("players").getAsJsonArray();
        boolean ongoing = false;
        // get the game state
        int gameState2 = jsonObject.get("state").getAsInt();
        if (gameState2 != ENDED) {
            for (JsonElement i : players) {
                if (i.getAsJsonObject().get("email").getAsString().equals(userEmail)) {
                    // get the player state
                    int playerState2 = i.getAsJsonObject().get("state").getAsInt();
                    if (playerState2 == ACCEPTED || playerState2 == PLAYING) {
                        ongoing = true;
                    }
                }
            }
        }
        return ongoing;
    }
}
