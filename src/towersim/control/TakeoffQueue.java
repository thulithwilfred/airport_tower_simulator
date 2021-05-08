package towersim.control;

/**
 * Represents a first-in-first-out (FIFO) queue of aircraft waiting to take off.
 * <p>
 * FIFO ensures that the order in which aircraft are allowed to take off is
 * based on long they have been waiting in the queue. An aircraft that has been
 * waiting for longer than another aircraft will always be allowed to take off before
 * the other aircraft.
 */
public class TakeoffQueue {
}
