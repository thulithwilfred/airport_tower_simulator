package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import towersim.util.MalformedSaveException;
import towersim.util.NoSpaceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class that contains static methods for loading a control tower and
 * associated entities from files.
 */
public class ControlTowerInitialiser {

    /**
     * Creates a control tower instance by reading various airport entities from the given readers.
     * <p>
     * The following methods should be called in this order, and their results stored temporarily,
     * to load information from the readers:
     * <p>
     * loadTick(Reader) to load the number of elapsed ticks
     * loadAircraft(Reader) to load the list of all aircraft
     * loadTerminalsWithGates(Reader, List) to load the terminals and their gates
     * loadQueues(Reader, List, TakeoffQueue, LandingQueue, Map) to load the takeoff queue,
     * landing queue and map of loading aircraft to their loading time remaining
     *
     * @param tick               reader from which to load the number of ticks elapsed
     * @param aircraft           reader from which to load the list of aircraft
     * @param queues             reader from which to load the aircraft queues and map of
     *                           loading aircraft
     * @param terminalsWithGates reader from which to load the terminals and their gates
     * @return control tower created by reading from the given readers
     */
    public static ControlTower createControlTower(Reader tick, Reader aircraft, Reader queues,
                                                  Reader terminalsWithGates)
            throws MalformedSaveException, IOException {
        //TODO THIS 
        return null;
    }

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
        List<Aircraft> loadedAircrafts = new ArrayList<>();

        //Create buffered reader and get first line.
        BufferedReader br = new BufferedReader(reader);
        String lineCount = br.readLine();

        try {
            parsedLineCount = Integer.parseInt(lineCount);
        } catch (NumberFormatException nfe) {
            throw new MalformedSaveException();
        }

        //Used to compare the actual num lines in the file against first line.
        int actualLineCount = 0;
        String line;

        //Iterate throw the following lines and parse aircrafts
        while ((line = br.readLine()) != null) {
            actualLineCount++;
            //Any exceptions here will ripple out.
            loadedAircrafts.add(readAircraft(line));
        }
        //Specified line count mismatch with actual.
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

        AircraftCharacteristics[] parsedCharacteristic = new AircraftCharacteristics[1];
        //Colon (:) count mismatch.
        if (getColonCountInString(line) != 5) {
            throw new MalformedSaveException();
        }

        //The aircraft's AircraftCharacteristics is not valid
        if (!aircraftCharacteristicValid(tokens[1], parsedCharacteristic)) {
            throw new MalformedSaveException();
        }

        //Parse Fuel
        double[] parsedFuelAmount = new double[1];
        if (!parseFuelValue(tokens[3], tokens[1], parsedFuelAmount)) {
            throw new MalformedSaveException();
        }

        //Parse Cargo
        int[] aircraftCargoPacket = new int[2]; //Holds the Type [0], and Amount[1]
        if (!parseCargo(tokens[5], tokens[1], aircraftCargoPacket)) {
            throw new MalformedSaveException();
        }
        //Specialised identifier for passenger aircraft 0xAA, Freight 0xFF
        if (aircraftCargoPacket[0] == 0xAA) {
            //Passenger
            return new PassengerAircraft(tokens[0], parsedCharacteristic[0],
                    readTaskList(tokens[2]), parsedFuelAmount[0], aircraftCargoPacket[1]);
        } else if (aircraftCargoPacket[0] == 0xFF) {
            //Freight
            return new FreightAircraft(tokens[0], parsedCharacteristic[0],
                    readTaskList(tokens[2]), parsedFuelAmount[0], aircraftCargoPacket[1]);
        }
        //Should not reach here.
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

        String[] tokens = taskListPart.split(",");
        List<Task> decodedTasks = new ArrayList<>();
        boolean taskValidFlag = false;

        //This value is used to save the parsed load percent through the parsing methods.
        int[] parsedLoadPercent = new int[1];

