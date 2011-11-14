/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2js.gameserver.handler.skillhandlers;

import java.util.logging.Level;

import com.l2js.gameserver.handler.ISkillHandler;
import com.l2js.gameserver.model.L2Object;
import com.l2js.gameserver.model.L2Party;
import com.l2js.gameserver.model.L2Skill;
import com.l2js.gameserver.model.actor.L2Character;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.network.SystemMessageId;
import com.l2js.gameserver.network.serverpackets.ConfirmDlg;
import com.l2js.gameserver.network.serverpackets.SystemMessage;
import com.l2js.gameserver.templates.skills.L2SkillType;
import com.l2js.gameserver.util.Util;

/**
 * @author BiTi, Sami, Zoey76
 */
public class SummonFriend implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	= {
														L2SkillType.SUMMON_FRIEND
													};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
		{
			return;
		}
		final boolean isMastersCall = skill.getId() == 23249;
		final L2PcInstance activePlayer = activeChar.getActingPlayer();
		if (!isMastersCall && !L2PcInstance.checkSummonerStatus(activePlayer))
		{
			return;
		}

		try
		{
			for (L2Character target : (L2Character[]) targets)
			{
				if ((target == null) || (activeChar == target))
				{
					continue;
				}

				if (target instanceof L2PcInstance)
				{
					if (isMastersCall) //Master's Call
					{
						final L2Party party = target.getParty();
						if (party != null)
						{
							for (L2PcInstance partyMember : party.getPartyMembers())
							{
								if (target != partyMember)
								{
									partyMember.teleToLocation(target.getX(), target.getY(), target.getZ(), true);
								}
							}
						}
						else
						{
							activePlayer.sendMessage(target.getName() + " doesn't have a party.");
						}
						continue;
					}

					final L2PcInstance targetPlayer = target.getActingPlayer();
					if (!L2PcInstance.checkSummonTargetStatus(targetPlayer, activePlayer))
					{
						continue;
					}

					if (!Util.checkIfInRange(0, activeChar, target, false))
					{
						if (!targetPlayer.teleportRequest(activePlayer, skill))
						{
							final SystemMessage sm = SystemMessage
									.getSystemMessage(SystemMessageId.C1_ALREADY_SUMMONED);
							sm.addString(target.getName());
							activePlayer.sendPacket(sm);
							continue;
						}

						if (skill.getId() == 1403) //Summon Friend
						{
							// Send message
							final ConfirmDlg confirm = new ConfirmDlg(
									SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
							confirm.addCharName(activeChar);
							confirm.addZoneName(activeChar.getX(), activeChar.getY(), activeChar.getZ());
							confirm.addTime(30000);
							confirm.addRequesterId(activePlayer.getObjectId());
							target.sendPacket(confirm);
						}
						else
						{
							L2PcInstance.teleToTarget(targetPlayer, activePlayer, skill);
							targetPlayer.teleportRequest(null, null);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "", e);
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
