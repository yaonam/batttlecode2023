package PoonPoonv3;

import battlecode.common.*;

public class Amplifier extends Base {
    int index = wellSection;

    public void run(RobotController rc) throws GameActionException {
        // Write to array the location, type, upgrade status of well
        WellInfo[] wells = rc.senseNearbyWells(rc.getLocation(), 34);
        for (WellInfo info : wells) {
            if (info.getResourceType() == ResourceType.ADAMANTIUM) {
                for (int i = adamantiumIndex; i < manaIndex; i++) {
                    // if well location matches a location already placed, then stop the loop. The
                    // IF CONDITION IS A PLACEHOLDER
                    int coord = locationToCoordInt(info.getMapLocation());
                    if (rc.readSharedArray(i) == coord) {
                        break;
                    } else if (rc.readSharedArray(i) == 0) {
                        // if we reach an empty index within our well type section, write down well
                        // location
                        rc.writeSharedArray(i, coord);
                    }
                }
            }

            if (rc.readSharedArray(index) == 0) {
                // there is a potential issue where the code can go pass the array length, will
                // never happen if we properly assign sections
                System.out.println(index);
                System.out.println("Found " + info.getResourceType() + " well at location: " + info.getMapLocation().x
                        + "," + info.getMapLocation().y);

                if (info.getResourceType().equals("MANA")) {
                    rc.writeSharedArray(index, 1); // 1 means mana well
                } else {
                    rc.writeSharedArray(index, 2); // 2 means adamantium
                }

                rc.writeSharedArray(index, locationToCoordInt(info.getMapLocation()));
                index++;
            }
        }

        // Also try to move randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    public void uploadWellLocation(RobotController rc) throws GameActionException {
        // check for nearby wells

        // determine if well location has been documented

        // document location if location hasn't been documented
    }
}
