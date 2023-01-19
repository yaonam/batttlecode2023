package PoonPoonv4;

import java.util.ArrayList;
import java.util.Random;
import battlecode.common.*;

public abstract class Base {
    // TODO: add comments @Bethel
    final int quad1 = 1;
    final int quad2 = 2;
    final int quad3 = 3;
    final int quad4 = 4;
    final int initialRobotCount = 7;
    Direction currentdirection = null;
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

    int adamantiumID = 101;
    int manaID = 102;
    int elixirID = 103;

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

    MapLocation locHistory = null;

    public abstract void run(RobotController rc) throws GameActionException;

    /**
     * Updates locHistory by a weighted avg
     */
    public void recordLoc(RobotController rc) {
        MapLocation myLoc = rc.getLocation();
        if (locHistory == null) {
            locHistory = myLoc;
        } else {
            int newX = (int) (0.3 * locHistory.x + 0.7 * myLoc.x);
            int newY = (int) (0.3 * locHistory.y + 0.7 * myLoc.y);
            locHistory = new MapLocation(newX, newY);
        }
    }

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
    public Direction getExploreDirection(RobotController rc) throws GameActionException {
        // find nearby robots
        // RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam());

        // figure out "outermost" direction
        // avg all the locations of the nearby robots
        // MapLocation thisLoc = rc.getLocation();
        // MapLocation avgRobotLoc = rc.getLocation();
        // for (int i = 0; i < nearbyRobots.length; i++)
        // avgRobotLoc.add(thisLoc.directionTo(nearbyRobots[i].getLocation()));
        // Direction awayDirection = avgRobotLoc.directionTo(thisLoc);
        // return getDirectionsTo(rc, thisLoc.add(awayDirection));

        // TODO: make them move away from same type

        // move away from locHistory
        if (rng.nextInt(2) == 0 && locHistory.equals(rc.getLocation())) {
            rc.setIndicatorString("Exploring away from: " + locHistory);
            return getDirectionsTo(rc, locHistory).opposite();
        }
        return getRandDirection(rc);
    }

    /**
     * Move to an unoccupied quadrant if there are enough friendly units nearby.
     * Otherwise, return to hq.
     */
    public MapLocation occupyNewQuadrant(RobotController rc) throws GameActionException {
        MapLocation location = null;
        location = findNearest(rc, quadSection, resourceSection + 1 - rc.readSharedArray(0) % 10);

        // if a certain amount of ally units are nearby, move towards target quadrant
        if (location != null && rc.getLocation().distanceSquaredTo(location) >= rc.getMapHeight() * quadRadiusFraction
                && scanForRobots(rc, "ally").length >= 2) {
            tryMoveTo(rc, location);
        } else if (scanForRobots(rc, "ally").length < 2) {
            returnToHQ(rc);
        } else {
            tryMoveTo(rc, getRandDirection(rc));
        }
        rc.setIndicatorString("Moving to quadrant: " + location);
        return location;
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
        tryMoveTo(rc, getDirectionsTo(rc, to));
    }

