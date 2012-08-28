package l2p.gameserver.serverpackets;

public class ShowXMasSeal extends L2GameServerPacket
{
  private int _item;

  public ShowXMasSeal(int item)
  {
    _item = item;
  }

  protected void writeImpl()
  {
    writeC(248);
    writeD(_item);
  }
}