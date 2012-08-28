package l2p.gameserver.serverpackets;

public class ExDominionChannelSet extends L2GameServerPacket
{
  public static final L2GameServerPacket ACTIVE = new ExDominionChannelSet(1);
  public static final L2GameServerPacket DEACTIVE = new ExDominionChannelSet(0);
  private int _active;

  public ExDominionChannelSet(int active)
  {
    _active = active;
  }

  protected void writeImpl()
  {
    writeEx(150);
    writeD(_active);
  }
}