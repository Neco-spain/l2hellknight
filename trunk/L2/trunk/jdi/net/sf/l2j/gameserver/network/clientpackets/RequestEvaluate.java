package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public final class RequestEvaluate extends L2GameClientPacket
{
  private static final String _C__B9_REQUESTEVALUATE = "[C] B9 RequestEvaluate";
  private int _targetId;

  protected void readImpl()
  {
    _targetId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }

    if (!(activeChar.getTarget() instanceof L2PcInstance))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT);
      activeChar.sendPacket(sm);
      sm = null;
      return;
    }

    if (activeChar.getLevel() < 10)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.ONLY_LEVEL_SUP_10_CAN_RECOMMEND);
      activeChar.sendPacket(sm);
      sm = null;
      return;
    }

    if (activeChar.getTarget() == activeChar)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF);
      activeChar.sendPacket(sm);
      sm = null;
      return;
    }

    if (activeChar.getRecomLeft() <= 0)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.NO_MORE_RECOMMENDATIONS_TO_HAVE);
      activeChar.sendPacket(sm);
      sm = null;
      return;
    }

    L2PcInstance target = (L2PcInstance)activeChar.getTarget();

    if (target.getRecomHave() >= 255)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.YOU_NO_LONGER_RECIVE_A_RECOMMENDATION);
      activeChar.sendPacket(sm);
      sm = null;
      return;
    }

    if (!activeChar.canRecom(target))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.THAT_CHARACTER_IS_RECOMMENDED);
      activeChar.sendPacket(sm);
      sm = null;
      return;
    }

    activeChar.giveRecom(target);

    SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED);
    sm.addString(target.getName());
    sm.addNumber(activeChar.getRecomLeft());
    activeChar.sendPacket(sm);

    sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED);
    sm.addString(activeChar.getName());
    target.sendPacket(sm);
    sm = null;

    activeChar.sendPacket(new UserInfo(activeChar));
    target.broadcastUserInfo();
  }

  public String getType()
  {
    return "[C] B9 RequestEvaluate";
  }
}