    public void tryMoveTo(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            tryMoveTo(rc, getDirectionsTo(rc, rc.getLocation().add(dir)));
        }
    }

    /**
     * Check for nearby enemies and attack the first unit within range. Ignores hq
     * as attack target.
     * Then, determine whether to chase or retreat.
     */
    public void attackEnemyThenChaseRetreat(RobotController rc) throws GameActionException {
        RobotInfo[] enemies = scanForRobots(rc, "enemy");
        if (enemies.length > 0) {
            RobotInfo target = attackNearestEnemy(rc, enemies);
            chaseOrEvadeEnemy(rc, target.location);
        }
    }

    // TODO: check to see which is more efficient: robot.location or
    // robot.getLocation()
    public RobotInfo attackNearestEnemy(RobotController rc, RobotInfo[] enemies) throws GameActionException {
        RobotInfo target = findNearestRobot(rc, enemies);
        if (target != null && target.getType() != RobotType.HEADQUARTERS && rc.canAttack(target.location))
            rc.attack(target.location);
        return target;
    }

    /**
     * robot returns to nearby hq
     **/
    public MapLocation returnToHQ(RobotController rc) throws GameActionException {
        MapLocation hqLocation = coordIntToLocation(RobotPlayer.closestTargetCoord);
        if (RobotPlayer.closestTargetCoord == 0) {
            int hqCount = rc.readSharedArray(0) / 10;
            hqLocation = findNearest(rc, hqSection, hqSection + hqCount);
            rc.setIndicatorString("Returning to HQ at: " + (hqLocation));
        }
        rc.setIndicatorString("Returning to HQ at: " + (hqLocation));
        tryMoveTo(rc, hqLocation);
        return hqLocation;
    }

    /**
     * If robot is a launcher, and enemy numbers are not greater than ally numbers,
     * chase enemy.
     * If robot is not a launcher or enemy numbers are too great, retreat to nearby
     * hq.
     **/
    public void chaseOrEvadeEnemy(RobotController rc, MapLocation targetLocation) throws GameActionException {
        RobotInfo[] allies = scanForRobots(rc, "ally");
        RobotInfo[] enemies = scanForRobots(rc, "enemy");

        if (allies.length + 1 > enemies.length && rc.getType() == RobotType.LAUNCHER) {
            chaseEnemy(rc, targetLocation, allies, enemies);
        } else {
            evadeEnemies(rc, enemies);
        }
    }

    public void chaseEnemy(RobotController rc, MapLocation targetLocation, RobotInfo[] allies, RobotInfo[] enemies)
            throws GameActionException {
        Direction dir = rc.getLocation().directionTo(targetLocation);
        if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getCurrentDirection() != null) {
            rc.move(dir);
        } else {
            dir = getRandDirection(rc);
            if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getCurrentDirection() != null) {
                rc.move(dir);
            }
        }
        rc.setIndicatorString("CHASING");
    }

    /**
     * Attempts to move the robot away from the given robot.
     */
    public void evadeRobot(RobotController rc, RobotInfo robot) throws GameActionException {
        rc.setIndicatorString("Evading enemy!");
        tryMoveTo(rc, rc.getLocation().directionTo(robot.location).opposite());
    }

    /**
     * Attempts to move the robot away from the first, nearest enemy.
     */
    public void evadeEnemies(RobotController rc, RobotInfo[] enemies) throws GameActionException {
        if (enemies.length > 0) {
            RobotInfo nearestEnemy = findNearestRobot(rc, enemies);
            evadeRobot(rc, nearestEnemy);
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

    /**
     * Reads the shared array over the given indices and returns the nearest loc.
     */
    public MapLocation findNearest(RobotController rc, int startIndex, int endIndex) throws GameActionException {
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

    /**
     * Finds the first, nearest robot.
     * Returns null if none.
     */
    public RobotInfo findNearestRobot(RobotController rc, RobotInfo[] robots) {
        MapLocation myLoc = rc.getLocation();
        RobotInfo nearestRobot = null;
        int nearestDist = 0;
        for (RobotInfo robot : robots) {
            int distTo = myLoc.distanceSquaredTo(robot.location);
            if (nearestDist == 0 || distTo < nearestDist) {
                nearestRobot = robot;
                nearestDist = distTo;
            }
        }
        return nearestRobot;
    }

    public RobotInfo[] scanForRobots(RobotController rc, String allyOrEnemy, RobotType type)
            throws GameActionException {
        RobotInfo[] robots = scanForRobots(rc, allyOrEnemy);
        ArrayList<RobotInfo> targets = new ArrayList<RobotInfo>();
        for (RobotInfo robot : robots) {
            if (robot.getType() == type) {
                targets.add(robot);
            }
        }
        return targets.toArray(new RobotInfo[targets.size()]);
    }

    /*
     * This handles navigating obstacles. Returns direction or Direction.CENTER if
     * no directions are present.
     */
    public Direction bugNav(RobotController rc, Direction direction) {

        currentdirection = direction;

        return direction;
    }
}
