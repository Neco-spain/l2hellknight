package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestChangePartyLeader extends L2GameClientPacket
{
  private static final String _C__EE_REQUESTCHANGEPARTYLEADER = "[C] EE RequestChangePartyLeader";
  private String _name;

  protected void readImpl()
  {
    _name = readS();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if ((activeChar.isInParty()) && (activeChar.getParty().isLeader(activeChar)))
      activeChar.getParty().changePartyLeader(_name);
  }

  public String getType()
  {
    return "[C] EE RequestChangePartyLeader";
  }
}