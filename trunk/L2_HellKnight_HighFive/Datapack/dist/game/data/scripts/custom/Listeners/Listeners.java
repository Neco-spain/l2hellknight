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
package custom.Listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2.hellknight.gameserver.datatables.CharNameTable;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;
import l2.hellknight.gameserver.scripting.scriptengine.events.AttackEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.AugmentEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.ClanCreationEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.ClanJoinEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.ClanLeaderChangeEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.ClanLeaveEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.ClanLevelUpEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.ClanWarEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.ClanWarehouseAddItemEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.ClanWarehouseDeleteItemEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.ClanWarehouseTransferEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.FortSiegeEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.HennaEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.ItemCreateEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.ItemDropEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.ItemPickupEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.SiegeEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.SkillUseEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.TransformEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.TvtKillEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.impl.L2Event;
import l2.hellknight.gameserver.scripting.scriptengine.impl.L2Script;

/**
 * An example class of using Listeners.
 * @author UnAfraid
 */
public class Listeners extends L2Script
{
	private static final Logger _log = Logger.getLogger(Listeners.class.getName());
	
	public Listeners(String name, String descr)
	{
		super(name, descr);
		addLoginLogoutNotify();
		addClanCreationLevelUpNotify();
		addFortSiegeNotify();
		addSiegeNotify();
		addTvTNotify();
		addItemAugmentNotify();
		addItemDropPickupNotify();
		addHennaNotify();
	}
	
	/**
	 * Fired when a player logs in
	 * @param player
	 */
	@Override
	public void onPlayerLogin(L2PcInstance player)
	{
		_log.log(Level.INFO, "Player " + player.getName() + " just logged in!");
		List<Integer> items = new ArrayList<>();
		for (L2ItemInstance item : player.getInventory().getItems())
		{
			items.add(item.getItemId());
		}
		addItemTracker(items);
		addTransformNotify(player);
		addSkillUseNotify(player, -1);
		addAttackNotify(player);
	}
	
	/**
	 * Fired when a player logs out
	 * @param player
	 */
	@Override
	public void onPlayerLogout(L2PcInstance player)
	{
		_log.log(Level.INFO, "Player " + player.getName() + " just logged out!");
		removeTransformNotify(player);
		removeSkillUseNotify(player);
		removeAttackNotify(player);
	}
	
	/**
	 * Fired when a clan is created Register the listener using addClanCreationLevelUpNotify()
	 * @param event
	 */
	@Override
	public void onClanCreated(ClanCreationEvent event)
	{
		_log.log(Level.INFO, "Clan " + event.getClan().getName() + " has been created by " + event.getClan().getLeaderName() + "!");
	}
	
	/**
	 * Fired when a clan levels up<br>
	 * Register the listener using addClanCreationLevelUpListener()
	 * @param event
	 */
	@Override
	public boolean onClanLeveledUp(ClanLevelUpEvent event)
	{
		_log.log(Level.INFO, "Clan " + event.getClan().getName() + " has leveled up!");
		return true;
	}
	
	/**
	 * Fired when a player joins a clan<br>
	 * Register the listener with addClanJoinLeaveNotify()<br>
	 * @param event
	 */
	@Override
	public boolean onClanJoin(ClanJoinEvent event)
	{
		_log.log(Level.INFO, "Player " + event.getPlayer().getName() + " has joined clan: " + event.getPlayer().getName() + "!");
		return true;
	}
	
	/**
	 * Fired when a player leaves a clan<br>
	 * Register the listener with addClanJoinLeaveNotify()<br>
	 * @param event
	 */
	@Override
	public boolean onClanLeave(ClanLeaveEvent event)
	{
		String name = CharNameTable.getInstance().getNameById(event.getPlayerId());
		_log.log(Level.INFO, "Player " + name + " has leaved clan: " + event.getClan().getName() + "!");
		return true;
	}
	
