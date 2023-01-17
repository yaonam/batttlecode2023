package PoonPoon;

import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;

public class Launcher extends Base {
    boolean anchorCreated = false;
    int anchorCarrierID = -1;

    public void run(RobotController rc) throws GameActionException {
        // scan for enemies and attack the first detected enemy unit that isn't the hq
        RobotInfo[] enemies = scanForRobots(rc, "enemy");
        if (enemies.length > 0) {
            attackEnemy(rc);
        }
        // move towards other quadrants when no nearby enemies are present. Once near
        // the center of the quadrants, launchers will roam around.
        if (enemies.length <= 0) {
            launcherMovement(rc, anchorCarrierID);
            attackEnemy(rc);
        }
        protectAnchorIsland(rc);
    }

    public void reinforceAllies(RobotController rc) {
        // find a nearby launcher
        // if launcher cooldown is greater than ten, move towards that launcher
        // if (rc.senseRobot(1).getType().actionCooldown > 10) {}
    }

    /**
     * Once an anchor is created, new launchers are assigned to protect carriers instead of moving to quadrants
     * @param rc
     * @param anchorCreated
     * @throws GameActionException
     */
    public void launcherMovement(RobotController rc, int id) throws GameActionException {
        // this handles all launcher movements
        if (id == -1) {
            MapLocation[] locations = findAnchorCarriers(rc);
            if (locations != null) {
                rc.setIndicatorString("Finding anchor carrier");
                assignToAnchorCarrier(rc, locations);
            }
            else {
                occupyNewQuadrant(rc);
            }
        }
        else {
            MapLocation location = findAnchorCarrier(rc, id);
            if (location != null) {
                tryMoveTo(rc, location);
            }
        }

    }

    /**
     *  Launchers check if there are any carriers carrying anchors, and the number of ally units within vision. 
     *  If there are carriers with anchors and there are more than X amount of allies nearby, then follow the carrier.
     *  Otherwise, attempt to defend current anchor islands.
     */
    public void defendAnchor(RobotController rc) throws GameActionException{
        MapLocation[] carrierLocations = findAnchorCarriers(rc);
        if (carrierLocations != null && scanForRobots(rc, "ally").length > 5) {
            MapLocation location = findNearest(rc, carrierLocations);
            if (location != null) {
                rc.setIndicatorString("Following carrier with anchor: " + location);
                tryMoveTo(rc, location);
            }
        }
        else {
            MapLocation location = findEnemyIslandLocation(rc, rc.senseNearbyIslands());
            if (location != null) {
                tryMoveTo(rc, location);
            }
            protectAnchorIsland(rc);
        }        
    }

    public void protectAnchorIsland(RobotController rc) throws GameActionException{
        // if the closest, visible island has anchor, move to the island
        MapLocation[] anchorIslands = findAnchorIslands(rc);
        MapLocation nearbyAnchorIsland = findNearest(rc, anchorIslands);
        if (nearbyAnchorIsland != null) {
            // when outside of X distance move towards center of nearby anchor island, move randomly when close enough to center
            if (rc.getLocation().distanceSquaredTo(nearbyAnchorIsland) >= 16) {
                tryMoveTo(rc, nearbyAnchorIsland);
            } 
            else {
                tryMoveTo(rc, getRandDirection(rc));
            }
        }
    }

    /**
     *  Returns an array of all locations of nearby anchor islands. The location is the center of the islands. 
     *  This will find enemy and ally anchor islands
     * */
    public MapLocation[] findAnchorIslands(RobotController rc) throws GameActionException{
        Set<Integer> nearbyAnchorIslands = new HashSet<Integer>();
        int[] islands = rc.senseNearbyIslands();
        if (islands != null) {
            for (int islandId : islands) {
                if (rc.senseAnchor(islandId) != null) {
                    nearbyAnchorIslands.add(islandId);
                }
            }
            MapLocation[] anchorIslands = new MapLocation[nearbyAnchorIslands.size()];
            int i = 0;
            for (Integer id : nearbyAnchorIslands) {
                anchorIslands[i] = findNearest(rc, rc.senseNearbyIslandLocations(id));
                i++;
            }
            return anchorIslands;
        }
        return null;
    }

    /**
     * Finds the nearest anchor island location occupied by an enemy anchor.  
     * @param rc
     * @param anchorIslands
     * @return
     * @throws GameActionException
     */
    public MapLocation findEnemyIslandLocation(RobotController rc, int[] anchorIslands) throws GameActionException {
        MapLocation location = null;
        for (int anchor : anchorIslands) {
            if (rc.senseTeamOccupyingIsland(anchor) == rc.getTeam().opponent()) {
                location = findNearest(rc, rc.senseNearbyIslandLocations(anchor));
                break;
            }
        }
        return location;
    }

    /**
     * Assign a launcher to a carrier that is carrying an anchor.
     * @param rc
     * @throws GameActionException
     */
    public int assignToAnchorCarrier(RobotController rc, MapLocation[] carrierLocations) throws GameActionException{
        // follow a visible carrier that is carrying anchor 
        if (carrierLocations != null) {
            RobotInfo robot = rc.senseRobotAtLocation(findNearest(rc, carrierLocations));
            anchorCarrierID = robot.ID;
        }
        return anchorCarrierID;
    }

    public MapLocation[] findAnchorCarriers(RobotController rc) throws GameActionException{
        MapLocation[] carrierLocations = null;
        RobotInfo[] robots = scanForRobots(rc, "ally");
        Set<MapLocation> nearbyCarriers = new HashSet<MapLocation>();

        if (robots != null) {
            for (RobotInfo robot : robots) {
                if (robot.getType() == RobotType.CARRIER && robot.getTotalAnchors() > 0) {
                    nearbyCarriers.add(robot.getLocation());
                }
            }
        }
        if (nearbyCarriers.size() > 0) {
            carrierLocations = new MapLocation[nearbyCarriers.size()];
            int i = 0;
            for (MapLocation location : nearbyCarriers) {
                carrierLocations[i] = location;
                i++;
            }
        }
        return carrierLocations;
    }

    /**
     * Finds assigned anchor carrier location. If launcher cannot find carrier or carrier drops their anchor,
     * the launcher un-assigns itself.
     * @param rc
     * @param id
     * @throws GameActionException
     */
    public MapLocation findAnchorCarrier(RobotController rc, int id) throws GameActionException{
        MapLocation location = null;
        if (rc.canSenseRobot(id)) {
            location = rc.senseRobot(id).getLocation();
            if (location != null && rc.senseRobot(id).getTotalAnchors() > 0 ) {
                rc.setIndicatorString("FOLLOWING ANCHOR CARRIER AT: " + location);
            } 
            else {
                anchorCarrierID = -1;
            }
        }
        return location;
    }
}
