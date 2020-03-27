package server;

public class SimpleTask implements Runnable{
    private int n;

    public SimpleTask(int n) {
        this.n = n;
        System.out.println("task "+ n + " created!");
    }

    @Override
    public void run() {
        System.out.println("Task " + n + " is running");
    }
}
