package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.conditions.Condition;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2WeaponType;

public class FuncEnchant extends Func
{
  public FuncEnchant(Stats pStat, int pOrder, Object owner, Lambda lambda)
  {
    super(pStat, pOrder, owner);
  }

  public void calc(Env env)
  {
    if ((cond != null) && (!cond.test(env))) return;
    L2ItemInstance item = (L2ItemInstance)funcOwner;
    int cristall = item.getItem().getCrystalType();
    Enum itemType = item.getItemType();

    if (cristall == 0) return;
    int enchant = item.getEnchantLevel();

    int overenchant = 0;
    if (enchant > 3)
    {
      overenchant = enchant - 3;
      enchant = 3;
    }
    if ((env.player != null) && ((env.player instanceof L2PcInstance)))
    {
      L2PcInstance player = (L2PcInstance)env.player;
      if ((player.isInOlympiadMode()) && (Config.ALT_OLY_MAX_ENCHANT >= 0) && (enchant + overenchant > Config.ALT_OLY_MAX_ENCHANT))
      {
        if (Config.ALT_OLY_MAX_ENCHANT > 3)
        {
          overenchant = Config.ALT_OLY_MAX_ENCHANT - 3;
        }
        else
        {
          overenchant = 0;
          enchant = Config.ALT_OLY_MAX_ENCHANT;
        }
      }
    }
    if ((stat == Stats.MAGIC_DEFENCE) || (stat == Stats.POWER_DEFENCE))
    {
      env.value += enchant + 3 * overenchant;
      return;
    }

    if (stat == Stats.MAGIC_ATTACK)
    {
      switch (item.getItem().getCrystalType())
      {
      case 5:
        env.value += 4 * enchant + 8 * overenchant;
        break;
      case 4:
        env.value += 3 * enchant + 6 * overenchant;
        break;
      case 3:
        env.value += 3 * enchant + 6 * overenchant;
        break;
      case 2:
        env.value += 3 * enchant + 6 * overenchant;
        break;
      case 1:
        env.value += 2 * enchant + 4 * overenchant;
      }

      return;
    }

    switch (item.getItem().getCrystalType())
    {
    case 4:
      if (itemType == L2WeaponType.BOW) env.value += 8 * enchant + 16 * overenchant;
      else if ((itemType == L2WeaponType.DUALFIST) || (itemType == L2WeaponType.DUAL) || ((itemType == L2WeaponType.SWORD) && (item.getItem().getBodyPart() == 16384)))
        env.value += 5 * enchant + 10 * overenchant;
      else
        env.value += 4 * enchant + 8 * overenchant;
      break;
    case 3:
      if (itemType == L2WeaponType.BOW) env.value += 6 * enchant + 12 * overenchant;
      else if ((itemType == L2WeaponType.DUALFIST) || (itemType == L2WeaponType.DUAL) || ((itemType == L2WeaponType.SWORD) && (item.getItem().getBodyPart() == 16384)))
        env.value += 4 * enchant + 8 * overenchant;
      else
        env.value += 3 * enchant + 6 * overenchant;
      break;
    case 2:
      if (itemType == L2WeaponType.BOW) env.value += 6 * enchant + 12 * overenchant;
      else if ((itemType == L2WeaponType.DUALFIST) || (itemType == L2WeaponType.DUAL) || ((itemType == L2WeaponType.SWORD) && (item.getItem().getBodyPart() == 16384)))
        env.value += 4 * enchant + 8 * overenchant;
      else {
        env.value += 3 * enchant + 6 * overenchant;
      }
      break;
    case 1:
      if (itemType == L2WeaponType.BOW) env.value += 4 * enchant + 8 * overenchant; else
        env.value += 2 * enchant + 4 * overenchant;
      break;
    case 5:
      if (itemType == L2WeaponType.BOW) env.value += 10 * enchant + 20 * overenchant;
      else if ((itemType == L2WeaponType.DUALFIST) || (itemType == L2WeaponType.DUAL) || ((itemType == L2WeaponType.SWORD) && (item.getItem().getBodyPart() == 16384)))
        env.value += 4 * enchant + 12 * overenchant;
      else
        env.value += 4 * enchant + 10 * overenchant;
    }
  }
}