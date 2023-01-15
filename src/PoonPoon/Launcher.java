package PoonPoon;

import battlecode.common.*;

public class Launcher extends Base {
    int starting_x_coord = -1;
    int starting_y_coord = -1;
    Direction initial_direction = null;
    Direction dir;
    int HQ_ID;

    public void runLauncher(RobotController rc) throws GameActionException {
        // scan for enemies
        // scan for enemies
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);

        // attack the first detected enemy unit that isn't the hq

        // attack the first detected enemy unit that isn't the hq
        if (enemies.length > 0) {
                attackEnemy(rc, enemies);
                attackEnemy(rc, enemies);
        }
        // move towards other quadrants when no nearby enemies are present. Once near the center of the quadrants, launchers will roam around.
        if (enemies.length <= 0) {            
            movetoQuadrant(rc);   
        }

    }

    public void movetoQuadrant (RobotController rc) throws GameActionException{
        // in index 63, we saved our hq count in the tens digit and our target quadrant count in the ones digit
        int quadIndex = rc.readSharedArray(hq_section_index);   
        int hq = Integer.parseInt(Integer.toString(quadIndex).substring(0, 1));
        int targetQuadrants = Integer.parseInt(Integer.toString(quadIndex).substring(1, 2));
        int index = rc.getID() % targetQuadrants;
        int quadrant = rc.readSharedArray(hq_section_index + 1 + hq*2 + index);
        // System.out.println("MOVING TO QUADRANT: " + quadrant);
        targetQuadrant(rc, quadrant);
    }

    public void attackEnemy (RobotController rc, RobotInfo[] enemies) throws GameActionException {
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
        //chase enemy target, move randomly if they cannot move in that direction, avoid currents
        dir = rc.getLocation().directionTo(enemies[0].location);
        if(rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getCurrentDirection() != null) {
            rc.move(dir);
        }
        else {
            dir = directions[rng.nextInt(directions.length)];
            if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getCurrentDirection() != null) {
            rc.move(dir);
            }
        }
    }
}
