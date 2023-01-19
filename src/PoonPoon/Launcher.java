package PoonPoon;

import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;

public class Launcher extends Base {
    // is this unit sitting by an enemy HQ?
    boolean occupyingEnemyHQ = false;

    public void run(RobotController rc) throws GameActionException {
        // scan for enemies and attack the first detected enemy unit that isn't the hq
        launcherCombat(rc);
        launcherMovement(rc);
        launcherCombat(rc);
    }

    /**
     * This handles how launchers move when not in combat.
     * @param rc
     * @param anchorCreated
     * @throws GameActionException
     */
    public void launcherMovement(RobotController rc) throws GameActionException {
        // move to the center and attack locations given at attackSection
        soldierMovement(rc);
        // roam randomly in our quadrants and occupy anchored islands
        moveToAnchorIsland(rc);       
    }

    /*
     * This handles how launchers act during combat.Launchers group up and move as one unit. 
     * They also will sit by enemy HQ if close enough. They prioritze enemy launchers.
     */
    public void launcherCombat(RobotController rc) throws GameActionException{
        RobotInfo[] enemies = scanForRobots(rc, "enemy");
        if (enemies.length == 1 && enemies[0].type == RobotType.HEADQUARTERS) {
            return;
        }
        RobotInfo[] enemyLaunchers = scanForRobots(rc, "enemy", RobotType.LAUNCHER);
        RobotInfo[] targets = scanForRobots(rc, "enemy");
        // soldiers prioritize launchers then other robot types
        attackNearestEnemy(rc, enemyLaunchers);
        if (rc.getActionCooldownTurns() < 10) {
            attackEnemy(rc);
        }
        attackEnemy(rc);
    }

    /* 
     * Launchers move to islands with anchors, ally or enemy.
     */
    public void moveToAnchorIsland(RobotController rc) throws GameActionException{
        // if the closest, visible island has anchor, move to the island
        MapLocation[] anchorIslands = findAnchorIslands(rc);
        MapLocation nearbyAnchorIsland = findNearest(rc, anchorIslands);
        if (nearbyAnchorIsland != null) {
            // when outside of X distance move towards center of nearby anchor island, move randomly when close enough to center
            if (rc.getLocation().distanceSquaredTo(nearbyAnchorIsland) >= 4) {
                tryMoveTo(rc, nearbyAnchorIsland);
            } 
            else {
                // if no anchored islands are found, roam
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

    /*
     * How soldiers will move when not under combat. 
     * If they are occupying enemy HQ (if occupyingEnemyHQ is true), they will not move to attack location.
     */
    public void soldierMovement(RobotController rc) throws GameActionException{
        // how soldiers will move when not under combat. 
        // If they are occupying enemy HQ (if occupyingEnemyHQ is true), they will not move to attack location.
        // occupyEnemyHQ(rc);
        if (rc.readSharedArray(attackSection) == 0 
            && !occupyingEnemyHQ 
            && rc.getRobotCount() < rc.getMapWidth() * rc.getMapHeight() / 4) {
            tryMoveTo(rc, new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
            rc.setIndicatorString("Moving to center:" );
        } 
        else if (!occupyingEnemyHQ && rc.getRobotCount() >= rc.getMapWidth() * rc.getMapHeight() / 4) {
            tryMoveTo(rc, getRandDirection(rc));
        }
        else if (!occupyingEnemyHQ) {
            rc.setIndicatorString("Attacking: " + coordIntToLocation(rc.readSharedArray(attackSection)));
            tryMoveTo(rc, coordIntToLocation(rc.readSharedArray(attackSection)));
        }
        
    }

    /*
     * If soldier launchers are near enemy HQ, they will stop moving and ignore commands. 
     */
    public void occupyEnemyHQ(RobotController rc)  throws GameActionException{
        RobotInfo[] robots = scanForRobots(rc, "enemy", RobotType.HEADQUARTERS);
        if(robots.length >= 1 && !occupyingEnemyHQ) {
            // if enemy hq is seen, move to it 
            MapLocation location = rc.senseRobot(robots[0].getID()).getLocation();
            rc.setIndicatorString("Advancing to enemy HQ");
            tryMoveTo(rc, location);
            if (rc.getLocation().distanceSquaredTo(location) <= 1) {
                occupyingEnemyHQ = true;
                rc.setIndicatorString("Occupying enemy HQ");
            }
        } 
        if (robots.length == 0) {
            occupyingEnemyHQ = false;
        }
    }
}
