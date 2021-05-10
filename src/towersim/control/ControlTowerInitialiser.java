package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.PassengerAircraft;
import towersim.tasks.TaskList;
import towersim.util.MalformedSaveException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that contains static methods for loading a control tower and
 * associated entities from files.
 */
public class ControlTowerInitialiser {

    /**
     * Loads the number of ticks elapsed from the given reader instance.
     * <p>
     * The contents of the reader should match the format specified in the
     * tickWriter row of in the table shown in ViewModel.saveAs().
     * <p>
     * For an example of valid tick reader contents, see the provided saves/tick_basic.txt
     * and saves/tick_default.txt files.
     * <p>
     * The contents read from the reader are invalid if any of the following conditions are true:
     *
     * @param reader reader from which to load the number of ticks elapsed
     * @return number of ticks elapsed
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     * @throws IOException            if an IOException is encountered when reading from the reader
     */
    public static long loadTick(Reader reader) throws MalformedSaveException, IOException {

        BufferedReader br = new BufferedReader(reader);
        String tick = br.readLine();
        long parsedTick;

        try {
            parsedTick = Long.parseLong(tick);
        } catch (NumberFormatException nfe) {
            throw new MalformedSaveException();
        }

        if (parsedTick < 0) {
            throw new MalformedSaveException();
        }

        return parsedTick;
    }

    /**
     * Loads the list of all aircraft managed by the control tower from the given reader instance.
     *
     * @param reader reader from which to load the list of aircraft
     * @return list of aircraft read from the  reader
     * @throws IOException            if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     */
    public static List<Aircraft> loadAircraft(Reader reader) throws IOException,
            MalformedSaveException {

        int parsedLineCount;
        List<Aircraft> loadedAircrafts = new ArrayList<Aircraft>();

        //Create buffered reader and get first line.
        BufferedReader br = new BufferedReader(reader);
        String lineCount = br.readLine();

        try {
            parsedLineCount = Integer.parseInt(lineCount);
        } catch (NumberFormatException nfe) {
            throw new MalformedSaveException();
        }

        String line;
        //Used to compare the actual num lines in the file against first line.
        int actualLineCount = 0;

        //Iterate throw the following lines and parse aircrafts
        while ((line = br.readLine()) != null) {
            actualLineCount++;
            //Any exceptions here will ripple out.
            loadedAircrafts.add(readAircraft(line));
        }

        if (actualLineCount != parsedLineCount) {
            throw new MalformedSaveException();
        }
        return loadedAircrafts;
    }

    /**
     * Reads an aircraft from its encoded representation in the given string.
     * <p>
     * If the AircraftCharacteristics.passengerCapacity of the encoded aircraft
     * is greater than zero, then a PassengerAircraft should be created and returned.
     * Otherwise, a FreightAircraft should be created and returned.
     * <p>
     * The format of the string should match the encoded representation of an aircraft,
     * as described in Aircraft.encode().
     * <p>
     * The encoded string is invalid if any of the following conditions are true:
     * <p>
     * More/fewer colons (:) are detected in the string than expected.
     * <p>
     * The aircraft's AircraftCharacteristics is not valid, i.e. it is not one of those
     * listed in AircraftCharacteristics.values().
     * <p>
     * The aircraft's fuel amount is not a double (i.e. cannot be parsed by
     * Double.parseDouble(String)).
     * <p>
     * The aircraft's fuel amount is less than zero or greater than
     * the aircraft's maximum fuel capacity.
     * <p>
     * The amount of cargo (freight/passengers) onboard the aircraft is not
     * an integer (i.e. cannot be parsed by Integer.parseInt(String)).
     * <p>
     * The amount of cargo (freight/passengers) onboard the aircraft is less
     * than zero or greater than the aircraft's maximum freight/passenger capacity.
     * <p>
     * Any of the conditions listed in the Javadoc for readTaskList(String) are true.
     *
     * @param line line of text containing the encoded aircraft
     * @return decoded aircraft instance
     * @throws MalformedSaveException if the format of the given string is invalid
     *                                according to the rules above
     */
    public static Aircraft readAircraft(String line) throws MalformedSaveException {

        String[] tokens = line.split(":");

        //Colon (:) count mismatch.
        if (getColonCountInString(line) != 5) {
            throw new MalformedSaveException();
        }
        //The aircraft's AircraftCharacteristics is not valid
        if (!aircraftCharacteristicValid(tokens[1])) {
            throw new MalformedSaveException();
        }

        //Parse Fuel
        if (!parseFuelValue(tokens[3], tokens[1])) {
            throw new MalformedSaveException();
        }

        //Parse Cargo
        if (!parseCargo(tokens[5], tokens[1])) {
            throw new MalformedSaveException();
        }


        return null;
    }

