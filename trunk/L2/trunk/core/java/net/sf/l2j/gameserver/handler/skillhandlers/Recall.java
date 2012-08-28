package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.instancemanager.CustomZoneManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class Recall implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(Recall.class.getName());
	private static final SkillType[] SKILL_IDS = {SkillType.RECALL};

 	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
        if (activeChar instanceof L2PcInstance)
        {
        	// Thanks nbd
        	if (!TvTEvent.onEscapeUse(((L2PcInstance)activeChar).getObjectId()))
        	{
        		((L2PcInstance)activeChar).sendPacket(new ActionFailed());
        		return;
        	}

            if (((L2PcInstance)activeChar).isInOlympiadMode())
            {
                ((L2PcInstance)activeChar).sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
                return;
            }
        }

		try
        {
			for (int index = 0; index < targets.length; index++)
			{
				if (!(targets[index] instanceof L2Character))
					continue;

				L2Character target = (L2Character)targets[index];

                if (target instanceof L2PcInstance)
                {
                    L2PcInstance targetChar = (L2PcInstance)target;

	                if(CustomZoneManager.getInstance().checkIfInZone("NoEscape", targetChar))
                    {
                        targetChar.sendPacket(SystemMessage.sendString("Ќевозможно использовать в этой местности"));
                        targetChar.sendPacket(new ActionFailed());
                        break;                   
                    }
                    // Check to see if the current player target is in a festival.
                    if (targetChar.isFestivalParticipant()) {
                        targetChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
                        continue;
                    }

	                if (targetChar._inEventCTF)
					{
						targetChar.sendMessage("You may not use an escape skill in a Event.");
						continue;
					}

                    // Check to see if player is in jail
                    if (targetChar.isInJail())
                    {
                        targetChar.sendPacket(SystemMessage.sendString("You can not escape from jail."));
                        continue;
                    }

                    // Check to see if player is in a duel
                    if (targetChar.isInDuel())
                    {
                        targetChar.sendPacket(SystemMessage.sendString("You cannot use escape skills during a duel."));
                        continue;
                    }
                }

                target.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
        } catch (Throwable e) {
 	 	}
 	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}