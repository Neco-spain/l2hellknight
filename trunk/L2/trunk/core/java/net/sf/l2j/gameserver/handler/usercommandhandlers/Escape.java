package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

public class Escape implements IUserCommandHandler
{
    private static final int[] COMMAND_IDS = { 52 };
    private static final int REQUIRED_LEVEL = Config.GM_ESCAPE;

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useUserCommand(int id, L2PcInstance activeChar)
    {
    	// Thanks nbd
    	if (!TvTEvent.onEscapeUse(activeChar.getObjectId()))
    	{
    		activeChar.sendPacket(new ActionFailed());
    		return false;
    	}

        if (activeChar.isCastingNow() || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() ||
                activeChar.isInOlympiadMode())
            return false;

        int unstuckTimer = (activeChar.getAccessLevel() >=REQUIRED_LEVEL? 5000 : Config.UNSTUCK_INTERVAL*1000 );

        // Check to see if the player is in a festival.
        if (activeChar.isFestivalParticipant())
        {
            activeChar.sendMessage("You may not use an escape command in a festival.");
            return false;
        }

        // Check to see if player is in jail
        if (activeChar.isInJail())
        {
            activeChar.sendMessage("You can not escape from jail.");
            return false;
        }

	    if (GrandBossManager.getInstance().getZone(activeChar) != null && !activeChar.isGM())
        {
            activeChar.sendMessage("You may not use an escape command in a Boss Zone.");
            return false;
        }
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
        sm.addString("After " + unstuckTimer/60000 + " min. you be returned to near village.");

        activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        //SoE Animation section
        activeChar.setTarget(activeChar);
        activeChar.disableAllSkills();

        MagicSkillUser msk = new MagicSkillUser(activeChar, 1050, 1, unstuckTimer, 0);
        Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000/*900*/);
        SetupGauge sg = new SetupGauge(0, unstuckTimer);
        activeChar.sendPacket(sg);
        //End SoE Animation section

        EscapeFinalizer ef = new EscapeFinalizer(activeChar);
        // continue execution later
        activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
        activeChar.setSkillCastEndTime(10+GameTimeController.getGameTicks()+unstuckTimer/GameTimeController.MILLIS_IN_TICK);

        return true;
    }

    static class EscapeFinalizer implements Runnable
    {
        private L2PcInstance _activeChar;

        EscapeFinalizer(L2PcInstance activeChar)
        {
            _activeChar = activeChar;
        }

        public void run()
        {
            if (_activeChar.isDead())
                return;

            _activeChar.setIsIn7sDungeon(false);

            _activeChar.enableAllSkills();

            try
            {
				_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
            } catch (Throwable e) { if (Config.DEBUG) e.printStackTrace(); }
        }
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public int[] getUserCommandList()
    {
        return COMMAND_IDS;
    }
}
