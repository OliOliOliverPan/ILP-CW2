package uk.ac.ed.inf;

/**
 * Enum containing 16 compass directions with their corresponding angle
 */
public enum Direction {


    East(0),
    North(90),
    West(180),
    South(270),
    NorthEast(45),
    NorthWest(135),
    SouthWest(225),
    SouthEast(315),
    EastNorthEast(22.5),
    NorthNorthEast(67.5),
    NorthNorthWest(112.5),
    WestNorthWest(157.5),
    WestSouthWest(202.5),
    SouthSouthWest(247.5),
    SouthSouthEast(292.5),
    EastSouthEast(337.5);

    private final double ANGLE;

    Direction(double angle) {
        this.ANGLE = angle;
    }

    double getAngle(){
        return this.ANGLE;
    }





}
