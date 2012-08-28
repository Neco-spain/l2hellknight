package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.GameTimeController;

public class ClientSetTime extends L2GameServerPacket
{
  private static final String _S__EC_CLIENTSETTIME = "[S] f2 ClientSetTime [dd]";

  protected final void writeImpl()
  {
    writeC(236);
    writeD(GameTimeController.getInstance().getGameTime());
    writeD(6);
  }

  public String getType()
  {
    return "[S] f2 ClientSetTime [dd]";
  }
}