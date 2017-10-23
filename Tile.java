package com.atomicobject.rts;

import org.json.simple.JSONObject;

public class Tile {

    Location loc;
    Boolean visible;
    boolean blocked;
    resource resource;
    Boolean isHome = false;

    public Tile(JSONObject json) {
        long y = (long) json.get("y");
        long x = (long) json.get("x");
        loc = new Location((int)x,(int) y);
        visible = (Boolean) json.get("visible");
        blocked = (Boolean) json.get("blocked");
        if (json.get("resources")!= null) {
            resource = new resource((JSONObject) json.get("resources"));
        }
        if (y == 0 && x == 0){
            isHome = true;
        }

    }

    int Distance(Tile t2)
    {
        long xdist = t2.loc.getX() - this.loc.getX();
        long ydist = t2.loc.getY() - this.loc.getY();
        long dist = Math.abs(xdist)+ Math.abs(ydist);
        return (int) dist;
    }

    public Tile(Location loc) {
        this.loc = loc;
        visible = false;
        blocked = false;
        resource = null;

    }

    public Location getLoc() {
        return loc;
    }
}
