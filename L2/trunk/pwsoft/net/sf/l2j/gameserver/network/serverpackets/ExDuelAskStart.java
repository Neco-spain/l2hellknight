package net.sf.l2j.gameserver.network.serverpackets;

public class ExDuelAskStart extends L2GameServerPacket
{
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
}