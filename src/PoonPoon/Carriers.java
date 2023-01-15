package PoonPoon;

import battlecode.common.*;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

public class Carriers extends Base {
    public void run(RobotController rc) throws GameActionException {
        if (rc.getAnchor() != null) {
            // If I have an anchor singularly focus on getting it to the first island I see
            int[] islands = rc.senseNearbyIslands();
            Set<MapLocation> islandLocs = new HashSet<>();
            for (int id : islands) {
                MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                islandLocs.addAll(Arrays.asList(thisIslandLocs));
            }
            if (islandLocs.size() > 0) {
                MapLocation islandLocation = islandLocs.iterator().next();
                rc.setIndicatorString("Moving my anchor towards " + islandLocation);
                while (!rc.getLocation().equals(islandLocation)) {
                    Direction dir = rc.getLocation().directionTo(islandLocation);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }
                if (rc.canPlaceAnchor()) {
                    rc.setIndicatorString("Huzzah, placed anchor!");
                    rc.placeAnchor();
                }
            }
        }


        collectResourceOrReturnToHQ(rc);
        // Try to gather from squares around us.
        // MapLocation me = rc.getLocation();
        // for (int dx = -1; dx <= 1; dx++) {
        //     for (int dy = -1; dy <= 1; dy++) {
        //         MapLocation wellLocation = new MapLocation(me.x + dx, me.y + dy);
        //         if (rc.canCollectResource(wellLocation, -1) 
        //             && rc.getResourceAmount(ResourceType.ADAMANTIUM) <= carrier_inventory/2
        //             && rc.getResourceAmount(ResourceType.MANA) <= carrier_inventory/2) {
        //             rc.collectResource(wellLocation, -1);
        //             rc.setIndicatorString("Collecting, now have, AD:" +
        //                     rc.getResourceAmount(ResourceType.ADAMANTIUM) +
        //                     " MN: " + rc.getResourceAmount(ResourceType.MANA) +
        //                     " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));
        //         }
        //     }
        // }
        // If carriers are carrying near max weight and are about to die, perform a
        // carrier attack
        attackEnemy(rc);

        // If we can see a well, move towards it
        WellInfo[] wells = rc.senseNearbyWells();
        if (wells.length > 0) {
            WellInfo well_one = wells[0];
            Direction dir = getDirectionsTo(rc, well_one.getMapLocation());
            if (rc.canMove(dir))
                rc.move(dir);
                rc.setIndicatorString("Moving to well at: " + wells[0].getMapLocation());
        }

        // read array for discovered Ad well
        // wellSection is defined in Base class, how many index spaces a well requires
        // to identify the location and type
        // int i = well_section;
        // while (rc.readSharedArray(i) == 0) {
        //     i = i + well_section_increment;
        // }
        // if (rc.readSharedArray(i) > 0) {
        //     // translate the xy coord
        //     MapLocation AdmanWellLocation = new MapLocation(rc.readSharedArray(+1), rc.readSharedArray(i + 2));
        //     // move to well location, replace following with Elim's path finding methods
        //     Direction dir = rc.getLocation().directionTo(AdmanWellLocation);
        //     if (rc.canMove(dir)) {
        //         rc.move(dir);
        //     }
        // }

        // If nothing else, explore
        Direction dir = getExploreDirection(rc);
        if (rc.canMove(dir)) {
            rc.move(dir); 
        }
        // moveToLocation(rc, rng.nextInt(rc.getMapHeight()), rng.nextInt(rc.getMapHeight()));
    }

    public void collectResourceOrReturnToHQ(RobotController rc) throws GameActionException{
        if (rc.getResourceAmount(ResourceType.ADAMANTIUM) <= carrier_inventory/2 && rc.getResourceAmount(ResourceType.MANA) <= carrier_inventory/2) {
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
        }
        else {
            MapLocation hqLocation = returnToHQ(rc);
            transferResources(rc, hqLocation);
        }
    }

    public void transferResources(RobotController rc, MapLocation hq_location) throws GameActionException{
        if (rc.canTransferResource(hq_location, ResourceType.MANA, rc.getResourceAmount(ResourceType.MANA))) {
            rc.transferResource(hq_location, ResourceType.MANA, rc.getResourceAmount(ResourceType.MANA));
        }
        if (rc.canTransferResource(hq_location, ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM))) {
            rc.transferResource(hq_location, ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM));
        }
        if (rc.canTransferResource(hq_location, ResourceType.ELIXIR, rc.getResourceAmount(ResourceType.ELIXIR))) {
            rc.transferResource(hq_location, ResourceType.ELIXIR,rc.getResourceAmount(ResourceType.ELIXIR));
        }

    }
}
