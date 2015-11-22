package ru.ifmo.acm.mainscreen.statuses;

public class ClockStatus {
    public synchronized void setClockVisible(boolean visible) {
            clockTimestamp = System.currentTimeMillis();
            isClockVisible = visible;
    }

    public synchronized boolean isClockVisible() {
        return isClockVisible;
    }

    public synchronized String clockStatus() {
        return clockTimestamp + "\n" + isClockVisible;
    }

    public long clockTimestamp;
    public boolean isClockVisible;
    final private Object clockLock = new Object();
}