package PoonPoon;

import battlecode.common.*;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

public class Carriers extends Base {
    public void run(RobotController rc) throws GameActionException {
        collectResourceOrReturnToHQ(rc);
        // If carriers are carrying near max weight and are about to die, perform a
        // carrier attack
        attackEnemy(rc);

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
