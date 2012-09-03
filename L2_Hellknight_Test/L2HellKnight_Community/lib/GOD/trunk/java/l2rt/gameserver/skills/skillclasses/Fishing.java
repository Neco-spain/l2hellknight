package l2rt.gameserver.skills.skillclasses;

import l2rt.Config;
import l2rt.gameserver.GameTimeController;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.FishData;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ExFishingStart;
import l2rt.gameserver.tables.FishTable;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.templates.L2Weapon.WeaponType;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Rnd;
import l2rt.util.Util;

public class Fishing extends L2Skill
{
	public Fishing(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		L2Player player = (L2Player) activeChar;

		if(player.getSkillLevel(SKILL_FISHING_MASTERY) == -1)
			return false;

		if(player.isFishing())
		{
			if(player.getFishCombat() != null)
				player.getFishCombat().doDie(false);
			else
				player.endFishing(false);
			player.sendPacket(Msg.CANCELS_FISHING);
			return false;
		}

		if(player.isInVehicle())
		{
			activeChar.sendPacket(Msg.YOU_CANT_FISH_WHILE_YOU_ARE_ON_BOARD);
			return false;
		}

		if(player.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_FISH_WHILE_USING_A_RECIPE_BOOK_PRIVATE_MANUFACTURE_OR_PRIVATE_STORE);
			return false;
		}

		if(Config.ENABLE_FISHING_CHECK_WATER)
		{
			int rnd = Rnd.get(50) + 150;
			double angle = Util.convertHeadingToDegree(player.getHeading());
			double radian = Math.toRadians(angle - 90);
			double sin = Math.sin(radian);
			double cos = Math.cos(radian);
			int x1 = -(int) (sin * rnd);
			int y1 = (int) (cos * rnd);
			int x = player.getX() + x1;
			int y = player.getY() + y1;
			int z = GeoEngine.getHeight(x, y, player.getZ(), activeChar.getReflection().getGeoIndex());
			player.setFishLoc(new Location(x, y, z));

			if(!ZoneManager.getInstance().checkIfInZoneFishing(x, y, z))
			{
				player.sendPacket(Msg.YOU_CANT_FISH_HERE);
				return false;
			}
		}

		L2Weapon weaponItem = player.getActiveWeaponItem();
		if(weaponItem == null || weaponItem.getItemType() != WeaponType.ROD)
		{
			//Fishing poles are not installed
			player.sendPacket(Msg.FISHING_POLES_ARE_NOT_INSTALLED);
			return false;
		}

		L2ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if(lure == null || lure.getCount() < 1)
		{
			player.sendPacket(Msg.BAITS_ARE_NOT_PUT_ON_A_HOOK);
			return false;
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(L2Character caster, GArray<L2Character> targets)
	{
		if(caster == null || !caster.isPlayer())
			return;

		L2Player player = (L2Player) caster;

		L2ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if(lure == null || lure.getCount() < 1)
		{
			player.sendPacket(Msg.BAITS_ARE_NOT_PUT_ON_A_HOOK);
			return;
		}

		L2ItemInstance lure2 = player.getInventory().destroyItem(player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, false);
		if(lure2 == null || lure2.getCount() == 0)
			player.sendPacket(Msg.NOT_ENOUGH_BAIT);

		int lvl = FishTable.getInstance().GetRandomFishLvl(player);
		int group = FishTable.getInstance().GetGroupForLure(lure.getItemId());
		int type = FishTable.getInstance().GetRandomFishType(group, lure.getItemId());

		GArray<FishData> fishs = FishTable.getInstance().getfish(lvl, type, group);
		if(fishs == null || fishs.size() == 0)
		{
			player.sendMessage("Error: Fishes are not definied for lvl " + lvl + " type " + type + " group " + group + ", report admin please.");
			player.endFishing(false);
			return;
		}

		int check = Rnd.get(fishs.size());
		FishData fish = fishs.get(check);

		if(!GameTimeController.getInstance().isNowNight() && lure.isNightLure())
			fish.setType(-1);

		player.setImobilised(true);
		player.setFishing(true);
		player.setFish(fish);
		player.setLure(lure);
		player.broadcastUserInfo(true);
		player.broadcastPacket(new ExFishingStart(player, fish.getType(), player.getFishLoc(), lure.isNightLure()));
		player.sendPacket(Msg.STARTS_FISHING);
		player.startLookingForFishTask();
	}
}