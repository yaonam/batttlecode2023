package PoonPoon;

import java.util.Random;
import battlecode.common.*;

public class Base {
    // this class contains all reused variables and methods shared across all robot
    // classes
    final int quad1 = 7;
    final int quad2 = 1;
    final int quad3 = 5;
    final int quad4 = 3;
    int HQ_count_index = 63;
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

    public Integer initialMapQuadrant(RobotController rc) {
        int location;
        int starting_x_coord = rc.getLocation().x;
        int starting_y_coord = rc.getLocation().y;
        // Pick a direction to build in. Dependent on location of HQ. We split the map
        // into quadrants. Build units towards the middle of the map.
        if (starting_x_coord <= rc.getMapWidth() / 2 && starting_y_coord >= rc.getMapHeight() / 2) {
            location = quad1; // top left
        } else if (starting_x_coord > rc.getMapWidth() / 2 && starting_y_coord > rc.getMapHeight() / 2) {
            location = quad2; // top right
        } else if (starting_x_coord < rc.getMapWidth() / 2 && starting_y_coord <= rc.getMapHeight() / 2) {
            location = quad3; // bottom left
        } else {
            location = quad4; // bottom right
        }

        return location;
    }

    public Direction initialDirection(RobotController rc, int quadrant) {
        // point robot towards the middle of the map
        Direction direction = null;
        switch (quadrant) {
            case quad1:
                direction = Direction.SOUTHEAST;
                break;
            case quad2:
                direction = Direction.SOUTHWEST;
                break;
            case quad3:
                direction = Direction.NORTHEAST;
                break;
            case quad4:
                direction = Direction.NORTHWEST;
                break;
        }

        return direction;
    }

    /**
     * getDirectionsTo() is a helper that returns the next optimal move given a
     * target destination.
     * If all "optimal" spaces blocked, then move randomly.
     * Return CENTER if no possible moves.
     **/
    public static Direction getDirectionsTo(RobotController rc, MapLocation to) {
        Direction approxDir = rc.getLocation().directionTo(to);

        int mood = rng.nextInt(3);

        // Identify and randomly try move(s) that will decrease distance to destination.
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

        return getRandDirection(rc);
    }

    /**
     * getRandDirection() is a helper that returns a random, valid direction
     * to move towards. If no such move is possible, it returns Directions.CENTER
     **/
    public static Direction getRandDirection(RobotController rc) {
        // Cycle through directions, starting randomly
        int startingPoint = rng.nextInt(7);
        for (int i = startingPoint; i < 8; i++) {
            if (rc.canMove(directions[i])) {
                return directions[i];
            }
        }
        for (int i = 0; i < startingPoint; i++) {
            if (rc.canMove(directions[i])) {
                return directions[i];
            }
        }
        return Direction.CENTER;
    }

    /**
     * 
     */
    public static Direction getExploreDirection(RobotController rc) {
        // find nearby robots
        RobotInfo[] nearbyRobots;
        try {
            nearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
        } catch (GameActionException e) {
            return getRandDirection(rc);
        }

        // figure out "outermost" direction
        // avg all the locations of the nearby robots
        MapLocation thisLoc = rc.getLocation();
        MapLocation avgRobotLoc = rc.getLocation();
        for (int i = 0; i < nearbyRobots.length;)
            avgRobotLoc.add(thisLoc.directionTo(nearbyRobots[i].getLocation()));
        Direction awayDirection = avgRobotLoc.directionTo(thisLoc);

        // try to move that way
        return getDirectionsTo(rc, thisLoc.add(awayDirection));
    }

    // work in progress
    public int foundEnemyHQ(RobotController rc) throws GameActionException {
        int quadrant = 0;
        int enemyHQ = 0;
        RobotInfo[] enemies = rc.senseNearbyRobots();
        for (RobotInfo info : enemies) {
            if (info.getType() == RobotType.HEADQUARTERS) {
                if (rc.canWriteSharedArray(enemyHQ, quadrant)) {
                    rc.writeSharedArray(enemyHQ, quadrant);
                }
            }
        }
        return quadrant;
    }

    public void targetQuadrant(RobotController rc, int quadrant) throws GameActionException {

        MapLocation location = null;
        switch (quadrant) {
            case quad1:
                location = new MapLocation(rc.getMapWidth() / 4, rc.getMapHeight() - rc.getMapHeight() / 4);
                break;
            case quad2:
                location = new MapLocation(rc.getMapWidth() - rc.getMapWidth() / 4,
                        rc.getMapHeight() - rc.getMapHeight() / 4);
                break;
            case quad3:
                location = new MapLocation(rc.getMapWidth() / 4, rc.getMapHeight() / 4);
                break;
            case quad4:
                location = new MapLocation(rc.getMapWidth() - rc.getMapWidth() / 4, rc.getMapHeight() / 4);
                break;

        }

        Direction dir = null;
        if (rc.getLocation().distanceSquaredTo(location) >= 16) {
            dir = getDirectionsTo(rc, location);
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        } else {
            dir = directions[rng.nextInt(directions.length)];
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }
}
