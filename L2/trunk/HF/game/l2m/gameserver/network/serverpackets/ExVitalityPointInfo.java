package l2m.gameserver.network.serverpackets;

public class ExVitalityPointInfo extends L2GameServerPacket
{
  private final int _vitality;

  public ExVitalityPointInfo(int vitality)
  {
    _vitality = vitality;
  }

  protected void writeImpl()
  {
    writeEx(160);
    writeD(_vitality);
  }
}