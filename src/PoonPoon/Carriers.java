package PoonPoon;

import battlecode.common.*;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

public class Carriers extends Base {
    public void runCarrier(RobotController rc) throws GameActionException {
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.setIndicatorString("Attacking");        
                rc.attack(toAttack);
            }
        }
        // rc.canTakeAnchor(rc.senseRobot(HQID).getLocation(), null);
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
        // Try to gather from squares around us.
        MapLocation me = rc.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation wellLocation = new MapLocation(me.x + dx, me.y + dy);
                if (rc.canCollectResource(wellLocation, -1)) {
                    if (rng.nextBoolean()) {
                        rc.collectResource(wellLocation, -1);
                        rc.setIndicatorString("Collecting, now have, AD:" +
                                rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                                " MN: " + rc.getResourceAmount(ResourceType.MANA) +
                                " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));
                    }
                }
            }
        }
        // If carriers are carrying near max weight and are about to die, perform a
        // carrier attack
        if (rc.getHealth() < 7 && rc.getWeight() > 30) {
            RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            if (enemyRobots.length > 0) {
                if (rc.canAttack(enemyRobots[0].location)) {
                    rc.attack(enemyRobots[0].location);
                }
            }
        }

        // If we can see a well, move towards it
        WellInfo[] wells = rc.senseNearbyWells();
        if (wells.length > 1 && rng.nextInt(3) == 1) {
            WellInfo well_one = wells[1];
            // Direction dir = me.directionTo(well_one.getMapLocation());
            Direction dir = getDirectionsTo(rc, well_one.getMapLocation());
            if (rc.canMove(dir))
                rc.move(dir);
        }

        // read array for discovered Ad well
        // wellSection is defined in Base class, how many index spaces a well requires
        // to identify the location and type
        int i = 0;
        while (rc.readSharedArray(i) != 0) {
            if (rc.readSharedArray(i) == 2 && i % wellSection == 0) {
                // translate the xy coord
                MapLocation AdmanWellLocation = new MapLocation(rc.readSharedArray(+1), rc.readSharedArray(i + 2));
                // move to well location, replace following with Elim's path finding methods
                Direction dir = rc.getLocation().directionTo(AdmanWellLocation);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    break;
                }
            }
            i = i + 3;
        }

        // If nothing else, explore
        Direction dir = getExploreDirection(rc);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }

    }
}
