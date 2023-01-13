package PoonPoon;
import battlecode.common.*;

public class Launcher extends Base {
    int starting_x_coord = -1;
    int starting_y_coord = -1;
    Direction initial_direction = null;
    Direction dir;
    int HQ_ID;

    public void runLauncher(RobotController rc) throws GameActionException {
        // if (starting_x_coord == -1) {
        //     starting_x_coord = rc.getLocation().x;
        //     starting_y_coord = rc.getLocation().y;
        //     System.out.println("I'm finding my location");
        //     initial_direction = this.initialMoveDirection(rc);
        // }

        // //move towards the middle of the map
        // if (rc.canMove(initial_direction)) {
        //     rc.move(initial_direction);
        // }

        // rc.senseRobot(HQ_ID).getLocation();
        //perform rotation  or reflection to find enemy HQ. 
        //

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
            //chase enemy target
            dir = rc.getLocation().directionTo(enemies[0].location);
            if(rc.canMove(dir)) {
                rc.move(dir);
            }
        }

        // Also try to move randomly.
        dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    public Direction initialMoveDirection (RobotController rc) {
        String quadrant = initialMapQuadrant(rc, starting_x_coord, starting_y_coord);
        return initialDirection(rc, quadrant);
    }
}
