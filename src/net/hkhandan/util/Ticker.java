
package net.hkhandan.util;

/**
 *
 * @author hamed
 */
public abstract class Ticker extends Thread {
    private boolean stop = false;
    long interval = 1000;
    public Ticker(long interval) {
        this.interval = interval;
    }
    public Ticker(String name, long interval) {
        this.interval = interval;
        setName(name);
    }
    public void setInterval(long delay) {
        interval = delay;
    }
    @Override synchronized public void run() {
        while(!stop) {
            try {
                wait(interval);
            } catch (InterruptedException ex) {
                // nothing
            }
            tick();
        }
    }
    public void die() {
        stop = true;
    }
    public void _die() {
        die();
    }
    public synchronized void trigger() {
        notify();
    }
    abstract public void tick();
}
