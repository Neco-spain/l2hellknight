package l2m.gameserver.network.serverpackets;

public class ExOlympiadMode extends L2GameServerPacket
{
  private int _mode;

  public ExOlympiadMode(int mode)
  {
    _mode = mode;
  }

  protected final void writeImpl()
  {
    writeEx(124);

    writeC(_mode);
  }
}