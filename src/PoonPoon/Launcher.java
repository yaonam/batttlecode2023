package PoonPoon;

import battlecode.common.*;

public class Launcher extends Base {
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
        int quadIndex = rc.readSharedArray(hqSectionIndex);
        int targetQuadrants = Integer.parseInt(Integer.toString(quadIndex).substring(1, 2));
        int index = rc.getID() % targetQuadrants;
        int quadrant = rc.readSharedArray(quadSection + index);
        rc.setIndicatorString("Moving to quadrant: " + quadrant);
        targetQuadrant(rc, quadrant);
    }
}
