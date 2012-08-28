package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TransactionType;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ExAskJoinMPCC;

public final class RequestExAskJoinMPCC extends L2GameClientPacket
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
    L2PcInstance target = L2World.getInstance().getPlayer(_name);
    if (target == null)
    {
      player.sendPacket(Static.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
      return;
    }

    if ((player.isInParty()) && (target.isInParty()) && (player.getParty().equals(target.getParty()))) {
      return;
    }
    L2Party activeParty = player.getParty();

    if ((activeParty == null) || ((activeParty.isInCommandChannel()) && (!activeParty.getCommandChannel().getChannelLeader().equals(player))))
    {
      player.sendMessage("\u0412\u044B \u043D\u0435 \u0438\u043C\u0435\u0435\u0442\u0435 \u043F\u0440\u0430\u0432 \u043D\u0430 \u043F\u0440\u0438\u0433\u043B\u0430\u0448\u0435\u043D\u0438\u0435 \u0432 \u043A\u0430\u043D\u0430\u043B \u043A\u043E\u043C\u0430\u043D\u0434\u044B");
      return;
    }

    if ((!target.isInParty()) || (!target.getParty().isLeader(target)))
    {
      player.sendMessage("\u041D\u0435\u043F\u0440\u0430\u0432\u0438\u043B\u044C\u043D\u0430\u044F \u0446\u0435\u043B\u044C \u0431\u044B\u043B\u0430 \u043F\u0440\u0438\u0433\u043B\u0430\u0448\u0435\u043D\u0430");
      return;
    }

    if (target.getParty().isInCommandChannel())
    {
      player.sendMessage("\u0413\u0440\u0443\u043F\u043F\u0430 " + target.getName() + " \u0443\u0436\u0435 \u043F\u0440\u0438\u0441\u043E\u0435\u0434\u0438\u043D\u0438\u043B\u0430\u0441\u044C \u043A \u043A\u0430\u043D\u0430\u043B\u0443 \u043A\u043E\u043C\u0430\u043D\u0434\u044B");
      return;
    }

    if (target.isTransactionInProgress())
    {
      player.sendMessage("\u041F\u0435\u0440\u0441\u043E\u043D\u0430\u0436 \u0437\u0430\u043D\u044F\u0442, \u043F\u043E\u043F\u0440\u043E\u0431\u0443\u0439\u0442\u0435 \u043F\u043E\u0437\u0436\u0435");
      return;
    }

    player.setTransactionType(L2PcInstance.TransactionType.CHANNEL);
    target.setTransactionRequester(player, System.currentTimeMillis() + 30000L);
    target.setTransactionType(L2PcInstance.TransactionType.CHANNEL);
    target.sendPacket(new ExAskJoinMPCC(player.getName()));
    player.sendMessage("\u0412\u044B \u043F\u0440\u0438\u0433\u043B\u0430\u0441\u0438\u043B\u0438 " + target.getName() + " \u0432 \u043A\u0430\u043D\u0430\u043B \u043A\u043E\u043C\u0430\u043D\u0434\u044B");
  }
}