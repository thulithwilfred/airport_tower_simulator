package towersim.ground;

import towersim.util.EmergencyState;
import towersim.util.NoSpaceException;
import towersim.util.NoSuitableGateException;
import towersim.util.OccupancyLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an airport terminal building, containing several aircraft gates.
 * @ass1
 */
public abstract class Terminal implements EmergencyState, OccupancyLevel {
    /**
     * Maximum possible number of gates allowed at a single terminal.
     * @ass1
     */
    public static final int MAX_NUM_GATES = 6;

    /** Unique terminal number to identify this terminal. */
    private final int terminalNumber;

    /** List of gates in this terminal. */
    private final List<Gate> gates;

    /** Whether or not the terminal is currently in a state of emergency. */
    private boolean emergency;

    /**
     * Creates a new Terminal with the given unique terminal number.
     * <p>
     * It is <b>not</b> the responsibility of the Terminal class to ensure terminal numbers are
     * unique. Instead, the user should check that no other terminal of the same type exists with
     * the same terminal number when instantiating a new terminal.
     * <p>
     * Newly created terminals should not be in a state of emergency by default.
     *
     * @param terminalNumber identifying number of this terminal
     * @ass1
     */
    protected Terminal(int terminalNumber) {
        this.terminalNumber = terminalNumber;
        this.gates = new ArrayList<>();
        this.emergency = false;
    }

    /**
     * Returns this terminal's terminal number.
     *
     * @return terminal number
     * @ass1
     */
    public int getTerminalNumber() {
        return terminalNumber;
    }

    /**
     * Adds a gate to the terminal.
     * <p>
     * If the terminal is currently at maximum capacity ({@link #MAX_NUM_GATES}), then the gate
     * should not be added, and instead a NoSpaceException should be thrown.
     *
     * @param gate gate to add to terminal
     * @throws NoSpaceException if there is no space at the terminal for the new gate
     * @ass1
     */
    public void addGate(Gate gate) throws NoSpaceException {
        if (this.gates.size() == MAX_NUM_GATES) {
            throw new NoSpaceException("Maximum number of gates reached (" + MAX_NUM_GATES + ")");
        }
        this.gates.add(gate);
    }

    /**
     * Returns a list of all gates in the terminal.
     * <p>
     * The order in which gates appear in this list should be the same as the order in which they
     * were added by calling {@link #addGate(Gate)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return list of terminal's gates
     * @ass1
     */
    public List<Gate> getGates() {
        return new ArrayList<>(this.gates);
    }

    /**
     * Finds and returns the first non-occupied gate in this terminal.
     * <p>
     * Gates should be searched in the same order as in {@link #getGates()}.
     * <p>
     * If all gates in this terminal are occupied with an aircraft, throws a
     * {@code NoSuitableGateException}.
     *
     * @return first non-occupied gate in this terminal
     * @throws NoSuitableGateException if all gates in this terminal are occupied
     * @ass1
     */
    public Gate findUnoccupiedGate() throws NoSuitableGateException {
        for (Gate gate : this.gates) {
            if (!gate.isOccupied()) {
                return gate;
            }
        }
        throw new NoSuitableGateException("No unoccupied gate in terminal " + this.terminalNumber);
    }

    /**
     * {@inheritDoc}
     * @ass1
     */
    @Override
    public void declareEmergency() {
        this.emergency = true;
    }

    /**
     * {@inheritDoc}
     * @ass1
     */
    @Override
    public void clearEmergency() {
        this.emergency = false;
    }

    /**
     * {@inheritDoc}
     * @ass1
     */
    @Override
    public boolean hasEmergency() {
        return emergency;
    }

    /**
     * Returns the ratio of occupied gates to total gates as a percentage from 0 to 100.
     * <p>
     * If there are no gates in this terminal, 0 should be returned.
     * The ratio should be rounded to the nearest whole percentage.
     * <p>
     * For example, if the terminal has 3 gates and 2 are occupied, the ratio should be
     * {@code 2/3 = 0.666...} and the rounded percentage is 67%, so 67 should be returned.
     *
     * @return percentage of occupied gates in this terminal, 0 to 100
     * @ass1
     */
    @Override
    public int calculateOccupancyLevel() {
        int numOccupiedGates = 0;
        for (Gate gate : this.gates) {
            if (gate.isOccupied()) {
                numOccupiedGates++;
            }
        }
        return (int) Math.round(100 * (double) numOccupiedGates / this.gates.size());
    }

    /**
     * Returns the human-readable string representation of this terminal.
     * <p>
     * The format of the string to return is
     * <pre>TerminalType terminalNum, numGates gates</pre>
     * where {@code TerminalType} is the class name of the concrete terminal class
     * (i.e. AirplaneTerminal or HelicopterTerminal),
     * {@code terminalNum} is the terminal number and {@code numGates} is the number of gates
     * in the terminal. If the terminal is currently in a state of emergency, the format of the
     * string to return is
     * <pre>TerminalType terminalNum, numGates gates (EMERGENCY)</pre>
     * For example, {@code "Terminal 3, 5 gates (EMERGENCY)"}.
     * <p>
     * <b>Hint:</b> {@code Object#getClass().getSimpleName()} can be used to find the class name
     * of an object.
     *
     * @return string representation of this terminal
     * @ass1
     */
    @Override
    public String toString() {
        return String.format("%s %d, %d gates%s",
                this.getClass().getSimpleName(),
                this.terminalNumber,
                this.gates.size(),
                this.emergency ? " (EMERGENCY)" : "");
    }
}