	/**
	 * Fired when a clan leader is changed for another<br>
	 * Register the listener with addClanJoinLeaveNotify()<br>
	 */
	@Override
	public boolean onClanLeaderChange(ClanLeaderChangeEvent event)
	{
		_log.log(Level.INFO, "Player " + event.getNewLeader().getName() + " become the new leader of clan: " + event.getClan().getName() + "!");
		return true;
	}
	
	/**
	 * Fired when an item is added to a clan warehouse<br>
	 * Register the listener with addClanWarehouseNotify(L2Clan)
	 * @param event
	 */
	@Override
	public boolean onClanWarehouseAddItem(ClanWarehouseAddItemEvent event)
	{
		_log.log(Level.INFO, "Player " + event.getActor().getName() + " added an item (" + event.getItem() + ") to clan warehouse (" + event.getProcess() + ")!");
		return true;
	}
	
	/**
	 * Fired when an item is deleted from a clan warehouse<br>
	 * Register the listener with addClanWarehouseNotify(L2Clan)
	 * @param event
	 */
	@Override
	public boolean onClanWarehouseDeleteItem(ClanWarehouseDeleteItemEvent event)
	{
		_log.log(Level.INFO, "Player " + event.getActor().getName() + " removed an item (" + event.getItem() + ") from clan warehouse (" + event.getProcess() + ")!");
		return true;
	}
	
	/**
	 * Fired when an item is transfered from/to a clan warehouse<br>
	 * Register the listener with addClanWarehouseNotify(L2Clan)
	 * @param event
	 */
	@Override
	public boolean onClanWarehouseTransferItem(ClanWarehouseTransferEvent event)
	{
		_log.log(Level.INFO, "Player " + event.getActor().getName() + " transfered an item (" + event.getItem() + ") from clan warehouse to " + event.getTarget() + " (" + event.getProcess() + ")!");
		return true;
	}
	
	/**
	 * Fired when a clan war starts or ends<br>
	 * Register the listener witn addClanWarNotify()
	 * @param event
	 */
	@Override
	public boolean onClanWarEvent(ClanWarEvent event)
	{
		_log.log(Level.INFO, "Clan " + event.getClan1().getName() + " challanges " + event.getClan2().getName() + " stage: " + event.getStage().toString() + "!");
		return true;
	}
	
	/**
	 * Fired when a fort siege starts or ends<br>
	 * Register using addFortSiegeNotify()
	 * @param event
	 */
	@Override
	public boolean onFortSiegeEvent(FortSiegeEvent event)
	{
		_log.log(Level.INFO, "FortSiege event: " + event.getSiege().getFort().getName() + " " + event.getSiege() + " " + event.getStage().toString() + "!");
		return true;
	}
	
	/**
	 * Fired when a castle siege starts or ends<br>
	 * Register using addSiegeNotify()
	 * @param event
	 */
	@Override
	public boolean onSiegeEvent(SiegeEvent event)
	{
		_log.log(Level.INFO, "Siege event: " + event.getSiege().getCastle().getName() + " " + event.getSiege() + " " + event.getStage().toString() + "!");
		return true;
	}
	
	/**
	 * Fired when the control of a castle changes during a siege<br>
	 * Register using addSiegeNotify()
	 * @param event
	 */
	@Override
	public void onCastleControlChange(SiegeEvent event)
	{
		_log.log(Level.INFO, "Castle control change: " + event.getSiege().getCastle().getName() + " " + event.getSiege() + "!");
	}
	
	/**
	 * Notifies of TvT events<br>
	 * Register using addTvtNotify()
	 * @param stage
	 */
	@Override
	public void onTvtEvent(EventStage stage)
	{
		_log.log(Level.INFO, "TvT event: " + stage.toString() + "!");
	}
	
