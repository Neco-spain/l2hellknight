package l2rt.gameserver.skills.skillclasses;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ExAutoSoulShot;
import l2rt.gameserver.network.serverpackets.InventoryUpdate;
import l2rt.gameserver.network.serverpackets.ShortCutInit;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.templates.StatsSet;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.GArray;

import java.util.HashMap;

public class KamaelWeaponExchange extends L2Skill
{
	public KamaelWeaponExchange(StatsSet set)
	{
		super(set);
	}

	private static HashMap<Integer, Integer> exchangemap;

	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{

		if(!activeChar.isPlayer() || activeChar.isOutOfControl() || activeChar.getDuel() != null || activeChar.getActiveWeaponInstance() == null)
			return false;

		L2Player p = (L2Player) activeChar;
		if(p.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE || p.isInTransaction())
			return false;

		L2ItemInstance item = activeChar.getActiveWeaponInstance();
		if(item != null && convertWeaponId(item.getItemId()) == 0)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_CONVERT_THIS_ITEM);
			return false;
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		final L2Player player = (L2Player) activeChar;
		final L2ItemInstance item = activeChar.getActiveWeaponInstance();
		if(item == null)
			return;

		int itemtoexchange = convertWeaponId(item.getItemId());

		if(itemtoexchange == 0) // how can it be?
			return;

		player.getInventory().unEquipItemInBodySlotAndNotify(item.getBodyPart(), item);
		player.sendPacket(new InventoryUpdate().addRemovedItem(item));
		item.setItemId(itemtoexchange);

		player.sendPacket(new ShortCutInit(player));
		for(int shotId : player.getAutoSoulShot())
			player.sendPacket(new ExAutoSoulShot(shotId, true));

		player.sendPacket(new InventoryUpdate().addNewItem(item));
		player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EQUIPPED_YOUR_S1).addItemName(itemtoexchange));
		ThreadPoolManager.getInstance().scheduleAi(new Runnable(){
			@Override
			public void run()
			{
				player.getInventory().equipItem(item, true);
			}
		}, 150, true);
	}

	private int convertWeaponId(int source)
	{
		if(exchangemap == null)
			fillExchangeMap();
		Integer ret = exchangemap.get(source);
		return ret != null ? ret : 0;
	}

	private synchronized void fillExchangeMap()
	{
		if(exchangemap != null)
			return;
		exchangemap = new HashMap<Integer, Integer>();
        for(L2Weapon weapon : ItemTemplates.getInstance().getAllWeapons()) {
            if(weapon.getKamaelAnalog() > 0)
                exchangemap.put(weapon.getItemId(), weapon.getKamaelAnalog());
        }
	}
}