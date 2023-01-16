package PoonPoon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;
import battlecode.world.Island;

public class Launcher extends Base {
    static int closestHQCoord = 0;
    public void run(RobotController rc) throws GameActionException {
        // scan for enemies and attack the first detected enemy unit that isn't the hq
        RobotInfo[] enemies = scanForRobots(rc, "enemy");
        if (enemies.length > 0) {
            attackEnemy(rc);
        }
        // move towards other quadrants when no nearby enemies are present. Once near
        // the center of the quadrants, launchers will roam around.
        if (enemies.length <= 0) {
            launcherMovement(rc);
            attackEnemy(rc);
        }
        defendAnchor(rc);

    }

    public void reinforceAllies(RobotController rc) {
        // find a nearby launcher
        // if launcher cooldown is greater than ten, move towards that launcher
        // if (rc.senseRobot(1).getType().actionCooldown > 10) {}
    }

    public void launcherMovement(RobotController rc) throws GameActionException{
        // this handles all launcher movements
        reinforceAllies(rc);
        occupyNewQuadrant(rc);
    }

    /**
     *  Launchers check if there are any carriers carrying anchors, and the number of ally units within vision. 
     *  If there are carriers with anchors and there are more than X amount of allies nearby, then follow the carrier.
     *  Otherwise, attempt to defend current anchor islands.
     */
    public void defendAnchor(RobotController rc) throws GameActionException{
        if (findAnchorCarriers(rc).length > 0 && scanForRobots(rc, "ally").length > 5) {
            tryMoveTo(rc, findNearest(rc, findAnchorCarriers(rc)));
        }
        else {
            protectAnchorIsland(rc);
        }        
    }

    public void protectAnchorIsland(RobotController rc) throws GameActionException{
        // if the closest, visible island has anchor, move to the island
        MapLocation[] anchorIslands = findAnchorIslands(rc);
        if (anchorIslands.length > 0) {
            MapLocation nearbyAnchorIsland = findNearest(rc, anchorIslands);
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
     * */
    public MapLocation[] findAnchorIslands(RobotController rc) throws GameActionException{
        Set<Integer> nearbyAnchorIslands = new HashSet<Integer>();
        int[] islands = rc.senseNearbyIslands();
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

    
    public void followAnchorCarrier(RobotController rc, int id) {
        // follow a visible carrier that is carrying anchor 
        // move towards that unit, might need to get their ID
    }

    public MapLocation[] findAnchorCarriers(RobotController rc) throws GameActionException{
        RobotInfo[] robots = scanForRobots(rc, "ally");
        Set<MapLocation> nearbyCarriers = new HashSet<MapLocation>();

        for (RobotInfo robot : robots) {
            if (robot.getTotalAnchors() > 0) {
                nearbyCarriers.add(robot.getLocation());
            }
        }
        MapLocation[] carrierLocations = new MapLocation[nearbyCarriers.size()];
        int i = 0;
        for (MapLocation location : carrierLocations) {
            carrierLocations[i] = location;
            i++;
        }
        return carrierLocations;
    }
}
