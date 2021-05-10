package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.util.Encodable;

import java.util.*;

/**
 * Represents a rule-based queue of aircraft waiting in the air to land.
 * <p>
 * The rules in the landing queue are designed to ensure that aircraft are
 * prioritised for landing based on "urgency" factors such as remaining fuel onboard,
 * emergency status and cargo type.
 */
public class LandingQueue extends AircraftQueue {

    /**
     * A list that contains Aircrafts in landing queue.
     */
    private ArrayList<Aircraft> landingQueue;

    /**
     * Constructs a new LandingQueue with an initially empty queue of aircraft.
     */
    public LandingQueue() {
        this.landingQueue = new ArrayList<>();
    }

    /**
     * Adds the given aircraft to the queue.
     *
     * @param aircraft aircraft to add to queue
     */
    public void addAircraft(Aircraft aircraft) {
        this.landingQueue.add(aircraft);
    }

    /**
     * Returns the aircraft at the front of the queue without removing it from the queue,
     * or null if the queue is empty.
     * <p>
     * The rules for determining which aircraft in the queue should be returned next are as follows:
     * <p>
     * If an aircraft is currently in a state of emergency, it should be returned.
     * If more than one aircraft are in an emergency, return the one added to the queue first.
     * <p>
     * If an aircraft has less than or equal to 20 percent fuel remaining, a critical level,
     * it should be returned (see Aircraft.getFuelPercentRemaining()).
     * If more than one aircraft have a critical level of fuel onboard,
     * return the one added to the queue first.
     * <p>
     * If there are any passenger aircraft in the queue,
     * return the passenger aircraft that was added to the queue first.
     * <p>
     * If this point is reached and no aircraft has been returned,
     * return the aircraft that was added to the queue first.
     *
     * @return aircraft at front of queue
     */
    public Aircraft peekAircraft() {
        //Return null if queue empty
        if (this.landingQueue.size() <= 0) {
            return null;
        }

        //Iterates through queue in FIFO order
        for (Aircraft aircraft : this.landingQueue) {
            //Check Emergency.
            if (aircraft.hasEmergency()) {
                return aircraft;
            }
        }

        //Iterates through queue in FIFO order
        for (Aircraft aircraft : this.landingQueue) {
            //Check fuel level.
            if (aircraft.getFuelPercentRemaining() <= 20) {
                return aircraft;
            }
        }

        //Iterates through queue in FIFO order
        for (Aircraft aircraft : this.landingQueue) {
            //Check if passenger aircraft.
            if (aircraft instanceof PassengerAircraft) {
                return aircraft;
            }
        }

        //Return first Aircraft in queue.
        return this.landingQueue.get(0);
    }

    /**
     * Removes and returns the aircraft at the front of the queue.
     * Returns null if the queue is empty.
     * <p>
     * The same rules as described in peekAircraft() should be used for determining
     * which aircraft to remove and return.
     *
     * @return aircraft at front of queue
     */
    public Aircraft removeAircraft() {
        //Return null if queue empty
        if (this.landingQueue.size() <= 0) {
            return null;
        }

        Iterator<Aircraft> itr = this.landingQueue.iterator();

        while (itr.hasNext()) {
            Aircraft aircraft = itr.next();
            //Check Emergency.
            if (aircraft.hasEmergency()) {
                itr.remove();
                return aircraft;
            }
        }

        //Reset Iterator
        itr = this.landingQueue.iterator();

        while (itr.hasNext()) {
            Aircraft aircraft = itr.next();
            //Check if passenger aircraft.
            if (aircraft.getFuelPercentRemaining() <= 20) {
                itr.remove();
                return aircraft;
            }
        }

        //Reset Iterator
        itr = this.landingQueue.iterator();

        while (itr.hasNext()) {
            Aircraft aircraft = itr.next();
            //Check if passenger aircraft.
            if (aircraft instanceof PassengerAircraft) {
                itr.remove();
                return aircraft;
            }
        }

        //Copy reference to first aircraft and remove from list.
        Aircraft firstAircraft = landingQueue.get(0);
        landingQueue.remove(0);

        return firstAircraft;
    }

    /**
     * Returns a list containing all aircraft in the queue, in order.
     * <p>
     * That is, the first element of the returned list should be the first aircraft
     * that would be returned by calling removeAircraft(), and so on.
     * <p>
     * Adding or removing elements from the returned list should not affect the original queue.
     *
     * @return list of all aircraft in queue, in queue order
     * @implNote The following methods creates a copy of the landing queue and removes air crafts
     * from it once added to the ordered queue. This was added, so that if there are any
     * duplicates in the landing queue (unhandled by spec), these will also be added to
     * the ordered queue in FIFO order (according to rules).
     */
    public List<Aircraft> getAircraftInOrder() {

        if (this.landingQueue.size() <= 0) {
            return null;
        }

        //Return a new list copied from the existing (new address)
        ArrayList<Aircraft> orderedQueue = new ArrayList<>();

        //Copy the original landing queue, this is used to account for duplicates.
        ArrayList<Aircraft> copyLandingQueue = new ArrayList<>(this.landingQueue);

        Iterator<Aircraft> itr = copyLandingQueue.iterator();

        while (itr.hasNext()) {
            Aircraft aircraft = itr.next();
            if (aircraft.hasEmergency()) {
                orderedQueue.add(aircraft);
                itr.remove();
            }
        }
        //Reset iterator
        itr = copyLandingQueue.iterator();

        while (itr.hasNext()) {
            Aircraft aircraft = itr.next();
            //Check fuel level, and not already added to list (contains will invoke equals()).
            if (aircraft.getFuelPercentRemaining() <= 20) {
                orderedQueue.add(aircraft);
                itr.remove();
            }
        }

        itr = copyLandingQueue.iterator();

        while (itr.hasNext()) {
            Aircraft aircraft = itr.next();
            //Check if passenger aircraft, and not already added to list.
            if (aircraft instanceof PassengerAircraft) {
                orderedQueue.add(aircraft);
                itr.remove();
            }
        }

        itr = copyLandingQueue.iterator();

        while (itr.hasNext()) {
            Aircraft aircraft = itr.next();
            //Check if passenger aircraft, and not already added to list.
            orderedQueue.add(aircraft);
            itr.remove();
        }
        return orderedQueue;
    }

    /**
     * Returns true if the given aircraft is in the queue.
     *
     * @param aircraft Returns true if the given aircraft is in the queue
     * @return true if aircraft is in queue; false otherwise
     */
    public boolean containsAircraft(Aircraft aircraft) {

        if (this.landingQueue.size() <= 0) {
            return false;
        }

        return this.landingQueue.contains(aircraft);
    }
}
