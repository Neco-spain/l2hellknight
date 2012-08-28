package l2p.gameserver.serverpackets;

public class ExChangeNpcState extends L2GameServerPacket
{
  private int _objId;
  private int _state;

  public ExChangeNpcState(int objId, int state)
  {
    _objId = objId;
    _state = state;
  }

  protected void writeImpl()
  {
    writeEx(190);
    writeD(_objId);
    writeD(_state);
  }
}