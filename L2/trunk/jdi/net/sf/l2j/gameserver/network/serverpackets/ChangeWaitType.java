package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class ChangeWaitType extends L2GameServerPacket
{
  private static final String _S__3F_CHANGEWAITTYPE = "[S] 2F ChangeWaitType";
  private int _charObjId;
  private int _moveType;
  private int _x;
  private int _y;
  private int _z;
  public static final int WT_SITTING = 0;
  public static final int WT_STANDING = 1;
  public static final int WT_START_FAKEDEATH = 2;
  public static final int WT_STOP_FAKEDEATH = 3;

  public ChangeWaitType(L2Character character, int newMoveType)
  {
    _charObjId = character.getObjectId();
    _moveType = newMoveType;

    _x = character.getX();
    _y = character.getY();
    _z = character.getZ();
  }

  protected final void writeImpl()
  {
    writeC(47);
    writeD(_charObjId);
    writeD(_moveType);
    writeD(_x);
    writeD(_y);
    writeD(_z);
  }

  public String getType()
  {
    return "[S] 2F ChangeWaitType";
  }
}