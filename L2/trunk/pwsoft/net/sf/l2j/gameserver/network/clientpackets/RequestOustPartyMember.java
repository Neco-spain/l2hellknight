package net.sf.l2j.gameserver.network.clientpackets;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestOustPartyMember extends L2GameClientPacket
{
  private String _name;

  protected void readImpl()
  {
    _name = readS();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.getCPD() < 400L) {
      return;
    }
    if (player.isOutOfControl()) {
      return;
    }
    player.setCPD();

    if ((player.isInParty()) && (player.getParty().isLeader(player)))
    {
      if ((player.getParty().isInDimensionalRift()) && (!player.getParty().getDimensionalRift().getRevivedAtWaitingRoom().contains(player)))
        player.sendMessage("You can't dismiss party member when you are in Dimensional Rift.");
      else
        player.getParty().oustPartyMember(_name);
    }
  }
}