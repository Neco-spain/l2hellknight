package l2.hellknight.gameserver.model;

import l2.hellknight.gameserver.model.actor.L2Character;

public final class Location
{
	public int _x;
	public int _y;
	public int _z;
	public int _heading;
	public int npcId;


	public Location(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}

	public Location(L2Object obj)
	{
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();
	}
	
	public Location(L2Character obj)
	{
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();
		_heading = obj.getHeading();
	}
	
	
	public Location(int x, int y, int z, int heading)
	{
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
	}
	
    public Location(int npcid, int x, int y, int z, int heading)
	{
		npcId = npcid;
        this._x = x;
		this._y = y;
		this._z = z;
		this._heading = heading;
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

	public int getHeading()
	{
		return _heading;
	}
}
