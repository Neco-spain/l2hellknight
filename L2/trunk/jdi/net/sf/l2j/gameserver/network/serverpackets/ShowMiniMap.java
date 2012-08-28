package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.SevenSigns;

public class ShowMiniMap extends L2GameServerPacket
{
  private static final String _S__B6_SHOWMINIMAP = "[S] 9d ShowMiniMap";
  private int _mapId;

  public ShowMiniMap(int mapId)
  {
    _mapId = mapId;
  }

  protected final void writeImpl()
  {
    writeC(157);
    writeD(_mapId);
    writeD(SevenSigns.getInstance().getCurrentPeriod());
  }

  public String getType()
  {
    return "[S] 9d ShowMiniMap";
  }
}