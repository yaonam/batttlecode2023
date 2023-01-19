package PoonPoonv4;

import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;

public class Launcher extends Base {
    boolean anchorCreated = false;
    int anchorCarrierID = -1;
    int launcherCount = 1;
    boolean occupyingEnemyHQ = false;

    /**
     * This is determines how launchers will behave.
     * 1 = Assassin. They avoid enemy launchers and target other robot types.
     * 2 = Soldier. They group up and move as an army. They prioritize enemy
     * launchers.
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

    public void assignRole(RobotController rc) throws GameActionException {
        // the max number of robots created at beginning of match
        if (role == 0) {
            int hq = rc.readSharedArray(0) / 10;
            int initialCount = hq + (hq * initialRobotCount);
            if (rc.getRobotCount() <= initialCount && rc.getMapHeight() * rc.getMapWidth() < 400) {
                role = 1;
            } else if (rc.getMapHeight() * rc.getMapWidth() >= 400) {
                role = 2;
            } else {
                role = 2;
            }
            rc.setIndicatorString("I've been assinged role: " + role);
        }
    }

    /**
     * Once an anchor is created, new launchers are assigned to protect carriers
     * instead of moving to quadrants
     * 
     * @param rc
     * @param anchorCreated
     * @throws GameActionException
     */
    public void launcherMovement(RobotController rc, int roleID) throws GameActionException {
        // this handles all launcher movements when not in combat. The ID determines the
        // launcher's movements
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

    public void launcherCombat(RobotController rc, int roleID) throws GameActionException {
        RobotInfo[] enemies = scanForRobots(rc, "enemy");
        if (enemies.length == 1 && enemies[0].type == RobotType.HEADQUARTERS) {
            return;
        }
        RobotInfo[] targets = scanForRobots(rc, "enemy", RobotType.LAUNCHER);
        switch (roleID) {
            case 1:
                // assassins will attempt to evade enemy launchers and attack other robot types
                // besides HQ
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
                    attackEnemyThenChaseRetreat(rc);
                }
            case 3:
                // guards attack on sight
                attackEnemyThenChaseRetreat(rc);
                break;
        }
    }

    public void protectAnchorIsland(RobotController rc) throws GameActionException {
        // if the closest, visible island has anchor, move to the island
        MapLocation[] anchorIslands = findAnchorIslands(rc);
        MapLocation nearbyAnchorIsland = findNearest(rc, anchorIslands);
        if (nearbyAnchorIsland != null) {
            // when outside of X distance move towards center of nearby anchor island, move
            // randomly when close enough to center
            if (rc.getLocation().distanceSquaredTo(nearbyAnchorIsland) >= 4) {
                tryMoveTo(rc, nearbyAnchorIsland);
            } else {
                // if no anchored islands are found, roam
                tryMoveTo(rc, getRandDirection(rc));
            }
        }
    }

    /**
     * Returns an array of all locations of nearby anchor islands. The location is
     * the center of the islands.
     * This will find enemy and ally anchor islands
     */
    public MapLocation[] findAnchorIslands(RobotController rc) throws GameActionException {
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
     * If they are occupying enemy HQ (if occupyingEnemyHQ is true), they will not
     * move to attack location.
     */
    public void soldierMovement(RobotController rc) throws GameActionException {
        // how soldiers will move when not under combat.
        // If they are occupying enemy HQ (if occupyingEnemyHQ is true), they will not
        // move to attack location.
        occupyEnemyHQ(rc);
        if (rc.readSharedArray(attackSection) == 0 && !occupyingEnemyHQ) {
            tryMoveTo(rc, new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
            rc.setIndicatorString("Moving to center:");
        } else if (!occupyingEnemyHQ) {
            rc.setIndicatorString("Attacking: " + coordIntToLocation(rc.readSharedArray(attackSection)));
            tryMoveTo(rc, coordIntToLocation(rc.readSharedArray(attackSection)));
        }
    }

    public void occupyEnemyHQ(RobotController rc) throws GameActionException {
        RobotInfo[] robots = scanForRobots(rc, "enemy", RobotType.HEADQUARTERS);
        if (robots.length >= 1 && !occupyingEnemyHQ) {
            // if enemy hq is seen, move to it
            MapLocation location = rc.senseRobot(robots[0].getID()).getLocation();
            rc.setIndicatorString("Advancing to enemy HQ");
            tryMoveTo(rc, location);
            if (rc.getLocation().distanceSquaredTo(location) <= 2) {
                occupyingEnemyHQ = true;
                rc.setIndicatorString("Occupying enemy HQ");
            }
        }
        if (robots.length == 0) {
            occupyingEnemyHQ = false;
        }
    }
}
