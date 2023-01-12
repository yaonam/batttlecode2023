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
            build_location = rc.getLocation().add(build_Direction);
        } else {
            starting_x_coord = rc.getLocation().x;
            starting_y_coord = rc.getLocation().y;
            build_Direction = this.initialBuildDIrection(rc);
            build_location = rc.getLocation().add(build_Direction);
        }
        // starting_x_coord = rc.getLocation().x;
        // starting_y_coord = rc.getLocation().y;
        // System.out.println("finding my initial location");
        // build_Direction = this.initialBuildDIrection(rc);
        // build_location = rc.getLocation().add(build_Direction);
        
        // if (rc.canBuildAnchor(Anchor.STANDARD)) {
        //     // If we can build an anchor do it!
        //     rc.buildAnchor(Anchor.STANDARD);
        //     rc.setIndicatorString("Building anchor! " + rc.getAnchor());
        // }

        int carrierCountIndex = 12;
        
        if (rc.canBuildRobot(RobotType.CARRIER, build_location)) {
            // Let's try to build a carrier.
            rc.setIndicatorString("Trying to build a carrier");
            if (rc.canBuildRobot(RobotType.CARRIER, build_location)) {
                rc.buildRobot(RobotType.CARRIER, build_location);
                rc.writeSharedArray(carrierCountIndex, rc.readSharedArray(carrierCountIndex)+1);
            }
        } 
        
        int launcherCountIndex = 13;
        if (rc.canBuildRobot(RobotType.LAUNCHER, build_location)) {
            // Let's try to build a launcher.
            rc.setIndicatorString("Trying to build a launcher");
            if (rc.canBuildRobot(RobotType.LAUNCHER, build_location)) {
                rc.buildRobot(RobotType.LAUNCHER, build_location);
                rc.writeSharedArray(launcherCountIndex, rc.readSharedArray(launcherCountIndex)+1);
            }
        }

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
}
