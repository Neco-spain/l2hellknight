package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.GameTimeController;

public class ClientSetTime extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(236);
    writeD(GameTimeController.getInstance().getGameTime());
    writeD(6);
  }
}