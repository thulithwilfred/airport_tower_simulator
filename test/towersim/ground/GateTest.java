package towersim.ground;

import org.junit.Before;
import org.junit.Test;
import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.PassengerAircraft;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import towersim.util.NoSpaceException;

import java.util.List;

import static org.junit.Assert.*;

public class GateTest {
    private Gate gate, gateX;
    private Aircraft aircraft1;
    private Aircraft aircraft2;

    @Before
    public void setup() {
        this.gate = new Gate(2);
        this.aircraft1 = new PassengerAircraft("ABC123", AircraftCharacteristics.AIRBUS_A320,
                new TaskList(List.of(
                        new Task(TaskType.AWAY),
                        new Task(TaskType.LAND),
                        new Task(TaskType.LOAD),
                        new Task(TaskType.TAKEOFF))),
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity,
                AircraftCharacteristics.AIRBUS_A320.passengerCapacity);

        this.aircraft2 = new PassengerAircraft("XYZ987", AircraftCharacteristics.AIRBUS_A320,
                new TaskList(List.of(
                        new Task(TaskType.AWAY),
                        new Task(TaskType.LAND),
                        new Task(TaskType.WAIT),
                        new Task(TaskType.LOAD),
                        new Task(TaskType.TAKEOFF))),
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity / 2,
                AircraftCharacteristics.AIRBUS_A320.passengerCapacity / 2);
    }

    @Test
    public void hashcode_Test2() {
        this.gateX = new Gate(5);

        try {
            gate.parkAircraft(aircraft2);
        } catch (NoSpaceException e) {
            fail("parkAircraft() should not throw an exception if the gate is unoccupied");
        }

        assertTrue("These gates should be equal", !gate.equals(gateX));
        assertNotEquals("Two equals() gates should have the same hashcode", gateX.hashCode(), gate.hashCode());
    }


    @Test
    public void hashcode_Test1() {
        this.gateX = new Gate(2);

        try {
            gate.parkAircraft(aircraft2);
        } catch (NoSpaceException e) {
            fail("parkAircraft() should not throw an exception if the gate is unoccupied");
        }

        assertTrue("These gates should be equal", gate.equals(gateX));
        assertEquals("Two equals() gates should have the same hashcode", gateX.hashCode(), gate.hashCode());
    }

    @Test
    public void equals_Test2() {
        this.gateX = new Gate(5);
        assertTrue("These gates should not be equal", !this.gateX.equals(gate));
        assertTrue("These gates should not be equal", !this.gateX.equals(null));
    }

    @Test
    public void equals_Test1() {
        this.gateX = new Gate(2);
        assertTrue("These gates should be equal", this.gate.equals(gateX));
    }

    /**
     * Test gate.encode();
     */
    @Test
    public void encode_Test2() {
        String expected = "2:empty";
        assertEquals("Encoded message should equal", expected, gate.encode());

        try {
            gate.parkAircraft(aircraft2);
        } catch (NoSpaceException e) {
            fail("parkAircraft() should not throw an exception if the gate is unoccupied");
        }
        String expectedNew = "2:XYZ987";
        assertEquals("Encoded message should equal", expectedNew, gate.encode());
    }

    /**
     * Test gate.encode();
     */
    @Test
    public void encode_Test1() {
        try {
            gate.parkAircraft(aircraft1);
        } catch (NoSpaceException e) {
            fail("parkAircraft() should not throw an exception if the gate is unoccupied");
        }
        String expected = "2:ABC123";
        assertEquals("Encoded message should equal", expected, gate.encode());
    }

    @Test
    public void getGateNumber_Test() {
        assertEquals("getGateNumber() should return the gate number passed to Gate(int)",
                2, gate.getGateNumber());
    }

    @Test
    public void isOccupied_FalseTest() {
        assertFalse("isOccupied() should return false for a newly created gate",
                gate.isOccupied());
    }

    @Test
    public void isOccupied_TrueTest() {
        try {
            gate.parkAircraft(aircraft1);
        } catch (NoSpaceException e) {
            fail("parkAircraft() should not throw an exception if the gate is unoccupied");
        }
        assertTrue("isOccupied() should return true after an aircraft has been added to a gate",
                gate.isOccupied());
    }

    @Test
    public void parkAircraft_OccupiedTest() {
        try {
            gate.parkAircraft(aircraft1);
        } catch (NoSpaceException e) {
            fail("parkAircraft() should not throw an exception if the gate is unoccupied");
        }
        try {
            gate.parkAircraft(aircraft1);
            fail("parkAircraft() should throw an exception if the gate is already occupied");
        } catch (NoSpaceException expected) {
        }
    }

    @Test
    public void aircraftLeaves_OccupiedTest() {
        try {
            gate.parkAircraft(aircraft1);
        } catch (NoSpaceException e) {
            fail("parkAircraft() should not throw an exception if the gate is unoccupied");
        }
        gate.aircraftLeaves();
        assertFalse("isOccupied() should return false after calling aircraftLeaves()",
                gate.isOccupied());
        assertNull("getAircraftAtGate() should return null after calling aircraftLeaves()",
                gate.getAircraftAtGate());
    }

    @Test
    public void aircraftLeaves_UnOccupiedTest() {
        gate.aircraftLeaves();
        assertFalse("isOccupied() should return false after calling aircraftLeaves()",
                gate.isOccupied());
        assertNull("getAircraftAtGate() should return null after calling aircraftLeaves()",
                gate.getAircraftAtGate());
    }

    @Test
    public void getAircraftAtGate_OccupiedTest() {
        try {
            gate.parkAircraft(aircraft1);
        } catch (NoSpaceException e) {
            fail("parkAircraft() should not throw an exception if the gate is unoccupied");
        }

        assertEquals("getAircraftAtGate() should return the aircraft added via parkAircraft()",
                aircraft1, gate.getAircraftAtGate());
    }

    @Test
    public void getAircraftAtGate_UnoccupiedTest() {
        assertNull("getAircraftAtGate() should return null if no aircraft have been parked yet",
                gate.getAircraftAtGate());
    }

    @Test
    public void toString_UnoccupiedTest() {
        assertEquals("Gate 2 [empty]", gate.toString());
    }

    @Test
    public void toString_OccupiedTest() {
        try {
            gate.parkAircraft(aircraft1);
        } catch (NoSpaceException e) {
            fail("parkAircraft() should not throw an exception if the gate is unoccupied");
        }

        assertEquals("Gate 2 [ABC123]", gate.toString());
        gate.aircraftLeaves();

        try {
            gate.parkAircraft(aircraft2);
        } catch (NoSpaceException e) {
            fail("parkAircraft() should not throw an exception if the gate is unoccupied");
        }

        assertEquals("Gate 2 [XYZ987]", gate.toString());
    }
}
