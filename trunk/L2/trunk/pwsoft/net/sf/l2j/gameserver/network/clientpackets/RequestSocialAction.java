package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;

public class RequestSocialAction extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestSocialAction.class.getName());
  private int _actionId;

  protected void readImpl()
  {
    _actionId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPP() < 500L) {
      return;
    }
    player.sCPP();

    if (player.isFishing())
    {
      player.sendPacket(Static.CANNOT_DO_WHILE_FISHING_3);
      return;
    }

    if ((_actionId < 2) || (_actionId > 13))
    {
      return;
    }

    if ((player.getPrivateStoreType() == 0) && (player.getTransactionRequester() == null) && (!player.isAlikeDead()) && ((!player.isAllSkillsDisabled()) || (player.isInDuel())) && (player.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE))
    {
      player.broadcastPacket(new SocialAction(player.getObjectId(), _actionId));
    }
  }

  public String getType()
  {
    return "C.SocialAction";
  }
}