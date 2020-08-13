package edu.illinois.cs.cs125.spring2020.mp.logic;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import static android.graphics.Color.BLACK;

/**
 * Represents grid objects that are configured to have roughly square, identically sized cells.
 * <p>
 * Class methods:
 * getCellBounds: gets cardinal boundaries, based on cartesian coordinates, given a cell index.
 * getXCells: gets number of cells between the west and east boundaries.
 * getXIndex: gets the X index of the cell containing a specified location.
 * getYCells: gets number of cells between the south and north boundaries.
 * getYIndex: gets the Y index of the cell containing a specified location.
 */
public class AreaDivider {
    // initialize private variables to hold values that are to be set through the constructor
    /** North boundary of the grid. */
    private double north;
    /** East boundary of the grid. */
    private double east;
    /** South boundary of the grid. */
    private double south;
    /** West boundary of the grid. */
    private double west;
    /** Desired cell size. */
    private int cellSize;

    /**
     * Constructor method for creating the grid object.
     * @param setNorth the maximum latitude of the grid
     * @param setEast the maximum longitude of the grid
     * @param setSouth the minimum latitude of the grid
     * @param setWest the minimum longitude of the grid
     * @param setCellSize the desired side lengths of cells in the grid
     */
    public AreaDivider(final double setNorth,
                       final double setEast,
                       final double setSouth,
                       final double setWest,
                       final int setCellSize) {
        north = setNorth;
        east = setEast;
        south = setSouth;
        west = setWest;
        cellSize = setCellSize;
    }

    /**
     * checks whether or not the configuration provided to the constructor is valid.
     * @return configuration valid or not
     */
    public boolean isValid() {
        if (north <= south) {
            return false;
        }
        if (east <= west) {
            return false;
        }
        return cellSize > 0;
    }

    /**
     * HELPER FUNCTION: gets the distance of the longest side of a specified grid
     * in both the X and Y directions .
     * @return double[] of {X, Y} distances
     */
    public double[] getGridDistances() {
        // create LatLng objects for corners
            // Southwest corner of the grid.
        LatLng sw = new LatLng(south, west);
            // Northwest corner of the grid.
        LatLng nw = new LatLng(north, west);
            // Northeast corner of the grid.
        LatLng ne = new LatLng(north, east);

        // store corner objects in a LatLng[] array
        LatLng[] gridCorners = {sw, nw, ne};

        // compute and store X [0] and Y [1] distances
        double[] gridDistances = new double[2];
        gridDistances[0] = LatLngUtils.distance(gridCorners[1], gridCorners[2]);
        gridDistances[1] = LatLngUtils.distance(gridCorners[0], gridCorners[1]);
        return gridDistances;
    }

    /**
     * HELPER FUNCTION: gets the unidirectional distance from the southwest corner of the specified grid.
     * @param component calculate distance in the X or Y direction
     * @param location LatLng object denoting the coordinates of the point of interest
     * @return one component of the distance between a point and and the southwest corner ("origin")
     */
    public double getUniDirection(final char component, final com.google.android.gms.maps.model.LatLng location) {
        // initialize variable to return
        double distance = 0;

        // gets distance in the X direction
        if (component == 'x') {
            distance = LatLngUtils.distance(south, location.longitude,
                                            south, west);
        }
        // get distance in the Y direction
        if (component == 'y') {
            distance = LatLngUtils.distance(location.latitude, west,
                                            south, west);
        }
        return distance;
    }


    /**
     * gets number of cells between the west and east boundaries.
     * @return the number of cells in the X direction
     */
    public int getXCells() {
        // get distance in the X direction
        double xDistance = this.getGridDistances()[0];
        // round to nearest integer greater than the ratio of distance to cell size
        return (int) Math.ceil(xDistance / (double) cellSize);
    }

    /**
     * gets number of cells between the west and east boundaries.
     * @return the number of cells in the Y direction
     */
    public int getYCells() {
        // get distance in the Y direction
        double yDistance = this.getGridDistances()[1];
        // round to nearest integer greater than the ratio of distance to cell size
        return (int) Math.ceil(yDistance / (double) cellSize);
    }

    /**
     * gets the X index of the cell containing a specified location.
     * @param location the location
     * @return the X index of the cell containing the lat-long point
     */
    public int getXIndex(final com.google.android.gms.maps.model.LatLng location) {
        if (location.longitude >= west && location.longitude <= east) {
            // get number of cells and distance in X direction (as an int)
            double xDistance = this.getGridDistances()[0];
            int numXCells = this.getXCells();

            // get interval between boundaries for cells in the X direction
            double interval = xDistance / numXCells;

            // bin the location longitude into an X interval
            if (interval >= 1) {
                return (int) Math.floor(getUniDirection('x', location) / interval);
            } else if (interval > 0 && interval < 1) {
                return (int) Math.floor(getUniDirection('y', location) * interval);
            }
        }
        return -1;
    }

