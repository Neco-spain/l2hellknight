package net.sf.l2j.gameserver.network.serverpackets;

public class Ride extends L2GameServerPacket
{
  public static final int ACTION_MOUNT = 1;
  public static final int ACTION_DISMOUNT = 0;
  private int _id;
  private int _bRide;
  private int _rideType;
  private int _rideClassID;

  public Ride(int id, int action, int rideClassId)
  {
    _id = id;
    _bRide = action;
    _rideClassID = (rideClassId + 1000000);

    if ((rideClassId == 12526) || (rideClassId == 12527) || (rideClassId == 12528))
    {
      _rideType = 1;
    }
    else if (rideClassId == 12621)
    {
      _rideType = 2;
    }
  }

  public void runImpl()
  {
  }

  public int getMountType()
  {
    return _rideType;
  }

  protected final void writeImpl()
  {
    writeC(134);
    writeD(_id);
    writeD(_bRide);
    writeD(_rideType);
    writeD(_rideClassID);
  }
}