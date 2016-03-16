import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

class Transition {
    int state;
    byte output;
    Direction direction;

    public Transition() {
        // Default
        setTransition(State.REJECT, (byte)0, Direction.LEFT);
    }

    public void setTransition(int state, byte output, Direction direction) {
        this.state = state;
        this.output = output;
        this.direction = direction;
    }
}

/**
 * State class contains an array indicating for every input character,
 * the character to be written, the direction the tape head is to
 * move, and the next state to go to. By default, every transition is
 * to the REJECT state.
 */
class State {
    static final int ACCEPT = -2;
    static final int REJECT = -1;

    Transition[] transitions = new Transition[256];

    public State() {
        for (int i=0; i<256; i++) {
            this.transitions[i] = new Transition();
        }
    }
}

enum Direction {
    LEFT,
    RIGHT
}

class TuringMachine {
    private State[] states;

    // An index into states array
    private int currentState = 0;

    // Tape
    private byte[] tape;

    // The position of the head on the tape
    private int headPosition;

    // Number of steps in the computation
    public int steps = 0;

    // Print configurations while stepping through
    public boolean traceEnabled;

    // Initialize the states array
    public TuringMachine(int numberOfStates) {
        this.states = new State[numberOfStates];

        for (int i = 0; i < numberOfStates; i++) {
            this.states[i] = new State();
        }
    }

    public void modifyState(int stateNumber, byte inputSymbol, int targetState, byte outputSymbol, Direction direction) {
        // Set transition
        if (stateNumber < states.length) {
            states[stateNumber].transitions[inputSymbol].setTransition(targetState, outputSymbol, direction);
        }
    }

    public void initializeTape(byte[] inputString) {
        // Copy the input to a new array. Add 1 to the length to make
        // sure there is at least one cell in case the input is empty.
        tape = Arrays.copyOf(inputString, inputString.length*2+1);
    }

    public void printConfiguration() {
        System.out.printf("%5d | ", steps);

        for (int i=0; i<tape.length; i++) {
            if (i == headPosition) {
                System.out.print("[" + currentState + "]");
            }
            if (tape[i] == 0) {
                System.out.print("   ");
            } else {
                System.out.print(" " + (char)tape[i] + " ");
            }
        }

        System.out.println();
    }

    public void stepComputation() {
        steps += 1;

        // Read transition function
        Transition thisTransition = states[currentState].transitions[tape[headPosition]];

        // Write to tape
        tape[headPosition] = thisTransition.output;

        // Update state
        currentState = thisTransition.state;

        // Move tape head
        moveHead(thisTransition.direction);
    }

    public int run(int stepLimit) {
        if (traceEnabled) {
            System.out.println();
            System.out.printf("%5s | %s%n", "Step", "Configuration");
            System.out.println("------+-----------------------------------------------------");

            printConfiguration();
        }

        for (int i = 0; i < stepLimit; i++) {
            stepComputation();

            if (traceEnabled) {
                printConfiguration();
            }

            if (currentState < 0) {
                break;
            }
        }

        if (traceEnabled) {
            System.out.println();
        }

        return currentState;
    }

    private void moveHead(Direction direction) {
        switch (direction) {
        case RIGHT:
            headPosition++;

            // If we have reached the end of the array, double it to
            // create the illusion of an infinite tape.
            if (headPosition == tape.length) {
                tape = Arrays.copyOf(tape, tape.length * 2);
            }
            break;
        case LEFT:
            if (headPosition > 0) {
                headPosition--;
            }
            break;
        }
    }

