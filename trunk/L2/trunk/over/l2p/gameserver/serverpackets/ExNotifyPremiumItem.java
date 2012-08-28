package l2p.gameserver.serverpackets;

public class ExNotifyPremiumItem extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ExNotifyPremiumItem();

  protected void writeImpl()
  {
    writeEx(133);
  }
}