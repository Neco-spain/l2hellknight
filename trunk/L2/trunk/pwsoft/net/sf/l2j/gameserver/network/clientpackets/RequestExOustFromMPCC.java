package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestExOustFromMPCC extends L2GameClientPacket
{
  private String _name;

  protected void readImpl()
  {
    _name = readS();
  }

  protected void runImpl()
  {
    L2PcInstance target = L2World.getInstance().getPlayer(_name);
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if ((target != null) && (target.isInParty()) && (player.isInParty()) && (player.getParty().isInCommandChannel()) && (target.getParty().isInCommandChannel()) && (player.getParty().getCommandChannel().getChannelLeader().equals(player)))
    {
      target.getParty().getCommandChannel().removeParty(target.getParty());

      SystemMessage sm = SystemMessage.sendString("Your party was dismissed from the CommandChannel.");
      target.getParty().broadcastToPartyMembers(sm);
    }
    else
    {
      player.sendMessage("Incorrect Target");
    }
  }
}