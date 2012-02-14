package l2rt.gameserver.skills.funcs;

import l2rt.Config;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.L2Item.Grade;
import l2rt.gameserver.templates.L2Weapon.WeaponType;

public class FuncEnchant extends Func
{
	public FuncEnchant(Stats stat, int order, Object owner, double value)
	{
		super(stat, order, owner);
	}

	@Override
	public void calc(Env env)
	{
		L2ItemInstance item = (L2ItemInstance) _funcOwner;

		if(item.getItem().getCrystalType() == Grade.NONE)
			return;

		int enchant = item.getEnchantLevel();
		int overenchant = Math.max(0, enchant - 3);

		switch(_stat)
		{
			case SHIELD_DEFENCE:
			case MAGIC_DEFENCE:
			case POWER_DEFENCE:
			{
				env.value += enchant + overenchant * 2;
				return;
			}

			case MAX_HP:
			{
				// D, Single - 8.45*Math.pow(overenchant,1.71)
				// D, Full - 12.36*Math.pow(overenchant,1.71)
				// C, Single - 11.4*Math.pow(overenchant,1.71)
				// C, Full - 17.08*Math.pow(overenchant,1.71)
				// B, Single - 13.31*Math.pow(overenchant,1.71)
				// B, Full - 19.97*Math.pow(overenchant,1.71)
				// A, Single - 15.09*Math.pow(overenchant,1.71)
				// A, Full - 22.59*Math.pow(overenchant,1.71)
				// S, Single - 16.27*Math.pow(overenchant,1.71)
				// S, Full - 24.08*Math.pow(overenchant,1.71)

				if(overenchant > 0)
				{
					double mult = 0;
					switch(item.getItem().getCrystalType().cry)
					{
						case L2Item.CRYSTAL_D:
							mult = 8.45;
							break;
						case L2Item.CRYSTAL_C:
							mult = 11.4;
							break;
						case L2Item.CRYSTAL_B:
							mult = 13.31;
							break;
						case L2Item.CRYSTAL_A:
							mult = 15.09;
							break;
						case L2Item.CRYSTAL_S:
							mult = 16.27;
							break;	
						case L2Item.CRYSTAL_R:
							mult = 16.27;
							break;
					}
					if(item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
						mult *= 1.5;
					env.value += mult * Math.pow(Math.min(Config.ARMOR_OVERENCHANT_HPBONUS_LIMIT, overenchant), 1.71);
				}
				return;
			}

			case MAGIC_ATTACK:
			{
				switch(item.getItem().getCrystalType().cry)
				{
					case L2Item.CRYSTAL_R:
						env.value += 4 * (enchant + overenchant);
						break;
					case L2Item.CRYSTAL_S:
						env.value += 4 * (enchant + overenchant);
						break;
					case L2Item.CRYSTAL_A:
						env.value += 3 * (enchant + overenchant);
						break;
					case L2Item.CRYSTAL_B:
						env.value += 3 * (enchant + overenchant);
						break;
					case L2Item.CRYSTAL_C:
						env.value += 3 * (enchant + overenchant);
						break;
					case L2Item.CRYSTAL_D:
						env.value += 2 * (enchant + overenchant);
						break;
				}
				return;
			}

			case POWER_ATTACK:
			{
				Enum itemType = item.getItemType();
				boolean isBow = itemType == WeaponType.BOW || itemType == WeaponType.CROSSBOW;
				boolean isSword = (itemType == WeaponType.DUALFIST || itemType == WeaponType.DUAL || itemType == WeaponType.BIGSWORD || itemType == WeaponType.SWORD || itemType == WeaponType.RAPIER || itemType == WeaponType.ANCIENTSWORD) && item.getItem().getBodyPart() == L2Item.SLOT_LR_HAND;
				switch(item.getItem().getCrystalType().cry)
				{
					case L2Item.CRYSTAL_R:
						if(isBow)
							env.value += 10 * (enchant + overenchant);
						else if(isSword)
							env.value += 6 * (enchant + overenchant);
						else
							env.value += 5 * (enchant + overenchant);
						break;	
					case L2Item.CRYSTAL_S:
						if(isBow)
							env.value += 10 * (enchant + overenchant);
						else if(isSword)
							env.value += 6 * (enchant + overenchant);
						else
							env.value += 5 * (enchant + overenchant);
						break;
					case L2Item.CRYSTAL_A:
						if(isBow)
							env.value += 8 * (enchant + overenchant);
						else if(isSword)
							env.value += 5 * (enchant + overenchant);
						else
							env.value += 4 * (enchant + overenchant);
						break;
					case L2Item.CRYSTAL_B:
					case L2Item.CRYSTAL_C:
						if(isBow)
							env.value += 6 * (enchant + overenchant);
						else if(isSword)
							env.value += 4 * (enchant + overenchant);
						else
							env.value += 3 * (enchant + overenchant);
						break;
					case L2Item.CRYSTAL_D:
						if(isBow)
							env.value += 4 * (enchant + overenchant);
						else
							env.value += 2 * (enchant + overenchant);
						break;
				}
				return;
			}
		}
	}
}