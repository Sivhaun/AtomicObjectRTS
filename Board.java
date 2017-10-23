package com.atomicobject.rts;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Board {
    int height;
    int width;

    private HashMap map;
    ArrayList <Tile> recourceTiles;

    public Board(long width, long height) {
        this.map = new HashMap<String, Tile>();
        this.height = (int)height;
        this.width = (int)width;
        recourceTiles = new ArrayList<Tile>();
    }

    public Location nearestResocurce(Tile start){
        Tile shortest = recourceTiles.get(1);
        int dist =  start.Distance(shortest);

       for (Tile tile: recourceTiles)
       {
           if (tile.Distance(start) < dist && tile.isHome == false){
               dist = tile.Distance(start);
               shortest = tile;
           }
       }
       return shortest.loc;
    }
    Location explore(Location start)
    {

        int x = (int) Math.floor(Math.random()*2);
        int y = (int) Math.floor(Math.random() * 2);
        Location newLoc = start;

        Boolean up = false;
        if (Math.random()* 2 > 1 )
            up = true;

        while (map.get(newLoc) != null)
        {
            long newY = newLoc.getY()+ y;
            long newx = newLoc.getX()+ x;
            newLoc = new Location(newx, newY);
        }

        return newLoc;

    }

    public void put(Tile tile)
    {
        Location loc = tile.getLoc();
        map.put(loc.toString(), tile);
        if (tile.resource != null)
            recourceTiles.add(tile);
    }

    public Tile get(Location loc)
    {
       Tile tile = (Tile)map.get(loc.toString());
       return tile;
    }

    public String pathFinding(Location startLoc, Location targetLoc)
    {
        Queue frontier = new ConcurrentLinkedQueue();
        frontier.add(startLoc);
        HashMap cameFrom = new HashMap<String, Location>();
        cameFrom.put(startLoc.toString(),  null);

        Location currentLoc = startLoc;
        while (!(frontier.isEmpty())){
            currentLoc = (Location)frontier.poll();
            if (currentLoc == targetLoc)
            {
                break;
            }

            ArrayList <Tile> neighbors = neighbors(currentLoc);
            for(Tile next : neighbors)
            {
                if (cameFrom.get(next.loc.toString()) == null)
                {
                    frontier.add(next.loc);
                    cameFrom.put(next.loc.toString(), currentLoc);

                }
            }

        }

        ArrayList<Location> path = traceback(startLoc, targetLoc, cameFrom);


        if (path.size() < 2)
        {
            return null;
        }
        Location tar = path.get(1);

        String Direction = DetermineDirection(startLoc, tar);
        return Direction;
    }

    private String DetermineDirection(Location start, Location target)
    {
        String direction = "";
        if (target.getX()> start.getX())
        {direction = "W";}
        if (target.getX()< start.getX())
        { direction = "E";}
        if (target.getY()> start.getY())
        { direction = "N";}
        else
        {direction = "S";}
        System.out.println(direction);
        return direction;
    }

    String isNexToRecource(Location loc)
        {
            ArrayList <Tile> neighborsList = this.neighbors(loc);
            for(Tile tile : neighborsList) {
                if (tile.resource == null && tile.isHome == false) {
                    return DetermineDirection(loc, tile.loc);
                }
            }
            return "null";

        }



    private ArrayList<Location> traceback (Location start, Location target, HashMap cameFrom){
        Location currentLoc = target;
        ArrayList <Location> path = new ArrayList<Location>();
        path.add(currentLoc);

        while (currentLoc != null && (currentLoc.toString().equalsIgnoreCase(start.toString()))) //(!currentLoc.toString().equalsIgnoreCase(start.toString()))
        {
            currentLoc = (Location)cameFrom.get(currentLoc.toString());
            path.add(currentLoc);

        }
        Collections.reverse(path);
        return path;
    }

    private ArrayList<Tile> neighbors(Location loc){
     ArrayList <Tile> neighbors = new ArrayList<Tile>();

     long col = loc.getY();
     long row = loc.getX();

     Location N1 = new Location(col+1, row);
     Location N2 = new Location(col-1, row);
     Location N3 = new Location(col, row+1);
     Location N4 = new Location(col, row-1);

     Tile T1 = (Tile)map.get(N1.toString());
     if (T1 != null && T1.blocked == false)
     {
       neighbors.add(T1);
     }
     else {
        // System.out.println("null tile");
     }


     Tile T2 = (Tile)map.get(N2.toString());
     if (T2 != null  && T2.blocked == false)
        {
            neighbors.add(T2);
        }

     else {
        // System.out.println("null tile");
     }

     Tile T3 = (Tile)map.get(N3.toString());
     if (T3 != null  && T3.blocked== false)
     {
     neighbors.add(T3);
     }
     else {
        // System.out.println("null tile");
     }


     Tile T4 = (Tile)map.get(N4.toString());
     if (T4 != null  && T4.blocked== false) {
         neighbors.add(T4);
     }
     else {
        // System.out.println("null tile");
     }
        return neighbors;
    }


}
