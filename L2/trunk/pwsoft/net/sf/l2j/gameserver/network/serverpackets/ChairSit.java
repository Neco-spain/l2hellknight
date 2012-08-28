package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ChairSit extends L2GameServerPacket
{
  private L2PcInstance _activeChar;
  private int _staticObjectId;

  public ChairSit(L2PcInstance player, int staticObjectId)
  {
    _activeChar = player;
    _staticObjectId = staticObjectId;
  }

  protected final void writeImpl()
  {
    writeC(225);
    writeD(_activeChar.getObjectId());
    writeD(_staticObjectId);
  }
}