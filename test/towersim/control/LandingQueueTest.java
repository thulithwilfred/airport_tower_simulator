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

public class LandingQueueTest {

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
    public void removeAircraft_Test4() {

        LandingQueue q1 = new LandingQueue();
        q1.addAircraft(fCraft2);
        q1.addAircraft(passengerAircraft2);
        q1.addAircraft(pCraft1); //Low Fuel
        q1.addAircraft(passengerAircraft3);
        q1.addAircraft(fCraft4); //low Fuel

        assertEquals("Remove Mismatch", pCraft1, q1.removeAircraft());
        assertEquals("Remove Mismatch", fCraft4, q1.removeAircraft());
        assertEquals("Remove Mismatch", passengerAircraft2, q1.removeAircraft());
        assertEquals("Remove Mismatch", passengerAircraft3, q1.removeAircraft());
        assertEquals("Remove Mismatch", fCraft2, q1.removeAircraft());
    }

    @Test
    public void removeAircraft_Test3() {
        //Test the removal order for Rule 1.
        //Test removal of duplicates
        LandingQueue q1 = new LandingQueue();
        q1.addAircraft(fCraft3);
        q1.addAircraft(fCraft3);
        q1.addAircraft(fCraft3);
        q1.addAircraft(passengerAircraft3);
        q1.addAircraft(passengerAircraft3);
        q1.addAircraft(passengerAircraft2);
        q1.addAircraft(passengerAircraft2);
        q1.addAircraft(fCraft1);

        //Incorrect use of expected/actual here too lazy to change.
        assertEquals("Aircraft mismatch", q1.removeAircraft(), passengerAircraft3);
        assertEquals("Aircraft mismatch", q1.removeAircraft(), passengerAircraft3);
        assertEquals("Aircraft mismatch", q1.removeAircraft(), passengerAircraft2);
        assertEquals("Aircraft mismatch", q1.removeAircraft(), passengerAircraft2);
        assertEquals("Aircraft mismatch", q1.removeAircraft(), fCraft3);
        assertEquals("Aircraft mismatch", q1.removeAircraft(), fCraft3);
        assertEquals("Aircraft mismatch", q1.removeAircraft(), fCraft3);
        assertEquals("Aircraft mismatch", q1.removeAircraft(), fCraft1);
        assertEquals("Aircraft mismatch", null, q1.removeAircraft());
    }

    @Test
    public void removeAircraft_Test2() {
        //Test the removal order for Rule 1.
        LandingQueue q1 = new LandingQueue();
        q1.addAircraft(fCraft4);
        fCraft4.declareEmergency();
        q1.addAircraft(fCraft2);
        q1.addAircraft(passengerAircraft2);
        q1.addAircraft(passengerAircraft3);
        q1.addAircraft(pCraft1); //Low Fuel

        ArrayList<Aircraft> expected = new ArrayList<>();

        expected.add(pCraft1);
        expected.add(passengerAircraft2);
        expected.add(passengerAircraft3);
        expected.add(fCraft2);

        assertEquals("Aircraft mismatch", q1.removeAircraft(), fCraft4);
        List<Aircraft> ordered = q1.getAircraftInOrder();
        assertEquals("Size Mismatch", ordered.size(), expected.size());
        //Sanity Check
        for (int i = 0; i < ordered.size(); ++i) {
            assertEquals("Ordered Mismatch", ordered.get(i), expected.get(i));
        }
        //Test the order in which items should be removed based on urgency rules.
        assertEquals("Peek Mismatch", q1.peekAircraft(), pCraft1);
        assertEquals("Aircraft mismatch", q1.removeAircraft(), pCraft1);

        assertEquals("Peek Mismatch", q1.peekAircraft(), passengerAircraft2);
        assertEquals("Aircraft mismatch", q1.removeAircraft(), passengerAircraft2);

        assertEquals("Peek Mismatch", q1.peekAircraft(), passengerAircraft3);
        assertEquals("Aircraft mismatch", q1.removeAircraft(), passengerAircraft3);

        assertEquals("Peek Mismatch", q1.peekAircraft(), fCraft2);
        assertEquals("Aircraft mismatch", q1.removeAircraft(), fCraft2);

        assertEquals("Peek Mismatch", null, q1.peekAircraft());
        assertEquals("Expected Null", null, q1.removeAircraft());
    }

    @Test
    public void removeAircraft_Test1() {
        LandingQueue q1 = new LandingQueue();
        assertEquals("Expected Null", null, q1.removeAircraft());
    }

    @Test
    public void containsAircraft_Test() {
        LandingQueue q1 = new LandingQueue();
        //Empty test
        assertTrue("This aircraft should not be here", !q1.containsAircraft(passengerAircraft3));

        q1.addAircraft(passengerAircraft2);
        q1.addAircraft(fCraft1);
        q1.addAircraft(fCraft2);
        q1.addAircraft(fCraft4);

        assertTrue("This aircraft should not be here", !q1.containsAircraft(passengerAircraft3));

        assertTrue("This aircraft must be here", q1.containsAircraft(fCraft4)
                && q1.containsAircraft(fCraft1) && q1.containsAircraft(fCraft2));
    }

