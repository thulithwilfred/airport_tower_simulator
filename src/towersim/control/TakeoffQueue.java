package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.util.Encodable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Represents a first-in-first-out (FIFO) queue of aircraft waiting to take off.
 * <p>
 * FIFO ensures that the order in which aircraft are allowed to take off is
 * based on long they have been waiting in the queue. An aircraft that has been
 * waiting for longer than another aircraft will always be allowed to take off before
 * the other aircraft.
 */
public class TakeoffQueue extends AircraftQueue implements Encodable {

    /**
     * A queue (FIFO) that contains Aircrafts in takeoff queue.
     */
    private Queue<Aircraft> takeOffQueue;

    /**
     * Constructs a new TakeoffQueue with an initially empty queue of aircraft.
     */
    public TakeoffQueue() {
        takeOffQueue = new LinkedList<Aircraft>();
    }

    /**
     * Adds the given aircraft to the queue
     *
     * @param aircraft aircraft to add to queue
     */
    public void addAircraft(Aircraft aircraft) {
        this.takeOffQueue.add(aircraft);
    }


    /**
     * Returns the aircraft at the front of the queue without removing it from the queue,
     * or null if the queue is empty.
     * <p>
     * Aircraft returned by peekAircraft() should be in the same order that they were
     * added via addAircraft().
     *
     * @return aircraft at front of queue
     */
    public Aircraft peekAircraft() {
        if (this.takeOffQueue.size() <= 0) {
            return null;
        }
        return this.takeOffQueue.peek();
    }

    /**
     * Removes and returns the aircraft at the front of the queue.
     * Returns null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    public Aircraft removeAircraft() {
        if (this.takeOffQueue.size() <= 0) {
            return null;
        }
        return this.takeOffQueue.poll();
    }


    /**
     * Returns a list containing all aircraft in the queue, in order.
     * <p>
     * That is, the first element of the returned list should be the first aircraft t
     * hat would be returned by calling removeAircraft(), and so on.
     * <p>
     * Adding or removing elements from the returned list should not affect the original queue.
     *
     * @return list of all aircraft in queue, in queue order
     */
    public List<Aircraft> getAircraftInOrder() {
        return new ArrayList<Aircraft>(this.takeOffQueue);
    }

    /**
     * Returns true if the given aircraft is in the queue.
     *
     * @param aircraft aircraft to find in queue
     * @return true if aircraft is in queue; false otherwise
     */
    public boolean containsAircraft(Aircraft aircraft) {

        if (this.takeOffQueue.size() <= 0) {
            return false;
        }

        return this.takeOffQueue.contains(aircraft);
    }
}
