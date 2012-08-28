package net.sf.l2j.gameserver.network.serverpackets;

public class ExDuelAskStart extends L2GameServerPacket
{
  private static final String _S__FE_4B_EXDUELASKSTART = "[S] FE:4B ExDuelAskStart";
  private String _requestorName;
  private int _partyDuel;

  public ExDuelAskStart(String requestor, int partyDuel)
  {
    _requestorName = requestor;
    _partyDuel = partyDuel;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(75);

    writeS(_requestorName);
    writeD(_partyDuel);
  }

  public String getType()
  {
    return "[S] FE:4B ExDuelAskStart";
  }
}