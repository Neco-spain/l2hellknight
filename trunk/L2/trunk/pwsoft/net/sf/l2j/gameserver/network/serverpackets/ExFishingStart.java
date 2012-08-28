package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class ExFishingStart extends L2GameServerPacket
{
  private L2Character _activeChar;
  private int _x;
  private int _y;
  private int _z;
  private int _fishType;
  private boolean _isNightLure;

  public ExFishingStart(L2Character character, int fishType, int x, int y, int z, boolean isNightLure)
  {
    _activeChar = character;
    _fishType = fishType;
    _x = x;
    _y = y;
    _z = z;
    _isNightLure = isNightLure;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(19);
    writeD(_activeChar.getObjectId());
    writeD(_fishType);
    writeD(_x);
    writeD(_y);
    writeD(_z);
    writeC(0);
    writeC(0);
    writeC((_fishType >= 7) && (_fishType <= 9) ? 1 : 0);
    writeC(0);
  }
}