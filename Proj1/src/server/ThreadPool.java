package server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;
import static utils.Logger.*;

public class ThreadPool {

    private final LinkedBlockingQueue<Runnable> queue;
    private ArrayList<Thread> workers;

    public ThreadPool(int totalThread) {
        log("creating a pool with " + totalThread +" workers");
        this.queue = new LinkedBlockingQueue<>();
        this.workers = new ArrayList<>();

        // add the worker thread to a flexible arrayList so that we can easily
        // manage it without need to manually change the size of the worker array
        for (int i = 0; i < totalThread; i++) {
            workers.add(new Worker());
        }

        for (Thread r:workers){
            r.start();
        }
    }

    public void exec(Runnable job){
        synchronized (queue) {
            queue.add(job);
            queue.notify();
        }
    }


    private class Worker extends Thread {

        @Override
        public void run() {
            Runnable task;

            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            System.out.println("An error occur when queue is empty" + e.getMessage());
                        }
                    }

                    //end waiting
                    task = queue.poll();
                }

                // may cause thread leaking if not catch the error
                try {
                    task.run();
                } catch (RuntimeException e) {
                    System.out.println("During running task, and error occur" + e.getMessage());
                }
            }
        }
    }
}
