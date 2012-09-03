package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2PetInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;

public class RequestPetUseItem extends L2GameClientPacket
{
	private int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2PetInstance pet = (L2PetInstance) activeChar.getPet();
		if(pet == null)
			return;

		L2ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);

		if(item == null || item.getCount() <= 0)
			return;

		if(activeChar.isAlikeDead() || pet.isDead())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return;
		}

		if(pet.tryEquipItem(item, true))
			return;

		// manual pet feeding
		if(pet.tryFeedItem(item))
			return;

		L2Skill[] skills = item.getItem().getAttachedSkills();
		if(skills != null && skills.length > 0)
			for(L2Skill skill : skills)
			{
				// Отсеиваем все что пету не положено
				if(skill.getHitTime() > 1000)
				{
					activeChar.sendPacket(Msg.ITEM_NOT_AVAILABLE_FOR_PETS);
					return;
				}
				switch(skill.getSkillType())
				{
					case BUFF:
					case CANCEL:
					case HEAL:
					case HEAL_PERCENT:
					case HOT:
					case MANAHEAL:
					case MANAHEAL_PERCENT:
					case NEGATE_EFFECTS:
					case NEGATE_STATS:
						L2Character aimingTarget = skill.getAimingTarget(pet, pet.getTarget());
						if(skill.checkCondition(pet, aimingTarget, false, false, true))
							pet.getAI().Cast(skill, aimingTarget, false, false);
						break;
					default:
						activeChar.sendPacket(Msg.ITEM_NOT_AVAILABLE_FOR_PETS);
						return;
				}
			}
		else
			activeChar.sendPacket(Msg.ITEM_NOT_AVAILABLE_FOR_PETS);
	}
}