package ru.spbau.intermessage.core;

import java.util.Queue;
import java.util.ArrayDeque;

public abstract class ServiceCommon {
    protected Queue<RequestCommon> queue;

    protected static class RequestCommon {
        public boolean completed = false;

        public void waitCompletion() {
            synchronized (this) {
                while (!completed)
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {} // poor java.
            }
        }
        public void complete() {
            synchronized (this) {
                completed = true;
                this.notifyAll();
            }
        }
    }

    public ServiceCommon() {
        queue = new ArrayDeque<RequestCommon>();
        
        new Thread() {
            public void run() {
                warmUp();
                
                while (true) {
                    RequestCommon r;
                    synchronized (queue) {
                        while (queue.isEmpty())
                            try {
                                queue.wait();
                            } catch (InterruptedException ex) {} // poor java.
                        r = queue.poll();
                    }
                    
                    // process new request.
                    if (r == null)
                        break; // termination.
                    
                    handleRequest(r);
                    r.complete();
                }

                onClose();
            }
        }.start();
    }

    protected void warmUp() {}
    protected void onClose() {}
    
    protected abstract void handleRequest(RequestCommon req);

    protected void postRequest(RequestCommon req) {
        synchronized (queue) {
            queue.add(req);
            queue.notify();
        }
    }

    public void close() {
        postRequest(null);
    }
}