        for (String parseTask : tokens) {

            String[] taskSplit = parseTask.split("@");

            if (taskSplit.length > 2) {
                throw new MalformedSaveException();
            }

            //Task has more that one @ symbol
            if (getAtCountInString(parseTask) > 1) {
                throw new MalformedSaveException();
            }
            //Parse task against valid tasks and load list.
            for (TaskType validTask : TaskType.values()) {

                if (validTask.toString().equals(taskSplit[0])) {
                    if (!loadPercentValid(parseTask, parsedLoadPercent)) {
                        //Invalid Load Percent
                        throw new MalformedSaveException();
                    }
                    decodedTasks.add(new Task(validTask,
                            validTask.equals(TaskType.LOAD) ? parsedLoadPercent[0] : 0));
                    taskValidFlag = true;
                }
            }

            if (!taskValidFlag) {
                //No match found while parsing taskTypes.
                throw new MalformedSaveException();
            }
            //Reset detection flag
            taskValidFlag = false;
        }

        //The task list is invalid according to the rules specified
        try {
            return new TaskList(decodedTasks);
        } catch (IllegalArgumentException iae) {
            throw new MalformedSaveException();
        }
    }

    /**
     * Determines if an encoded particular load task is of valid format
     *
     * @param loadTask to parse
     * @return true if valid, false else.
     */
    private static boolean loadPercentValid(String loadTask, int[] parsedLoadPercent) {
        String[] tokens = loadTask.split("@");

        //Load token needs to be @ delimited.
        if (tokens.length > 2 || tokens.length == 0) {
            return false;
        }

        try {
            if (tokens.length == 2) {
                parsedLoadPercent[0] = Integer.parseInt(tokens[1]);
            }
            if (parsedLoadPercent[0] < 0) {
                return false;
            }
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
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
    private static boolean parseCargo(String cargo, String aircraftCharacteristic,
                                      int[] aircraftCargoPacket) {
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
                if (c.freightCapacity == 0) {
                    aircraftCargoPacket[0] = 0xAA;
                    //Passenger Aircraft exceeds passengers.
                    aircraftCargoPacket[1] = parsedCargo;
                    return parsedCargo <= c.passengerCapacity;
                } else if (c.passengerCapacity == 0) {
                    aircraftCargoPacket[0] = 0xFF;
                    //Freight Aircraft exceeds freight.
                    aircraftCargoPacket[1] = parsedCargo;
                    return parsedCargo <= c.freightCapacity;
                }
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
     * @param parsedFuelAmount       saves the parsed fuel value into reference.
     * @return true if valid, false else.
     */
    private static boolean parseFuelValue(String fuelVal, String aircraftCharacteristic,
                                          double[] parsedFuelAmount) {

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
        parsedFuelAmount[0] = fuel;
        return true;
    }

    /**
     * Checks if a given aircraft characteristic string is a valid argument
     *
     * @param match aircraft characteristic name to compare.
     * @return true if valid.
     */
    private static boolean aircraftCharacteristicValid(String match,
                                                       AircraftCharacteristics[]
                                                               parsedCharacteristic) {

        for (AircraftCharacteristics c : AircraftCharacteristics.values()) {
            if (c.toString().equals(match)) {
                parsedCharacteristic[0] = c;
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

    /**
     * Returns the numbers of Colons in a given string (line)
     *
     * @param line string to parse.
     * @return count of colons in string.
     */
    private static int getAtCountInString(String line) {
        int count = 0;
        for (int i = 0; i < line.length(); ++i) {
            if (line.charAt(i) == '@') {
                count++;
            }
        }
        return count;
    }

    /**
     * Loads the takeoff queue, landing queue and map of loading aircraft from the given reader
     * instance.
     * <p>
     * Rather than returning a list of queues, this method does not return anything.
     * Instead, it should modify the given takeoff queue, landing queue and loading map
     * by adding aircraft, etc.
     *
     * @param reader          reader from which to load the queues and loading map
     * @param aircraft        list of all aircraft, used when validating that callsigns exist
     * @param takeoffQueue    empty takeoff queue that aircraft will be added to
     * @param landingQueue    empty landing queue that aircraft will be added to
     * @param loadingAircraft empty map that aircraft and loading times will be added to
     * @throws MalformedSaveException if the format of the text read from the reader
     *                                is invalid according to the rules above
     * @throws IOException            if an IOException is encountered when reading from the reader
     */
    public static void loadQueues(Reader reader, List<Aircraft> aircraft,
                                  TakeoffQueue takeoffQueue, LandingQueue landingQueue,
                                  Map<Aircraft, Integer> loadingAircraft)
            throws MalformedSaveException, IOException {
        //Create buffered reader.
        BufferedReader br = new BufferedReader(reader);

        readQueue(br, aircraft, takeoffQueue);
        readQueue(br, aircraft, landingQueue);
        readLoadingAircraft(br, aircraft, loadingAircraft);
        //Next line should be EOF
        if (br.readLine() != null) {
            //The file still contains more data.
            throw new MalformedSaveException();
        }
    }

    /**
     * Reads the map of currently loading aircraft from the given reader instance.
     * <p>
     * Rather than returning a map, this method does not return anything. Instead,
     * it should modify the given map by adding entries (aircraft/integer pairs) to it.
     * <p>
     * The contents of the text read from the reader should match the format specified in the
     * queuesWriter row of in the table shown in ViewModel.saveAs(). Note that this method
     * should only read the map of loading aircraft, not the takeoff queue or landing queue.
     * Reading these queues is handled in the readQueue(BufferedReader, List, AircraftQueue)
     * method.
     * <p>
     * For an example of valid encoded map of loading aircraft, see the provided
     * saves/queues_basic.txt and saves/queues_default.txt files.
     *
     * @param reader          reader from which to load the map of loading aircraft
     * @param aircraft        list of all aircraft, used when validating that callsigns exist
     * @param loadingAircraft empty map that aircraft and their loading times will be added to
     * @throws IOException            if an IOException is encountered when reading from
     *                                the reader
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     */
    private static void readLoadingAircraft(BufferedReader reader, List<Aircraft> aircraft,
                                            Map<Aircraft, Integer> loadingAircraft) throws
            IOException, MalformedSaveException {
        String line;
        if ((line = reader.readLine()) != null) {
            //The first line contains more/fewer colons (:) than expected.
            if (getColonCountInString(line) != 1) {
                throw new MalformedSaveException();
            }

            String[] tokens = line.split(":");

            //Parses The number of aircraft specified on the first line is not an integer
            int airCraftSpecified = parseAircraftSpecified(tokens);

            if (airCraftSpecified > 0) {

                if ((line = reader.readLine()) != null) {

                    String[] loadingAircrafts = line.split(",");
                    //The number of aircraft specified on the first line is not equal
                    if (loadingAircrafts.length != airCraftSpecified) {
                        throw new MalformedSaveException();
                    }
                    for (String loadingAircraftIndex : loadingAircrafts) {
                        //Contain the Map <key, val> at index [0](callsign), [1](tick) respectively
                        String[] mapKeyVal = loadingAircraftIndex.split(":");
                        if (mapKeyVal.length != 2
                                || getColonCountInString(loadingAircraftIndex) != 1) {
                            //Unable to ':' separate map key and value or the number of
                            // colons detected is not equal to one
                            throw new MalformedSaveException();
                        }


                        int[] airCraftIndex = new int[1];

                        if (!callsignInAircraftList(mapKeyVal[0], aircraft, airCraftIndex)) {
                            //A callsign listed on the second line does not correspond to
                            // the callsign of any aircraft in list.
                            throw new MalformedSaveException();
                        }
                        //ticksRemaining value on the second line is not an integer
                        //ticksRemaining value on the second line is less than one
                        int tickRemaining = parseTickString(mapKeyVal[1]);
                        //All conditions met, load Map.
                        loadingAircraft.put(aircraft.get(airCraftIndex[0]), tickRemaining);
                    }
                } else {
                    //The number of aircraft is greater than zero and the second line
                    // read from the reader is null
                    throw new MalformedSaveException();
                }
            }

        } else {
            //The first line read from the reader is null.
            throw new MalformedSaveException();
        }

    }


    /**
     * Converted the map key value 'tick' into a string hearing to the rules
     * associated with parsing this value as per javadoc.
     * <p>
     * Rules:
     * Any ticksRemaining value on the second line is not an integer
     * Any ticksRemaining value on the second line is less than one
     *
     * @param tickParse string value to be parsed.
     * @return parsed tick int value.
     * @throws MalformedSaveException unable to parse/doesn't meet spec.
     */
    private static int parseTickString(String tickParse) throws MalformedSaveException {
        int tick;
        try {
            tick = Integer.parseInt(tickParse);
            if (tick < 1) {
                throw new MalformedSaveException();
            }
        } catch (NumberFormatException nfe) {
            throw new MalformedSaveException();
        }
        return tick;
    }

    /**
     * Reads an aircraft queue from the given reader instance.
     * <p>
     * Rather than returning a queue, this method does not return anything. Instead,
     * it should modify the given aircraft queue by adding aircraft to it.
     *
     * @param reader   reader from which to load the aircraft queue
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @param queue    empty queue that aircraft will be added to
     * @throws IOException            if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     */
    private static void readQueue(BufferedReader reader, List<Aircraft> aircraft,
                                  AircraftQueue queue) throws IOException,
            MalformedSaveException {

        String line;
        if ((line = reader.readLine()) != null) {
            //The first line contains more/fewer colons (:) than expected.
            if (getColonCountInString(line) != 1) {
                throw new MalformedSaveException();
            }

            String[] tokens = line.split(":");

            //The queue type specified in the first line is not equal
            if (!tokens[0].equals(queue.getClass().getSimpleName())) {
                throw new MalformedSaveException();
            }

            //Parse the specified amount of Airfracts in string
            int amountOfAircrafts = parseAircraftSpecified(tokens);

            if (amountOfAircrafts > 0) {
                if ((line = reader.readLine()) != null) {

                    String[] callsigns = line.split(",");

                    if (callsigns.length != amountOfAircrafts) {
                        //The number of callsigns listed on the second line
                        // is not equal to the number of aircraft specified on the first line
                        throw new MalformedSaveException();
                    }

                    int[] airCraftIndex = new int[1];
                    for (String callsign : callsigns) {
                        if (!callsignInAircraftList(callsign, aircraft, airCraftIndex)) {
                            //A callsign listed on the second line does not correspond to
                            // the callsign of any aircraft in list.
                            throw new MalformedSaveException();
                        }
                        //Callsign exists within given list, add to queue.
                        queue.addAircraft(aircraft.get(airCraftIndex[0]));
                    }
                } else {
                    //The number of aircraft specified is greater than zero and the second
                    // line read is null
                    throw new MalformedSaveException();
                }
            }

        } else {
            //The first line read from the reader is null.
            throw new MalformedSaveException();
        }

    }

    /**
     * Parses the specified amount of aircrafts.
     *
     * @param tokens string array containing ':' seperated data.
     * @return parsed specifiedAircrafts.
     * @throws MalformedSaveException argument is invalid according to specified rules.
     */
    private static int parseAircraftSpecified(String[] tokens) throws MalformedSaveException {
        if (tokens.length != 2) {
            //Expected ':' delimiter to split into two.
            throw new MalformedSaveException();
        }
        int aircraftSpecified;
        try {
            aircraftSpecified = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException nfe) {
            //The number of aircraft specified on the first line is not an integer
            throw new MalformedSaveException();
        }
        return aircraftSpecified;
    }

    /**
     * Checks if a given callsign exists within the given aircraft list
     *
     * @param callsign      to check for
     * @param aircraft      list to detect in
     * @param airCraftIndex saves the index at which the aircraft was found in the list.
     * @return true if callsign exists in list, false else.
     */
    private static boolean callsignInAircraftList(String callsign, List<Aircraft> aircraft,
                                                  int[] airCraftIndex) {
        int count = 0;
        for (Aircraft aircraftMatch : aircraft) {
            if (aircraftMatch.getCallsign().equals(callsign)) {
                airCraftIndex[0] = count;
                return true;
            }
            count++;
        }
        return false;
    }

    /**
     * Loads the list of terminals and their gates from the given reader instance.
     * <p>
     * The contents of the reader should match the format specified in the
     * terminalsWithGatesWriter row of in the table shown in ViewModel.saveAs().
     * <p>
     * For an example of valid queues reader contents, see the provided
     * saves/terminalsWithGates_basic.txt and saves/terminalsWithGates_default.txt files.
     *
     * @param reader   reader from which to load the list of terminals and their gates
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return list of terminals (with their gates) read from the reader
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     * @throws IOException            if an IOException is encountered when reading from the reader
     */
    public static List<Terminal> loadTerminalsWithGates(Reader reader, List<Aircraft> aircraft)
            throws MalformedSaveException, IOException {
        BufferedReader br = new BufferedReader(reader);
        String line;
        int numOfTerminals;
        List<Terminal> loadedTerminals = new ArrayList<>();

        if ((line = br.readLine()) != null) {

            try {
                numOfTerminals = Integer.parseInt(line);
            } catch (NumberFormatException nfe) {
                //The number of terminals specified at the top of the file is not an integer 
                throw new MalformedSaveException();
            }

            //Attempt to read terminals
            for (int i = 0; i < numOfTerminals; ++i) {
                if ((line = br.readLine()) != null) {
                    loadedTerminals.add(readTerminal(line, br, aircraft));
                } else {
                    throw new MalformedSaveException();
                }
            }
            //Theres more data beyond the last specified gate,
            //Thus, number of terminals specified is not equal to the number of
            // terminals actually read from the reader.
            if ((line = br.readLine()) != null) {
                throw new MalformedSaveException();
            }


        } else {
            //File unable to be parsed. (Likely empty)
            throw new MalformedSaveException();
        }

        return loadedTerminals;
    }

    /**
     * Reads a terminal from the given string and reads its gates from the given reader instance.
     * <p>
     * The format of the given string and the text read from the reader should match the encoded
     * representation of a terminal, as described in Terminal.encode().
     * <p>
     * For an example of valid encoded terminal with gates, see the provided
     * saves/terminalsWithGates_basic.txt and saves/terminalsWithGates_default.txt files.
     *
     * @param line     string containing the first line of the encoded terminal
     * @param reader   reader from which to load the gates of the terminal (subsequent lines)
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return decoded terminal with its gates added.
     * @throws IOException            if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the given string or the text read from t
     *                                he reader is invalid according to the rules above
     */
    public static Terminal readTerminal(String line, BufferedReader reader,
                                        List<Aircraft> aircraft)
            throws IOException, MalformedSaveException {
        Terminal decodedTerminal;
        String[] terminalData = line.split(":");
        //The number of colons (:) detected mismatch
        if (getColonCountInString(line) != 3 || terminalData.length != 4) {
            throw new MalformedSaveException();
        }

        //Parses terminal number and number of gates based on specified rules.
        //MalformedSaveException is thrown in the following function either is invalid.
        int terminalNum = parseTerminalNumber(terminalData[1]);
        int numGates = parseNumGates(terminalData[3]);

        if (terminalData[0].equals("AirplaneTerminal")) {
            decodedTerminal = new AirplaneTerminal(terminalNum);
        } else if (terminalData[0].equals("HelicopterTerminal")) {
            decodedTerminal = new HelicopterTerminal(terminalNum);
        } else {
            //Neither AirplaneTerminal nor HelicopterTerminal
            throw new MalformedSaveException();
        }

        //Attempt to read gates based on specified numGates.
        String gateLine;

        for (int i = 0; i < numGates; ++i) {
            if ((gateLine = reader.readLine()) != null) {
                try {
                    decodedTerminal.addGate(readGate(gateLine, aircraft));
                } catch (NoSpaceException nse) {
                    //MAX_NUM_GATES not reached
                }
            } else {
                //A line containing an encoded gate was expected, but EOF (end of file)
                throw new MalformedSaveException();
            }
        }
        return decodedTerminal;
    }

    /**
     * Reads a gate from its encoded representation in the given string.
     * <p>
     * The format of the string should match the encoded representation of a gate,
     * as described in Gate.encode().
     * <p>
     * The encoded string is invalid if any of the following conditions are true:
     * <p>
     * The number of colons (:) detected was more/fewer than expected.
     * The gate number is not an integer (i.e. cannot be parsed by
     * Integer.parseInt(String)).
     * The gate number is less than one (1).
     * The callsign of the aircraft parked at the gate is not empty and the callsign
     * does not correspond to the callsign of any aircraft contained in the list of
     * aircraft given as a parameter.
     *
     * @param line     string containing the encoded gate
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return decoded gate instance
     * @throws MalformedSaveException if the format of the given string is invalid according
     *                                to the rules above
     */
    public static Gate readGate(String line, List<Aircraft> aircraft)
            throws MalformedSaveException {

        String[] gateData = line.split(":");

        if (getColonCountInString(line) != 1 || gateData.length != 2) {
            //The number of colons (:) detected was more/fewer than expected.
            throw new MalformedSaveException();
        }

        //Parse gate number, throws MalformedSaveException if invalid.
        int gateNum = parseGateNum(gateData[0]);
        int[] airCraftIndex = new int[1];
        //The callsign of the aircraft parked at the gate is not empty and
        // the callsign does not correspond to the callsign of any aircraft contained
        // in the list of aircraft given as a parameter.
        if (!gateData[1].equals("empty")
                && !callsignInAircraftList(gateData[1], aircraft, airCraftIndex)) {
            throw new MalformedSaveException();
        }

        Gate decodedGate = new Gate(gateNum);

        try {
            if (!gateData[1].equals("empty")) {
                decodedGate.parkAircraft(aircraft.get(airCraftIndex[0]));
            }
        } catch (NoSpaceException nse) {
            //Gate will be empty
        }
        return decodedGate;
    }

    /**
     * Converted string gate num to int gateNum, ensures that the converted type
     * adheres to the following rules.
     * The gate number is not an integer (i.e. cannot be parsed by Integer.parseInt(String)).
     * The gate number is less than one (1).
     *
     * @param num to convert to string and parse.
     * @return int converted gate num.
     * @throws MalformedSaveException if num is invalid according.
     */
    private static int parseGateNum(String num) throws MalformedSaveException {
        int gateNum;
        try {
            gateNum = Integer.parseInt(num);
            if (gateNum < 1) {
                //The gate number is less than one
                throw new MalformedSaveException();
            }
        } catch (NumberFormatException nfe) {
            //The gate number is not an integer
            throw new MalformedSaveException();
        }
        return gateNum;
    }

    /**
     * Parses a string value to an integer value for a given terminal number.
     * If num is < 1 throws MalformedSaveExp or if num in non int type.
     *
     * @param num to convert to int.
     * @return converted terminal number
     * @throws MalformedSaveException if num is invalid according.
     */
    private static int parseTerminalNumber(String num) throws MalformedSaveException {
        int terminalNum;
        try {
            terminalNum = Integer.parseInt(num);
            if (terminalNum < 1) {
                //The terminal number is less than one (1).
                throw new MalformedSaveException();
            }
        } catch (NumberFormatException nfe) {
            //The terminal number is not an integer
            throw new MalformedSaveException();
        }
        return terminalNum;
    }

    /**
     * Converts number of gates to int and returns.
     * MalformedSaveException if:
     * The number of gates is less than zero or is greater than Terminal.MAX_NUM_GATES
     * The number of gates in the terminal is not an integer.
     *
     * @param num of gates to convert
     * @return int converted value
     * @throws MalformedSaveException if num is invalid based on above rules
     */
    private static int parseNumGates(String num) throws MalformedSaveException {
        int numGates;
        try {
            numGates = Integer.parseInt(num);
            if (numGates < 0 || numGates > Terminal.MAX_NUM_GATES) {
                throw new MalformedSaveException();
            }
        } catch (NumberFormatException nfe) {
            throw new MalformedSaveException();
        }
        return numGates;
    }

}