    static TuringMachine createTMFromFile(String filename) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename), "UTF-8");

        // First line contains the number of states
        int numberOfStates = Integer.parseInt(sc.nextLine());

        // Number of lines read so far
        int lineNumber = 1;

        // Create Turing machine
        TuringMachine tm = new TuringMachine(numberOfStates);

        // Rest of file contains the transition function
        while (sc.hasNextLine()) {
            // Trim and split string into array with whitespace as delimiter
            String[] fields = sc.nextLine().trim().split(" +");
            lineNumber += 1;

            // Skip empty lines
            if (fields.length == 0 || fields[0].compareTo("") == 0) {
                continue;
            }

            // Skip if line is a comment (begins with three dashes)
            if (fields[0].length() >= 3 && fields[0].substring(0, 3).compareTo("---") == 0) {
                continue;
            }

            // Error checking
            if (fields.length != 5) {
                System.err.println("Error: Line " + lineNumber + ": Wrong number of fields");
                return null;
            }
            // Error checking
            if (fields[1].length() != 1 || fields[3].length() != 1 || fields[4].length() != 1) {
                System.err.println("Error: Line " + lineNumber + ": Field too long");
                return null;
            }

            int currentState;
            int nextState;

            try {
                currentState = Integer.parseInt(fields[0]);
                nextState    = Integer.parseInt(fields[2]);
            } catch (NumberFormatException e) {
                System.err.println("Error: Line " + lineNumber + ": Failed to parse number");
                return null;
            }

            char inputSymbol  = fields[1].charAt(0);
            char outputSymbol = fields[3].charAt(0);

            // Parse direction field

            Direction direction;
            switch (fields[4]) {
            case "L":
                direction = Direction.LEFT;
                break;
            case "R":
                direction = Direction.RIGHT;
                break;
            default:
                System.err.println("Error: Line " + lineNumber + ": Failed to parse direction");
                return null;
            }

            if (inputSymbol == '_') {
                inputSymbol = 0;
            }
            if (outputSymbol == '_') {
                outputSymbol = 0;
            }

            tm.modifyState(currentState, (byte)inputSymbol, nextState, (byte)outputSymbol, direction);
        }

        return tm;
    }
}

public class UniversalTM {
    static final int lineWidth = 60;

    public static void printUsage() {
        System.out.println("Usage: java UniversalTM [--trace] <filename> <inputString> <maxSteps>");

        System.out.println("\nOptional:");
        System.out.println("  --silent            Do not print trace of the computation");

        System.out.println("\nRequired:");
        System.out.println("  <filename>          File containing TM encoding");
        System.out.println("  <inputString>       Input to tape");
        System.out.println("  <maxSteps>          Max number of steps");

        System.out.println("\nNOTE: Use underscore '_' as blank when encoding a TM.");
    }

    static void printMessage(String message) {
        String pre = "--- ";
        String post = " ";

        for (int i=pre.length() + message.length() + post.length(); i<lineWidth; i++) {
            post += "-";
        }

        System.out.println(pre + message + post);
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 3 && (args.length != 4 || args[0].compareTo("--silent") != 0)) {
            printUsage();
            System.exit(1);
        }

        boolean traceEnabled = true;
        int startIndex = 0;

        if (args[0].compareTo("--silent") == 0) {
            if (args.length != 4) {
                printUsage();
                System.exit(1);
            }

            traceEnabled = false;
            startIndex = 1;
        }

        String filename = args[startIndex++];
        String inputString = args[startIndex++];
        int maxSteps = Integer.parseInt(args[startIndex++]);
        TuringMachine tm = TuringMachine.createTMFromFile(filename);

        if (traceEnabled) {
            tm.traceEnabled = true;
        }

        tm.initializeTape(inputString.getBytes(StandardCharsets.UTF_8));

        printMessage("Started on input \"" + inputString + "\"");

        int finalState = tm.run(maxSteps);

        switch (finalState) {
        case State.ACCEPT:
            printMessage("Accepted input \"" + inputString + "\" in " + tm.steps + (tm.steps == 1 ? " step" : " steps"));
            break;
        case State.REJECT:
            printMessage("Rejected input \"" + inputString + "\" in " + tm.steps + (tm.steps == 1 ? " step" : " steps"));
            break;
        default:
            printMessage("Did not finish within " + maxSteps + " steps. Computation aborted");
            break;
        }
    }
}
