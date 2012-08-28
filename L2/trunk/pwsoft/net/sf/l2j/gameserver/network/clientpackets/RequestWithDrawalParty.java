package net.sf.l2j.gameserver.network.clientpackets;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestWithDrawalParty extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    if (player.isInParty())
      if ((player.getParty().isInDimensionalRift()) && (!player.getParty().getDimensionalRift().getRevivedAtWaitingRoom().contains(player)))
        player.sendMessage("You can't exit party when you are in Dimensional Rift.");
      else
        player.getParty().oustPartyMember(player);
  }
}