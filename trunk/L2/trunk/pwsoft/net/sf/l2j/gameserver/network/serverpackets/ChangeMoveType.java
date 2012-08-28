package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class ChangeMoveType extends L2GameServerPacket
{
  private int _charObjId;
  private boolean _running;

  public ChangeMoveType(L2Character character)
  {
    _charObjId = character.getObjectId();
    _running = character.isRunning();
  }

  protected final void writeImpl()
  {
    writeC(46);
    writeD(_charObjId);
    writeD(_running ? 1 : 0);
    writeD(0);
  }
}