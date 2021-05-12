package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftType;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.TaskType;
import towersim.util.NoSpaceException;
import towersim.util.NoSuitableGateException;
import towersim.util.Tickable;

import java.util.*;

/**
 * Represents a the control tower of an airport.
 * <p>
 * The control tower is responsible for managing the operations of the airport, including arrivals
 * and departures in/out of the airport, as well as aircraft that need to be loaded with cargo
 * at gates in terminals.
 *
 * @ass1
 */
public class ControlTower implements Tickable {
    /**
     * List of all aircraft managed by the control tower.
     */
    private final List<Aircraft> aircraft;

    /**
     * List of all terminals in the airport.
     */
    private final List<Terminal> terminals;

    /**
     * Number of ticks that have elapsed since the tower was first created
     */
    private long ticksElapsed;

    /**
     * Queue of aircraft waiting to land
     */
    private LandingQueue landingQueue;

    /**
     * Queue of aircraft waiting to take off
     */
    private TakeoffQueue takeoffQueue;

    /**
     * mapping of aircraft that are loading cargo to the number of ticks remaining for loading
     */
    private Map<Aircraft, Integer> loadingAircraft;


    /**
     * Creates a new ControlTower.
     * <p>
     * The number of ticks elapsed, list of aircraft, landing queue,
     * takeoff queue and map of loading aircraft to loading times should all be
     * set to the values passed as parameters.
     *
     * @param ticksElapsed    number of ticks that have elapsed since the tower was first created
     * @param aircraft        list of aircraft managed by the control tower
     * @param landingQueue    queue of aircraft waiting to land
     * @param takeoffQueue    queue of aircraft waiting to take off
     * @param loadingAircraft mapping of aircraft that are loading cargo to the number of ticks
     *                        remaining for loading
     */
    public ControlTower(long ticksElapsed, List<Aircraft> aircraft, LandingQueue landingQueue,
                        TakeoffQueue takeoffQueue, Map<Aircraft, Integer> loadingAircraft) {

        this.ticksElapsed = ticksElapsed;
        this.aircraft = aircraft;
        this.landingQueue = landingQueue;
        this.takeoffQueue = takeoffQueue;
        this.loadingAircraft = loadingAircraft;
        this.terminals = new ArrayList<>();
    }

    /**
     * Adds the given terminal to the jurisdiction of this control tower.
     *
     * @param terminal terminal to add
     * @ass1
     */
    public void addTerminal(Terminal terminal) {
        this.terminals.add(terminal);
    }

    /**
     * Returns a list of all terminals currently managed by this control tower.
     * <p>
     * The order in which terminals appear in this list should be the same as the order in which
     * they were added by calling {@link #addTerminal(Terminal)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all terminals
     * @ass1
     */
    public List<Terminal> getTerminals() {
        return new ArrayList<>(this.terminals);
    }

    /**
     * Adds the given aircraft to the jurisdiction of this control tower.
     * <p>
     * If the aircraft's current task type is {@code WAIT} or {@code LOAD}, it should be parked at a
     * suitable gate as found by the {@link #findUnoccupiedGate(Aircraft)} method.
     * If there is no suitable gate for the aircraft, the {@code NoSuitableGateException} thrown by
     * {@code findUnoccupiedGate()} should be propagated out of this method.
     * <p>
     * After the aircraft has been added, it should be placed in the appropriate queues by calling
     * placeAircraftInQueues(Aircraft).
     *
     * @param aircraft aircraft to add
     * @throws NoSuitableGateException if there is no suitable gate for an aircraft with a current
     *                                 task type of {@code WAIT} or {@code LOAD}
     * @ass1
     */
    public void addAircraft(Aircraft aircraft) throws NoSuitableGateException {
        TaskType currentTaskType = aircraft.getTaskList().getCurrentTask().getType();
        if (currentTaskType == TaskType.WAIT || currentTaskType == TaskType.LOAD) {
            Gate gate = findUnoccupiedGate(aircraft);
            try {
                gate.parkAircraft(aircraft);
            } catch (NoSpaceException ignored) {
                // not possible, gate unoccupied
            }
        }
        this.aircraft.add(aircraft);
        this.placeAircraftInQueues(aircraft);
    }

