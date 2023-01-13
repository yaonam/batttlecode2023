package PoonPoon;

import java.util.Random;
import battlecode.common.*;

public class Base {
    // this class contains all reused variables and methods shared across all robot
    // classes
    static final Random rng = new Random(6147);
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static int wellSection = 3;

    public String initialMapQuadrant(RobotController rc, int starting_x_coord, int starting_y_coord) {
        String location;

        if (starting_x_coord == -1) {
            starting_x_coord = rc.getLocation().x;
            starting_y_coord = rc.getLocation().y;
        }
        // Pick a direction to build in. Dependent on location of HQ. We split the map
        // into quadrants. Build units towards the middle of the map.
        if (starting_x_coord <= rc.getMapWidth() / 2 && starting_y_coord >= rc.getMapHeight() / 2) {
            location = "top left";
        } else if (starting_x_coord > rc.getMapWidth() / 2 && starting_y_coord > rc.getMapHeight() / 2) {
            location = "top right";
        } else if (starting_x_coord < rc.getMapWidth() / 2 && starting_y_coord <= rc.getMapHeight() / 2) {
            location = "bottom left";
        } else {
            location = "bottom right";
        }

        return location;
    }

    public Direction initialDirection(RobotController rc, String quadrant) {
        // point robot towards the middle of the map
        Direction direction = null;
        switch (quadrant) {
            case "top left":
                direction = Direction.SOUTHEAST;
                break;
            case "top right":
                direction = Direction.SOUTHWEST;
                break;
            case "bottom left":
                direction = Direction.NORTHEAST;
                break;
            case "bottom right":
                direction = Direction.NORTHWEST;
                break;
        }

        return direction;
    }

    /**
     * getDirectionsTo() is a helper that returns the next optimal move given a
     * target destination.
     * Plan is to identify and randomly try move(s) that will decrease distance to
     * destination.
     * If all "optimal" spaces blocked, then move randomly.
     **/
    public static Direction getDirectionsTo(RobotController rc, MapLocation from, MapLocation to) {
        Direction approxDir = from.directionTo(to);

        int mood = rng.nextInt(3);

        switch (mood) {
            case 0:
                if (rc.canMove(approxDir)) {
                    return approxDir;
                }
            case 1:
                if (rc.canMove(approxDir.rotateRight())) {
                    return approxDir.rotateRight();
                }
            default: // case 2
                if (rc.canMove(approxDir.rotateLeft())) {
                    return approxDir.rotateLeft();
                }
        }

        return directions[rng.nextInt(8)];
    }
}
