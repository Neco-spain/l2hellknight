package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class MoveOnVehicle extends L2GameServerPacket
{
  private int _id;
  private int _x;
  private int _y;
  private int _z;
  private L2PcInstance _activeChar;

  public MoveOnVehicle(int vehicleID, L2PcInstance player, int x, int y, int z)
  {
    _id = vehicleID;
    _activeChar = player;
    _x = x;
    _y = y;
    _z = z;
  }

  protected final void writeImpl()
  {
    writeC(113);

    writeD(_activeChar.getObjectId());
    writeD(_id);
    writeD(_x);
    writeD(_y);
    writeD(_z);
    writeD(_activeChar.getX());
    writeD(_activeChar.getY());
    writeD(_activeChar.getZ());
  }
}