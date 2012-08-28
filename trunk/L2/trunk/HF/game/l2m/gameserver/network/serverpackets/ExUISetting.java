package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;

public class ExUISetting extends L2GameServerPacket
{
  private final byte[] data;

  public ExUISetting(Player player)
  {
    data = player.getKeyBindings();
  }

  protected void writeImpl()
  {
    writeEx(112);
    writeD(data.length);
    writeB(data);
  }
}