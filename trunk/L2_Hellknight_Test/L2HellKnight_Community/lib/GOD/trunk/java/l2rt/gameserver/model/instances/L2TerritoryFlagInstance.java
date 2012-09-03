package l2rt.gameserver.model.instances;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Rnd;

public class L2TerritoryFlagInstance extends L2SiegeGuardInstance
{
	private L2ItemInstance _item = null;
	private int _itemId = 0;
	private int _baseTerritoryId = 0;
	private int _currentTerritoryId = 0;

	public L2TerritoryFlagInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public L2ItemInstance getItem()
	{
		return _item;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}

	public void setBaseTerritoryId(int territoryId)
	{
		_baseTerritoryId = territoryId;
	}

	public int getBaseTerritoryId()
	{
		return _baseTerritoryId;
	}

	public void setCurrentTerritoryId(int territoryId)
	{
		_currentTerritoryId = territoryId;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		L2Player player = attacker.getPlayer();
		if(player == null)
			return false;
		if(player.getTerritorySiege() == -1 || player.getTerritorySiege() == _currentTerritoryId)
			return false;
		if(player.getClan() == null || player.getClan().getHasCastle() == 0 || player.getClan().getHasCastle() == _currentTerritoryId)
			return false;
		return true;
	}

	@Override
	public synchronized void reduceCurrentHp(double i, L2Character attacker, L2Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect)
	{
		if(!isVisible() || attacker == null || !attacker.isPlayer() || getDistance(attacker) > 200 || getZ() - attacker.getZ() > 100)
			return;

		if(Rnd.chance(95))
			return;

		L2Player player = attacker.getPlayer();
		if(player == null || player.getClan() == null || player.getClan().getHasCastle() == 0)
			return;
		if(player.getTerritorySiege() == _currentTerritoryId || player.getClan().getHasCastle() == _currentTerritoryId)
			return;
		if(player.isMounted())
			return;
		if(player.isTerritoryFlagEquipped())
			return;

		decayMe();

		L2ItemInstance item = ItemTemplates.getInstance().createItem(_itemId);
		item.setCustomFlags(L2ItemInstance.FLAG_EQUIP_ON_PICKUP | L2ItemInstance.FLAG_NO_DESTROY | L2ItemInstance.FLAG_NO_TRADE | L2ItemInstance.FLAG_NO_UNEQUIP, false);
		player.getInventory().addItem(item);
		player.getInventory().equipItem(item, false);
		player.sendChanges();
		_item = item;
		player.sendPacket(Msg.YOU_VE_ACQUIRED_THE_WARD_MOVE_QUICKLY_TO_YOUR_FORCES__OUTPOST);
		String terrName = CastleManager.getInstance().getCastleByIndex(_baseTerritoryId).getName();
		TerritorySiege.announceToPlayer(new SystemMessage(SystemMessage.THE_S1_WARD_HAS_BEEN_DESTROYED_C2_NOW_HAS_THE_TERRITORY_WARD).addString(terrName).addName(player), true);
	}

	public void drop(L2Player player)
	{
		if(player != null)
		{
			_item.setCustomFlags(0, false);
			player.getInventory().destroyItem(_item, 1, false);
			_item = null;
			setXYZInvisible(player.getLoc().rnd(0, 100, false));
			spawnMe();
			TerritorySiege.setWardLoc(_baseTerritoryId, getLoc());
			player.broadcastUserInfo(true);
		}
	}

	public void returnToCastle(L2Player player)
	{
		if(player != null)
		{
			_item.setCustomFlags(0, false);
			player.getInventory().destroyItem(_item, 1, false);
			_item = null;
			TerritorySiege.removeFlag(this);
			TerritorySiege.spawnFlags(_baseTerritoryId); // Заспавнит только нужный нам флаг в замке
			TerritorySiege.setWardLoc(_baseTerritoryId, getLoc());
			player.sendPacket(Msg.THE_EFFECT_OF_TERRITORY_WARD_IS_DISAPPEARING);
			player.broadcastUserInfo(true);
		}
	}

	public void engrave(L2Player player)
	{
		if(player != null)
		{
			_item.setCustomFlags(0, false);
			player.getInventory().destroyItem(_item, 1, false);
			_item = null;

			Castle oldOwner = CastleManager.getInstance().getCastleByIndex(_currentTerritoryId);
			oldOwner.removeFlag(_baseTerritoryId);

			Castle newOwner = CastleManager.getInstance().getCastleByIndex(player.getTerritorySiege());
			newOwner.addFlag(_baseTerritoryId);

			TerritorySiege.removeFlag(this);
			TerritorySiege.spawnFlags(_baseTerritoryId); // Заспавнит только нужный нам флаг в замке
			TerritorySiege.setWardLoc(_baseTerritoryId, getLoc());
			TerritorySiege.refreshTerritorySkills();

			player.sendPacket(Msg.THE_EFFECT_OF_TERRITORY_WARD_IS_DISAPPEARING);
			String terrName = CastleManager.getInstance().getCastleByIndex(_baseTerritoryId).getName();
			TerritorySiege.announceToPlayer(new SystemMessage(SystemMessage.CLAN_S1_HAS_SUCCEEDED_IN_CAPTURING_S2_S_TERRITORY_WARD).addString(player.getClan().getName()).addString(terrName), true);

			deleteMe();
		}
	}
}