    /**
     * Returns a list of all aircraft currently managed by this control tower.
     * <p>
     * The order in which aircraft appear in this list should be the same as the order in which
     * they were added by calling {@link #addAircraft(Aircraft)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all aircraft
     * @ass1
     */
    public List<Aircraft> getAircraft() {
        return new ArrayList<>(this.aircraft);
    }

    /**
     * Returns the number of ticks that have elapsed for this control tower.
     * <p>
     * If the control tower was created with a non-zero number of elapsed ticks,
     * this number should be taken into account in the return value of this method.
     *
     * @return number of ticks elapsed;
     */
    public long getTicksElapsed() {
        return this.ticksElapsed;
    }

    /**
     * Returns the queue of aircraft waiting to land.
     *
     * @return Landing Queue
     */
    public AircraftQueue getLandingQueue() {
        return this.landingQueue;
    }

    /**
     * Returns the queue of aircraft waiting to take off.
     *
     * @return Takeoff Queue
     */
    public AircraftQueue getTakeoffQueue() {
        return this.takeoffQueue;
    }

    /**
     * Returns the mapping of loading aircraft to their remaining load times.
     *
     * @return loading aircraft map
     */
    public Map<Aircraft, Integer> getLoadingAircraft() {
        return this.loadingAircraft;
    }

    /**
     * Attempts to find an unoccupied gate in a compatible terminal for the given aircraft.
     * <p>
     * Only terminals of the same type as the aircraft's AircraftType (see
     * {@link towersim.aircraft.AircraftCharacteristics#type}) should be considered. For example,
     * for an aircraft with an AircraftType of {@code AIRPLANE}, only AirplaneTerminals may be
     * considered.
     * <p>
     * For each compatible terminal, the {@link Terminal#findUnoccupiedGate()} method should be
     * called to attempt to find an unoccupied gate in that terminal. If
     * {@code findUnoccupiedGate()} does not find a suitable gate, the next compatible terminal
     * in the order they were added should be checked instead, and so on.
     * <p>
     * If no unoccupied gates could be found across all compatible terminals, a
     * {@code NoSuitableGateException} should be thrown.
     *
     * @param aircraft aircraft for which to find gate
     * @return gate for given aircraft if one exists
     * @throws NoSuitableGateException if no suitable gate could be found
     * @ass1
     */
    public Gate findUnoccupiedGate(Aircraft aircraft) throws NoSuitableGateException {
        AircraftType aircraftType = aircraft.getCharacteristics().type;
        for (Terminal terminal : terminals) {
            //Terminals that are currently in a state of emergency should not be considered
            if (terminal.hasEmergency()) {
                continue;
            }
            /*
             * Only check for available gates at terminals that are of the same aircraft type as
             * the aircraft
             */
            if ((terminal instanceof AirplaneTerminal && aircraftType == AircraftType.AIRPLANE)
                    || (terminal instanceof HelicopterTerminal
                    && aircraftType == AircraftType.HELICOPTER)) {
                try {
                    // This terminal found a gate, return it
                    return terminal.findUnoccupiedGate();
                } catch (NoSuitableGateException e) {
                    // If this terminal has no unoccupied gates, try the next one
                }
            }
        }
        throw new NoSuitableGateException("No gate available for aircraft");
    }

