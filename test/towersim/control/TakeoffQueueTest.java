package towersim.control;


import org.junit.Before;
import org.junit.Test;
import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TakeoffQueueTest {
    private Aircraft passengerAircraft1;
    private Aircraft passengerAircraft2;
    private Aircraft passengerAircraft3;
    private Aircraft fCraft1, fCraft2, fCraft3, fCraft4, pCraft1;

    TaskList taskList1;

    @Before
    public void setup() {

        taskList1 = new TaskList(List.of(
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 100),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT)));

        TaskList taskList2 = new TaskList(List.of(
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 50),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT)));

        TaskList taskList3 = new TaskList(List.of(
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 35),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT)));


        this.passengerAircraft1 = new PassengerAircraft("AP1",
                AircraftCharacteristics.AIRBUS_A320,
                taskList1,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity / 10, 0);

        this.passengerAircraft2 = new PassengerAircraft("AP2",
                AircraftCharacteristics.AIRBUS_A320,
                taskList2,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity, 0);

        this.passengerAircraft3 = new PassengerAircraft("HP1",
                AircraftCharacteristics.ROBINSON_R44,
                taskList3,
                AircraftCharacteristics.ROBINSON_R44.fuelCapacity, 0);


        //Adding Extras
        this.fCraft1 = new FreightAircraft("fcraft1",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity, 0);

        this.fCraft2 = new FreightAircraft("fcraft2",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity, 0);

        this.fCraft3 = new FreightAircraft("fcraft3",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity, 0);

        this.fCraft4 = new FreightAircraft("fcraft4_low_fuel",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0.20, 0);

        this.pCraft1 = new FreightAircraft("pcraft1_low_fuel",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0, 0);

    }

    @Test
    public void containsAircraft_Test() {
        TakeoffQueue q1 = new TakeoffQueue();
        q1.addAircraft(fCraft3);
        q1.addAircraft(fCraft2);
        q1.addAircraft(passengerAircraft3);
        q1.addAircraft(fCraft1);

        boolean pass = q1.containsAircraft(fCraft3) && q1.containsAircraft(fCraft2) && q1.containsAircraft(passengerAircraft3) && q1.containsAircraft(fCraft1);
        assertTrue("These should all be contained", pass);

        boolean fail = q1.containsAircraft(fCraft4);
        assertTrue("These should all be contained", !fail);
    }

    @Test
    public void getAircraftInOrder_Test2() {

        TakeoffQueue q1 = new TakeoffQueue();
        q1.addAircraft(fCraft3);
        q1.addAircraft(fCraft2);
        q1.addAircraft(passengerAircraft3);
        q1.addAircraft(fCraft1);


        List<Aircraft> ordered = q1.getAircraftInOrder();

        ordered.add(passengerAircraft2);
        ordered.add(fCraft4);

        assertNotEquals("Original list should not be changed", ordered.size(), q1.getAircraftInOrder().size());
        assertEquals("Original list should not be changed", q1.getAircraftInOrder().size(), 4);
        assertTrue("Returned ordered list modification should not affect original",
                !(q1.getAircraftInOrder().contains(passengerAircraft2) || q1.getAircraftInOrder().contains(fCraft4)));

    }


    @Test
    public void getAircraftInOrder_Test1() {

        TakeoffQueue q1 = new TakeoffQueue();
        q1.addAircraft(fCraft3);
        q1.addAircraft(fCraft2);
        q1.addAircraft(passengerAircraft3);
        q1.addAircraft(fCraft1);

        ArrayList<Aircraft> expected = new ArrayList<>();

        expected.add(fCraft3);
        expected.add(fCraft2);
        expected.add(passengerAircraft3);
        expected.add(fCraft1);

        List<Aircraft> ordered = q1.getAircraftInOrder();

        assertEquals("Size mismatch", expected.size(), ordered.size());

        for (int i = 0; i < expected.size(); ++i) {
            assertEquals("Order mismatch", expected.get(i), ordered.get(i));
        }
    }

    @Test
    public void peekAircraft_Test2() {
        TakeoffQueue q1 = new TakeoffQueue();
        q1.addAircraft(passengerAircraft2);
        q1.addAircraft(passengerAircraft3);
        q1.addAircraft(pCraft1); //Low Fuel
        q1.addAircraft(fCraft2);

        passengerAircraft2.declareEmergency();

        assertEquals("Peek mismatch", q1.peekAircraft(), passengerAircraft2);
        assertEquals("Remove Mismatch", q1.removeAircraft(), passengerAircraft2);

        assertEquals("Peek mismatch", q1.peekAircraft(), passengerAircraft3);
        assertEquals("Remove Mismatch", q1.removeAircraft(), passengerAircraft3);

        assertEquals("Peek mismatch", q1.peekAircraft(), pCraft1);
        assertEquals("Remove Mismatch", q1.removeAircraft(), pCraft1);

        assertEquals("Peek mismatch", q1.peekAircraft(), fCraft2);
        assertEquals("Remove Mismatch", q1.removeAircraft(), fCraft2);

        assertEquals("Expected Null", q1.peekAircraft(), null);
        assertEquals("Expected Null", q1.removeAircraft(), null);
    }

    @Test
    public void peekAircraft_Test1() {

        TakeoffQueue q1 = new TakeoffQueue();
        q1.addAircraft(fCraft4);
        q1.addAircraft(fCraft2);
        q1.addAircraft(passengerAircraft2);
        q1.addAircraft(passengerAircraft3);
        q1.addAircraft(pCraft1); //Low Fuel

        assertEquals("Peek mismatch", q1.peekAircraft(), fCraft4);
        fCraft2.declareEmergency();
        assertEquals("Peek mismatch", q1.peekAircraft(), fCraft4);
    }
}
