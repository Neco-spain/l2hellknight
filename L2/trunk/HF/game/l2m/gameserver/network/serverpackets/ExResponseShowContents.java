package l2m.gameserver.network.serverpackets;

public class ExResponseShowContents extends L2GameServerPacket
{
  private final String _contents;

  public ExResponseShowContents(String contents)
  {
    _contents = contents;
  }

  protected void writeImpl()
  {
    writeEx(176);
    writeS(_contents);
  }
}