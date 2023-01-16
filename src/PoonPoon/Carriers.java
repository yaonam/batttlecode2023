package PoonPoon;

import battlecode.common.*;

public class Carriers extends Base {
    MapLocation hqLoc = null;

    public void run(RobotController rc) throws GameActionException {
        // TODO: remove this and use comm array
        if (hqLoc == null)
            hqLoc = rc.getLocation();

        // if near enemy, evade
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (nearbyEnemies.length > 0) {
            // TODO: attack if non-zero weight? otherwise run?
            attackNearestEnemy(rc, nearbyEnemies);
            evadeEnemies(rc, nearbyEnemies);
        }
        // if not full capacity then go explore/collect
        else if (rc.getWeight() < GameConstants.CARRIER_CAPACITY) {
            MapLocation nearestWell = findNearestWell(rc);
            if (nearestWell != null) {
                rc.setIndicatorString("Collecting at " + nearestWell);
                collectOrMoveToWell(rc, nearestWell);
            } else {
                rc.setIndicatorString("Exploring!");
                tryMoveTo(rc, getExploreDirection(rc));
            }
        }
        // if full capacity, then return to hq/deposit
        else if (rc.getWeight() == GameConstants.CARRIER_CAPACITY) {
            MapLocation hqLocation = returnToHQ(rc);
            transferResources(rc, hqLocation);
        }
    }

    /**
     * Tries to collect resource from well.
     * If can't, then tries to move towards well.
     */
    public void collectOrMoveToWell(RobotController rc, MapLocation wellLoc) throws GameActionException {
        if (rc.canCollectResource(wellLoc, -1)) {
            rc.collectResource(wellLoc, -1);
        } else {
            tryMoveTo(rc, wellLoc);
        }
    }

    public void transferResources(RobotController rc, MapLocation hqLoc) throws GameActionException {
        ResourceType[] allResourceTypes = { ResourceType.MANA, ResourceType.ADAMANTIUM, ResourceType.ELIXIR };
        for (ResourceType resourceType : allResourceTypes) {
            if (rc.canTransferResource(hqLoc, resourceType, rc.getResourceAmount(resourceType))) {
                rc.transferResource(hqLoc, resourceType, rc.getResourceAmount(resourceType));
            }
        }
    }
}
