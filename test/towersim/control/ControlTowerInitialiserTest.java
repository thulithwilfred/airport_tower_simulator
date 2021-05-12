package towersim.control;

import org.junit.Before;
import org.junit.Test;
import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
import towersim.ground.Terminal;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import towersim.util.MalformedSaveException;

import static org.junit.Assert.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControlTowerInitialiserTest {

    String goodP = String.join(System.lineSeparator(),
            "1", "WAYNE:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0");
    String goodF = String.join(System.lineSeparator(),
            "1", "WAYNE_F:BOEING_747_8F:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0");
    String goodTl = "WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND";


    private Aircraft fCraft1, fCraft2, fCraft3, fCraft4, pCraft1, pCraft2, pCraft3;
    private ArrayList<Aircraft> craftsList;

    @Before
    public void setup() {
        TaskList taskList1 = new TaskList(List.of(
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 100),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT)));

        TaskList taskListTakeoff = new TaskList(List.of(
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 100)));

        TaskList taskListLand = new TaskList(List.of(
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 100),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY)));

        TaskList taskListLoad = new TaskList(List.of(
                new Task(TaskType.LOAD, 70),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT)));

        TaskList taskListAway = new TaskList(List.of(
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 70),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY)));

        this.fCraft1 = new FreightAircraft("fcraft1",
                AircraftCharacteristics.BOEING_747_8F,
                taskListLand,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity, 0);

        this.fCraft2 = new FreightAircraft("fcraft2",
                AircraftCharacteristics.BOEING_747_8F,
                taskListTakeoff,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity, 0);

        this.fCraft3 = new FreightAircraft("fcraft3",
                AircraftCharacteristics.BOEING_747_8F,
                taskListLoad,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity, 0);

        this.fCraft4 = new FreightAircraft("fcraft4_low_fuel",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0.20, 0);

        this.pCraft1 = new FreightAircraft("VH-BFK",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0, 0);

        this.pCraft2 = new FreightAircraft("UPS119",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0, 0);
        this.pCraft3 = new FreightAircraft("UTD302",
                AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0, 0);

        craftsList = new ArrayList<Aircraft>(List.of(pCraft1, fCraft1, fCraft2, fCraft3, fCraft4, pCraft2, pCraft3));

    }

    @Test
    public void createControlTower_Test() throws MalformedSaveException {

    }

    @Test
    public void loadTerminals_Test2() throws MalformedSaveException {
        String fContent = String.join(System.lineSeparator(), "1", "AirplaneTerminal:1:false:2", "69:UTD302", "2:empty");
        String expected = "AirplaneTerminal:1:false:2" + System.lineSeparator() + "69:UTD302" + System.lineSeparator() + "2:empty";
        try {
            List<Terminal> loadedTerminals = ControlTowerInitialiser.loadTerminalsWithGates(new StringReader(fContent), craftsList);
            assertEquals("Terminal Load mismatch", expected, loadedTerminals.get(0).encode());
        } catch (IOException e) {
            fail("Should not be malformed");
        }
    }


    @Test
    public void loadTerminals_Test() throws MalformedSaveException {
        try {
            BufferedReader br = new BufferedReader(new FileReader("saves/terminalsWithGates_basic.txt"));
            List<Terminal> loadedTerminals = ControlTowerInitialiser.loadTerminalsWithGates(br, craftsList);
            assertEquals("Terminal list size mismatch", 5, loadedTerminals.size());
        } catch (IOException e) {
            fail("Should not be malformed");
        }
    }

    @Test
    public void loadQueues_readLoad_Test1() throws MalformedSaveException {
        LandingQueue landingQueue = new LandingQueue();
        TakeoffQueue takeoffQueue = new TakeoffQueue();
        Map<Aircraft, Integer> loadingMap = new HashMap<Aircraft, Integer>();

        String fContent = String.join(System.lineSeparator(),
                "TakeoffQueue:2", "fcraft2,fcraft3", "LandingQueue:1", "VH-BFK",
                "LoadingAircraft:2", "fcraft4_low_fuel:99,fcraft1:4");

        String takeOffExpected = "TakeoffQueue:2" + System.lineSeparator() + "fcraft2,fcraft3";
        String landingExpected = "LandingQueue:1" + System.lineSeparator() + "VH-BFK";

        try {
            ControlTowerInitialiser.loadQueues(new StringReader(fContent), craftsList, takeoffQueue, landingQueue, loadingMap);
            assertEquals("Takeoff encode mismatch", takeOffExpected, takeoffQueue.encode());
            assertEquals("Landing encode mismatch", landingExpected, landingQueue.encode());

            assertEquals("Map mismatch", 4, loadingMap.get(fCraft1).intValue());
            assertEquals("Map mismatch", 99, loadingMap.get(fCraft4).intValue());
        } catch (IOException e) {
            fail("Should not be malformed");
        }
    }


    @Test
    public void loadQueues_Test2() throws MalformedSaveException {
        LandingQueue landingQueue = new LandingQueue();
        TakeoffQueue takeoffQueue = new TakeoffQueue();
        Map<Aircraft, Integer> loadingMap = new HashMap<Aircraft, Integer>();

        try {
            BufferedReader br = new BufferedReader(new FileReader("saves/queues_default.txt"));
            ControlTowerInitialiser.loadQueues(br, craftsList, takeoffQueue, landingQueue, loadingMap);
            //System.out.println(landingQueue.peekAircraft());

            String expectedT = "TakeoffQueue:0";
            String expectedL = "LandingQueue:0";
            assertEquals("Encode Mismatch", expectedT, takeoffQueue.encode());
            assertEquals("Encode Mismatch", expectedL, landingQueue.encode());
        } catch (IOException e) {
            fail("Should not be malformed");
        }
    }

    @Test
    public void loadQueues_Test1() throws MalformedSaveException {
        LandingQueue landingQueue = new LandingQueue();
        TakeoffQueue takeoffQueue = new TakeoffQueue();
        Map<Aircraft, Integer> loadingMap = new HashMap<Aircraft, Integer>();

        try {
            BufferedReader br = new BufferedReader(new FileReader("saves/queues_basic.txt"));
            ControlTowerInitialiser.loadQueues(br, craftsList, takeoffQueue, landingQueue, loadingMap);
            //System.out.println(landingQueue.peekAircraft());

            String expectedT = "TakeoffQueue:0";
            String expectedL = "LandingQueue:1" + System.lineSeparator() + "VH-BFK";
            assertEquals("Encode Mismatch", expectedT, takeoffQueue.encode());
            assertEquals("Encode Mismatch", expectedL, landingQueue.encode());
        } catch (IOException e) {
            fail("Should not be malformed");
        }
    }

    @Test
    public void loadAircraft_readComplete_Test1() throws MalformedSaveException {
        String fContent = String.join(System.lineSeparator(),
                "4", "WAYNE:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0",
                "UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0",
                "VH-BFK:ROBINSON_R44:LAND,WAIT,LOAD@75,TAKEOFF,AWAY,AWAY:40.00:false:4",
                "UPS119:BOEING_747_8F:WAIT,LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND:4000.00:false:1414");

        String expected1 = "WAYNE:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0";
        String expected2 = "UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0";
        String expected3 = "VH-BFK:ROBINSON_R44:LAND,WAIT,LOAD@75,TAKEOFF,AWAY,AWAY:40.00:false:4";
        String expected4 = "UPS119:BOEING_747_8F:WAIT,LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND:4000.00:false:1414";


        try {
            List<Aircraft> loadedList = ControlTowerInitialiser.loadAircraft(new StringReader(fContent));
            assertEquals("Size must match", loadedList.size(), 4);
            assertEquals("Enoded must match", loadedList.get(0).encode(), expected1);
            assertEquals("Enoded must match", loadedList.get(1).encode(), expected2);
            assertEquals("Enoded must match", loadedList.get(2).encode(), expected3);
            assertEquals("Enoded must match", loadedList.get(3).encode(), expected4);
        } catch (IOException e) {
            fail("Should not be malformed");
        }
    }

    //Following Tests are for readTaskList()
    @Test(expected = MalformedSaveException.class)
    public void readTaskList_invalid_Test2() throws MalformedSaveException {
        //AWAY -> WAIT is invalid
        String fContent = String.join(System.lineSeparator(),
                //TODO AWAY@60 is valid
                "WAIT,LOAD@10,TAKEOFF,AWAY,AWAY,AWAY, WAIT,LAND");
        ControlTowerInitialiser.readTaskList(fContent);
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList_invalid_Test1() throws MalformedSaveException {
        //Land -> Takeoff is invalid
        String fContent = String.join(System.lineSeparator(),
                //TODO AWAY@60 is valid
                "WAIT,LOAD@10,TAKEOFF,AWAY,AWAY,AWAY,LAND,TAKEOFF");
        ControlTowerInitialiser.readTaskList(fContent);
    }


    @Test(expected = MalformedSaveException.class)
    public void readTaskList_type_Test6() throws MalformedSaveException {
        //Too many @
        String fContent = String.join(System.lineSeparator(),
                //TODO AWAY@60 is valid
                "WAIT,LOAD@10,TAKEOFF@12,AWAY,AWAY@5@5,AWAY,LAND");
        ControlTowerInitialiser.readTaskList(fContent);
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList_type_Test5() throws MalformedSaveException {
        //Too many @
        String fContent = String.join(System.lineSeparator(),
                //TODO AWAY@60 is valid
                "WAIT,LOAD@10,TAKEOFF@12@12,AWAY,AWAY,AWAY,LAND");
        ControlTowerInitialiser.readTaskList(fContent);
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList_type_Test4() throws MalformedSaveException {
        //Too many @
        String fContent = String.join(System.lineSeparator(),
                "WAIT,LOAD@@100,TAKEOFF,AWAY,AWAY,AWAY,LAND");
        ControlTowerInitialiser.readTaskList(fContent);
    }


    @Test(expected = MalformedSaveException.class)
    public void readTaskList_type_Test3() throws MalformedSaveException {
        //Invalid Load % (negative)
        String fContent = String.join(System.lineSeparator(),
                "WAIT,LOAD@-1,TAKEOFF,AWAY,AWAY,AWAY,LAND");
        ControlTowerInitialiser.readTaskList(fContent);
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList_type_Test2() throws MalformedSaveException {
        //Invalid Load %
        String fContent = String.join(System.lineSeparator(),
                "WAIT,LOAD@9x0,TAKEOFF,AWAY,AWAY,AWAY,LAND");
        ControlTowerInitialiser.readTaskList(fContent);
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList_type_Test1() throws MalformedSaveException {
        //Invalid Type
        String fContent = String.join(System.lineSeparator(),
                "WAIT,LOAXD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND");
        ControlTowerInitialiser.readTaskList(fContent);
    }

    //Following Tests are for readAircraft()
    @Test(expected = MalformedSaveException.class)
    public void readAircraft_cargo_Test5() throws MalformedSaveException {
        //Cargo negative (freight)
        String fContent = String.join(System.lineSeparator(),
                "WAYNE_F:BOEING_747_8F:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:-137757");
        ControlTowerInitialiser.readAircraft(fContent);
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft_cargo_Test4() throws MalformedSaveException {
        //Cargo over limit (freight)
        String fContent = String.join(System.lineSeparator(),
                "WAYNE_F:BOEING_747_8F:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:137757");
        ControlTowerInitialiser.readAircraft(fContent);
    }


    @Test(expected = MalformedSaveException.class)
    public void readAircraft_cargo_Test3() throws MalformedSaveException {
        //Cargo non int
        String fContent = String.join(System.lineSeparator(),
                "WAYNE:BOEING_787:WAIT,LOAD@10,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:x");
        ControlTowerInitialiser.readAircraft(fContent);
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft_cargo_Test2() throws MalformedSaveException {
        //Cargo over limit
        String fContent = String.join(System.lineSeparator(),
                "WAYNE:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:-10");
        ControlTowerInitialiser.readAircraft(fContent);
    }

    //Following Tests are for loadAircraft()
    @Test(expected = MalformedSaveException.class)
    public void readAircraft_cargo_Test1() throws MalformedSaveException {
        //Cargo over limit
        String fContent = String.join(System.lineSeparator(),
                "WAYNE:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:243");
        ControlTowerInitialiser.readAircraft(fContent);
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft_fuel_Test3() throws MalformedSaveException {
        //Over fuel limit
        String fContent = String.join(System.lineSeparator(),
                "WAYNE:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:126207:false:0");
        ControlTowerInitialiser.readAircraft(fContent);
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft_fuel_Test2() throws MalformedSaveException {
        //Invalid characteristic BORED
        String fContent = String.join(System.lineSeparator(),
                "WAYNE:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:-1.00:false:0");
        ControlTowerInitialiser.readAircraft(fContent);
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft_fuel_Test1() throws MalformedSaveException {
        //Invalid characteristic BORED
        String fContent = String.join(System.lineSeparator(),
                "WAYNE:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:1000x0.00:false:0");
        ControlTowerInitialiser.readAircraft(fContent);
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft_characteristic_Test2() throws MalformedSaveException {
        //Invalid characteristic BORED
        String fContent = String.join(System.lineSeparator(),
                "WAYNE:BOEING_787:WAIT,LOAD@100,BORED,AWAY,AWAY,AWAY,LAND:10000.00:false:0");
        ControlTowerInitialiser.readAircraft(fContent);
    }

    //Following Tests are for loadAircraft()
    @Test(expected = MalformedSaveException.class)
    public void readAircraft_characteristic_Test1() throws MalformedSaveException {
        //Invalid characteristic WAXXIT
        String fContent = String.join(System.lineSeparator(),
                "WAYNE:BOEING_787:WAXXIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0");
        ControlTowerInitialiser.readAircraft(fContent);
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft_readColon_Test2() throws MalformedSaveException {
        String fContent = String.join(System.lineSeparator(),
                "WA:YNE:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0");
        ControlTowerInitialiser.readAircraft(fContent);
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft_readColon_Test1() throws MalformedSaveException {
        String fContent = String.join(System.lineSeparator(),
                "WAYNE:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0:");

        ControlTowerInitialiser.readAircraft(fContent);
    }


    @Test(expected = MalformedSaveException.class)
    public void loadAircraft_lineNumTest3() throws MalformedSaveException {
        String fContent = String.join(System.lineSeparator(),
                "xx", "WAYNE:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0");
        try {
            ControlTowerInitialiser.loadAircraft(new StringReader(fContent));
        } catch (IOException e) {
            fail("Should be malformed");
        }
    }

    @Test(expected = MalformedSaveException.class)
    public void loadAircraft_lineNumTest2() throws MalformedSaveException {
        String fContent = String.join(System.lineSeparator(),
                "1");
        try {
            ControlTowerInitialiser.loadAircraft(new StringReader(fContent));
        } catch (IOException e) {
            fail("Should be malformed");
        }
    }

    @Test(expected = MalformedSaveException.class)
    public void loadAircraft_lineNumTest1() throws MalformedSaveException {
        String fContent = String.join(System.lineSeparator(),
                "0", "WAYNE:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0");
        try {
            ControlTowerInitialiser.loadAircraft(new StringReader(fContent));
        } catch (IOException e) {
            fail("Should be malformed");
        }
    }

    @Test
    public void loadAircraft_Test2() throws MalformedSaveException {
        try {
            BufferedReader br = new BufferedReader(new FileReader("saves/aircraft_basic.txt"));
            ControlTowerInitialiser.loadAircraft(br);
        } catch (IOException e) {
            fail("These files are valid format");
        }
    }

    @Test
    public void loadAircraft_Test1() throws MalformedSaveException, IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader("saves/aircraft_default.txt"));
            ControlTowerInitialiser.loadAircraft(br);
        } catch (IOException e) {
            fail("These files are valid format");
        }
    }


    //Following Tests are for loadTick()
    @Test(expected = MalformedSaveException.class)
    public void loadTickMalformedNeg_Test() throws MalformedSaveException, IOException {
        String fileContent = String.join(System.lineSeparator(), "-1");
        ControlTowerInitialiser.loadTick(new StringReader(fileContent));
    }

    @Test(expected = MalformedSaveException.class)
    public void loadTickMalformed_Test() throws MalformedSaveException, IOException {
        String fileContent = String.join(System.lineSeparator(), "LIGMA");
        ControlTowerInitialiser.loadTick(new StringReader(fileContent));
    }

    @Test
    public void loadTick_Test() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("saves/tick_default.txt"));
            assertEquals("Tick should be 5", ControlTowerInitialiser.loadTick(br), 0);
            br = new BufferedReader(new FileReader("saves/tick_basic.txt"));
            assertEquals("Tick should be 5", ControlTowerInitialiser.loadTick(br), 5);
        } catch (MalformedSaveException | IOException e) {
            fail("These files are valid format");
        }
    }

}