    /**
     * Reads a task list from its encoded representation in the given string.
     * <p>
     * The encoded string is invalid if any of the following conditions are true:
     * <p>
     * The task list's TaskType is not valid (i.e. it is not one of those listed in
     * TaskType.values()).
     * <p>
     * A task's load percentage is not an integer
     * (i.e. cannot be parsed by Integer.parseInt(String)).
     * <p>
     * A task's load percentage is less than zero.
     * <p>
     * More than one at-symbol (@) is detected for any task in the task list.
     * <p>
     * The task list is invalid according to the rules specified in TaskList(List).
     *
     * @param taskListPart string containing the encoded task list
     * @return decoded task list instance
     * @throws MalformedSaveException if the format of the given string is invalid according
     *                                to the rules above
     */
    public static TaskList readTaskList(String taskListPart) throws MalformedSaveException {
        //TODO Pickup Here.
        return null;
    }

    /**
     * Parses the cargo amount based on
     * <p>
     * The amount of cargo (freight/passengers) onboard the aircraft is not an integer
     * <p>
     * The amount of cargo (freight/passengers) onboard the aircraft is less than zero or
     * greater than the aircraft's maximum freight/passenger capacity.
     *
     * @param cargo                  amount of cargo, to be parsed
     * @param aircraftCharacteristic characteristic string, to detect model.
     * @return true if cargo is valid, false else.
     */
    private static boolean parseCargo(String cargo, String aircraftCharacteristic) {
        int parsedCargo;
        try {
            if ((parsedCargo = Integer.parseInt(cargo)) < 0) {
                return false;
            }
        } catch (NumberFormatException nfe) {
            //Suppress
            return false;
        }
        //Fuel greater than the aircraft's maximum fuel capacity
        for (AircraftCharacteristics c : AircraftCharacteristics.values()) {
            if (c.toString().equals(aircraftCharacteristic)) {
                if (c.freightCapacity == 0 && (parsedCargo > c.passengerCapacity)) {
                    //Passenger Aircraft exceeds passengers.
                    return false;
                } else if (c.passengerCapacity == 0 && (parsedCargo > c.freightCapacity)) {
                    //Freight Aircraft exceeds freight.
                    return false;
                }
                //All conditions are met.
                return true;
            }
        }
        //Aircraft was not matched in values array.
        return false;
    }

    /**
     * Attempt to convert fuelVal into a double
     * (Active Low, returning false means unable to parse)
     *
     * @param fuelVal                fuel value to parse
     * @param aircraftCharacteristic checks against max capacity.
     * @return true if valid, false else.
     */
    private static boolean parseFuelValue(String fuelVal, String aircraftCharacteristic) {

        double fuel;
        try {
            if ((fuel = Double.parseDouble(fuelVal)) < 0) {
                return false;
            }
        } catch (NumberFormatException nfe) {
            //Suppress
            return false;
        }

        //Fuel greater than the aircraft's maximum fuel capacity
        for (AircraftCharacteristics c : AircraftCharacteristics.values()) {
            if (c.toString().equals(aircraftCharacteristic) && (fuel > c.fuelCapacity)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a given aircraft characteristic string is a valid argument
     *
     * @param match aircraft characteristic name to compare.
     * @return true if valid.
     */
    private static boolean aircraftCharacteristicValid(String match) {

        for (AircraftCharacteristics c : AircraftCharacteristics.values()) {
            if (c.toString().equals(match)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the numbers of Colons in a given string (line)
     *
     * @param line string to parse.
     * @return count of colons in string.
     */
    private static int getColonCountInString(String line) {
        int count = 0;
        for (int i = 0; i < line.length(); ++i) {
            if (line.charAt(i) == ':') {
                count++;
            }
        }
        return count;
    }

}
