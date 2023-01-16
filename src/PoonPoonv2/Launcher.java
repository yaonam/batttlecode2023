package PoonPoonv2;

import battlecode.common.*;

public class Launcher extends Base {
    static int closestHQCoord = 0;

    public void run(RobotController rc) throws GameActionException {
        // scan for enemies and attack the first detected enemy unit that isn't the hq
        RobotInfo[] enemies = scanForRobots(rc, "enemy");
        if (enemies.length > 0) {
            attackEnemy(rc);
        }
        // move towards other quadrants when no nearby enemies are present. Once near
        // the center of the quadrants, launchers will roam around.
        if (enemies.length <= 0) {
            movetoQuadrant(rc);
            attackEnemy(rc);
        }
        // rc.setIndicatorString("End of loop");
    }

    public void movetoQuadrant(RobotController rc) throws GameActionException {
        // count in the ones digit
        int targetQuadrants = rc.readSharedArray(0) % 10;
        int index = rc.getID() % targetQuadrants;
        int quadrant = rc.readSharedArray(quadSection + index);
        rc.setIndicatorString("Moving to quadrant: " + quadrant);
        targetQuadrant(rc, quadrant);
    }
}
