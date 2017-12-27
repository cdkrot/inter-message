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

    protected abstract static class RunnableRequest extends RequestCommon {
        public abstract void run();
    }
    
    public ServiceCommon() {
        queue = new ArrayDeque<RequestCommon>();
        
        new Thread() {
            public void run() {
                synchronized (queue) {
                    warmUp();
                }
                
                while (true) {
                    RequestCommon r;
                    while (true) {
                        synchronized (queue) {
                            if (!queue.isEmpty())
                                break;
                        }
                        
                        special();
                    }

                    synchronized (queue) {
                        r = queue.poll();
                    }
                    
                    // process new request.
                    if (r == null)
                        break; // termination.

                    if (r instanceof RunnableRequest)
                        ((RunnableRequest)(r)).run();
                    else
                        handleRequest(r);
                    
                    r.complete();
                }

                onClose();
            }
        }.start();

        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException ex) {}
    }

    protected void special() {
        try {
            queue.wait();
        } catch (InterruptedException ex) {} // poor java
    }

    protected void interrupt() {
        queue.notify();        
    }
    
    protected void warmUp() {}
    protected void onClose() {}
    
    protected abstract void handleRequest(RequestCommon req);

    protected void postRequest(RequestCommon req) {
        synchronized (queue) {
            queue.add(req);
            interrupt();
        }
    }

    public void close() {
        postRequest(null);
    }
}
