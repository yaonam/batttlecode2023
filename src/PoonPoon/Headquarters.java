package PoonPoon;

import java.util.ArrayList;
import battlecode.common.*;

public class Headquarters extends Base {
    MapLocation initialBuildLocation;
    MapLocation buildLocation;
    Direction buildDirection;
    int robotCount = 0;
    WellInfo[] wellInfo = null;

    public void run(RobotController rc) throws GameActionException {
        if (rc.readSharedArray(hqSectionIndex) == 0) {
            robotCount = rc.getRobotCount();
            uploadCoord(rc);
            uploadQuadrant(rc);
            uploadWellCoord(rc, rc.senseNearbyWells());
        }

        // Pick a direction to build in. Dependent on location of HQ. We split the map
        // into quadrants. Build units towards the middle of the map or towards wells.
        // limit the number of units we can build
        setInitialBuildLocation(rc, rc.senseNearbyWells());
        if (rc.readSharedArray(hqSectionIndex) != 0 && rc.getRobotCount() < getMaxRobotCount(rc)) {
            buildRobot(rc, RobotType.LAUNCHER);
            buildRobot(rc, RobotType.CARRIER);

            int quadIndex = rc.readSharedArray(hqSectionIndex);
            int hqCount = Integer.parseInt(Integer.toString(quadIndex).substring(0, 1));
            if (rc.getRobotCount() > initialRobotCount * hqCount) {
                buildRobot(rc, RobotType.AMPLIFIER);
            }
        }

        uploadResources(rc);
    }

    public void setInitialBuildLocation(RobotController rc, WellInfo[] wellInfo) {
        if (wellInfo.length > 0) {
            MapLocation wellLocation = wellInfo[0].getMapLocation();
            buildDirection = rc.getLocation().directionTo(wellLocation);
            initialBuildLocation = rc.getLocation().add(buildDirection).add(buildDirection).add(buildDirection);
        } else {
            buildDirection = initialBuildDirection(rc);
            initialBuildLocation = rc.getLocation().add(buildDirection).add(buildDirection).add(buildDirection);
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
            location = location.subtract(buildDirection);
        }
        return location;
    }

    public void buildRobot(RobotController rc, RobotType robotType) throws GameActionException {
        buildLocation = adjustBuildLocation(rc, RobotType.CARRIER, initialBuildLocation);
        if (rc.canBuildRobot(robotType, buildLocation)) {
            if (rc.canBuildRobot(robotType, buildLocation)) {
                rc.buildRobot(robotType, buildLocation);
            }
        }
    }

    public void writeToCommsArray(RobotController rc, int index, int val) throws GameActionException {
        if (rc.canWriteSharedArray(index, val)) {
            rc.writeSharedArray(index, val);
        }
    }

    public void uploadCoord(RobotController rc) throws GameActionException {
        int index = hqSectionIndex + 1;
        int hqSection = robotCount * 2 + 1;
        while (index < hqSection && rc.readSharedArray(index) != 0) {
            index = index + 2;
        }
        if (rc.canWriteSharedArray(index, 0) && rc.canWriteSharedArray(index + 1, 0) && index < hqSection) {
            rc.writeSharedArray(index, rc.getLocation().x);
            rc.writeSharedArray(index + 1, rc.getLocation().y);
        }
    }

    public void uploadQuadrant(RobotController rc) throws GameActionException {
        int startIndex = quadSection;
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
            writeToCommsArray(rc, hqSectionIndex, rc.getRobotCount() * 10 + list.size()); 
        }
    }
     
    public void uploadWellCoord(RobotController rc, WellInfo[] info) throws GameActionException{
        if (info.length != 0) {
            MapLocation wellLocation = info[0].getMapLocation();
            int index = wellSection;
            while (rc.readSharedArray(index) != 0) {
                index = index + wellSectionIncrement;
            }
            int resource = convertResourcetoInt(info[0].getResourceType());
            writeToCommsArray(rc, index, resource);
            writeToCommsArray(rc, index + 1, wellLocation.x);
            writeToCommsArray(rc, index + 2 , wellLocation.y);
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
