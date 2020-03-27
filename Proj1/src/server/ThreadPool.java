package server;

import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {

    private final LinkedBlockingQueue<Runnable> queue;
    private Worker[] workers;

    public ThreadPool(int totalThread) {
        println("creating a pool with " + totalThread +" workers");
        this.queue = new LinkedBlockingQueue<>();
        this.workers = new Worker[totalThread];

        for (int i = 0; i < totalThread; i++) {
            workers[i] = new Worker();
            workers[i].start();
        }
    }

    public void exec(Runnable job){
        synchronized (queue) {
            queue.add(job);
            queue.notify();
        }
    }

    public static void println(String thing) {
        System.out.println(thing);
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
                    task = (Runnable) queue.poll();
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
