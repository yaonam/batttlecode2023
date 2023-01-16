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
            // MapLocation[] nearbyEnemyLocs = new MapLocation[nearbyEnemies.length];
            // for (int i = 0; i < nearbyEnemies.length; i++)
            //     nearbyEnemyLocs[i] = nearbyEnemies[i].location;
            // MapLocation nearestEnemyLoc = findNearest(rc, nearbyEnemyLocs);
            // rc.setIndicatorString("Evading enemy!");
            // tryMoveTo(rc, rc.getLocation().directionTo(nearestEnemyLoc).opposite());
            attackEnemy(rc);
        }
        // if not full capacity then go explore/collect
        else if (rc.getWeight() < GameConstants.CARRIER_CAPACITY) {
            MapLocation nearestWell = findNearestWell(rc);
            // findClosest(rc, wells)
            if (nearestWell != null) {
                // go to/collect
                rc.setIndicatorString("Collecting at " + nearestWell);
                collectOrMoveToWell(rc, nearestWell);
            } else {
                // explore
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

    public void collectResourceOrReturnToHQ(RobotController rc) throws GameActionException {
        if (rc.getResourceAmount(ResourceType.ADAMANTIUM) <= carrier_inventory / 2
                && rc.getResourceAmount(ResourceType.MANA) <= carrier_inventory / 2) {
            WellInfo[] wells = rc.senseNearbyWells();
            if (wells.length > 0) {
                MapLocation well_location = wells[0].getMapLocation();
                if (rc.canCollectResource(well_location, -1)) {
                    rc.collectResource(well_location, -1);
                    rc.setIndicatorString("Collecting, now have, AD:" +
                            rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                            " MN: " + rc.getResourceAmount(ResourceType.MANA) +
                            " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));
                }
            }
        } else {
            MapLocation hqLocation = returnToHQ(rc);
            transferResources(rc, hqLocation);
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
