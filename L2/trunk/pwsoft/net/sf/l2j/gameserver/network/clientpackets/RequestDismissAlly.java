package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestDismissAlly extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null)
    {
      return;
    }
    if (!player.isClanLeader())
    {
      player.sendPacket(Static.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
      return;
    }
    player.getClan().dissolveAlly(player);
  }
}