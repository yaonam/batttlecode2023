package PoonPoon;
import battlecode.common.*;

public class Amplifier extends Base {
    static int mapIndex = 0;
    public void runAmplifier(RobotController rc) throws GameActionException {
        //Write to array the location, type, upgrade status of well
        WellInfo[] wells = rc.senseNearbyWells(rc.getLocation(), 34);
        for (WellInfo info:wells) {
            if (rc.readSharedArray(mapIndex)==0) {
                //there is a potential issue where the code can go pass the array length, will never happen if we properly assign sections
                System.out.println(mapIndex);
                System.out.println("Found " + info.getResourceType() + " well at location: " + info.getMapLocation().x + "," + info.getMapLocation().y);

                if (info.getResourceType().equals("MANA")) {
                    rc.writeSharedArray(mapIndex, 1); //1 means mana well
                } else {
                    rc.writeSharedArray(mapIndex, 2); //2 means adamantium
                }
        
                rc.writeSharedArray(mapIndex+1, info.getMapLocation().x);
                rc.writeSharedArray(mapIndex+2,info.getMapLocation().y);
                mapIndex=mapIndex+3;
            }
        }       

        // Also try to move randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}
