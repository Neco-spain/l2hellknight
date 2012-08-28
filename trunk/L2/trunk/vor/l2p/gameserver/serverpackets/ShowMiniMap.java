package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.SevenSigns;

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