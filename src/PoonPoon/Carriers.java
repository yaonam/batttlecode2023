package PoonPoon;

import battlecode.common.*;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class Carriers extends Base {
    public void run(RobotController rc) throws GameActionException {
        collectResourceOrReturnToHQ(rc);
        // If carriers are carrying near max weight and are about to die, perform a
        // carrier attack
        if (rc.getHealth() < 7) {
            RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            if (enemyRobots.length > 0) {
                if (rc.canAttack(enemyRobots[0].location)) {
                    rc.attack(enemyRobots[0].location);
                }
            }
        }

        // If we can see a well, move towards it
        WellInfo[] wells = rc.senseNearbyWells();
        if (wells.length > 0) {
            moveToLocation(rc, wells[0].getMapLocation());
            rc.setIndicatorString("Moving to well at: " + wells[0].getMapLocation());
        }

        // Direction nearestHq = getNearestHqLoc();

        // If nothing else, explore
        Direction dir = getExploreDirection(rc);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
        // moveToLocation(rc, rng.nextInt(rc.getMapHeight()),
        // rng.nextInt(rc.getMapHeight()));
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
            returnToHQAndTransfer(rc);
        }
    }

    public void returnToHQAndTransfer(RobotController rc) throws GameActionException {
        MapLocation current_location = rc.getLocation();
        int distance = rc.getMapHeight();
        int index = hq_section_index + 1;
        int closest_hq_index = index;
        while (rc.readSharedArray(index) != 0) {
            MapLocation location = new MapLocation(rc.readSharedArray(index), rc.readSharedArray(index + 1));
            int new_distance = current_location.distanceSquaredTo(location);
            if (new_distance < distance) {
                distance = new_distance;
                closest_hq_index = index;
            }
            index = index + hq_section_increment;
        }
        MapLocation hq_location = new MapLocation(rc.readSharedArray(closest_hq_index),
                rc.readSharedArray(closest_hq_index + 1));
        moveToLocation(rc, hq_location);
        rc.setIndicatorString("Returning to HQ at: " + hq_location);
        transferResources(rc, hq_location);
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
