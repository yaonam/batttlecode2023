package PoonPoonLauncherRoles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;

public class Launcher extends Base {
    boolean anchorCreated = false;
    int anchorCarrierID = -1;
    int launcherCount = 1;


    /**
     *  This is determines how launchers will behave.
     *  1 = Assassin. They avoid enemy launchers and target other robot types. 
     *  2 = Soldier. They group up and move as an army. They prioritize enemy launchers.
     *  3 = Guards. They roam around our initial quadrants and occupy anchored islands.
     */ 
    int role = 0; 

    public void run(RobotController rc) throws GameActionException {
        // assign launcher role
        assignRole(rc);
        // scan for enemies and attack the first detected enemy unit that isn't the hq
        RobotInfo[] enemies = scanForRobots(rc, "enemy");
        if (enemies.length > 0 && enemies[0].type != RobotType.HEADQUARTERS) {
            launcherCombat(rc, role);
        }
        // move towards other quadrants when no nearby enemies are present. Once near
        // the center of the quadrants, launchers will roam around.
        launcherMovement(rc, role);
        launcherCombat(rc, role);
    }

    public void assignRole(RobotController rc) throws GameActionException{
        // the max number of robots created at beginning of match
        int initialCount = rc.readSharedArray(0) / 10 + (rc.readSharedArray(0) / 10 * initialRobotCount);
        if (role == 0) {
            if (rc.getRobotCount() <= initialCount && rc.getRoundNum() < 20) {
                role = 1;
            }
            else if (rc.getRobotCount() > initialCount) {
                role = 2;
            }
            else if (rc.getRoundNum() > initialCount && rc.getRoundNum() % 20 == 0) {
                role = 3;
            }
            else {
                role = 2;
            }
        }
        rc.setIndicatorString("I've been assinged role: " + role + ", " + rc.readSharedArray(0) / 10 * initialRobotCount);
    }

    /**
     * Once an anchor is created, new launchers are assigned to protect carriers instead of moving to quadrants
     * @param rc
     * @param anchorCreated
     * @throws GameActionException
     */
    public void launcherMovement(RobotController rc, int roleID) throws GameActionException {
        // this handles all launcher movements when not in combat. The ID determines the launcher's movements
        switch (roleID) {
            case 1:
                // assassins go into unoccupied quadrants 
                occupyNewQuadrant(rc);
            case 2: 
                // soldiers move to the center and attack locations given at attackSection
                soldierMovement(rc);
            case 3:
                // patrols roam randomly in our quadrants and occupy anchored islands
                protectAnchorIsland(rc);
                break;
        }        
    }

    public void launcherCombat(RobotController rc, int roleID) throws GameActionException{
        RobotInfo[] targets = findRobots(rc, "enemy", RobotType.LAUNCHER);
        switch (roleID) {
            case 1:
                // assassins will attempt to evade enemy launchers and attack other robot types besides HQ
                if (targets != null) {
                    evadeEnemies(rc, targets);
                }
                targets = scanForRobots(rc, "enemy");
                RobotInfo robot = attackNearestEnemy(rc, targets);
                if (robot != null) {
                    tryMoveTo(rc, robot.getLocation());
                }

            case 2: 
                // soldiers prioritize launchers then other robot types
                if (targets != null) {
                    attackNearestEnemy(rc, targets);
                }
                if (rc.getActionCooldownTurns() < 10) {
                    attackEnemy(rc);
                }
            case 3:
                // guards attack on sight
                attackEnemy(rc);
                break;  
        }
    }

    public void protectAnchorIsland(RobotController rc) throws GameActionException{
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

    public RobotInfo[] findRobots(RobotController rc, String allyOrEnemy,RobotType type) throws GameActionException{
        RobotInfo[] robots = scanForRobots(rc, allyOrEnemy);
        ArrayList<RobotInfo> targets = new ArrayList<RobotInfo>();
        for (RobotInfo robot : robots) {
            if (robot.getType() == type) {
                targets.add(robot);
            }
        }
        return targets.toArray(new RobotInfo[targets.size()]);
    }

    public void soldierMovement(RobotController rc) throws GameActionException{
        if (rc.readSharedArray(attackSection) == 0) {
            tryMoveTo(rc, new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
            rc.setIndicatorString("Moving to center:" );
        } else {
            rc.setIndicatorString("Attacking: " + coordIntToLocation(rc.readSharedArray(attackSection)));
            tryMoveTo(rc, coordIntToLocation(rc.readSharedArray(attackSection)));
        }
    }
}
