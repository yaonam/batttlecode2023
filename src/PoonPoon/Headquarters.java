package PoonPoon;

import java.util.ArrayList;
import battlecode.common.*;

public class Headquarters extends Base {
    // int starting_x_coord = -1;
    // int starting_y_coord = -1;
    MapLocation initial_build_location;
    MapLocation build_location;
    Direction build_Direction;
    int robotCount = 0;
    WellInfo[] wellInfo = null;

    public void run(RobotController rc) throws GameActionException {
        if (rc.readSharedArray(hq_section_index) == 0) {
            robotCount = rc.getRobotCount();
            uploadCoord(rc);
            uploadQuadrant(rc);
            uploadWellCoord(rc, rc.senseNearbyWells());
        }

        // Pick a direction to build in. Dependent on location of HQ. We split the map
        // into quadrants. Build units towards the middle of the map or towards wells.
        setInitialBuildLocation(rc, rc.senseNearbyWells());
        if (rc.readSharedArray(hq_section_index) != 0) {
            buildRobot(rc, RobotType.LAUNCHER);
            buildRobot(rc, RobotType.CARRIER);
        }

        uploadResources(rc);
    }

    public void setInitialBuildLocation(RobotController rc, WellInfo[] wellInfo) {
        if (wellInfo.length > 2) {
            MapLocation wellLocation = wellInfo[0].getMapLocation();
            build_Direction = rc.getLocation().directionTo(wellLocation);
            initial_build_location = rc.getLocation().add(build_Direction).add(build_Direction).add(build_Direction);
        } else {
            // starting_x_coord = rc.getLocation().x;
            // starting_y_coord = rc.getLocation().y;
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
        build_location = adjustBuildLocation(rc, RobotType.CARRIER, initial_build_location);
        if (rc.canBuildRobot(robotType, build_location)) {
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
        int index = hq_section_index + 1;
        int hq_section = robotCount * 2 + 1;
        while (index < hq_section && rc.readSharedArray(index) != 0) {
            index = index + 2;
        }
        if (rc.canWriteSharedArray(index, 0) && rc.canWriteSharedArray(index + 1, 0) && index < hq_section) {
            rc.writeSharedArray(index, rc.getLocation().x);
            rc.writeSharedArray(index + 1, rc.getLocation().y);
        }
    }

    public void uploadQuadrant(RobotController rc) throws GameActionException {
        int startIndex = quad_section;
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
                }
            }

            index = startIndex;
            for (int i : list) {
                rc.writeSharedArray(index, i);
                index++;
            }
            writeToCommsArray(rc, hq_section_index, rc.getRobotCount() * 10 + list.size()); 
        }
    }
     
    public void uploadWellCoord(RobotController rc, WellInfo[] info) throws GameActionException{
        if (info.length != 0) {
            MapLocation well_location = info[0].getMapLocation();
            int index = well_section;
            while (rc.readSharedArray(index) != 0) {
                index = index + well_section_increment;
            }
            int resource = convertResourcetoInt(info[0].getResourceType());
            writeToCommsArray(rc, index, resource);
            writeToCommsArray(rc, index + 1, well_location.x);
            writeToCommsArray(rc, index + 2 , well_location.y);
            System.out.println("HERE1111111111 at index " + index + ": " + rc.readSharedArray(index) + ", " + rc.readSharedArray(index + 1) + ", " + rc.readSharedArray(index + 2));
        }
    }

    public void uploadResources(RobotController rc) throws GameActionException {
        if (rc.getRoundNum() % 10 == 0 ) {
            rc.setIndicatorString("UPLOADING TO COMMS ARRAY: " + rc.getResourceAmount(ResourceType.ADAMANTIUM) + "," 
                + rc.getResourceAmount(ResourceType.MANA) + ", " 
                + rc.getResourceAmount(ResourceType.MANA));
            rc.writeSharedArray(adamantiumIndex, rc.getResourceAmount(ResourceType.ADAMANTIUM));
            rc.writeSharedArray(manaIndex, rc.getResourceAmount(ResourceType.MANA));
            rc.writeSharedArray(elixirIndex, rc.getResourceAmount(ResourceType.MANA));
        }

    }
}
