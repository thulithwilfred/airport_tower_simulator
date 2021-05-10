package towersim.control;

import org.junit.Test;
import towersim.util.MalformedSaveException;

import static org.junit.Assert.*;

import java.io.*;

public class ControlTowerInitialiserTest {

    //Following Tests are for loadAircraft()
    @Test
    public void loadAircraft_Test2() throws MalformedSaveException, IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader("saves/aircraft_basic.txt"));
            ControlTowerInitialiser.loadAircraft(br);
        } catch (IOException e) {
            fail("These files are valid format");
        }
    }


    @Test(expected = MalformedSaveException.class)
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
