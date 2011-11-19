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

/**
 *
 * @author FBIagent
 *
 */
package handlers.itemhandlers;

import java.util.Collection;
import java.util.logging.Level;

import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.datatables.SummonItemsData;
import l2.hellknight.gameserver.handler.IItemHandler;
import l2.hellknight.gameserver.model.L2ItemInstance;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.L2SummonItem;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PetInstance;
import l2.hellknight.gameserver.model.actor.instance.L2XmassTreeInstance;
import l2.hellknight.gameserver.model.entity.TvTEvent;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.MagicSkillLaunched;
import l2.hellknight.gameserver.network.serverpackets.MagicSkillUse;
import l2.hellknight.gameserver.network.serverpackets.PetItemList;
import l2.hellknight.gameserver.network.serverpackets.SetupGauge;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.templates.chars.L2NpcTemplate;
import l2.hellknight.gameserver.util.Broadcast;

public class SummonItems implements IItemHandler
{
	/**
	 * 
	 * @see l2.hellknight.gameserver.handler.IItemHandler#useItem(l2.hellknight.gameserver.model.actor.L2Playable, l2.hellknight.gameserver.model.L2ItemInstance, boolean)
	 */
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		if (!TvTEvent.onItemSummon(playable.getObjectId()))
			return;
		
		final L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (!activeChar.getFloodProtectors().getItemPetSummon().tryPerformAction("summon items"))
			return;
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}
		
		if(activeChar.getBlockCheckerArena() != -1)
			return;

		if (activeChar.inObserverMode())
			return;
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		if (activeChar.isAllSkillsDisabled() || activeChar.isCastingNow())
			return;
		
		final L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());
		
		if ((activeChar.getPet() != null || activeChar.isMounted()) && sitem.isPetSummon())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
			return;
		}
		
		if (activeChar.isAttackingNow())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
			return;
		}
		
		final int npcId = sitem.getNpcId();
		if (npcId == 0)
			return;
		
		final L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		if (npcTemplate == null)
			return;
		
		activeChar.stopMove(null, false);
		
		switch (sitem.getType())
		{
			case 0: // static summons (like Christmas tree)
				try
				{
					Collection<L2Character> characters = activeChar.getKnownList().getKnownCharactersInRadius(1200);
					for (L2Character ch : characters)
					{
						if (ch instanceof L2XmassTreeInstance && npcTemplate.isSpecialTree())
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_SUMMON_S1_AGAIN);
							sm.addCharName(ch);
							activeChar.sendPacket(sm);
							return;
						}
					}
					
					if (activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false))
					{
						final L2Spawn spawn = new L2Spawn(npcTemplate);
						spawn.setLocx(activeChar.getX());
						spawn.setLocy(activeChar.getY());
						spawn.setLocz(activeChar.getZ());
						spawn.setInstanceId(activeChar.getInstanceId());
						spawn.stopRespawn();
						final L2Npc npc = spawn.spawnOne(true);
						npc.setTitle(activeChar.getName());
						npc.setIsRunning(false); // broadcast info
						if (sitem.getDespawnDelay() > 0)
							npc.scheduleDespawn(sitem.getDespawnDelay() * 1000L);
					}
				}
				catch (Exception e)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_CANT_FOUND));
				}
				break;
			case 1: // pet summons
				final L2Object oldTarget = activeChar.getTarget();
				activeChar.setTarget(activeChar);
				Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, 2046, 1, 5000, 0));
				activeChar.setTarget(oldTarget);
				activeChar.sendPacket(new SetupGauge(0, 5000));
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SUMMON_A_PET));
				activeChar.setIsCastingNow(true);
				
				ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFinalizer(activeChar, npcTemplate, item), 5000);
				break;
			case 2: // wyvern
				activeChar.mount(sitem.getNpcId(), item.getObjectId(), true);
				break;
			case 3: // Great Wolf
				activeChar.mount(sitem.getNpcId(), item.getObjectId(), false);
				break;
		}
	}
	
	static class PetSummonFeedWait implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2PetInstance _petSummon;
		
		PetSummonFeedWait(L2PcInstance activeChar, L2PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}
		
		public void run()
		{
			try
			{
				if (_petSummon.getCurrentFed() <= 0)
					_petSummon.unSummon(_activeChar);
				else
					_petSummon.startFeed();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	// TODO: this should be inside skill handler
	static class PetSummonFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2ItemInstance _item;
		private final L2NpcTemplate _npcTemplate;
		
		PetSummonFinalizer(L2PcInstance activeChar, L2NpcTemplate npcTemplate, L2ItemInstance item)
		{
			_activeChar = activeChar;
			_npcTemplate = npcTemplate;
			_item = item;
		}
		
		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
				_activeChar.setIsCastingNow(false);
				
				// check for summon item validity
				if (_item == null
						|| _item.getOwnerId() != _activeChar.getObjectId()
						|| _item.getLocation() != L2ItemInstance.ItemLocation.INVENTORY)
					return;
				
				final L2PetInstance petSummon = L2PetInstance.spawnPet(_npcTemplate, _activeChar, _item);
				if (petSummon == null)
					return;
				
				petSummon.setShowSummonAnimation(true);
				petSummon.setTitle(_activeChar.getName());
				
				if (!petSummon.isRespawned())
				{
					petSummon.setCurrentHp(petSummon.getMaxHp());
					petSummon.setCurrentMp(petSummon.getMaxMp());
					petSummon.getStat().setExp(petSummon.getExpForThisLevel());
					petSummon.setCurrentFed(petSummon.getMaxFed());
				}
				
				petSummon.setRunning();
				
				if (!petSummon.isRespawned())
					petSummon.store();
				
				_activeChar.setPet(petSummon);
				
				//JIV remove - done on spawn
				//L2World.getInstance().storeObject(petSummon);
				petSummon.spawnMe(_activeChar.getX() + 50, _activeChar.getY() + 100, _activeChar.getZ());
				petSummon.startFeed();
				_item.setEnchantLevel(petSummon.getLevel());
				
				if (petSummon.getCurrentFed() <= 0)
					ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFeedWait(_activeChar, petSummon), 60000);
				else
					petSummon.startFeed();
				
				petSummon.setFollowStatus(true);
				
				petSummon.sendPacket(new PetItemList(petSummon));
				petSummon.broadcastStatusUpdate();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
}
