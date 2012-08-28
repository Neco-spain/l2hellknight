package l2p.gameserver.serverpackets;

public class ExChangeClientEffectInfo extends L2GameServerPacket
{
  private int _state;

  public ExChangeClientEffectInfo(int state)
  {
    _state = state;
  }

  protected void writeImpl()
  {
    writeEx(193);
    writeD(0);
    writeD(_state);
  }
}