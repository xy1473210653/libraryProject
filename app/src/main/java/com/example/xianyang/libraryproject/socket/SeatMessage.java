package com.example.xianyang.libraryproject.socket;

public class SeatMessage {
    private int floorID;
    private int areaID;
    private int [][] seat=new int[8][10];

    public int getFloorID() {
        return floorID;
    }

    public int getAreaID() {
        return areaID;
    }

    private SeatMessage(int floorID, int areaID, int[][] seat)
    {
        this.floorID=floorID;
        this.areaID=areaID;
        this.seat=seat;
    }
    public boolean getSeatSold(int row, int column)
    {
        if (seat[row][column]==1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