    /**
     * gets the Y index of the cell containing a specified location.
     * @param location the location
     * @return the Y index of the cell containing the lat-long point
     */
    public int getYIndex(final com.google.android.gms.maps.model.LatLng location) {
        if (location.latitude >= south && location.latitude <= north) {
            // get number of cells and distance in Y direction (as an int)
            double yDistance = this.getGridDistances()[1];
            int numYCells = this.getYCells();

            // get interval between boundaries for cells in the Y direction
            double interval = yDistance / numYCells;

            // bin the location latitude into an Y interval
            if (interval >= 1) {
                return (int) Math.floor(getUniDirection('y', location) / interval);
            } else if (interval > 0 && interval < 1) {
                return (int) Math.floor(getUniDirection('y', location) * interval);
            }
        }
        return -1;
    }

    /**
     * HELPER FUNCTION: generates grid X and Y boundaries in the specified grid.
     * @return 2D array {2}{X, Y} of X and Y boundaries
     */
    public double[][] getGridBounds() {
        // get number of cells and distance in X direction
        double xRange = east - west;
        int numXCells = this.getXCells();

        // get number of cells and distance in Y direction
        double yRange = north - south;
        int numYCells = this.getYCells();

        // generate boundaries for cells in the X direction
        double xInterval = xRange / (double) numXCells;
        double[] xBounds = new double[numXCells + 1];
        for (int i = 0; i < xBounds.length; i++) {
            xBounds[i] = west + xInterval * i;
        }

        // generate boundaries for cells in the Y direction
        double yInterval = yRange / (double) numYCells;
        double[] yBounds = new double[numYCells + 1];
        for (int i = 0; i < yBounds.length; i++) {
            yBounds[i] = south + yInterval * i;
        }

        // format and return output
        double[][] gridBounds = new double[2][];
        gridBounds[0] = xBounds;
        gridBounds[1] = yBounds;
        return gridBounds;
    }

    /**
     * Gets the boundaries of the specified cell as a Google Maps LatLngBounds object.
     * @param x the cell's X coordinate
     * @param y the cell's Y coordinate
     * @return the boundaries of the cell
     */
    public com.google.android.gms.maps.model.LatLngBounds getCellBounds(final int x,
                                                                        final int y) {
        if (x > -1 && y > -1) {
            // get grid boundaries
            double[][] gridBounds = this.getGridBounds();

            // get west and east boundaries (X direction / longitude)
            double westBound = gridBounds[0][x];
            double eastBound = gridBounds[0][x + 1];

            // get south and north boundaries (Y direction / latitude)
            double southBound = gridBounds[1][y];
            double northBound = gridBounds[1][y + 1];

            // construct LatLng object for southwest & northeast corners of the specified cell
            LatLng southWest = new LatLng(southBound, westBound);
            LatLng northEast = new LatLng(northBound, eastBound);

            // return LatLngBounds object for the cell boundaries
            return new LatLngBounds(southWest, northEast);
        }
        return null;
    }

    /**
     * HELPER FUNCTION: adds a colored line to a Google map.
     * @param map the Google map to draw on
     * @param start position of one endpoint of the line
     * @param end position of the other endpoint of the line
     * @param color the color to fill the line with
     */
    public void addLine(final com.google.android.gms.maps.GoogleMap map,
                        final LatLng start, final LatLng end, final int color) {
//        // Package the loose coordinates into LatLng objects usable by Google Maps
//        LatLng start = new LatLng(startLat, startLng);
//        LatLng end = new LatLng(endLat, endLng);

        // Configure and add a colored line
        final int lineThickness = 12;
        PolylineOptions fill = new PolylineOptions().add(start, end).color(color).width(lineThickness).zIndex(1);
        map.addPolyline(fill);
    }

    /**
     * draws the grid to a map using solid black polylines.
     * @param map the Google map to draw on
     */
    public void renderGrid(final com.google.android.gms.maps.GoogleMap map) {
        // get grid boundaries
        // [0][1]
        // [x][y]
        // [longitude][latitude]
        double[][] gridBounds = this.getGridBounds();

        // draw lines in the X direction
        for (int i = 0; i < gridBounds[1].length; i++) {

            // package coordinates into LatLng objects
            LatLng start = new LatLng(gridBounds[1][i], gridBounds[0][0]);
            LatLng end = new LatLng(gridBounds[1][i], gridBounds[0][gridBounds[0].length - 1]);

            this.addLine(map,
                    // latitude += interval, longitude = fixed range
                    start, end, BLACK);

        }

        // draw lines in the Y direction
        for (int i = 0; i < gridBounds[0].length; i++) {

            // package coordinates into LatLng objects
            LatLng start = new LatLng(gridBounds[1][0], gridBounds[0][i]);
            LatLng end = new LatLng(gridBounds[1][gridBounds[1].length - 1], gridBounds[0][i]);

            this.addLine(map,
                    // longitude += interval, latitude = fixed range
                    start, end, BLACK);
        }
    }
}
