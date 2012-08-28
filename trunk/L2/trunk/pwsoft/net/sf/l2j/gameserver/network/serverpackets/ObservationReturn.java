package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ObservationReturn extends L2GameServerPacket
{
  private L2PcInstance _activeChar;

  public ObservationReturn(L2PcInstance observer)
  {
    _activeChar = observer;
  }

  protected final void writeImpl()
  {
    writeC(224);
    writeD(_activeChar.getObsX());
    writeD(_activeChar.getObsY());
    writeD(_activeChar.getObsZ());
  }
}