    @Test
    public void getAircraftInOrderModify_Test() {
        //Adding or removing elements from the returned list should not affect the original queue
        LandingQueue q1 = new LandingQueue();
        q1.addAircraft(fCraft1);
        q1.addAircraft(fCraft2);
        q1.addAircraft(fCraft3);

        List<Aircraft> orderedQueue = q1.getAircraftInOrder();

        orderedQueue.add(fCraft4);
        orderedQueue.add(passengerAircraft2);

        assertNotEquals("Modified list size must be different", orderedQueue.size(), q1.getAircraftInOrder().size());

        //Ordered queue should not contain fcraft4 and passengerAircraft2
        List<Aircraft> orderedQueueNew = q1.getAircraftInOrder();
        boolean expected = (orderedQueueNew.contains(fCraft4) || orderedQueueNew.contains(passengerAircraft2));

        assertTrue("Adding or removing elements from the returned list should not affect the original queue", !expected);
    }

    @Test
    public void getAircraftInOrder_Test4() {
        //Test Duplicates
        LandingQueue q1 = new LandingQueue();
        q1.addAircraft(fCraft3);
        q1.addAircraft(fCraft3);
        q1.addAircraft(fCraft3);
        q1.addAircraft(passengerAircraft3);
        q1.addAircraft(passengerAircraft3);
        q1.addAircraft(passengerAircraft2);
        q1.addAircraft(passengerAircraft2);
        q1.addAircraft(fCraft1);

        passengerAircraft2.declareEmergency();
        List<Aircraft> ordered = q1.getAircraftInOrder();

        ArrayList<Aircraft> expected = new ArrayList<>();

        expected.add(passengerAircraft2);
        expected.add(passengerAircraft2);
        expected.add(passengerAircraft3);
        expected.add(passengerAircraft3);
        expected.add(fCraft3);
        expected.add(fCraft3);
        expected.add(fCraft3);
        expected.add(fCraft1);

        for (int i = 0; i < ordered.size(); ++i) {
            assertEquals("Order mismatch with duplicates", expected.get(i), ordered.get(i));
        }
    }

    @Test
    public void getAircraftInOrder_Test3() {
        //Test the FIFO order for air crafts with no additional rules. i.e no emergency, low fuel.
        //Ok I Lied, i'm adding a cheeky passenger aircraft in here for the bois. 
        LandingQueue q1 = new LandingQueue();
        q1.addAircraft(fCraft3);
        q1.addAircraft(fCraft2);
        q1.addAircraft(passengerAircraft3);
        q1.addAircraft(fCraft1);

        ArrayList<Aircraft> expected = new ArrayList<>();

        expected.add(passengerAircraft3);
        expected.add(fCraft3);
        expected.add(fCraft2);
        expected.add(fCraft1);

        List<Aircraft> ordered = q1.getAircraftInOrder();

        assertEquals("Size mismatch", expected.size(), ordered.size());

        for (int i = 0; i < expected.size(); ++i) {
            assertEquals("Order mismatch", expected.get(i), ordered.get(i));
        }
    }

    @Test
    public void getAircraftInOrder_Test2() {
        //R1: Emergency Testing
        LandingQueue q1 = new LandingQueue();
        q1.addAircraft(fCraft4);
        q1.addAircraft(fCraft2);
        q1.addAircraft(fCraft1);
        q1.addAircraft(fCraft3);
        q1.addAircraft(passengerAircraft2);
        q1.addAircraft(passengerAircraft3);
        q1.addAircraft(pCraft1); //Low Fuel

        fCraft4.declareEmergency();
        fCraft2.declareEmergency();
        fCraft1.declareEmergency();

        ArrayList<Aircraft> expected = new ArrayList<>();
        //Emergency FIFO
        expected.add(fCraft4);
        expected.add(fCraft2);
        expected.add(fCraft1);
        //Fuel
        expected.add(pCraft1);
        //Passenger Aircraft over freight
        expected.add(passengerAircraft2);
        expected.add(passengerAircraft3);
        //Freight FIFO
        expected.add(fCraft3);

        List<Aircraft> orderedQueue = q1.getAircraftInOrder();

        assertEquals("List size must match", expected.size(), orderedQueue.size());

        for (int i = 0; i < orderedQueue.size(); ++i) {
            //System.out.println(orderedQueue.get(i));
            //Invokes the equals() methods, this should be safe
            assertEquals("Order must match", expected.get(i), orderedQueue.get(i));
        }
    }

