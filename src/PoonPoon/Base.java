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
    final int initialRobotCount = 7;
    final int leadingZero = 70;
    int hqSection = 1;
    int quadSection = 5;
    int resourceSection = 8;
    int attackSection = 11;
    int wellSection = 15;
    int adamantiumIndex = resourceSection;
    int manaIndex = resourceSection + 1;
    int elixirIndex = resourceSection + 2;

    int adamantiumWellSection = wellSection + 4;
    int manaWellSection = adamantiumWellSection + 4;
    int elixirWellSection = manaWellSection + 4;
    int arrayLength = 63;

    int adamantiumID = 101;
    int manaID = 102;
    int elixirID = 103;

    int carrier_inventory = 40;
    int maxRobotCount;

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

    /** 
     * Move to an unoccupied quadrant if there are enough friendly units nearby. Otherwise, attempt to move randomly.
     */
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

        //if a certain amount of ally units are nearby, move towards target quadrant
        Direction dir = null;
        if (location != null && rc.getLocation().distanceSquaredTo(location) >= rc.getMapHeight() * quadRadiusFraction && scanForRobots(rc, "ally").length >= 2) {
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

    public RobotInfo[] scanForRobots(RobotController rc, String status) throws GameActionException {
        Team team = null;
        switch (status) {
            case "enemy":
                team = rc.getTeam().opponent();
                break;
            case "ally":
                team = rc.getTeam();
                break;
            default:
                team = rc.getTeam(); 
                break;
        }
        return rc.senseNearbyRobots(-1, team);
    }

    public void tryMoveTo(RobotController rc, MapLocation to) throws GameActionException {
        Direction dir = getDirectionsTo(rc, to);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    public void tryMoveTo(RobotController rc, Direction dir) throws GameActionException {
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

    /** Check for nearby enemies and attack the first unit within range. Ignores hq as attack target. 
     * Then, determine whether to chase or retreat.
    */
    public void attackEnemy(RobotController rc) throws GameActionException {
        RobotInfo[] enemies = scanForRobots(rc, "enemy");
        if (enemies.length > 0) {
            MapLocation targetLocation = enemies[0].location;
            for (RobotInfo enemy : enemies) {
                if (enemy.getType() != RobotType.HEADQUARTERS && rc.canAttack(enemy.getLocation())) {
                    // this means we detected an enemy unit besides their hq within attack range
                    targetLocation = enemy.location;
                    rc.setIndicatorString("ENEMY FOUND AT: " + targetLocation);
                    rc.attack(targetLocation);
                }
            }
            chaseOrRetreat(rc, targetLocation);
        }
    }

    /**  robot returns to nearby hq
     **/
    public MapLocation returnToHQ(RobotController rc) throws GameActionException{
        MapLocation hqLocation = coordIntToLocation(RobotPlayer.closestTargetCoord);
        if (RobotPlayer.closestTargetCoord == 0) {
            hqLocation = findClosestLocationFromArray(rc, hqSection, quadSection);
            rc.setIndicatorString("Returning to HQ at: " + (hqLocation));
        }
        rc.setIndicatorString("Returning to HQ at: " + (hqLocation));
        tryMoveTo(rc, hqLocation);
        return hqLocation;
    }

    /**  If robot is a launcher, and enemy numbers are not greater than ally numbers, chase enemy. 
     * If robot is not a launcher or enemy numbers are too great, retreat to nearby hq. 
     **/
    public void chaseOrRetreat (RobotController rc, MapLocation targetLocation) throws GameActionException {
        // if robot is a launcher, perform chase or retreat manuever, move randomly if they cannot move in that direction, avoid currents
        // if robot is a carrier, perform retreat maneuver
        RobotInfo[] allies = scanForRobots(rc, "ally");
        RobotInfo[] enemies = scanForRobots(rc, "enemy");

        if (allies.length > enemies.length && rc.getType() == RobotType.LAUNCHER) {
            Direction dir = rc.getLocation().directionTo(targetLocation);
            if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getCurrentDirection() != null) {
                rc.move(dir);
            } else {
                dir = getRandDirection(rc);
                if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getCurrentDirection() != null) {
                    rc.move(dir);
                    rc.setIndicatorString("CHASING");
                }
            }
        } else {
            MapLocation[] nearbyEnemyLocs = new MapLocation[enemies.length];
            for (int i = 0; i < enemies.length; i++)
                nearbyEnemyLocs[i] = enemies[i].location;
            MapLocation nearestEnemyLoc = findNearest(rc, nearbyEnemyLocs);
            rc.setIndicatorString("Evading enemy!");
            tryMoveTo(rc, rc.getLocation().directionTo(nearestEnemyLoc).opposite());
        }
    }

    public int locationToCoordInt(MapLocation location) {
        return (location.x * 100 + location.y);
    }

    public MapLocation coordIntToLocation(int coord) {
        return new MapLocation(coord / 100, coord % 100);
    }

    public void writeToCommsArray(RobotController rc, int index, int val) throws GameActionException {
        if (rc.canWriteSharedArray(index, val)) {
            rc.writeSharedArray(index, val);
        }
    }

    public MapLocation findClosestLocationFromArray(RobotController rc, int startIndex, int endIndex) throws GameActionException{
        MapLocation currentLocation = rc.getLocation();
        int distance = 3600;
        int newDistance = 3600;
        while (startIndex != endIndex) {
            newDistance = currentLocation.distanceSquaredTo(coordIntToLocation(rc.readSharedArray(startIndex)));
            if (distance > newDistance) {
                distance = newDistance;
                RobotPlayer.closestTargetCoord = rc.readSharedArray(startIndex);
            }
            startIndex++;
        }
        return coordIntToLocation(RobotPlayer.closestTargetCoord);
    }

    /**
     * Finds the first, closest MapLocation from the input array.
     * If empty, will return null.
     */
    public MapLocation findNearest(RobotController rc, MapLocation[] mapLocs) {
        MapLocation myLoc = rc.getLocation();
        MapLocation closestLoc = null;
        int closestDist = 0;
        for (MapLocation mapLoc : mapLocs) {
            int distTo = myLoc.distanceSquaredTo(mapLoc);
            if (closestDist == 0 || distTo < closestDist) {
                closestLoc = mapLoc;
                closestDist = distTo;
            }
        }
        return closestLoc;
    }

    /**
     * Finds the first, nearest well.
     * Returns null if none.
     */
    public MapLocation findNearestWell(RobotController rc) {
        WellInfo[] wells = rc.senseNearbyWells();
        MapLocation[] wellLocs = new MapLocation[wells.length];
        for (int i = 0; i < wells.length; i++)
            wellLocs[i] = wells[i].getMapLocation();
        return findNearest(rc, wellLocs);
    }
}
