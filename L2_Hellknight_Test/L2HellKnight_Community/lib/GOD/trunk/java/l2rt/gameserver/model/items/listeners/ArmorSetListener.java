package l2rt.gameserver.model.items.listeners;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2ArmorSet;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SkillList;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.xml.loader.XmlArmorsetLoader;

import java.util.logging.Logger;

public final class ArmorSetListener implements PaperdollListener
{
	private static final L2Skill COMMON_SET_SKILL = SkillTable.getInstance().getInfo(3006, 1);
	protected static final Logger _log = Logger.getLogger(ArmorSetListener.class.getName());

	private Inventory _inv;

	public ArmorSetListener(Inventory inv)
	{
		_inv = inv;
	}

	public void notifyEquipped(int slot, L2ItemInstance item)
	{
		if(_inv.getOwner() == null || !_inv.getOwner().isPlayer() || !item.isEquipable())
			return;

		L2Player player = _inv.getOwner().getPlayer();

		// checks if player worns chest item
		L2ItemInstance chestItem = _inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if(chestItem == null)
			return;

		// checks if there is armorset for chest item that player worns
		L2ArmorSet armorSet = XmlArmorsetLoader.getInstance().getSet(chestItem.getItemId());
		if(armorSet == null)
			return;

		boolean update = false;
		// checks if equipped item is part of set
		if(armorSet.containItem(slot, item.getItemId()))
		{
				L2Skill skill = null;
				if (armorSet.getSkill().getLevel() == 4)
					skill = SkillTable.getInstance().getInfo(armorSet.getSkill().getId(), armorSet.containGetSkillLvL(player));
				else 
					skill = armorSet.getSkill();
				
				if(skill != null)
				{
					player.addSkill(skill, false);
					player.addSkill(COMMON_SET_SKILL, false);
					update = true;
				}

				if(armorSet.containAll(player))
				{
					if(armorSet.containShield(player)) // has shield from set
					{
						L2Skill skills = armorSet.getShieldSkill();
						if(skills != null)
						{
							player.addSkill(skills, false);
							update = true;
						}
					}
					if(armorSet.isEnchanted6(player)) // has all parts of set enchanted to 6 or more
					{
						L2Skill skille = armorSet.getEnchant6skill();
						if(skille != null)
						{
							player.addSkill(skille, false);
							update = true;
						}
					}
				}
		}
		else if(armorSet.containShield(item.getItemId()))
			if(armorSet.containAll(player))
			{
				L2Skill skills = armorSet.getShieldSkill();
				if(skills != null)
				{
					player.addSkill(skills, false);
					update = true;
				}
			}

		if(update)
		{
			player.sendPacket(new SkillList(player));
			player.updateStats();
		}
	}

	public void notifyUnequipped(int slot, L2ItemInstance item)
	{
		if(_inv.getOwner() == null || !_inv.getOwner().isPlayer() || !item.isEquipable())
			return;

		boolean remove = false;
		L2Skill removeSkillId1 = null; // set skill
		L2Skill removeSkillId2 = null; // shield skill
		L2Skill removeSkillId3 = null; // enchant +6 skill

		if(slot == Inventory.PAPERDOLL_CHEST)
		{
			L2ArmorSet armorSet = XmlArmorsetLoader.getInstance().getSet(item.getItemId());
			if(armorSet == null)
				return;

			remove = true;
			removeSkillId1 = armorSet.getSkill();
			removeSkillId2 = armorSet.getShieldSkill();
			removeSkillId3 = armorSet.getEnchant6skill();

		}
		else
		{
			L2ItemInstance chestItem = _inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if(chestItem == null)
				return;

			L2ArmorSet armorSet = XmlArmorsetLoader.getInstance().getSet(chestItem.getItemId());
			if(armorSet == null)
				return;
			
			if (armorSet.getSkill().getLevel() == 4)
			{
				L2Player player = _inv.getOwner().getPlayer();
				L2Skill skill = null;
				if (armorSet.containGetSkillLvL(player) == 0)
					return;
				else {
					skill = SkillTable.getInstance().getInfo(armorSet.getSkill().getId(), armorSet.containGetSkillLvL(player));
					player.addSkill(skill, false);
					player.sendPacket(new SkillList(player));
					player.updateStats();
				}
			}
			else if(armorSet.containItem(slot, item.getItemId())) // removed part of set
			{
				remove = true;
				removeSkillId1 = armorSet.getSkill();
				removeSkillId2 = armorSet.getShieldSkill();
				removeSkillId3 = armorSet.getEnchant6skill();
			}
			else if(armorSet.containShield(item.getItemId())) // removed shield
			{
				remove = true;
				removeSkillId2 = armorSet.getShieldSkill();
			}
		}

		L2Player player = _inv.getOwner().getPlayer();
		boolean update = false;
		if(remove)
		{
			if(removeSkillId1 != null)
			{
				player.removeSkill(removeSkillId1, false);
				player.removeSkill(COMMON_SET_SKILL, false);

				// При снятии вещей из состава S80 или S84 сета снимаем плащ
				if(!_inv.isRefreshingListeners())
					for(int skill : L2Skill.SKILLS_S80_AND_S84_SETS)
						if(skill == removeSkillId1.getId())
						{
							_inv.unEquipItemInSlot(Inventory.PAPERDOLL_BACK);
							player.sendPacket(Msg.THE_CLOAK_EQUIP_HAS_BEEN_REMOVED_BECAUSE_THE_ARMOR_SET_EQUIP_HAS_BEEN_REMOVED);
							break;
						}
				update = true;
			}
			if(removeSkillId2 != null)
			{
				player.removeSkill(removeSkillId2);
				update = true;
			}
			if(removeSkillId3 != null)
			{
				player.removeSkill(removeSkillId3);
				update = true;
			}
		}

		if(update)
		{
			player.sendPacket(new SkillList(player));
			player.updateStats();
		}
	}
}