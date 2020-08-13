package edu.illinois.cs.cs125.spring2020.mp.logic;

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neovisionaries.ws.client.WebSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cs125.spring2020.mp.R;

import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.OBSERVER;

// import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.OBSERVER;

/**
 * Represents an area mode game. Keeps track of cells and the player's most recent capture.
 * <p>
 * All these functions are stubs that you need to implement.
 * Feel free to add any private helper functions that would be useful.
 * See {@link TargetGame} for an example of how multiplayer games are handled.
 */
public final class AreaGame extends Game {
    // You will probably want some instance variables to keep track of the game state
    // (similar to the area mode gameplay logic you previously wrote in GameActivity)
    /** AreaDivider instance. */
    private AreaDivider ad;
    /** Stores cells and occupying player looked up by server ID. */
    private int[][] capturedCells;
    /** Map of player emails to their paths (visited cells). */
    private Map<String, List<int[]>> playerPaths = new HashMap<>();

    /**
     * Creates a game in area mode.
     * <p>
     * Loads the current game state from JSON into instance variables and populates the map
     * to show existing cell captures.
     * @param email the user's email
     * @param map the Google Maps control to render to
     * @param webSocket the websocket to send updates to
     * @param fullState the "full" update from the server
     * @param context the Android UI context
     */
    public AreaGame(final String email, final GoogleMap map, final WebSocket webSocket,
                    final JsonObject fullState, final Context context) {
        super(email, map, webSocket, fullState, context);

        /* Load grid information from server. */
        double north = fullState.get("areaNorth").getAsDouble();
        double south = fullState.get("areaSouth").getAsDouble();
        double east = fullState.get("areaEast").getAsDouble();
        double west = fullState.get("areaWest").getAsDouble();
        int cellSize = fullState.get("cellSize").getAsInt();

        /* Render the grid */
        ad = new AreaDivider(north, east, south, west, cellSize);
        ad.renderGrid(map);
        capturedCells = new int[ad.getYCells()][ad.getXCells()];

        /* Load cell information and update map */
        if (fullState.getAsJsonArray("cells").size() != 0) {
            int[] colors = getContext().getResources().getIntArray(R.array.team_colors);
            for (JsonElement c : fullState.getAsJsonArray("cells")) {
                JsonObject cell = c.getAsJsonObject();

                /* Load cell information */
                int x = cell.get("x").getAsInt();
                int y = cell.get("y").getAsInt();
                int team = cell.get("team").getAsInt();

                /* Update cell ownership */
                capturedCells[y][x] = team;

                /* Update the google map */
                int teamColor = colors[team];
                drawPolygon(cell.get("x").getAsInt(), cell.get("y").getAsInt(), teamColor);
            }
        }

        /* Load paths of all players */
        if (fullState.getAsJsonArray("players").size() != 0) {
            for (JsonElement p : fullState.getAsJsonArray("players")) {
                JsonObject player = p.getAsJsonObject(); // individual player in the players array
                String playerEmail = player.get("email").getAsString(); // identifying email

                /* Create an ArrayList to hold the cells visited by the player, in order */
                ArrayList<int[]> path = new ArrayList<>();
                playerPaths.put(playerEmail, path); // add path to map (key: email, value: path array)

                /* Visit each captured cell in the player's path */
                if (player.get("path").getAsJsonArray().size() != 0) {
                    for (JsonElement c : player.getAsJsonArray("path")) {
                        JsonObject cell = c.getAsJsonObject();

                        /* Store y and x: y in index 0, x in index 1 */
                        int y = cell.get("y").getAsInt();
                        int x = cell.get("x").getAsInt();
                        int[] coordinate = new int[]{y, x};

                        // Add coordinate to player path, using hash map lookup
                        path.add(coordinate);
                    }
                }
            }
        }
    }

    /**
     * Called when the user's location changes.
     * <p>
     * Area mode games detect whether the player is in an uncaptured cell. Capture is possible if
     * the player has no captures yet or if the cell shares a side with the previous cell captured by
     * the player. If capture occurs, a polygon with the team color is added to the cell on the map
     * and a cellCapture update is sent to the server.
     * @param location the player's most recently known location
     */
    @SuppressLint("MissingSuperCall")
    @Override
    public void locationUpdated(final LatLng location) {

        /* Load current information */
        super.locationUpdated(location);
        int team = super.getMyTeam();
        String playerEmail = super.getEmail();
        List<int[]> captures = playerPaths.get(playerEmail);

        /* Get current location */
        int x = ad.getXIndex(location);
        int y = ad.getYIndex(location);

        /* Proceed only if this location is within the bounds of the game */
        if (ad.getCellBounds(x, y) == null) {
            return;
        }

        /* Proceed only if this cell has not been captured */
        if (capturedCells[y][x] != OBSERVER) {
            return;
        }

        /* Get last visited location, if it exists */
        int lx = -1;
        int ly = -1;
        if (captures != null && captures.size() > 0) {
            lx = captures.get(captures.size() - 1)[1];
            ly = captures.get(captures.size() - 1)[0];
        }

        /* Proceed only if this cell shares a side with the last visited cell */
        if ((lx == -1 && ly == -1) || shareSide(lx, ly, x, y)) {

            /* Call a helper function to update the player's path, ownership, and map */
            captureUpdates(playerEmail, y, x, team);

            /* Send cell capture update */
            JsonObject update = new JsonObject();
            update.addProperty("type", "cellCapture");
            update.addProperty("x", x);
            update.addProperty("y", y);
            sendMessage(update);
        }
    }

