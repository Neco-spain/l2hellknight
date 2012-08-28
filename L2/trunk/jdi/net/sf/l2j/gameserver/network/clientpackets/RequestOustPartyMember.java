package net.sf.l2j.gameserver.network.clientpackets;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestOustPartyMember extends L2GameClientPacket
{
  private static final String _C__2C_REQUESTOUSTPARTYMEMBER = "[C] 2C RequestOustPartyMember";
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
    {
      if ((activeChar.getParty().isInDimensionalRift()) && (!activeChar.getParty().getDimensionalRift().getRevivedAtWaitingRoom().contains(activeChar)))
        activeChar.sendMessage("You can't dismiss party member when you are in Dimensional Rift.");
      else
        activeChar.getParty().oustPartyMember(_name);
    }
  }

  public String getType()
  {
    return "[C] 2C RequestOustPartyMember";
  }
}