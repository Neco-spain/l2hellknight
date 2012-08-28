package net.sf.l2j.gameserver.network.clientpackets;

import java.util.List;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestExOustFromMPCC extends L2GameClientPacket
{
  private static final String _C__D0_0F_REQUESTEXOUSTFROMMPCC = "[C] D0:0F RequestExOustFromMPCC";
  private String _name;

  protected void readImpl()
  {
    _name = readS();
  }

  protected void runImpl()
  {
    L2PcInstance target = L2World.getInstance().getPlayer(_name);
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if ((target != null) && (target.isInParty()) && (activeChar.isInParty()) && (activeChar.getParty().isInCommandChannel()) && (target.getParty().isInCommandChannel()) && (activeChar.getParty().getCommandChannel().getChannelLeader().equals(activeChar)))
    {
      target.getParty().getCommandChannel().removeParty(target.getParty());

      SystemMessage sm = SystemMessage.sendString("Your party was dismissed from the CommandChannel.");
      target.getParty().broadcastToPartyMembers(sm);

      sm = SystemMessage.sendString(((L2PcInstance)target.getParty().getPartyMembers().get(0)).getName() + "'s party was dismissed from the CommandChannel.");
    }
    else
    {
      activeChar.sendMessage("Incorrect Target");
    }
  }

  public String getType()
  {
    return "[C] D0:0F RequestExOustFromMPCC";
  }
}