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
package l2.hellknight.gameserver.model.skills.l2skills;

import java.util.logging.Level;

import l2.hellknight.gameserver.instancemanager.GrandBossManager;
import l2.hellknight.gameserver.instancemanager.MapRegionManager;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.Location;
import l2.hellknight.gameserver.model.StatsSet;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.TvTEvent;
import l2.hellknight.gameserver.model.entity.TvTRoundEvent;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.model.skills.L2SkillType;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;

public class L2SkillTeleport extends L2Skill
{
	private final String _recallType;
	private final Location _loc;
	
	public L2SkillTeleport(StatsSet set)
	{
		super(set);
		
		_recallType = set.getString("recallType", "");
		String coords = set.getString("teleCoords", null);
		if (coords != null)
		{
			String[] valuesSplit = coords.split(",");
			_loc = new Location(Integer.parseInt(valuesSplit[0]),
					Integer.parseInt(valuesSplit[1]),
					Integer.parseInt(valuesSplit[2]));
		}
		else
			_loc = null;
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{	
		if (activeChar.isPlayer())
		{
			// Thanks nbd
			if (!TvTEvent.onEscapeUse(activeChar.getActingPlayer().getObjectId()))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!TvTRoundEvent.onEscapeUse(((L2PcInstance) activeChar).getObjectId()))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (activeChar.isAfraid())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (activeChar.getActingPlayer().isCombatFlagEquipped())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (activeChar.getActingPlayer().isInOlympiadMode())
			{
				activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
				return;
			}
			
			if (GrandBossManager.getInstance().getZone(activeChar) != null && !activeChar.isGM())
			{
				activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
				return;
			}
		}
		
		try
		{
			for (L2Character target: (L2Character[]) targets)
			{
				if (target.isPlayer())
				{
					L2PcInstance targetChar = target.getActingPlayer();
					
					// Check to see if the current player target is in a festival.
					if (targetChar.isFestivalParticipant())
					{
						targetChar.sendMessage("You may not use an escape skill in a festival.");
						continue;
					}
					
					// Check to see if player is in jail
					if (targetChar.isInJail())
					{
						targetChar.sendMessage("You can not escape from jail.");
						continue;
					}
					
					// Check to see if player is in a duel
					if (targetChar.isInDuel())
					{
						targetChar.sendMessage("You cannot use escape skills during a duel.");
						continue;
					}
					
					if (targetChar != activeChar)
					{
						if (!TvTEvent.onEscapeUse(targetChar.getObjectId()))
							continue;
						
						if (!TvTRoundEvent.onEscapeUse(targetChar.getObjectId()))
							continue;
						
						if (targetChar.isInOlympiadMode())
							continue;
						
						if (GrandBossManager.getInstance().getZone(targetChar) != null)
							continue;
						
						if (targetChar.isCombatFlagEquipped())
							continue;
					}
				}
				Location loc = null;
				if (getSkillType() == L2SkillType.TELEPORT)
				{
					if (_loc != null)
					{
						// target is not player OR player is not flying or flymounted
						// TODO: add check for gracia continent coords
						if (!(target.isPlayer())
								|| !(target.isFlying() || (target.getActingPlayer().isFlyingMounted())))
							loc = _loc;
					}
				}
				else
				{
					if (_recallType.equalsIgnoreCase("Castle"))
						loc = MapRegionManager.getInstance().getTeleToLocation(target, MapRegionManager.TeleportWhereType.Castle);
					else if (_recallType.equalsIgnoreCase("ClanHall"))
						loc = MapRegionManager.getInstance().getTeleToLocation(target, MapRegionManager.TeleportWhereType.ClanHall);
					else if (_recallType.equalsIgnoreCase("Fortress"))
						loc = MapRegionManager.getInstance().getTeleToLocation(target, MapRegionManager.TeleportWhereType.Fortress);
					else
						loc = MapRegionManager.getInstance().getTeleToLocation(target, MapRegionManager.TeleportWhereType.Town);
				}
				if (loc != null)
				{
					target.setInstanceId(0);
					if (target.isPlayer())
						target.getActingPlayer().setIsIn7sDungeon(false);
					target.teleToLocation(loc, true);
				}
			}
			
			activeChar.spsUncharge(this);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "", e);
		}
	}
}