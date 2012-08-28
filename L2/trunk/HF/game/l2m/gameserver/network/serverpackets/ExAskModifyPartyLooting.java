package l2m.gameserver.network.serverpackets;

public class ExAskModifyPartyLooting extends L2GameServerPacket
{
  private String _requestor;
  private int _mode;

  public ExAskModifyPartyLooting(String name, int mode)
  {
    _requestor = name;
    _mode = mode;
  }

  protected void writeImpl()
  {
    writeEx(191);
    writeS(_requestor);
    writeD(_mode);
  }
}