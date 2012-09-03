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
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.xml.ItemTemplates;

public class SpiritShot implements IItemHandler, ScriptFile
{
	// all the items ids that this handler knowns
	private static final int[] _itemIds = { 5790, 2509, 2510, 2511, 2512, 2513, 2514, 22077, 22078, 22079, 22080, 22081, 19441 };
	private static final short[] _skillIds = { 2061, 2155, 2156, 2157, 2158, 2159, 26055, 26056, 26057, 26058, 26059, 9194, 9195 };

	public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return;
		L2Player player = (L2Player) playable;

		L2ItemInstance weaponInst = player.getActiveWeaponInstance();
		L2Weapon weaponItem = player.getActiveWeaponItem();
		int SoulshotId = item.getItemId();
		boolean isAutoSoulShot = false;
		L2Item itemTemplate = ItemTemplates.getInstance().getTemplate(item.getItemId());

		if(player.getAutoSoulShot().contains(SoulshotId))
			isAutoSoulShot = true;

		if(weaponInst == null)
		{
			if(!isAutoSoulShot)
				player.sendPacket(Msg.CANNOT_USE_SPIRITSHOTS);
			return;
		}

		// spiritshot is already active
		if(weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_NONE)
			return;

		int SpiritshotId = item.getItemId();
		int grade = weaponItem.getCrystalType().externalOrdinal;
		int soulSpiritConsumption = weaponItem.getSpiritShotCount();
		long count = item.getCount();

		if(soulSpiritConsumption == 0)
		{
			// Can't use Spiritshots
			if(isAutoSoulShot)
			{
				player.removeAutoSoulShot(SoulshotId);
				player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(SystemMessage.THE_AUTOMATIC_USE_OF_S1_WILL_NOW_BE_CANCELLED).addString(itemTemplate.getName()));
				return;
			}
			player.sendPacket(Msg.CANNOT_USE_SPIRITSHOTS);
			return;
		}

		if(grade == 0 && SpiritshotId != 5790 && SpiritshotId != 2509 // NG
				|| grade == 1 && SpiritshotId != 2510 && SpiritshotId != 22077 // D
				|| grade == 2 && SpiritshotId != 2511 && SpiritshotId != 22078 // C
				|| grade == 3 && SpiritshotId != 2512 && SpiritshotId != 22079 // B
				|| grade == 4 && SpiritshotId != 2513 && SpiritshotId != 22080 // A
				|| grade == 5 && SpiritshotId != 2514 && SpiritshotId != 22081 // S
				|| grade == 6 && SpiritshotId != 19441 // R
		)
		{
			// wrong grade for weapon
			if(isAutoSoulShot)
				return;
			player.sendPacket(Msg.SPIRITSHOT_DOES_NOT_MATCH_WEAPON_GRADE);
			return;
		}

		if(count < soulSpiritConsumption)
		{
			if(isAutoSoulShot)
			{
				player.removeAutoSoulShot(SoulshotId);
				player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(SystemMessage.THE_AUTOMATIC_USE_OF_S1_WILL_NOW_BE_CANCELLED).addString(itemTemplate.getName()));
				return;
			}
			player.sendPacket(Msg.NOT_ENOUGH_SPIRITSHOTS);
			return;
		}

		weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_SPIRITSHOT);
		if(!ConfigSystem.getBoolean("InfinitySS"))
			player.getInventory().destroyItem(item, soulSpiritConsumption, false);
		player.sendPacket(Msg.POWER_OF_MANA_ENABLED);
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