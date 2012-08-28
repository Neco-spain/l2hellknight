package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreManageListSell;

public final class RequestPrivateStoreManageSell extends L2GameClientPacket
{
  private static final String _C__73_REQUESTPRIVATESTOREMANAGESELL = "[C] 73 RequestPrivateStoreManageSell";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    if (player.isAlikeDead())
    {
      sendPacket(new ActionFailed());
      return;
    }

    if (player.isInOlympiadMode())
    {
      sendPacket(new ActionFailed());
      return;
    }
    if (player.getMountType() != 0)
    {
      return;
    }

    if ((Config.USE_TRADE_ZONE) && (!player.isInsideZone(32768)))
    {
      player.sendMessage("\u0412\u044B \u043D\u0430\u0445\u043E\u0434\u0438\u0442\u0435\u0441\u044C \u0432 \u0437\u043E\u043D\u0435 \u0433\u0434\u0435 \u0437\u0430\u043F\u0440\u0435\u0449\u0435\u043D\u043E \u0442\u043E\u0440\u0433\u043E\u0432\u0430\u0442\u044C.");
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

  public String getType()
  {
    return "[C] 73 RequestPrivateStoreManageSell";
  }
}