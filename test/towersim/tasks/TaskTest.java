package towersim.tasks;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class TaskTest {

    private final Random random = new Random();

    @Test
    public void testHashCode4() {
        Task loadTask1 = new Task(TaskType.LOAD, 44);
        Task loadTask2 = new Task(TaskType.LOAD, 99);

        boolean compare = loadTask1.hashCode() == loadTask2.hashCode();

        assertFalse("Hashcode should not be equal", compare);
    }

    @Test
    public void testHashCode3() {
        Task loadTask1 = new Task(TaskType.LOAD, 44);
        Task loadTask2 = loadTask1;

        boolean compare = loadTask1.hashCode() == loadTask2.hashCode();

        assertTrue("Hashcode should be equal", compare);
    }

    @Test
    public void testHashCode2() {
        Task loadTask1 = new Task(TaskType.LOAD, 44);
        Task loadTask2 = new Task(TaskType.LOAD, 44);

        boolean compare = loadTask1.hashCode() == loadTask2.hashCode();

        assertTrue("Hashcode should be equal", compare);
    }

    @Test
    public void testHashCode1() {
        Task loadTask = new Task(TaskType.LOAD, 44);
        Task awayTask = new Task(TaskType.AWAY);
        boolean compare = awayTask.hashCode() == loadTask.hashCode();

        assertFalse("Hashcode should not be equal", compare);
    }

    /**
     * Test the equals method
     */
    @Test
    public void testEquals3() {
        Task awayTask = new Task(TaskType.LOAD, 44);
        Task test1 = new Task(TaskType.LOAD, 45);
        Task test2 = new Task(TaskType.AWAY);

        assertEquals("Expected Objects to Match", false, awayTask.equals(test1));
        assertEquals("Expected Objects to Match", false, test2.equals(test1));
        assertEquals("Expected Objects to Match", false, test2.equals(awayTask));
    }

    /**
     * Test the equals method
     */
    @Test
    public void testEquals2() {
        Task awayTask = new Task(TaskType.LOAD, 44);
        Task test1 = new Task(TaskType.LOAD, 44);

        assertEquals("Expected Objects to Match", true, awayTask.equals(test1));
    }

    /**
     * Test the equals method
     */
    @Test
    public void testEquals1() {
        Task awayTask = new Task(TaskType.LOAD, 44);
        Task copy = awayTask;

        assertEquals("Expected Objects to Match", true, awayTask.equals(awayTask));
        assertEquals("Expected Objects to Match", true, awayTask.equals(copy));
        assertEquals("Expected Objects to Match", true, copy.equals(awayTask));
    }

    /**
     * Tests encode methods in Task
     */
    @Test
    public void testEncode1() {
        Task awayTask = new Task(TaskType.LOAD, 44);
        String expected = "LOAD@44";
        assertEquals("Encode Mismatch", expected, awayTask.encode());

        String expected1 = "TAKEOFF";
        Task toTask = new Task(TaskType.TAKEOFF);
        assertEquals("Encode Mismatch", expected1, toTask.encode());
    }


    @Test
    public void getType_Test() {
        assertEquals("getType() should return the TaskType passed to the Task(TaskType) "
                + "constructor", TaskType.AWAY, new Task(TaskType.AWAY).getType());

        assertEquals("getType() should return the TaskType passed to the Task(TaskType, int) "
                + "constructor", TaskType.LOAD, new Task(TaskType.LOAD, 40).getType());
    }

    @Test
    public void getLoadPercent_OtherTaskTest() {
        assertEquals("getLoadPercent() should return the load percentage passed to the "
                        + "Task(TaskType, int) constructor",
                40, new Task(TaskType.LOAD, 40).getLoadPercent());
    }

    @Test
    public void getLoadPercent_LoadTaskTest() {
        assertEquals("getLoadPercent() should return 0 for tasks other than LOAD",
                0, new Task(TaskType.AWAY).getLoadPercent());
    }

    @Test
    public void toString_OtherTaskTest() {
        assertEquals("WAIT", new Task(TaskType.WAIT).toString());
    }

    @Test
    public void toString_LoadTaskTest() {
        assertEquals("LOAD at 42%", new Task(TaskType.LOAD, 42).toString());
    }
}
