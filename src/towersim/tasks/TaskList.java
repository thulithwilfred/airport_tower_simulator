package towersim.tasks;

import towersim.util.Encodable;

import java.util.List;

/**
 * Represents a circular list of tasks for an aircraft to cycle through.
 *
 * @ass1
 */
public class TaskList implements Encodable {
    /**
     * List of tasks to cycle through.
     */
    private final List<Task> tasks;
    /**
     * Index of current task in tasks list.
     */
    private int currentTaskIndex;

    /**
     * Creates a new TaskList with the given list of tasks.
     * <p>
     * Initially, the current task (as returned by {@link #getCurrentTask()}) should be the first
     * task in the given list.
     *
     * @param tasks list of tasks
     * @ass1
     */
    public TaskList(List<Task> tasks) {
        this.tasks = tasks;
        this.currentTaskIndex = 0;

        //Throw Exception task list does not comply
        if (!parseTaskList(tasks)) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Parses the task list to match list order requirements
     *
     * @param tasks task list to test.
     * @return true if valid, false else.
     * @ass2
     */
    private boolean parseTaskList(List<Task> tasks) {

        //Empty Task List
        if (tasks.size() <= 0) {
            return false;
        }

        //Used to access the index prior to the current one in the taskList.
        int prevPositionIndex;

        for (int i = 0; i < tasks.size(); ++i) {

            TaskType curTaskType = tasks.get(i).getType();
            prevPositionIndex = i - 1;

            if (i == 0) {
                prevPositionIndex = tasks.size() - 1;
            }

            switch (curTaskType) {
                case AWAY:
                    if (!(tasks.get(prevPositionIndex).getType() == TaskType.AWAY
                            || tasks.get(prevPositionIndex).getType() == TaskType.TAKEOFF)) {
                        return false;
                    }
                    continue;
                case LAND:
                    if (!(tasks.get(prevPositionIndex).getType() == TaskType.AWAY)) {
                        return false;
                    }
                    continue;
                case WAIT:
                case LOAD:
                    if (!(tasks.get(prevPositionIndex).getType() == TaskType.LAND
                            || tasks.get(prevPositionIndex).getType() == TaskType.WAIT)) {
                        return false;
                    }
                    continue;
                case TAKEOFF:
                    if (!(tasks.get(prevPositionIndex).getType() == TaskType.LOAD)) {
                        return false;
                    }
                    continue;
                default:
                    //Invalid TaskType
                    return false;
            }
        }
        //All tasks in the list comply
        return true;
    }

    /**
     * Returns the current task in the list.
     *
     * @return current task
     * @ass1
     */
    public Task getCurrentTask() {
        return this.tasks.get(this.currentTaskIndex);
    }

    /**
     * Returns the task in the list that comes after the current task.
     * <p>
     * After calling this method, the current task should still be the same as it was before calling
     * the method.
     * <p>
     * Note that the list is treated as circular, so if the current task is the last in the list,
     * this method should return the first element of the list.
     *
     * @return next task
     * @ass1
     */
    public Task getNextTask() {
        int nextTaskIndex = (this.currentTaskIndex + 1) % this.tasks.size();
        return this.tasks.get(nextTaskIndex);
    }

    /**
     * Moves the reference to the current task forward by one in the circular task list.
     * <p>
     * After calling this method, the current task should be the next task in the circular list
     * after the "old" current task.
     * <p>
     * Note that the list is treated as circular, so if the current task is the last in the list,
     * the new current task should be the first element of the list.
     *
     * @ass1
     */
    public void moveToNextTask() {
        this.currentTaskIndex = (this.currentTaskIndex + 1) % this.tasks.size();
    }

    /**
     * Returns the human-readable string representation of this task list.
     * <p>
     * The format of the string to return is
     * <pre>TaskList currently on currentTask [taskNum/totalNumTasks]</pre>
     * where {@code currentTask} is the {@code toString()} representation of the current task as
     * returned by {@link Task#toString()},
     * {@code taskNum} is the place the current task occurs in the task list, and
     * {@code totalNumTasks} is the number of tasks in the task list.
     * <p>
     * For example, a task list with the list of tasks {@code [AWAY, LAND, WAIT, LOAD, TAKEOFF]}
     * which is currently on the {@code WAIT} task would have a string representation of
     * {@code "TaskList currently on WAIT [3/5]"}.
     *
     * @return string representation of this task list
     * @ass1
     */
    @Override
    public String toString() {
        return String.format("TaskList currently on %s [%d/%d]",
                this.getCurrentTask(),
                this.currentTaskIndex + 1,
                this.tasks.size());
    }

    /**
     * Returns the machine-readable string representation of this task list.
     * The format of the string to return is:
     * encodedTask1,encodedTask2,...,encodedTaskN
     * <p>
     * where encodedTaskX is the encoded representation of the Xth task in the task list,
     * for X between 1 and N inclusive, where N is the number of tasks in the task list
     * and encodedTask1 represents the current task.
     * <p>
     * For example, for a task list with 6 tasks and a current task of WAIT:
     * WAIT,LOAD@75,TAKEOFF,AWAY,AWAY,LAND
     *
     * @return encoded string representation of this task list
     */
    @Override
    public String encode() {
        int listIndex = this.currentTaskIndex;
        StringBuilder encoded = new StringBuilder();

        for (int i = 0; i < this.tasks.size(); ++i) {

            encoded.append(this.tasks.get(listIndex).encode());

            //Add comma only if this element isn't the last.
            if (i + 1 != this.tasks.size()) {
                encoded.append(",");
            }
            listIndex++;

            //Wrap around the taskList
            if (listIndex + 1 > this.tasks.size()) {
                listIndex = 0;
            }
        }

        return encoded.toString();
    }
}
