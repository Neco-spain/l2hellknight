package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.SevenSigns;

public class ShowMiniMap extends L2GameServerPacket
{
  private int _mapId;
  private int _period;

  public ShowMiniMap(Player player, int mapId)
  {
    _mapId = mapId;
    _period = SevenSigns.getInstance().getCurrentPeriod();
  }

  protected final void writeImpl()
  {
    writeC(163);
    writeD(_mapId);
    writeC(_period);
  }
}