package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;

public class ExSetCompassZoneCode extends L2GameServerPacket
{
  public static final int ZONE_ALTERED = 8;
  public static final int ZONE_ALTERED2 = 9;
  public static final int ZONE_REMINDER = 10;
  public static final int ZONE_SIEGE = 11;
  public static final int ZONE_PEACE = 12;
  public static final int ZONE_SSQ = 13;
  public static final int ZONE_PVP = 14;
  public static final int ZONE_GENERAL_FIELD = 15;
  public static final int ZONE_PVP_FLAG = 16384;
  public static final int ZONE_ALTERED_FLAG = 256;
  public static final int ZONE_SIEGE_FLAG = 2048;
  public static final int ZONE_PEACE_FLAG = 4096;
  public static final int ZONE_SSQ_FLAG = 8192;
  private final int _zone;

  public ExSetCompassZoneCode(Player player)
  {
    this(player.getZoneMask());
  }

  public ExSetCompassZoneCode(int zoneMask)
  {
    if ((zoneMask & 0x100) == 256)
      _zone = 8;
    else if ((zoneMask & 0x800) == 2048)
      _zone = 11;
    else if ((zoneMask & 0x4000) == 16384)
      _zone = 14;
    else if ((zoneMask & 0x1000) == 4096)
      _zone = 12;
    else if ((zoneMask & 0x2000) == 8192)
      _zone = 13;
    else
      _zone = 15;
  }

  protected final void writeImpl()
  {
    writeEx(51);
    writeD(_zone);
  }
}