package net.sf.l2j.gameserver.network.serverpackets;

public class ShowXMasSeal extends L2GameServerPacket
{
  private int _item;

  public ShowXMasSeal(int item)
  {
    _item = item;
  }

  protected void writeImpl()
  {
    writeC(242);

    writeD(_item);
  }

  public String getType()
  {
    return "S.ShowXMasSeal";
  }
}