package net.sf.l2j.gameserver.network.serverpackets;

public class JoinParty extends L2GameServerPacket
{
  private static final String _S__4C_JOINPARTY = "[S] 3a JoinParty";
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

  public String getType()
  {
    return "[S] 3a JoinParty";
  }
}