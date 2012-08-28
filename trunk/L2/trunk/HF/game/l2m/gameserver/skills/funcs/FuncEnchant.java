package l2m.gameserver.skills.funcs;

import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.skills.Env;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.data.tables.EnchantHPBonusTable;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.templates.item.ItemTemplate.Grade;
import l2m.gameserver.templates.item.ItemType;
import l2m.gameserver.templates.item.WeaponTemplate.WeaponType;

public class FuncEnchant extends Func
{
  public FuncEnchant(Stats stat, int order, Object owner, double value)
  {
    super(stat, order, owner);
  }

  public void calc(Env env)
  {
    ItemInstance item = (ItemInstance)owner;

    int enchant = item.getEnchantLevel();
    int overenchant = Math.max(0, enchant - 3);

    switch (1.$SwitchMap$l2p$gameserver$stats$Stats[stat.ordinal()])
    {
    case 1:
    case 2:
    case 3:
      env.value += enchant + overenchant * 2;
      return;
    case 4:
      env.value += EnchantHPBonusTable.getInstance().getHPBonus(item);
      return;
    case 5:
      switch (item.getTemplate().getCrystalType().cry)
      {
      case 1462:
        env.value += 4 * (enchant + overenchant);
        break;
      case 1461:
        env.value += 3 * (enchant + overenchant);
        break;
      case 1460:
        env.value += 3 * (enchant + overenchant);
        break;
      case 1459:
        env.value += 3 * (enchant + overenchant);
        break;
      case 0:
      case 1458:
        env.value += 2 * (enchant + overenchant);
      }

      return;
    case 6:
      ItemType itemType = item.getItemType();
      boolean isBow = (itemType == WeaponTemplate.WeaponType.BOW) || (itemType == WeaponTemplate.WeaponType.CROSSBOW);
      boolean isSword = ((itemType == WeaponTemplate.WeaponType.DUALFIST) || (itemType == WeaponTemplate.WeaponType.DUAL) || (itemType == WeaponTemplate.WeaponType.BIGSWORD) || (itemType == WeaponTemplate.WeaponType.SWORD) || (itemType == WeaponTemplate.WeaponType.RAPIER) || (itemType == WeaponTemplate.WeaponType.ANCIENTSWORD)) && (item.getTemplate().getBodyPart() == 16384);
      switch (item.getTemplate().getCrystalType().cry)
      {
      case 1462:
        if (isBow)
          env.value += 10 * (enchant + overenchant);
        else if (isSword)
          env.value += 6 * (enchant + overenchant);
        else
          env.value += 5 * (enchant + overenchant);
        break;
      case 1461:
        if (isBow)
          env.value += 8 * (enchant + overenchant);
        else if (isSword)
          env.value += 5 * (enchant + overenchant);
        else
          env.value += 4 * (enchant + overenchant);
        break;
      case 1459:
      case 1460:
        if (isBow)
          env.value += 6 * (enchant + overenchant);
        else if (isSword)
          env.value += 4 * (enchant + overenchant);
        else
          env.value += 3 * (enchant + overenchant);
        break;
      case 0:
      case 1458:
        if (isBow)
          env.value += 4 * (enchant + overenchant);
        else
          env.value += 2 * (enchant + overenchant);
      }
    }
  }
}