    @Test
    public void getAircraftInOrder_Test1() {

        LandingQueue q1 = new LandingQueue();
        q1.addAircraft(fCraft1);
        q1.addAircraft(fCraft2);
        q1.addAircraft(fCraft3);
        q1.addAircraft(fCraft4);
        q1.addAircraft(passengerAircraft2);

        fCraft3.declareEmergency();
        List<Aircraft> orderedQueue = q1.getAircraftInOrder();

        ArrayList<Aircraft> expected = new ArrayList<>();

        expected.add(fCraft3); //Emergency
        expected.add(fCraft4); //Low Fuel
        expected.add(passengerAircraft2); //Passenger
        expected.add(fCraft1); //FIFO order
        expected.add(fCraft2); //FIFO order

        assertEquals("List size mismatch", expected.size(), orderedQueue.size());

        for (int i = 0; i < orderedQueue.size(); ++i) {
            //System.out.println(orderedQueue.get(i));
            assertEquals("Ordered Queue Incorrect", expected.get(i), orderedQueue.get(i));
        }

    }

    @Test
    public void peekAircraft_Test5() {
        //Test Rule 4, First Added
        LandingQueue q1 = new LandingQueue();

        assertEquals("Expected Null", null, q1.peekAircraft());
    }

    @Test
    public void peekAircraft_Test4() {
        //Test Rule 4, First Added
        LandingQueue q1 = new LandingQueue();


        Aircraft freightCraft1 = new FreightAircraft("XX11F",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity, 0);

        Aircraft freightCraft2 = new FreightAircraft("XX12F",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity, 0);

        Aircraft freightCraft3 = new FreightAircraft("XX14F",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity, 0);

        Aircraft freightCraft4 = new FreightAircraft("XX14F",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0.20, 0);


        q1.addAircraft(freightCraft3);
        q1.addAircraft(freightCraft1);
        q1.addAircraft(freightCraft2);

        //First added
        assertEquals("Peek Mismatch", freightCraft3, q1.peekAircraft());
        //Low Fuel
        q1.addAircraft(freightCraft4);

        assertEquals("Peek Mismatch", freightCraft4, q1.peekAircraft());

        freightCraft1.declareEmergency();
        assertEquals("Peek Mismatch", freightCraft1, q1.peekAircraft());
    }

    @Test
    public void peekAircraft_Test3() {
        //Test Passenger Rule
        LandingQueue q1 = new LandingQueue();

        Aircraft passCraft1 = new PassengerAircraft("PASS",
                AircraftCharacteristics.AIRBUS_A320,
                taskList1,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity, 0);


        Aircraft freightCraft1 = new FreightAircraft("XX11F",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity, 0);

        Aircraft freightCraft2 = new FreightAircraft("XX12F",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity, 0);

        Aircraft freightCraft3 = new FreightAircraft("XX14F",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity, 0);

        q1.addAircraft(freightCraft3);
        q1.addAircraft(freightCraft1);
        q1.addAircraft(passCraft1);
        q1.addAircraft(freightCraft2);

        assertEquals("Peek Mismatch", passCraft1, q1.peekAircraft());
    }


    @Test
    public void peekAircraft_Test2() {
        //Test Fuel Rule
        LandingQueue q1 = new LandingQueue();

        Aircraft testCraft = new PassengerAircraft("XX11",
                AircraftCharacteristics.AIRBUS_A320,
                taskList1,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity * 0.05, 0);

        q1.addAircraft(passengerAircraft2);
        q1.addAircraft(passengerAircraft3);
        q1.addAircraft(testCraft);
        q1.addAircraft(passengerAircraft1);


        assertEquals("Peek Mismatch", testCraft, q1.peekAircraft());
    }

    @Test
    public void peekAircraft_Test1() {
        //Test Emergency Rule
        LandingQueue q1 = new LandingQueue();

        Aircraft freightCraft3 = new FreightAircraft("XX14F",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity, 0);

        q1.addAircraft(passengerAircraft1);
        q1.addAircraft(passengerAircraft2);
        q1.addAircraft(freightCraft3);

        assertEquals("Peek Mismatch", passengerAircraft1, q1.peekAircraft());

        passengerAircraft3.declareEmergency();
        q1.addAircraft(passengerAircraft3);
        assertEquals("Peek Mismatch", passengerAircraft3, q1.peekAircraft());

        passengerAircraft1.declareEmergency();
        assertEquals("Peek Mismatch", passengerAircraft1, q1.peekAircraft());

        passengerAircraft1.clearEmergency();
        passengerAircraft3.clearEmergency();
        passengerAircraft2.declareEmergency();

        assertEquals("Peek Mismatch", passengerAircraft2, q1.peekAircraft());
    }

    @Test
    public void addAircraft_Test() {
        LandingQueue q1 = new LandingQueue();

        q1.addAircraft(passengerAircraft1);
        q1.addAircraft(passengerAircraft2);

        assertEquals("Aircraft added incorrectly", q1.peekAircraft(), passengerAircraft1);
        assertTrue("Aircraft added incorrectly", q1.containsAircraft(passengerAircraft2));
        assertTrue("Aircraft added incorrectly", q1.containsAircraft(passengerAircraft1));
    }


}
