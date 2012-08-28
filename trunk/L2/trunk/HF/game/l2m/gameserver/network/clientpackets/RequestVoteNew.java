package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.SystemMsg;

public class RequestVoteNew extends L2GameClientPacket
{
  private int _targetObjectId;

  protected void readImpl()
  {
    _targetObjectId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    if (!activeChar.getPlayerAccess().CanEvaluate) {
      return;
    }
    GameObject target = activeChar.getTarget();
    if ((target == null) || (!target.isPlayer()) || (target.getObjectId() != _targetObjectId))
    {
      activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
      return;
    }

    if (target.getObjectId() == activeChar.getObjectId())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_RECOMMEND_YOURSELF);
      return;
    }

    Player targetPlayer = (Player)target;

    if (activeChar.getRecomLeft() <= 0)
    {
      activeChar.sendPacket(Msg.NO_MORE_RECOMMENDATIONS_TO_HAVE);
      return;
    }

    if (targetPlayer.getRecomHave() >= 255)
    {
      activeChar.sendPacket(Msg.YOU_NO_LONGER_RECIVE_A_RECOMMENDATION);
      return;
    }

    activeChar.giveRecom(targetPlayer);
    SystemMessage sm = new SystemMessage(830);
    sm.addString(target.getName());
    sm.addNumber(activeChar.getRecomLeft());
    activeChar.sendPacket(sm);

    sm = new SystemMessage(831);
    sm.addString(activeChar.getName());
    targetPlayer.sendPacket(sm);
  }
}