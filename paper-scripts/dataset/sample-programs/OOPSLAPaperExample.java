package vulnerable;

import java.io.*;
import java.util.*;

/**
 * To generate JAR:
 * <code>
 *      mkJar vulnerable/ProposalExample3.java 1.7 && mv ProposalExample3.jar ../build/ProposalExample3-JRE1.7.jar
 * </code>
 * @author Joanna C. S. Santos <jds5109@rit.edu>
 */
class Main {

    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fs = new FileInputStream(args[0]);
        ObjectInputStream objIn = new ObjectInputStream(fs);
        Config obj = (Config) objIn.readObject();
    }
}

class Config implements Serializable {
    private String page;
    public void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Runtime.getRuntime().exec("open http://localhost:/" + page);
    }
}
class CacheManager implements Serializable {

    // fields that could be used in an attack
    private Runnable task;
    private Runnable[] taskArray;
    private List<Runnable> taskList;
    private Set<Runnable> taskSet;
    private Map<String, Runnable> taskMap;
    // field that influence the reachability of the sink
    private String os;
    // field irrelevant for the exploit
    private long timestamp;

    /* Node 9612 */
    public void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        Runnable r;
        if (os.equals("windows") && task instanceof CommandTask) {
            r = getInitHook(); r.run();
        }else {
            r = getFromArray(); r.run();
            r = getFromList(); r.run();
            r = getFromSet(); r.run();
            r = getFromMap(); r.run();
        }
    }

    Runnable getInitHook(){ return task; }

    Runnable getFromArray() { return taskArray[0]; }

    Runnable getFromList() { return taskList.get(0); }

    Runnable getFromSet() { return taskSet.iterator().next(); }

    Runnable getFromMap() { return taskMap.get("xyz"); }
}

class CommandTask implements Runnable, Serializable {

    private String command;
    private TaskExecutor taskExecutor;

    public CommandTask(String command, TaskExecutor taskExecutor) {
        this.command = command;
        this.taskExecutor = taskExecutor;
    }

    private CommandTask() {

    }


    @Override /* Nodes: 10278, 10280, 10285 */
    public void run() {
        if (!command.isEmpty() && taskExecutor != null) {
            taskExecutor.executeCmd(command);
        }
    }
}

class TaskExecutor implements Serializable {

    /* Node: 10423 */
    public void executeCmd(String cmd) {
        try { Runtime.getRuntime().exec(cmd); }
        catch (IOException e) { }
    }
}
