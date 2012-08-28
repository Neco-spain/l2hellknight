package net.sf.l2j.gameserver.network.serverpackets;

public class NormalCamera extends L2GameServerPacket
{
  private static final String _S__C8_NORMALCAMERA = "[S] C8 NormalCamera";

  public void writeImpl()
  {
    writeC(200);
  }
}