package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public final class RequestEvaluate extends L2GameClientPacket
{
  private int _targetId;

  protected void readImpl()
  {
    _targetId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (player.getTarget() == null) {
      return;
    }

    if (!player.getTarget().isPlayer())
    {
      player.sendPacket(Static.TARGET_IS_INCORRECT);
      return;
    }

    if (player.getLevel() < 10)
    {
      player.sendPacket(Static.ONLY_LEVEL_SUP_10_CAN_RECOMMEND);
      return;
    }

    if (player.getTarget() == player)
    {
      player.sendPacket(Static.YOU_CANNOT_RECOMMEND_YOURSELF);
      return;
    }

    if (player.getRecomLeft() <= 0)
    {
      player.sendPacket(Static.NO_MORE_RECOMMENDATIONS_TO_HAVE);
      return;
    }

    L2PcInstance target = player.getTarget().getPlayer();
    if (target.getRecomHave() >= 255)
    {
      player.sendPacket(Static.YOU_NO_LONGER_RECIVE_A_RECOMMENDATION);
      return;
    }

    if (!player.canRecom(target))
    {
      player.sendPacket(Static.THAT_CHARACTER_IS_RECOMMENDED);
      return;
    }

    player.giveRecom(target);
    player.sendPacket(new UserInfo(player));
    target.broadcastUserInfo();
    player.sendPacket(SystemMessage.id(SystemMessageId.YOU_HAVE_RECOMMENDED).addString(target.getName()).addNumber(player.getRecomLeft()));
    target.sendPacket(SystemMessage.id(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED).addString(player.getName()));
  }
}