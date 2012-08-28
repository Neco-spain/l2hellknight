package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreManageListSell;

public final class RequestPrivateStoreManageSell extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    if (player.isAlikeDead())
    {
      player.sendActionFailed();
      return;
    }

    if (!player.canTrade())
    {
      player.sendActionFailed();
      return;
    }

    if ((player.isInOlympiadMode()) || (player.getMountType() != 0))
    {
      player.sendActionFailed();
      return;
    }

    if (player.getActiveTradeList() != null)
    {
      player.cancelActiveTrade();
      player.sendActionFailed();
      return;
    }

    if ((player.getPrivateStoreType() == 1) || (player.getPrivateStoreType() == 2) || (player.getPrivateStoreType() == 8))
    {
      player.setPrivateStoreType(0);
    }
    if (player.getPrivateStoreType() == 0)
    {
      if (player.isSitting()) player.standUp();
      player.setPrivateStoreType(2);
      player.sendPacket(new PrivateStoreManageListSell(player));
    }
  }
}