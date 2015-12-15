package com.davidaq.logio.util;

public class QueueConsumer<Type> implements Runnable {
    public interface ConsumeLogic<Type> {
        void consume(Type value);
    }

    private final Queue<?, Type> queue;
    private final ConsumeLogic<Type> logic;

    private boolean idle = true;
    private boolean stopped = false;
    private Thread thread;

    public QueueConsumer(Queue<?, Type> queue, ConsumeLogic<Type> logic) {
        this.queue = queue;
        this.logic = logic;
    }

    public Thread start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }
        return thread;
    }

    @Override
    public void run() {
        stopped = false;
        while (!stopped) {
            synchronized (queue) {
                try {
                    queue.wait(1000);
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Type dequeue = queue.shift();
            if (dequeue == null) {
                idle = true;
                continue;
            }
            idle = false;
            try {
                logic.consume(dequeue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        idle = true;
        Thread t = thread;
        thread = null;
        synchronized (t) {
            t.notifyAll();
        }
    }

    public boolean isIdle() {
        return idle;
    }

    public void stop() {
        stopped = true;
        while (thread != null) {
            synchronized (thread) {
                try {
                    thread.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
