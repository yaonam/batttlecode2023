package PoonPoon;

import battlecode.common.*;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

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
            returnToHQAndTransfer(rc);
        }
    }

    public void returnToHQAndTransfer(RobotController rc) throws GameActionException{
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
        MapLocation hq_location = new MapLocation(rc.readSharedArray(closest_hq_index), rc.readSharedArray(closest_hq_index + 1));
        moveToLocation(rc, hq_location.x, hq_location.y);
        rc.setIndicatorString("Returning to HQ at: " + hq_location);
        transferResources(rc, hq_location);
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
