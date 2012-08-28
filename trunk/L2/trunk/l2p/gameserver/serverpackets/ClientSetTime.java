package l2p.gameserver.serverpackets;

import l2p.gameserver.GameTimeController;

public class ClientSetTime extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ClientSetTime();

  protected final void writeImpl()
  {
    writeC(242);
    writeD(GameTimeController.getInstance().getGameTime());
    writeD(6);
  }
}