    /**
     * Attempts to land one aircraft waiting in the landing queue and park it at a suitable gate.
     * <p>
     * If there are no aircraft in the landing queue waiting to land,
     * then the method should return false and no further action should be taken.
     * <p>
     * If there is at least one aircraft in the landing queue, then a suitable gate should be
     * found for the aircraft at the front of the queue (see findUnoccupiedGate(Aircraft)).
     * <p>
     * If there is no suitable gate, the aircraft should not be landed and should remain
     * in the queue, and the method should return false and no further action should be taken.
     * <p>
     * If there is a suitable gate, the aircraft should be removed from the queue and it
     * should be parked at that gate. The aircraft's passengers or freight should be
     * unloaded immediately, by calling Aircraft.unload().
     * <p>
     * Finally, the landed aircraft should move on to the next task in its task list and the
     * method should return true.
     *
     * @return true if an aircraft was successfully landed and parked; false otherwise
     */
    public boolean tryLandAircraft() {
        //No air crafts in landing queue
        if (this.getLandingQueue().peekAircraft() == null) {
            return false;
        }
        //Get an air craft that is pending landing, according to urgency
        Aircraft pendingLanding = this.getLandingQueue().peekAircraft();

        try {
            //Find gate and attempt to park.
            Gate availableGate = findUnoccupiedGate(pendingLanding);
            availableGate.parkAircraft(pendingLanding);
            //Remove Pending Landing Aircraft from Queue, it has been parked
            this.getLandingQueue().removeAircraft();
            //Unload passengers/freight.
            pendingLanding.unload();
            //Move on to the next task
            pendingLanding.getTaskList().moveToNextTask();
            return true;
        } catch (NoSuitableGateException | NoSpaceException noGateOrSpaceException) {
            //No suitable gate to land.
            return false;
        }

    }

    /**
     * Attempts to allow one aircraft waiting in the takeoff queue to take off.
     * <p>
     * If there are no aircraft waiting in the takeoff queue, then the method should return.
     * Otherwise, the aircraft at the front of the takeoff queue should be removed from
     * the queue and it should move to the next task in its task list.
     */
    public void tryTakeOffAircraft() {
        //Check if takeOffQueue is not empty
        if (this.getTakeoffQueue().peekAircraft() != null) {
            Aircraft pendingTakeOff = this.getTakeoffQueue().removeAircraft();
            pendingTakeOff.getTaskList().moveToNextTask();
        }
    }

    /**
     * Updates the time remaining to load on all currently loading aircraft and removes
     * aircraft from their gate once finished loading.
     * <p>
     * Any aircraft in the loading map should have their time remaining decremented by one tick.
     * If any aircraft's time remaining is now zero, it has finished loading and should be removed
     * from the loading map. Additionally, it should leave the gate it is parked at and should
     * move on to its next task.
     */
    public void loadAircraft() {
        Iterator<Map.Entry<Aircraft, Integer>> itr =
                this.getLoadingAircraft().entrySet().iterator();
        int decrementedTickTime;

        while (itr.hasNext()) {
            Map.Entry<Aircraft, Integer> mapIndex = itr.next();
            decrementedTickTime = mapIndex.getValue() - 1;

            //Caps the tick value to 0.
            mapIndex.setValue(Math.max(decrementedTickTime, 0));

            //Remove from map if tick is 0, leave current gate and move to next task.
            if (decrementedTickTime <= 0) {
                this.findGateOfAircraft(mapIndex.getKey()).aircraftLeaves();
                mapIndex.getKey().getTaskList().moveToNextTask();
                itr.remove();
            }
        }
    }

    /**
     * Calls placeAircraftInQueues(Aircraft) on all aircraft managed by the control tower.
     */
    public void placeAllAircraftInQueues() {
        //All air crafts managed by this tower.
        for (Aircraft aircraft : this.getAircraft()) {
            placeAircraftInQueues(aircraft);
        }
    }

    /**
     * Moves the given aircraft to the appropriate queue based on its current task.
     * <p>
     * If the aircraft's current task type is LAND and the landing queue does
     * not already contain the aircraft, it should be added to the landing queue.
     * <p>
     * If the aircraft's current task type is TAKEOFF and the takeoff queue does
     * not already contain the aircraft, it should be added to the takeoff queue.
     * <p>
     * If the aircraft's current task type is LOAD and the loading map does not
     * already contain the aircraft, it should be added to the loading map with an
     * associated value of Aircraft.getLoadingTime() (this is the number of ticks
     * it will remain in the loading phase).
     *
     * @param aircraft aircraft to move to appropriate queue
     */
    public void placeAircraftInQueues(Aircraft aircraft) {
        switch (aircraft.getTaskList().getCurrentTask().getType()) {
            case LAND:
                //If LandingQueue does not contain aircraft
                if (!this.getLandingQueue().containsAircraft(aircraft)) {
                    this.getLandingQueue().addAircraft(aircraft);
                }
                break;
            case TAKEOFF:
                //If TakeOffQueue does not contain aircraft
                if (!this.getTakeoffQueue().containsAircraft(aircraft)) {
                    this.getTakeoffQueue().addAircraft(aircraft);
                }
                break;
            case LOAD:
                //Aircraft not already in loading map
                if (!aircraftInLoadingMap(aircraft)) {
                    this.getLoadingAircraft().put(aircraft, aircraft.getLoadingTime());
                }
                break;
        }
    }

