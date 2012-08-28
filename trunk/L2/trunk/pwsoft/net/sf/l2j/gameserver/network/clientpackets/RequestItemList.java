package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;

public final class RequestItemList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    if ((getClient() != null) && (((L2GameClient)getClient()).getActiveChar() != null) && (!((L2GameClient)getClient()).getActiveChar().isInvetoryDisabled()))
    {
      L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

      if (player == null) {
        return;
      }
      if (System.currentTimeMillis() - player.gCPV() < 100L)
      {
        player.sendActionFailed();
        return;
      }

      player.sCPV();

      if (player.getActiveTradeList() != null)
      {
        player.cancelActiveTrade();
        player.sendActionFailed();
        return;
      }

      if (player.getActiveEnchantItem() != null)
      {
        player.setActiveEnchantItem(null);
        player.sendPacket(new EnchantResult(0, true));
        player.sendActionFailed();
        return;
      }

      if (player.getActiveWarehouse() != null)
      {
        player.cancelActiveWarehouse();
        player.sendActionFailed();
        return;
      }

      player.sendItems(true);
    }
  }
}