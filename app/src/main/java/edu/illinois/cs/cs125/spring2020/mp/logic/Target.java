package edu.illinois.cs.cs125.spring2020.mp.logic;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.OBSERVER;
import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.TEAM_BLUE;
import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.TEAM_GREEN;
import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.TEAM_RED;
import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.TEAM_YELLOW;

/**
 * Represents a target in an ongoing target-mode game and manages the marker displaying it.
 */
public class Target {

    /** A reference to the map control. */
    private GoogleMap map;

    /** Store Google Maps Marker options. */
    private MarkerOptions options;

    /** Store a Google Maps Marker. */
    private Marker marker;

    /** Store target position. */
    private LatLng position;

    /** Store teamID of team currently owning the target. */
    private int team;

//    /** Red team marker color. */
//    private static final float RED_COLOR = BitmapDescriptorFactory.HUE_RED;
//
//    /** Yellow team marker color. */
//    private static final float YELLOW_COLOR = BitmapDescriptorFactory.HUE_YELLOW;
//
//    /** Green team marker color. */
//    private static final float GREEN_COLOR = BitmapDescriptorFactory.HUE_GREEN;
//
//    /** Blue team marker color. */
//    private static final float BLUE_COLOR = BitmapDescriptorFactory.HUE_BLUE;
//
//    /** Unclaimed, default team marker color. */
//    private static final float DEFAULT_COLOR = BitmapDescriptorFactory.HUE_VIOLET;

    /** Store target marker options. */
    private BitmapDescriptor icon;

    /**
     * Constructor method for creating a target.
     * @param setMap the map to render to
     * @param setPosition the position of the target
     * @param setTeam the TeamID code of the team currently owning the target
     */
    public Target(final GoogleMap setMap, final LatLng setPosition, final int setTeam) {

        // Set private fields to passed objects
        map = setMap;
        position = setPosition;
        team = setTeam;

        // Add marker to map
        options = new MarkerOptions().position(position);
        marker = map.addMarker(options);

        // Update marker color upon creation to reflect currently owning team
        if (setTeam == TEAM_RED) {
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        } else if (setTeam == TEAM_YELLOW) {
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
        } else if (setTeam == TEAM_GREEN) {
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        } else if (setTeam == TEAM_BLUE) {
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        } else if (setTeam == OBSERVER) {
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
        }
        marker.setIcon(icon);
    }

    /**
     * Gets the position of the target.
     * @return the coordinates of the target
     */
    public LatLng getPosition() {
        return position;
    }

    /**
     * Gets the ID of the team currently owning this target.
     * @return the owning team ID or OBSERVER if unclaimed
     */
    public int getTeam() {
        return team;
    }

    /**
     * Updates the owning team of this target and updates the hue of the marker to match.
     * @param newTeam the ID of the team that captured the target
     */
    public void setTeam(final int newTeam) {

        // Update team ID
        team = newTeam;

        // Update marker color to reflect new owning team
        if (newTeam == TEAM_RED) {
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        } else if (newTeam == TEAM_YELLOW) {
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
        } else if (newTeam == TEAM_GREEN) {
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        } else if (newTeam == TEAM_BLUE) {
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        } else if (newTeam == OBSERVER) {
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
        }
        marker.setIcon(icon);
    }
}
