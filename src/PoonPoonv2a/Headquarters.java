package PoonPoonv2a;

import java.util.ArrayList;
import battlecode.common.*;

public class Headquarters extends Base {
    MapLocation initialBuildLocation;
    MapLocation buildLocation;
    Direction buildDirection;
    int robotCount = 0;
    WellInfo[] wellInfo = null;

    public void run(RobotController rc) throws GameActionException {
        if (rc.readSharedArray(0) == 0) {
            robotCount = rc.getRobotCount();
            uploadCoord(rc);
            uploadQuadrant(rc);
            uploadWellCoord(rc, rc.senseNearbyWells());

            // for (int i = 0; i < resourceSection; i++) {
            // System.out.println(rc.readSharedArray(i));
            // }
        }

        // Pick a direction to build in. Dependent on location of HQ. We split the map
        // into quadrants. Build units towards the middle of the map or towards wells.
        // limit the number of units we can build
        setInitialBuildLocation(rc, rc.senseNearbyWells());
        if (rc.readSharedArray(0) != 0 && rc.getRobotCount() < getMaxRobotCount(rc)) {
            // buildRobot(rc, RobotType.AMPLIFIER);
            buildRobot(rc, RobotType.LAUNCHER);
            buildRobot(rc, RobotType.CARRIER);
        }
        uploadResourceAmount(rc);
    }

    public void setInitialBuildLocation(RobotController rc, WellInfo[] wellInfo) {
        if (wellInfo.length > 0) {
            MapLocation wellLocation = wellInfo[0].getMapLocation();
            buildDirection = rc.getLocation().directionTo(wellLocation);
            initialBuildLocation = rc.getLocation().add(buildDirection).add(buildDirection).add(buildDirection);
        } else {
            buildDirection = rc.getLocation().directionTo(new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
            initialBuildLocation = rc.getLocation().add(buildDirection).add(buildDirection).add(buildDirection);
        }
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
        buildLocation = adjustBuildLocation(rc, robotType, initialBuildLocation);
        if (rc.canBuildRobot(robotType, buildLocation)) {
            if (robotType == RobotType.AMPLIFIER
                    && rc.getRobotCount() > initialRobotCount * rc.readSharedArray(hqSection) / 10) {
                rc.buildRobot(robotType, buildLocation);
            } else if (robotType == RobotType.CARRIER || robotType == RobotType.LAUNCHER) {
                rc.buildRobot(robotType, buildLocation);
            }
            subtractResourceAmount(rc, robotType);
        }
    }

    public void uploadCoord(RobotController rc) throws GameActionException {
        int index = hqSection;
        while (index < quadSection && rc.readSharedArray(index) != 0) {
            index++;
        }
        if (rc.canWriteSharedArray(index, 0) && index < quadSection) {
            rc.setIndicatorString("UPLOADING COORD: " + coordIntToLocation(rc.readSharedArray(index)));
            rc.writeSharedArray(index, locationToCoordInt(rc.getLocation()));
        }
    }

    public void uploadQuadrant(RobotController rc) throws GameActionException {
        int startIndex = quadSection;
        int quadSectionEnd = startIndex + robotCount;
        int index = startIndex;
        while (index < quadSectionEnd && rc.readSharedArray(index) != 0) {
            index++;
        }
        // We overwrite our quadrants with unoccupied quadrants
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
            MapLocation location = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
            for (int i : list) {
                switch (i) {
                    case quad1:
                        location = new MapLocation(rc.getMapWidth() / 4, rc.getMapHeight() - rc.getMapHeight() / 4);
                        break;
                    case quad2:
                        location = new MapLocation(rc.getMapWidth() - rc.getMapWidth() / 4,
                                rc.getMapHeight() - rc.getMapHeight() / 4);
                        break;
                    case quad3:
                        location = new MapLocation(rc.getMapWidth() / 4, rc.getMapHeight() / 4);
                        break;
                    case quad4:
                        location = new MapLocation(rc.getMapWidth() - rc.getMapWidth() / 4, rc.getMapHeight() / 4);
                        break;
                }
                rc.writeSharedArray(index, locationToCoordInt(location));
                index++;
            }
            writeToCommsArray(rc, 0, rc.getRobotCount() * 10 + list.size());
        }
        rc.setIndicatorString("UPLOADING QUADRANT");

    }

    public void uploadWellCoord(RobotController rc, WellInfo[] info) throws GameActionException {
        if (info.length != 0) {
            MapLocation wellLocation = info[0].getMapLocation();
            int index = wellSection;
            while (rc.readSharedArray(index) != 0) {
                index++;
            }
            writeToCommsArray(rc, index, locationToCoordInt(wellLocation));
            // System.out.println("HERE1111111111 at index " + index + ": " +
            // rc.readSharedArray(index));
        }
    }

    /**
     * Update the resources section in the comms array every X turns by adding HQ
     * current resources to resources section
     */
    public void uploadResourceAmount(RobotController rc) throws GameActionException {
        if (rc.getRoundNum() % 5 == 0) {
            int index = resourceSection - 1;
            for (ResourceType rType : ResourceType.values()) {
                rc.writeSharedArray(index + rType.resourceID,
                        rc.readSharedArray(index + rType.resourceID) + rc.getResourceAmount(rType));
            }
            rc.setIndicatorString("CURRENT RESOURCES: " + rc.readSharedArray(adamantiumIndex) + ", "
                    + rc.readSharedArray(manaIndex) + ", " + rc.readSharedArray(elixirIndex));
        }
    }

    /**
     * When building a robot, subtract the cost from the resources section in the
     * comms array only when the amount of resources is greater than 0.
     */
    public void subtractResourceAmount(RobotController rc, RobotType robotType) throws GameActionException {
        if (rc.readSharedArray(adamantiumIndex) > 0 && rc.readSharedArray(manaIndex) > 0) {
            int index = resourceSection - 1;
            for (ResourceType rType : ResourceType.values()) {
                rc.writeSharedArray(index + rType.resourceID,
                        rc.readSharedArray(index + rType.resourceID) - robotType.getBuildCost(rType));
            }
            rc.setIndicatorString("CURRENT RESOURCES: " + rc.readSharedArray(adamantiumIndex) + ", "
                    + rc.readSharedArray(manaIndex) + ", " + rc.readSharedArray(elixirIndex));
        }
    }

    public int getMaxRobotCount(RobotController rc) {
        return rc.getMapHeight() * rc.getMapWidth() / 4;
    }
}
