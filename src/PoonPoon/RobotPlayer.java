package PoonPoon;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what
 * we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been
     * alive.
     * You can use static variables like this to save any information you want. Keep
     * in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between
     * your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided
     * by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant
     * number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very
     * useful for debugging!
     */
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the
     * Battlecode world.
     * It is like the main function for your robot. If this method returns, the
     * robot dies!
     *
     * @param rc The RobotController object. You use it to perform actions from this
     *           robot, and to get
     *           information on its current status. Essentially your portal to
     *           interacting with the world.
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

        System.out.println(rb);

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in
            // an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At
            // the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to
            // do.

            turnCount += 1; // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to
            // explode.
            try {
                // The same run() function is called for every robot on your team, even if they
                // are different types.
                rb.run(rc);

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You
                // should
                // handle GameActionExceptions judiciously, in case unexpected events occur in
                // the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
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

    /**
     * Run a single turn for a Headquarters.
     * This code is wrapped inside the infinite loop in run(), so it is called once
     * per turn.
     */
    static void runHeadquarters(RobotController rc) throws GameActionException {
        Headquarters hq = new Headquarters();
        hq.run(rc);
    }

    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once
     * per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
        Carriers cr = new Carriers();
        cr.run(rc);
    }

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once
     * per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {
        Launcher launcher = new Launcher();
        launcher.run(rc);
    }

    static void runAmplifier(RobotController rc) throws GameActionException {
        Amplifier amp = new Amplifier();
        amp.run(rc);
    }
}
