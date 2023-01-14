package PoonPoon;

import java.util.ArrayList;

import battlecode.common.*;

public class Launcher extends Base {
    int starting_x_coord = -1;
    int starting_y_coord = -1;
    Direction initial_direction = null;
    Direction dir;
    int HQ_ID;

    public void runLauncher(RobotController rc) throws GameActionException {
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.setIndicatorString("Attacking");
                rc.attack(toAttack);
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

        // move towards other quadrants when no nearby enemies are present. Once there,
        // launchers will roam around.
        // find number of hq
        if (enemies.length <= 0) {
            // in index 63, we saved our hq count in the tens digit and our target quadrant
            // count in the ones digit
            int quadIndex = rc.readSharedArray(HQ_count_index);
            int hq = Integer.parseInt(Integer.toString(quadIndex).substring(0, 1));
            int targetQuadrants = Integer.parseInt(Integer.toString(quadIndex).substring(1, 2));
            int index = rc.getID() % targetQuadrants;
            int quadrant = rc.readSharedArray(HQ_count_index - hq - targetQuadrants + index);
            // System.out.println("MOVING TO QUADRANT: " + quadrant);
            targetQuadrant(rc, quadrant);
        }
    }

    public Direction initialMoveDirection(RobotController rc) throws GameActionException {
        int quadAdd = 5;// rc.readSharedArray(HQ_quadrants);
        int num;
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < directions.length; i++) {
            list.add(i, i);
        }

        do {
            num = rng.nextInt(directions.length);
        } while (quadAdd + 2 >= num && num <= quadAdd - 2);
        dir = directions[rng.nextInt(directions.length)];

        Direction direction = null;
        return direction;
    }
}
