package l2p.gameserver.serverpackets;

public class ExNavitAdventEffect extends L2GameServerPacket
{
  private int _time;

  public ExNavitAdventEffect(int time)
  {
    _time = time;
  }

  protected final void writeImpl()
  {
    writeEx(224);
    writeD(_time);
  }
}