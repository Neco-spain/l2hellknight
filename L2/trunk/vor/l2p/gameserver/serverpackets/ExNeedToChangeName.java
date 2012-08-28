package l2p.gameserver.serverpackets;

public class ExNeedToChangeName extends L2GameServerPacket
{
  private int _type;
  private int _reason;
  private String _origName;

  public ExNeedToChangeName(int type, int reason, String origName)
  {
    _type = type;
    _reason = reason;
    _origName = origName;
  }

  protected final void writeImpl()
  {
    writeEx(105);
    writeD(_type);
    writeD(_reason);
    writeS(_origName);
  }
}