    /**
     * Helper method to check if a given aircraft exists within the loadingMap
     *
     * @param aircraft aircraft to check for
     * @return true if aircraft in map, false else.
     */
    private boolean aircraftInLoadingMap(Aircraft aircraft) {
        for (Map.Entry<Aircraft, Integer> mapIndex : this.getLoadingAircraft().entrySet()) {
            if (mapIndex.getKey().equals(aircraft)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Finds the gate where the given aircraft is parked, and returns null if the aircraft is
     * not parked at any gate in any terminal.
     *
     * @param aircraft aircraft whose gate to find
     * @return gate occupied by the given aircraft; or null if none exists
     * @ass1
     */
    public Gate findGateOfAircraft(Aircraft aircraft) {
        for (Terminal terminal : this.getTerminals()) {
            for (Gate gate : terminal.getGates()) {
                if (Objects.equals(gate.getAircraftAtGate(), aircraft)) {
                    return gate;
                }
            }
        }
        return null;
    }

    /**
     * Advances the simulation by one tick.
     * <p>
     * On each tick, the control tower should call {@link Aircraft#tick()} on all aircraft managed
     * by the control tower.
     * <p>
     * Note that the actions performed by {@code tick()} are very simple at the moment and will be
     * expanded on in assignment 2.
     *
     * @ass1
     */
    @Override
    public void tick() {
        for (Aircraft aircraft : this.getAircraft()) {
            //1. Call Aircraft.tick() on all aircraft.
            aircraft.tick();
            //2. Move all aircraft with a current task type of AWAY or WAIT to their next task.
            switch (aircraft.getTaskList().getCurrentTask().getType()) {
                case AWAY:
                case WAIT:
                    aircraft.getTaskList().moveToNextTask();
                    break;
            }
        }

        //3. Process loading aircraft by calling loadAircraft().
        loadAircraft();

        //4.On every second tick, attempt to land an aircraft by calling tryLandAircraft().
        if (this.getTicksElapsed() % 2 == 1) {
            //Second Call, tick indexing starts from 0.
            if (!tryLandAircraft()) {
                //Aircraft could not be landed
                tryTakeOffAircraft();
            }
        } else {
            /* First Call
            5. If this is not a tick where the control tower is attempting to land
               an aircraft, an aircraft should be allowed to take off instead.
            */
            tryTakeOffAircraft();
        }

        //6. Place all aircraft in their appropriate queues by calling
        placeAllAircraftInQueues();

        //Increment TickCount;
        this.ticksElapsed++;
    }

    /**
     * Returns the human-readable string representation of this control tower.
     * <p>
     * The format of the string to return is
     * ControlTower: numTerminals terminals, numAircraft total aircraft
     * (numLanding LAND, numTakeoff TAKEOFF, numLoad LOAD)
     * <p>
     * where numTerminals is the number of terminals, numAircraft is the number of aircraft,
     * umLanding is the number of aircraft in the landing queue, numTakeoff is the number of
     * aircraft in the takeoff queue, and numLoad is the number of aircraft in the loading map.
     *
     * @return string representation of this control tower
     */
    @Override
    public String toString() {
        return String.format("ControlTower: %d terminals"
                        + ", %d total aircraft (%d LAND, %d TAKEOFF, %d LOAD)",
                this.getTerminals().size(), //numTerminals
                this.getAircraft().size(), //numAircraft
                this.getLandingQueue().getAircraftInOrder().size(), //numLanding
                this.getTakeoffQueue().getAircraftInOrder().size(), //numTakeOff
                this.getLoadingAircraft().size());
    }
}
