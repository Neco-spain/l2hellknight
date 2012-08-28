package net.sf.l2j.gameserver.network.clientpackets;

public final class AnswerJoinPartyRoom extends L2GameClientPacket
{
  private static final String _C__D0_15_ANSWERJOINPARTYROOM = "[C] D0:15 AnswerJoinPartyRoom";
  private int _requesterID;

  protected void readImpl()
  {
    _requesterID = readD();
  }

  protected void runImpl()
  {
  }

  public String getType()
  {
    return "[C] D0:15 AnswerJoinPartyRoom";
  }
}