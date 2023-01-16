package PoonPoon;

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
            launcherMovement(rc);
            attackEnemy(rc);
        }
    }

    public void reinforceAllies(RobotController rc) {
        // find a nearby launcher
        // if launcher cooldown is greater than ten, move towards that launcher
        // if (rc.senseRobot(1).getType().actionCooldown > 10) {}
    }

    public void launcherMovement(RobotController rc) throws GameActionException{
        // this handles all launcher movements
        reinforceAllies(rc);
        occupyNewQuadrant(rc);
    }
}
