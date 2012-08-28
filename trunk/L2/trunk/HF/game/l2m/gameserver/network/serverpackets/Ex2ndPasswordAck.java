package l2m.gameserver.network.serverpackets;

public class Ex2ndPasswordAck extends L2GameServerPacket
{
  public static final int SUCCESS = 0;
  public static final int WRONG_PATTERN = 1;
  int _response;

  public Ex2ndPasswordAck(int response)
  {
    _response = response;
  }

  protected void writeImpl()
  {
    writeEx(231);
    writeC(0);
    writeD(_response == 1 ? 1 : 0);
    writeD(0);
  }
}