    /**
     * draws polygon lol.
     * @param x cell row
     * @param y cell column
     * @param color color
     */
    private void drawPolygon(final int x, final int y, final int color) {
        LatLngBounds bounds = ad.getCellBounds(x, y);
        if (bounds != null) {
            List<LatLng> sq = new ArrayList<>();
            LatLng sw = bounds.southwest;
            LatLng ne = bounds.northeast;
            LatLng se = new LatLng(sw.latitude, ne.longitude);
            LatLng nw = new LatLng(ne.latitude, sw.longitude);
            sq.add(sw);
            sq.add(nw);
            sq.add(ne);
            sq.add(se);
            sq.add(sw);
            PolygonOptions po = new PolygonOptions();
            po.addAll(sq);
            po.fillColor(color);
            super.getMap().addPolygon(po);
        }
    }

    /**
     * helper function that checks two locations share a cell side.
     * @param x1 first x
     * @param y1 first y
     * @param x2 second x
     * @param y2 second y
     * @return whether or not side is shared
     */
    private boolean shareSide(final int x1, final int y1, final int x2, final int y2) {
        if (x1 == x2) {
            if (y1 == y2 + 1 || y1 == y2 - 1) {
                return true;
            }
        } else if (y1 == y2) {
            if (x1 == x2 + 1 || x1 == x2 - 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Processes an update from the server.
     * <p>
     * Since playerCellCapture events are specific to area mode games, this function handles those
     * by placing a polygon of the capturing player's team color on the newly captured cell and
     * recording the cell's new owning team.
     * All other message types are delegated to the superclass.
     * @param message JSON from the server (the "type" property indicates the update type)
     * @return whether the message type was recognized
     */
    @SuppressLint("MissingSuperCall")
    @Override
    public boolean handleMessage(final JsonObject message) {
        // Some messages are common to all games - see if the superclass can handle it
        if (super.handleMessage(message)) {
            // If it took care of the update, this class's implementation doesn't need to do anything
            // Inform the caller that the update was handled
            return true;
        }

        // Check the type of update to see if we can handle it and what to do
        if (message.get("type").getAsString().equals("playerCellCapture")) {
            // Got an update indicating that another player captured a cell
            // Load the information from the JSON
            String playerEmail = message.get("email").getAsString();
            int team = message.get("team").getAsInt();
            int x = message.get("x").getAsInt();
            int y = message.get("y").getAsInt();

        // You need to use that information to update the game state and map
            /* Call a helper function to update the player's path, ownership, and map */
            captureUpdates(playerEmail, y, x, team);

            /* Once that's done, inform the caller that we handled the message */
            return true;
        } else {
            // An unknown type of update was received - inform the caller of the situation
            return false;
        }
    }

    /**
     * Adds a cell to a player's path.
     * <p>
     * Updates the game state (the player's path list in playerPaths) and adds a polygon to the map
     * <p>
     * @param email email of the player who just visited the cell
     * @param team the player's team ID
     * @param y y index of the cell
     * @param x x index of the cell
     */
    private void captureUpdates(final String email, final int y, final int x, final int team) {
        // Get the specified player's path from the players/paths map
        List<int[]> path = playerPaths.get(email);

        /* Update captured cell's team ownership */
        capturedCells[y][x] = team;

        /* Update user's path */
        int[] newCoordinate = new int[]{y, x};
        path.add(newCoordinate);

        /* Update map */
        int[] colors = getContext().getResources().getIntArray(R.array.team_colors);
        int teamColor = colors[team];
        drawPolygon(x, y, teamColor);
    }

    /**
     * Gets a team's score in this area mode game.
     * @param teamId the team ID
     * @return the number of cells owned by the team
     */
    @Override
    public int getTeamScore(final int teamId) {
        // Find how many cell are currently owned by the specified team
        // Iterate through captured cells array
        int total = 0;
        for (int i = 0; i < capturedCells.length; i++) {
            for (int j = 0; j < capturedCells[i].length; j++) {
                if (capturedCells[i][j] == teamId) {
                    total++;
                }
            }
        }
        return total;
    }

}
