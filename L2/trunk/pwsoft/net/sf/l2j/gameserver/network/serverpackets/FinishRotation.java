package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class FinishRotation extends L2GameServerPacket
{
  private int _heading;
  private int _charObjId;

  public FinishRotation(L2Character cha)
  {
    _charObjId = cha.getObjectId();
    _heading = cha.getHeading();
  }

  protected final void writeImpl()
  {
    writeC(99);
    writeD(_charObjId);
    writeD(_heading);
  }
}