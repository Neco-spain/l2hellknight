package l2m.gameserver.network.serverpackets;

public class JoinParty extends L2GameServerPacket
{
  public static final L2GameServerPacket SUCCESS = new JoinParty(1);
  public static final L2GameServerPacket FAIL = new JoinParty(0);
  private int _response;

  public JoinParty(int response)
  {
    _response = response;
  }

  protected final void writeImpl()
  {
    writeC(58);
    writeD(_response);
  }
}