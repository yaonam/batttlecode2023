package PoonPoon;

import java.util.ArrayList;

import battlecode.common.*;

public class Headquarters extends Base {
    MapLocation initialBuildLocation;
    MapLocation buildLocation;
    Direction buildDirection;
    int robotCount = 0;
    WellInfo[] wellInfo = null;
    int calledAttackTurn = 2000;
    int currentIndex = -1;
    // determines if hq is overrun by enemy units
    boolean hqOverran = false;
    int anchorCount = 0;

    public void run(RobotController rc) throws GameActionException {
        if (rc.readSharedArray(0) == 0) {
            robotCount = rc.getRobotCount();
            uploadCoord(rc, robotCount);
            writeToQuadrantSection(rc);
            uploadWellCoord(rc, rc.senseNearbyWells());
            // System.out.println("HERE111111111111111");
            // for (int i = 0; i < resourceSection; i++) {
            // System.out.println(rc.readSharedArray(i));
            // }
        }

        // Pick a direction to build in. 
        // Build units towards the middle of the map or towards wells.
        // Limit the number of units we can build
        setInitialBuildLocation(rc, rc.senseNearbyWells());
        int max = rc.getMapHeight() * rc.getMapWidth() / 4 ;
        if (rc.readSharedArray(0) != 0 && rc.getRobotCount() <= max && !hqOverran) {
            buildRobot(rc, RobotType.CARRIER);
            if (anchorCount < 2) {
                buildRobot(rc, RobotType.LAUNCHER);
            }
        }
        
        if (rc.readSharedArray(0) != 0 && rc.getRobotCount() > (rc.readSharedArray(0) / 10 * initialRobotCount * 2) && rc.canBuildAnchor(Anchor.STANDARD)) {
            System.out.println("BUILDING ANCHOR AT TURN " + rc.getRoundNum());
            rc.setIndicatorString("Building anchor! " + anchorCount);
            rc.buildAnchor(Anchor.STANDARD);
            anchorCount += 1;
        }
        
        assignAttackLocation(rc);
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

    // While loop doesn't work with !rc.canBuildRobot() for some reason. 
    public MapLocation adjustBuildLocation(RobotController rc, RobotType robotType, MapLocation location) throws GameActionException{
        if (!rc.canBuildRobot(robotType, location)) {
            location = location.subtract(buildDirection);
        } 
        if (!rc.canBuildRobot(robotType, location)) {
            location = location.subtract(buildDirection);
        } 
        if (!rc.canBuildRobot(robotType, location)) {
            for (int i = 0; i < directions.length; i++) {
                location = rc.getLocation().add(directions[i]);
                if (rc.canBuildRobot(robotType, location)) {
                    break;
                }
            }
        }
        rc.setIndicatorString("Attemping to build at:" + location);             
        return location;
    }

    /*
     * Build robot type.
     */
    public void buildRobot(RobotController rc, RobotType robotType) throws GameActionException {
        buildLocation = adjustBuildLocation(rc, robotType, initialBuildLocation);
        if (rc.canBuildRobot(robotType, buildLocation)) {
            if (robotType == RobotType.AMPLIFIER) {
                rc.buildRobot(robotType, buildLocation);
            } else if (robotType == RobotType.CARRIER || robotType == RobotType.LAUNCHER) {
                rc.buildRobot(robotType, buildLocation);
            }
        }
    }

    /*
     * HQ will upload their coord to the comms array, in the hqSection
     */
    public void uploadCoord(RobotController rc, int hqCount) throws GameActionException {
        int index = hqSection;
        while (index < hqSection + hqCount && rc.readSharedArray(index) != 0) {
            index++;
        }
        if (rc.canWriteSharedArray(index, 0) && index < hqSection + hqCount) {
            rc.setIndicatorString("UPLOADING COORD: " + coordIntToLocation(rc.readSharedArray(index)));
            rc.writeSharedArray(index, locationToCoordInt(rc.getLocation()));
        }
    }

    /*
     * This finds all unoccupied quadrant and writes them to the comms array quadSection
     */
    public void writeToQuadrantSection(RobotController rc) throws GameActionException{
        int startIndex = quadSection;
        int quadSectionEnd = quadSection + robotCount;
        int index = uploadQuadrantID(rc, startIndex, quadSectionEnd);
        if (index == quadSectionEnd) {
            ArrayList<Integer> list = findUnoccupiedQuadrantID(rc, startIndex, quadSectionEnd);
            translateQuadrantIDToLocations(rc, list);
        }
    }

    /*
     * Upload the quadrant ID to the comms array.
     */
    public int uploadQuadrantID(RobotController rc, int startIndex, int indexEnd) throws GameActionException {
        int index = startIndex;
        while (index < indexEnd && rc.readSharedArray(index) != 0) {
            index++;
        }
        // We upload the quadrant IDs that our hq are located in
        if (rc.canWriteSharedArray(index, 0) && index < indexEnd) {
            int quadrant = initialMapQuadrant(rc);
            rc.writeSharedArray(index, quadrant);
            rc.setIndicatorString("UPLOADING QUADRANT: " + quadrant);
        }
        return index;
    }

    public ArrayList<Integer> findUnoccupiedQuadrantID(RobotController rc, int index, int indexEnd) throws GameActionException{
        // We overwrite our hq quadrants with quadrant IDs that we do not occupy
        ArrayList<Integer> list = null;
        list = new ArrayList<Integer>();
        list.add(quad1);
        list.add(quad2);
        list.add(quad3);
        list.add(quad4);
        for (int i = quadSection; i < indexEnd; i++) {
            if (list.contains(rc.readSharedArray(i))) {
                list.remove(Integer.valueOf(rc.readSharedArray(i)));
            }
        }
        return list;
    }

    public void translateQuadrantIDToLocations(RobotController rc, ArrayList<Integer> list) throws GameActionException{
        // We translate the unoccupied quadrant IDs into quadrant locations
        if (list != null) {
            int index = quadSection;
            MapLocation location = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
            int x = rc.getMapWidth();
            int y = rc.getMapHeight();
            for (int i : list) {
                switch (i) {
                    case quad1:
                    // top left
                        location = new MapLocation(x / 4, y - y / 4);
                        break;
                    case quad2:
                    // top right
                        location = new MapLocation(x - x / 4, y - y / 4);
                        break;
                    case quad3:
                    // bottom left
                        location = new MapLocation(x / 4, y / 4);
                        break;
                    case quad4:
                    // bottome right
                        location = new MapLocation(x - x / 4, y / 4);
                        break;
                }
                rc.writeSharedArray(index, locationToCoordInt(location));
                index++;
            }
            writeToCommsArray(rc, 0, rc.getRobotCount() * 10 + list.size());
        } 
        else {
            // if list is null, then there are no unoccupied quadrants to translate
            writeToCommsArray(rc, 0, rc.getRobotCount() * 10);
        }
    }

    public void uploadWellCoord(RobotController rc, WellInfo[] info) throws GameActionException {
        if (info.length != 0) {
            MapLocation wellLocation = info[0].getMapLocation();
            int index = wellSection;
            while (rc.readSharedArray(index) != 0) {
                index++;
            }
            writeToCommsArray(rc, index, locationToCoordInt(wellLocation));
        }
    }
  /**
     * HQ and Amplifiers can assign locations for launchers with soldier roles to attack. 
     * By default, HQ will upload locations based on ally units created, and current round number
     * @param rc
     * @param coord
     * @throws GameActionException
     */
    public void assignAttackLocation(RobotController rc) throws GameActionException{
        RobotInfo[] enemies = scanForRobots(rc, "enemy");
        // if hq is overran, remove SOS signal
        if (enemies.length > 5 && !hqOverran) {
            hqOverran = true;
            System.out.println("HQ OVERRAN");
            rc.writeSharedArray(attackSection, 0);
        }
        // if enemies are at HQ send help, != locationToCoordInt(rc.getLocation()
        if (!hqOverran && enemies != null && enemies.length > 0 && rc.readSharedArray(attackSection) == 0) {
            System.out.println("SOS:" + locationToCoordInt(rc.getLocation()));
            rc.writeSharedArray(attackSection, locationToCoordInt(rc.getLocation()));
            calledAttackTurn = rc.getRoundNum() + 100;
        } else if (hqOverran && enemies.length == 0 && rc.readSharedArray(attackSection) != 0 && rc.getLocation().equals(coordIntToLocation(rc.readSharedArray(attackSection)))) {
            // if no enemies are present, and HQ location matches attack location, then HQ removes their location from attack section
            System.out.println("REMOVING SOS");
            rc.writeSharedArray(attackSection, 0);
            hqOverran = false;
        }
        if (rc.readSharedArray(0) != 0) {
            attackQuadrants(rc);
        }
    }

    /*
     * if we have enough units, then iterate through the quadrant section, and direct our forces to attack that quadrant
     * In attack section, we have attack location, current target quadrant, and turn count of when we declared our attack at that quadrant
     */
    public void attackQuadrants(RobotController rc) throws GameActionException{
        int robotCount = rc.readSharedArray(0) / 10 * initialRobotCount * 2;
        //initialize our variables
        if (currentIndex == -1) {
            currentIndex = hqSection;
            calledAttackTurn = rc.getRoundNum();
        }
        // reset our variable
        if (currentIndex >= quadSection && rc.readSharedArray(currentIndex) < 10) {
            // in our quadrant section, coord have a min value of 10
            currentIndex = hqSection;
        }

        // read through our section
        if (rc.readSharedArray(0) != 0 && calledAttackTurn < rc.getRoundNum() && rc.getRobotCount() >= robotCount) {
            rc.writeSharedArray(attackSection, rc.readSharedArray(currentIndex));
            calledAttackTurn = rc.getRoundNum() + 50;
            // System.out.println("Targeting location: " + rc.readSharedArray(currentIndex) + ", " + "Next attack call at:" + (calledAttackTurn ));
            currentIndex = currentIndex + 1;
        }   
    }
}
