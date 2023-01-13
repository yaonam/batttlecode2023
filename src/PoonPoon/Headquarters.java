package PoonPoon;
import battlecode.common.*;

public class Headquarters extends Base{
    int starting_x_coord = -1;
    int starting_y_coord = -1;
    MapLocation build_location;
    Direction build_Direction;

    public void runHeadquarters(RobotController rc) throws GameActionException {
        // Pick a direction to build in. Dependent on location of HQ. We split the map into quadrants. Build units towards the middle of the map. We can also set HQ to build carriers 
        // towards wells 
        WellInfo[] wellInfo = rc.senseNearbyWells();
        if (wellInfo.length != 0) {
            MapLocation wellLocation = wellInfo[0].getMapLocation();
            build_Direction = rc.getLocation().directionTo(wellLocation);
            build_location = rc.getLocation().add(build_Direction).add(build_Direction).add(build_Direction);
        } else {
            starting_x_coord = rc.getLocation().x;
            starting_y_coord = rc.getLocation().y;
            build_Direction = initialBuildDIrection(rc);
            build_location = rc.getLocation().add(build_Direction).add(build_Direction).add(build_Direction);
        }
        
        // if (rc.canBuildAnchor(Anchor.STANDARD)) {
        //     // If we can build an anchor do it!
        //     rc.buildAnchor(Anchor.STANDARD);
        //     rc.setIndicatorString("Building anchor! " + rc.getAnchor());
        // }

        int carrierCountIndex = 12;
            
        //readjust location if HQ cannot build unit at current location. Cannot spawn units in clouds but can spawn units in currents that are not at edge of action radius.
        
        build_location = findBuildLocation(rc, RobotType.CARRIER, build_location);

        // rc.senseMapInfo(build_location).hasCloud() && rc.senseMapInfo(build_location).isPassable()
        // if (rc.canBuildRobot(RobotType.CARRIER, build_location)) {
        //     // Let's try to build a carrier.
        //     rc.setIndicatorString("Trying to build a carrier");
        //     if (rc.canBuildRobot(RobotType.CARRIER, build_location)) {
        //         rc.buildRobot(RobotType.CARRIER, build_location);
        //         rc.writeSharedArray(carrierCountIndex, rc.readSharedArray(carrierCountIndex)+1);
        //     }
        // } 

        buildRobot(rc, RobotType.CARRIER, build_location);
        
        int launcherCountIndex = 13;
        // if (rc.canBuildRobot(RobotType.LAUNCHER, build_location)) {
        //     // Let's try to build a launcher.
        //     rc.setIndicatorString("Trying to build a launcher");
        //     if (rc.canBuildRobot(RobotType.LAUNCHER, build_location)) {
        //         rc.buildRobot(RobotType.LAUNCHER, build_location);
        //         rc.writeSharedArray(launcherCountIndex, rc.readSharedArray(launcherCountIndex)+1);
        //     }
        // }

        buildRobot(rc, RobotType.LAUNCHER, build_location);

        //build an amplifier if possible and the amout of amplifiers are too few
        // int ampCountIndex = 14;
        // if (rc.canBuildRobot(RobotType.AMPLIFIER, build_location) && rc.readSharedArray(ampCountIndex) < 2) {
        //     rc.setIndicatorString("Trying to build an amplifier");
        //     rc.buildRobot(RobotType.AMPLIFIER, build_location);
        //     rc.writeSharedArray(ampCountIndex, rc.readSharedArray(ampCountIndex)+1);
        // }
    }

    public Direction initialBuildDIrection (RobotController rc) {
        String quadrant = initialMapQuadrant(rc, starting_x_coord, starting_y_coord);
        return initialDirection(rc, quadrant);
    }

    //The extra conditions prevent the method from changing the location due to a lack of resources. A lack of resources will stop the while loop.
    public MapLocation findBuildLocation(RobotController rc, RobotType robotType, MapLocation build_location) {
        while (
            !(rc.canBuildRobot(robotType, build_location)) 
            && robotType.buildCostAdamantium <= rc.getResourceAmount(ResourceType.ADAMANTIUM) 
            && robotType.buildCostMana <= rc.getResourceAmount(ResourceType.MANA)
            && robotType.buildCostElixir <= rc.getResourceAmount(ResourceType.ELIXIR)
            ) {
            build_location = build_location.subtract(build_Direction);
        }
        return build_location;
    }

    public void buildRobot(RobotController rc, RobotType robotType, MapLocation location) throws GameActionException{
        if (rc.canBuildRobot(robotType, location)) {
            // Let's try to build a robot.
            rc.setIndicatorString("Trying to build a " + robotType);
            if (rc.canBuildRobot(robotType, location)) {
                rc.buildRobot(robotType, location);
                // rc.writeSharedArray(carrierCountIndex, rc.readSharedArray(carrierCountIndex)+1);
            }
        } 
    }
}
