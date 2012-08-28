package net.sf.l2j.gameserver.network.serverpackets;

public class ExSetCompassZoneCode extends L2GameServerPacket
{
  public static final int SIEGEWARZONE1 = 10;
  public static final int SIEGEWARZONE2 = 11;
  public static final int PEACEZONE = 12;
  public static final int SEVENSIGNSZONE = 13;
  public static final int PVPZONE = 14;
  public static final int GENERALZONE = 15;
  private int _zoneType;

  public ExSetCompassZoneCode(int val)
  {
    _zoneType = val;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(50);
    writeD(_zoneType);
  }
}