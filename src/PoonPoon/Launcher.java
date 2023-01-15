package PoonPoon;

import battlecode.common.*;

public class Launcher extends Base {
    int starting_x_coord = -1;
    int starting_y_coord = -1;
    Direction initial_direction = null;
    Direction dir;
    int HQ_ID;

    public void run(RobotController rc) throws GameActionException {
        // scan for enemies and attack the first detected enemy unit that isn't the hq
        RobotInfo[] enemies = scanForRobots(rc);
        if (enemies.length > 0) {
            attackEnemy(rc, enemies);
            attackEnemy(rc, enemies);
        }
        // move towards other quadrants when no nearby enemies are present. Once near
        // the center of the quadrants, launchers will roam around.
        if (enemies.length <= 0) {
            movetoQuadrant(rc);
        }
    }

    public void movetoQuadrant(RobotController rc) throws GameActionException {
        // count in the ones digit
        int quadIndex = rc.readSharedArray(hq_section_index);
        int targetQuadrants = Integer.parseInt(Integer.toString(quadIndex).substring(1, 2));
        int index = rc.getID() % targetQuadrants;
        int quadrant = rc.readSharedArray(quad_section + index);
        rc.setIndicatorString("Moving to quadrant: " + quadrant);
        targetQuadrant(rc, quadrant);
    }

    public void attackEnemy(RobotController rc, RobotInfo[] enemies) throws GameActionException {
        // unit will search for a robot type that isn't headquarters
        int i = 0;
        MapLocation toAttack = null;
        while (i < enemies.length && enemies[i].getType() == RobotType.HEADQUARTERS) {
            i++; 
        }

        if (i >= enemies.length) {
            // this means we only detected the enemy hq
            toAttack = enemies[0].location;
        } else {
            // this means we detected an enemy unit besides their hq
            toAttack = enemies[i].location;
        }

        if (rc.canAttack(toAttack)) {
            rc.setIndicatorString("Attacking");
            rc.attack(toAttack);
            rc.setIndicatorString("ATTACKING LOCATION: " + toAttack);
        }
        // chase enemy target, move randomly if they cannot move in that direction,
        // avoid currents
        dir = rc.getLocation().directionTo(enemies[0].location);
        if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getCurrentDirection() != null) {
            rc.move(dir);
        } else {
            dir = directions[rng.nextInt(directions.length)];
            if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getCurrentDirection() != null) {
                rc.move(dir);
            }
        }
    }
}