	/**
	 * Notifies that a player was killed during TvT<br>
	 * Register using addTvtNotify()
	 * @param event
	 */
	@Override
	public void onTvtKill(TvtKillEvent event)
	{
		_log.log(Level.INFO, "TvT event killed " + event.getVictim().getName() + " killer " + event.getKiller().getName() + " killer team: " + event.getKillerTeam().getName() + "!");
	}
	
	/**
	 * triggered when an item is augmented or when the augmentation is removed<br>
	 * Register using addItemAugmentNotify()
	 * @param event
	 */
	@Override
	public boolean onItemAugment(AugmentEvent event)
	{
		_log.log(Level.INFO, "Item (" + event.getItem().getName() + " has been augumented added = " + event.getAugmentation() + "!");
		return true;
	}
	
	/**
	 * Fired when an item is dropped by a player<br>
	 * Register using addItemDropPickupNotify()
	 * @param event
	 */
	@Override
	public boolean onItemDrop(ItemDropEvent event)
	{
		_log.log(Level.INFO, "Item (" + event.getItem().getName() + " has been dropped by (" + event.getDropper().getName() + " ) at: " + event.getLocation() + "!");
		return true;
	}
	
	/**
	 * Fired when an item is picked up by a player<br>
	 * Register using addItemDropPickupNotify()
	 * @param event
	 */
	@Override
	public boolean onItemPickup(ItemPickupEvent event)
	{
		_log.log(Level.INFO, "Item (" + event.getItem().getName() + " has been pickup by (" + event.getPicker().getName() + " ) from: " + event.getLocation() + "!");
		return true;
	}
	
	/**
	 * Fired when a player's henna changes (add/remove)<br>
	 * Register using addHennaNotify()
	 * @param event
	 */
	@Override
	public boolean onHennaModify(HennaEvent event)
	{
		_log.log(Level.INFO, "Henna Modify: player: " + event.getPlayer().getName() + " henna: " + event.getHenna().getDyeName() + " added: " + event.isAdd());
		return true;
	}
	
	/**
	 * Fired when an item on the item tracker list has an event<br>
	 * Register using addItemTracker(itemIds)
	 * @param event
	 */
	@Override
	public void onItemTrackerEvent(L2Event event)
	{
		//_log.log(Level.INFO, "ItemTrackerEvent: " + event.getName() + " has been " + event + " owner: " + player + " target: " + target);
		// TODO: Fix it?
	}
	
	/**
	 * Fired when an item is created<br>
	 * Register using addNewItemNotify(itemIds)
	 * @param event
	 */
	@Override
	public boolean onItemCreate(ItemCreateEvent event)
	{
		_log.log(Level.INFO, "ItemTrackerEvent: " + event.getItemId() + " has been created owner: " + event.getPlayer().getName());
		return true;
	}
	
	/**
	 * Fired when a player transforms/untransforms<br>
	 * Register using addTransformNotify(player)
	 * @param event
	 */
	@Override
	public boolean onPlayerTransform(TransformEvent event)
	{
		_log.log(Level.INFO, "Player (" + event.getTransformation().getPlayer() + ") has been transformed to " + event.getTransformation().toString() + " transform: " + event.isTransforming());
		return true;
	}
	
	/**
	 * Fired when a L2Character registered with addAttackNotify is either attacked or attacks another L2Character
	 * @param event
	 */
	@Override
	public boolean onAttack(AttackEvent event)
	{
		_log.log(Level.INFO, event.getTarget() + " has been attacked by " + event.getAttacker());
		return true;
	}
	
	/**
	 * Fired when a SKillUseListener gets triggered.<br>
	 * Register using addSkillUseNotify()
	 * @param event
	 */
	@Override
	public boolean onUseSkill(SkillUseEvent event)
	{
		_log.log(Level.INFO, event.getTargets() + " has been used by " + event.getCaster());
		return true;
	}
	
	public static void main(String[] args)
	{
		new Listeners(Listeners.class.getSimpleName(), "custom");
	}
}