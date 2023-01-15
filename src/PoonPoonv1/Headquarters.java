package PoonPoonv1;

import java.util.ArrayList;
import battlecode.common.*;

public class Headquarters extends Base {
    int starting_x_coord = -1;
    int starting_y_coord = -1;
    MapLocation initial_build_location;
    MapLocation build_location;
    Direction build_Direction;
    int robotCount = 0;

    public void run(RobotController rc) throws GameActionException {
        // upload HQ quadrants if not uploaded
        if (rc.readSharedArray(hq_section_index) == 0) {
            robotCount = rc.getRobotCount();
            uploadCoord(rc);
            uploadQuadrant(rc);
            // uploadQuadrants(rc);
        }

        // Pick a direction to build in. Dependent on location of HQ. We split the map
        // into quadrants. Build units towards the middle of the map. We can also set HQ
        // to build carriers
        // towards wells
        WellInfo[] wellInfo = rc.senseNearbyWells();
        setInitialBuildLocation(rc, wellInfo);

        if (rc.readSharedArray(hq_section_index) != 0) {
            // if (rc.canBuildAnchor(Anchor.STANDARD)) {
            // // If we can build an anchor do it!
            // rc.buildAnchor(Anchor.STANDARD);
            // rc.setIndicatorString("Building anchor! " + rc.getAnchor());
            // }

            // readjust location if HQ cannot build unit at current location. Cannot spawn
            // units in clouds but can spawn units in currents that are not at edge of
            // action radius.
            build_location = adjustBuildLocation(rc, RobotType.CARRIER, initial_build_location);
            buildRobot(rc, RobotType.CARRIER);

            build_location = adjustBuildLocation(rc, RobotType.LAUNCHER, initial_build_location);
            buildRobot(rc, RobotType.LAUNCHER);
        }

    }

    public void setInitialBuildLocation(RobotController rc, WellInfo[] wellInfo) {
        if (wellInfo.length != 0) {
            MapLocation wellLocation = wellInfo[0].getMapLocation();
            build_Direction = rc.getLocation().directionTo(wellLocation);
            initial_build_location = rc.getLocation().add(build_Direction).add(build_Direction).add(build_Direction);
        } else {
            starting_x_coord = rc.getLocation().x;
            starting_y_coord = rc.getLocation().y;
            build_Direction = initialBuildDirection(rc);
            initial_build_location = rc.getLocation().add(build_Direction).add(build_Direction).add(build_Direction);
        }
    }

    public Direction initialBuildDirection(RobotController rc) {
        int quadrant = initialMapQuadrant(rc);
        return initialDirection(rc, quadrant);
    }

    // The extra conditions prevent the method from changing the location due to a
    // lack of resources or when location is too far away. A lack of resources will
    // stop the while loop.
    public MapLocation adjustBuildLocation(RobotController rc, RobotType robotType, MapLocation location) {
        while (!(rc.canBuildRobot(robotType, location))
                && robotType.buildCostAdamantium <= rc.getResourceAmount(ResourceType.ADAMANTIUM)
                && robotType.buildCostMana <= rc.getResourceAmount(ResourceType.MANA)
                && robotType.buildCostElixir <= rc.getResourceAmount(ResourceType.ELIXIR)
                && !rc.canActLocation(location)) {
            location = location.subtract(build_Direction);
        }
        return location;
    }

    public void buildRobot(RobotController rc, RobotType robotType) throws GameActionException {
        if (rc.canBuildRobot(robotType, build_location)) {
            // Let's try to build a robot.
            if (rc.canBuildRobot(robotType, build_location)) {
                rc.buildRobot(robotType, build_location);
            }
        }
    }

    public void writeToCommsArray(RobotController rc, int index, int val) throws GameActionException {
        if (rc.canWriteSharedArray(index, val)) {
            rc.writeSharedArray(index, val);
        }
    }

    public void uploadCoord(RobotController rc) throws GameActionException {
        // we have them upload their coordinates
        int index = hq_section_index + 1;
        int hq_section = robotCount * 2 + 1;
        while (index < hq_section && rc.readSharedArray(index) != 0) {
            index = index + 2;
        }
        if (rc.canWriteSharedArray(index, 0) && rc.canWriteSharedArray(index + 1, 0) && index < hq_section) {
            rc.writeSharedArray(index, rc.getLocation().x);
            rc.writeSharedArray(index + 1, rc.getLocation().y);
            // System.out.println("the location at index: " + index + " is: " +
            // rc.readSharedArray(index) + ", " + rc.readSharedArray(index+1));
        }
    }

    public void uploadQuadrant(RobotController rc) throws GameActionException {
        // find how many HQ we have. Then have them upload their Quadrant locations.
        int startIndex = robotCount * 2 + 1;
        int quadSectionEnd = startIndex + robotCount;
        int index = startIndex;
        while (index < quadSectionEnd && rc.readSharedArray(index) != 0) {
            index++;
        }
        // WE ARE PRINTING OUR OCCUPIED QUADRANTS 5 3 before our target quadrants, not
        // needed to know our quadrants. Can be overwritten with unoccupied quadrants
        if (rc.canWriteSharedArray(index, 0) && index < quadSectionEnd) {
            int quadrant = initialMapQuadrant(rc);
            rc.writeSharedArray(index, quadrant);
            // System.out.println("the quadrant at index: " + index + " is: " +
            // rc.readSharedArray(index));
        }
        if (index == quadSectionEnd && rc.canWriteSharedArray(index, 0)) {
            ArrayList<Integer> list = new ArrayList<Integer>();
            list.add(quad1);
            list.add(quad2);
            list.add(quad3);
            list.add(quad4);
            for (int i = startIndex; i < quadSectionEnd; i++) {
                if (list.contains(rc.readSharedArray(i))) {
                    list.remove(Integer.valueOf(rc.readSharedArray(i)));
                    // System.out.println(rc.readSharedArray(i) + " at index: " + i);
                }
            }

            System.out.println("here is the list of target quadrants:" + list);
            index = startIndex;
            for (int i : list) {
                rc.writeSharedArray(index, i);
                // System.out.println("THIS IS A TARGET QUADRANT: " + rc.readSharedArray(index)
                // + " placed in: " + index);
                index++;
            }
            // THIS NEEDS TO UPDATE, WE ARE PRINTING 7 1 5 3 SINCE WE OCCUPY QUADRANTS 5 3
            // AND ARE TARGETING 7 1
            writeToCommsArray(rc, hq_section_index, rc.getRobotCount() * 10 + list.size()); // we list the number of HQ
                                                                                            // so future units know
                                                                                            // where to start reading HQ
                                                                                            // quadrants
            System.out.println("DONE1111111111111 WE ARE PRINTING THE COMMS ARRAY HERE:");
            for (int i = 0; i < 15; i++) {
                System.out.println(rc.readSharedArray(i));
            }
        }
    }
}
