package PoonPoonv0;

import battlecode.common.*;

public class Headquarters extends Base {
    int starting_x_coord = -1;
    int starting_y_coord = -1;
    MapLocation initial_build_location;
    MapLocation build_location;
    Direction build_Direction;

    public void runHeadquarters(RobotController rc) throws GameActionException {
        // Pick a direction to build in. Dependent on location of HQ. We split the map
        // into quadrants. Build units towards the middle of the map. We can also set HQ
        // to build carriers
        // towards wells
        WellInfo[] wellInfo = rc.senseNearbyWells();
        setInitialBuildLocation(rc, wellInfo);

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
        String quadrant = initialMapQuadrant(rc, starting_x_coord, starting_y_coord);
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
            System.out.println("finding location. Currently at: " + location);
        }
        return location;
    }

    public void buildRobot(RobotController rc, RobotType robotType) throws GameActionException {
        if (rc.canBuildRobot(robotType, build_location)) {
            // Let's try to build a robot.
            System.out.println("Trying to build a " + robotType);
            if (rc.canBuildRobot(robotType, build_location)) {
                rc.buildRobot(robotType, build_location);
            }
        }
    }
}
