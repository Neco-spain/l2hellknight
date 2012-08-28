package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.gameserver.util.Util;

public class RequestSocialAction extends L2GameClientPacket
{
	private static final String _C__1B_REQUESTSOCIALACTION = "[C] 1B RequestSocialAction";
	private static Logger _log = Logger.getLogger(RequestSocialAction.class.getName());

	// format  cd
	private int _actionId;


	@Override
	protected void readImpl()
	{
		_actionId  = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		    return;

        // You cannot do anything else while fishing
        if (activeChar.isFishing())
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
            activeChar.sendPacket(sm);
            sm = null;
            return;
        }

        // check if its the actionId is allowed
        if (_actionId < 2 || _actionId > 13)
        {
        	Util.handleIllegalPlayerAction(activeChar, "Warning!! Character "+activeChar.getName()+" of account "+activeChar.getAccountName()+" requested an internal Social Action.", Config.DEFAULT_PUNISH);
        	return;
        }

		if (	activeChar.getPrivateStoreType()==0 &&
				activeChar.getActiveRequester()==null &&
				!activeChar.isAlikeDead() &&
				(!activeChar.isAllSkillsDisabled() || activeChar.isInDuel()) &&
				activeChar.getAI().getIntention()==CtrlIntention.AI_INTENTION_IDLE &&
				FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_SOCIAL))
		{
			if (Config.DEBUG) _log.fine("Social Action:" + _actionId);

			SocialAction atk = new SocialAction(activeChar.getObjectId(), _actionId);
			activeChar.broadcastPacket(atk);
			/*
			// Schedule a social task to wait for the animation to finish
			ThreadPoolManager.getInstance().scheduleGeneral(new SocialTask(this), 2600);
			activeChar.setIsParalyzed(true);
			*/
		}
	}
	/*
	class SocialTask implements Runnable
	{
		L2PcInstance _player;
		SocialTask(RequestSocialAction action)
		{
			_player = getClient().getActiveChar();
		}
		public void run()
		{
			_player.setIsParalyzed(false);
		}
	}
	*/

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__1B_REQUESTSOCIALACTION;
	}
}
