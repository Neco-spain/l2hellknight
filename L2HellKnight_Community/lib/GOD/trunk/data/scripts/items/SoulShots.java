package items;

import l2rt.config.ConfigSystem;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ExAutoSoulShot;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.templates.L2Weapon.WeaponType;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Rnd;

public class SoulShots implements IItemHandler, ScriptFile
{
	private static final int[] _itemIds = { 5789, 1835, 1463, 1464, 1465, 1466, 1467, 13037, 13045, 13055, 22082, 22083, 22084, 22085, 22086, 17754 };
	private static final short[] _skillIds = { 2039, 2150, 2151, 2152, 2153, 2154, 26060, 26061, 26062, 26063, 26064, 9193 };

	public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return;
		L2Player player = (L2Player) playable;

		L2Weapon weaponItem = player.getActiveWeaponItem();

		L2ItemInstance weaponInst = player.getActiveWeaponInstance();
		int SoulshotId = item.getItemId();
		boolean isAutoSoulShot = false;
		L2Item itemTemplate = ItemTemplates.getInstance().getTemplate(item.getItemId());

		if(player.getAutoSoulShot().contains(SoulshotId))
			isAutoSoulShot = true;

		if(weaponInst == null)
		{
			if(!isAutoSoulShot)
				player.sendPacket(Msg.CANNOT_USE_SOULSHOTS);
			return;
		}

		// soulshot is already active
		if(weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
			return;

		int grade = weaponItem.getCrystalType().externalOrdinal;
		int soulShotConsumption = weaponItem.getSoulShotCount();
		long count = item.getCount();

		if(soulShotConsumption == 0)
		{
			// Can't use soulshots
			if(isAutoSoulShot)
			{
				player.removeAutoSoulShot(SoulshotId);
				player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(SystemMessage.THE_AUTOMATIC_USE_OF_S1_WILL_NOW_BE_CANCELLED).addString(itemTemplate.getName()));
				return;
			}
			player.sendPacket(Msg.CANNOT_USE_SOULSHOTS);
			return;
		}

		if(grade == 0 && SoulshotId != 5789 && SoulshotId != 1835 // NG
				|| grade == 1 && SoulshotId != 1463 && SoulshotId != 22082 && SoulshotId != 13037 // D
				|| grade == 2 && SoulshotId != 1464 && SoulshotId != 22083 && SoulshotId != 13045 // C
				|| grade == 3 && SoulshotId != 1465 && SoulshotId != 22084 // B
				|| grade == 4 && SoulshotId != 1466 && SoulshotId != 22085 && SoulshotId != 13055 // A
				|| grade == 5 && SoulshotId != 1467 && SoulshotId != 22086 // S
				|| grade == 6 && SoulshotId != 17754 // R
		)
		{
			// wrong grade for weapon
			if(isAutoSoulShot)
				return;
			player.sendPacket(Msg.SOULSHOT_DOES_NOT_MATCH_WEAPON_GRADE);
			return;
		}

		if(weaponItem.getItemType() == WeaponType.BOW || weaponItem.getItemType() == WeaponType.CROSSBOW)
		{
			int newSS = (int) player.calcStat(Stats.SS_USE_BOW, soulShotConsumption, null, null);
			if(newSS < soulShotConsumption && Rnd.chance(player.calcStat(Stats.SS_USE_BOW_CHANCE, soulShotConsumption, null, null)))
				soulShotConsumption = newSS;
		}

		if(count < soulShotConsumption)
		{
			player.sendPacket(Msg.NOT_ENOUGH_SOULSHOTS);
			return;
		}

		weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_SOULSHOT);
		if(!ConfigSystem.getBoolean("InfinitySS"))
			player.getInventory().destroyItem(item, soulShotConsumption, false);
		player.sendPacket(Msg.POWER_OF_THE_SPIRITS_ENABLED);
		player.broadcastPacket(new MagicSkillUse(player, player, _skillIds[grade], 1, 0, 0));
	}

	public final int[] getItemIds()
	{
		return _itemIds;
	}

	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}