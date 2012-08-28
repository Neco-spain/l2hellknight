package net.sf.l2j.gameserver.model;

import java.io.Serializable;

@SuppressWarnings ("serial")
public class Location implements Serializable
{
    private int _x;
    private int _y;
    private int _z;
    private int _heading;

    public Location(int x, int y, int z)
    {
        _x = x;
        _y = y;
        _z = z;
    }

    public Location(int x, int y, int z, int heading)
    {
        _x = x;
        _y = y;
        _z = z;
        _heading = heading;
    }

    public int getX()
    {
        return _x;
    }

    public int getY()
    {
        return _y;
    }

    public int getZ()
    {
        return _z;
    }

    public void setX(int x)
    {
        _x = x;
    }

    public void setY(int y)
    {
        _y = y;
    }

    public void setZ(int z)
    {
        _z = z;
    }

    public int getHeading()
    {
        return _heading;
    }
}