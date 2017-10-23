package com.atomicobject.rts;

public class Location {

    long x;
    long y;



    public Location(long x, long y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        String toRetrun = Long.toString(x)+ "," + Long.toString(y);
        return toRetrun;
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }
}
