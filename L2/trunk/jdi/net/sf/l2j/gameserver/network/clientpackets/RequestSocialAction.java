package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.gameserver.util.Util;

public class RequestSocialAction extends L2GameClientPacket
{
  private static final String _C__1B_REQUESTSOCIALACTION = "[C] 1B RequestSocialAction";
  private static Logger _log = Logger.getLogger(RequestSocialAction.class.getName());
  private int _actionId;

  protected void readImpl()
  {
    _actionId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }

    if (activeChar.isFishing())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
      activeChar.sendPacket(sm);
      sm = null;
      return;
    }

    if ((_actionId < 2) || (_actionId > 13))
    {
      Util.handleIllegalPlayerAction(activeChar, "Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " requested an internal Social Action.", Config.DEFAULT_PUNISH);
      return;
    }

    if ((activeChar.getPrivateStoreType() == 0) && (activeChar.getActiveRequester() == null) && (!activeChar.isAlikeDead()) && ((!activeChar.isAllSkillsDisabled()) || (activeChar.isInDuel())) && (activeChar.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) && (FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), 9)))
    {
      if (Config.DEBUG) _log.fine("Social Action:" + _actionId);

      SocialAction atk = new SocialAction(activeChar.getObjectId(), _actionId);
      activeChar.broadcastPacket(atk);
    }
  }

  public String getType()
  {
    return "[C] 1B RequestSocialAction";
  }
}