package l2rt.gameserver.model.items.listeners;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2Item;

public final class BraceletListener implements PaperdollListener
{
	private Inventory _inv;

	public BraceletListener(Inventory inv)
	{
		_inv = inv;
	}

	public void notifyUnequipped(int slot, L2ItemInstance item)
	{
		if(_inv.getOwner() == null || !_inv.getOwner().isPlayer() || !item.isEquipable())
			return;

		L2Player owner = (L2Player) _inv.getOwner();

		if(item.getBodyPart() == L2Item.SLOT_L_BRACELET && item.getItem().getAttachedSkills() != null)
		{
			int agathionId = owner.getAgathion() == null ? 0 : owner.getAgathion().getId();
			int mountNpcId = owner.getMountNpcId();
			for(L2Skill skill : item.getItem().getAttachedSkills())
			{
				if(agathionId > 0 && skill.getNpcId() == agathionId)
					owner.setAgathion(0);
				if(mountNpcId > 0 && skill.getNpcId() == mountNpcId)
					owner.setMount(0, 0, 0);
			}
			owner.getPlayer().removeSkillById(L2Skill.SKILL_DISMISS_AGATHION); // Удаляем скилл Dismiss Agathion
		}

		// При снятии правого браслета, снимаем и талисманы тоже
		if(item.getBodyPart() == L2Item.SLOT_R_BRACELET)
		{
			_inv.setPaperdollItem(Inventory.PAPERDOLL_DECO1, null);
			_inv.setPaperdollItem(Inventory.PAPERDOLL_DECO2, null);
			_inv.setPaperdollItem(Inventory.PAPERDOLL_DECO3, null);
			_inv.setPaperdollItem(Inventory.PAPERDOLL_DECO4, null);
			_inv.setPaperdollItem(Inventory.PAPERDOLL_DECO5, null);
			_inv.setPaperdollItem(Inventory.PAPERDOLL_DECO6, null);
		}
	}

	public void notifyEquipped(int slot, L2ItemInstance item)
	{
		if(_inv.getOwner() == null || !_inv.getOwner().isPlayer() || !item.isEquipable())
			return;

		if(item.getBodyPart() == L2Item.SLOT_L_BRACELET && item.getItem().getAttachedSkills() != null)
			_inv.getOwner().getPlayer().addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_DISMISS_AGATHION, 1), false); // Выдаем скилл Dismiss Agathion
	}
}