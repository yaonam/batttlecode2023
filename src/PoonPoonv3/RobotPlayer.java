package PoonPoonv3;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what
 * we'll call once your robot is created!
 */

public strictfp class RobotPlayer {
    static int turnCount = 0;
    static int wellCoord = 0;
    // TODO: Need comment @Bethel
    static int closestTargetCoord = 0;

    /**
     * run() is the method that is called when a robot is instantiated in the
     * Battlecode world.
     * It is like the main function for your robot. If this method returns, the
     * robot dies!
     *
     * @param rc The RobotController object. You use it to perform actions from this
     *           robot, and to get information on its current status. Essentially
     *           your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // Instantiate based on robot type
        Base rb = null;
        switch (rc.getType()) {
            case AMPLIFIER:
                rb = new Amplifier();
                break;
            case CARRIER:
                rb = new Carriers();
                break;
            case HEADQUARTERS:
                rb = new Headquarters();
                break;
            case LAUNCHER:
                rb = new Launcher();
                break;
            case BOOSTER:
                break;
            case DESTABILIZER:
                break;
        }

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in
            // an infinite loop. If we ever leave this loop and return from run(), the robot
            // dies! At the end of the loop, we call Clock.yield(), signifying that we've
            // done everything we want to do.

            turnCount += 1; // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to
            // explode.
            try {
                // The same run() function is called for every robot on your team, even if they
                // are different types.
                rb.run(rc);

            } catch (GameActionException e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop
                // again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for
            // another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction
        // imminent...
    }

}
