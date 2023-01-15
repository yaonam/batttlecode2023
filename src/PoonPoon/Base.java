package PoonPoon;

import java.util.Random;
import battlecode.common.*;

public abstract class Base {
    // this class contains all reused variables and methods shared across all robot
    // classes
    final int quad1 = 7;
    final int quad2 = 1;
    final int quad3 = 5;
    final int quad4 = 3;
    int hq_section_index = 0;
    int quad_section = 9;
    int resource_section = 12;
    int adamantiumIndex = resource_section;
    int manaIndex = resource_section + 1;
    int elixirIndex = resource_section + 2;
    int attack_location_section = 15;
    int well_section = 23;
    int arrayLength = 63;

    int hq_section_increment = 2;
    int well_section_increment = 3;
    int attack_section_increment = 2;

    int adamantiumID = 1;
    int manaID = 2;
    int elixirID = 3;

    int carrier_inventory = 40;

    int quadRadiusFraction = 3 / 16;
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

    public abstract void run(RobotController rc) throws GameActionException;

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
     * getExploreDirection() gives a direction that tries to point away from
     * other robots on your team.
     */
    public static Direction getExploreDirection(RobotController rc) throws GameActionException {
        // find nearby robots
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam());

        // figure out "outermost" direction
        // avg all the locations of the nearby robots
        MapLocation thisLoc = rc.getLocation();
        MapLocation avgRobotLoc = rc.getLocation();
        for (int i = 0; i < nearbyRobots.length; i++)
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
        if (location != null
                && rc.getLocation().distanceSquaredTo(location) >= rc.getMapHeight() * quadRadiusFraction) {
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

    public int iterateArray(RobotController rc, int startIndex, int loopCount, int increment)
            throws GameActionException {
        startIndex += loopCount * increment;
        return startIndex;
    }

    public RobotInfo[] scanForRobots(RobotController rc) throws GameActionException {
        Team opponent = rc.getTeam().opponent();
        return rc.senseNearbyRobots(-1, opponent);
    }

    public void moveToLocation(RobotController rc, MapLocation to) throws GameActionException {
        Direction dir = getDirectionsTo(rc, to);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    public int convertResourcetoInt(ResourceType type) {
        int id = 0;
        switch (type) {
            case ADAMANTIUM:
                id = adamantiumID;
                break;
            case MANA:
                id = manaID;
                break;
            case ELIXIR:
                id = elixirID;
                break;
            default:
                break;
        }
        return id;
    }
}
