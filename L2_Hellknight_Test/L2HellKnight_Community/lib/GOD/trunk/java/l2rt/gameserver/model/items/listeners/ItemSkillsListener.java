package l2rt.gameserver.model.items.listeners;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SkillList;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2Item;

/**
 * Добавление/удалениe скилов, прописанных предметам в sql или в xml.
 */
public final class ItemSkillsListener implements PaperdollListener
{
	private Inventory _inv;

	public ItemSkillsListener(Inventory inv)
	{
		_inv = inv;
	}

	public void notifyUnequipped(int slot, L2ItemInstance item)
	{
		if(_inv.getOwner() == null || !_inv.getOwner().isPlayer())
			return;

		L2Player player = _inv.getOwner().getPlayer();
		L2Skill[] itemSkills = null;
		L2Skill enchant4Skill = null;

		L2Item it = item.getItem();

		itemSkills = it.getAttachedSkills();

		enchant4Skill = it.getEnchant4Skill();

		if(itemSkills != null)
			for(L2Skill itemSkill : itemSkills)
				if(itemSkill.getId() >= 26046 && itemSkill.getId() <= 26048)
				{
					int level = player.getSkillLevel(itemSkill.getId());
					int newlevel = level - 1;
					if(newlevel > 0)
						player.addSkill(SkillTable.getInstance().getInfo(itemSkill.getId(), newlevel), false);
					else
						player.removeSkillById(itemSkill.getId());
				}
				else
					player.removeSkill(itemSkill, false);

		if(enchant4Skill != null)
			player.removeSkill(enchant4Skill, false);

		if(itemSkills != null || enchant4Skill != null)
		{
			player.sendPacket(new SkillList(player));
			player.updateStats();
		}
	}

	public void notifyEquipped(int slot, L2ItemInstance item)
	{
		if(_inv.getOwner() == null || !_inv.getOwner().isPlayer())
			return;

		L2Player player = _inv.getOwner().getPlayer();
		L2Skill[] itemSkills = null;
		L2Skill enchant4Skill = null;

		L2Item it = item.getItem();

		itemSkills = it.getAttachedSkills();

		if(item.getEnchantLevel() >= 4)
			enchant4Skill = it.getEnchant4Skill();

		// Для оружия при несоотвествии грейда скилы не выдаем
		if(it.getType2() == L2Item.TYPE2_WEAPON && (player.getWeaponsExpertisePenalty() > 0 || player.getArmorExpertisePenalty() > 0))
		{
			itemSkills = null;
			enchant4Skill = null;
		}

		if(itemSkills != null && itemSkills.length > 0)
			for(L2Skill itemSkill : itemSkills)
				if(itemSkill.getId() >= 26046 && itemSkill.getId() <= 26048)
				{
					int level = player.getSkillLevel(itemSkill.getId());
					int newlevel = level;
					if(level > 0)
					{
						if(SkillTable.getInstance().getInfo(itemSkill.getId(), level + 1) != null)
							newlevel = level + 1;
					}
					else
						newlevel = 1;
					if(newlevel != level)
						player.addSkill(SkillTable.getInstance().getInfo(itemSkill.getId(), newlevel), false);
				}
				else if(player.getSkillLevel(itemSkill.getId()) < itemSkill.getLevel())
				{
					player.addSkill(itemSkill, false);
					if(itemSkill.isActive())
					{
						long reuseDelay = Formulas.calcSkillReuseDelay(player, itemSkill);
						reuseDelay = Math.min(reuseDelay, 30000);
						if(reuseDelay > 0 && !player.isSkillDisabled(itemSkill.getId()))
							player.disableSkill(itemSkill.getId(), reuseDelay);
					}
				}

		if(enchant4Skill != null)
			player.addSkill(enchant4Skill, false);

		if(itemSkills != null || enchant4Skill != null)
		{
			player.sendPacket(new SkillList(player));
			player.updateStats();
		}
	}
}