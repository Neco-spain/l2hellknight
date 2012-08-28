package scripts.skills.skillhandlers;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Fishing;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;
import scripts.skills.ISkillHandler;
import scripts.zone.L2ZoneType;
import scripts.zone.type.L2FishingZone;
import scripts.zone.type.L2WaterZone;

public class Fishing
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.FISHING };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if ((activeChar == null) || (!activeChar.isPlayer())) return;

    L2PcInstance player = (L2PcInstance)activeChar;

    if ((!Config.ALLOWFISHING) && (!player.isGM()))
    {
      player.sendMessage("\u0420\u044B\u0431\u0430\u043B\u043A\u0430 \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u0430 \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435");
      return;
    }

    if (player.isFishing())
    {
      if (player.GetFishCombat() != null) player.GetFishCombat().doDie(false); else {
        player.EndFishing(false);
      }
      player.sendPacket(Static.FISHING_ATTEMPT_CANCELLED);
      return;
    }
    L2Weapon weaponItem = player.getActiveWeaponItem();
    if ((weaponItem == null) || (weaponItem.getItemType() != L2WeaponType.ROD))
    {
      player.sendPacket(Static.FISHING_POLE_NOT_EQUIPPED);
      return;
    }
    L2ItemInstance lure = player.getInventory().getPaperdollItem(8);
    if (lure == null)
    {
      player.sendPacket(Static.BAIT_ON_HOOK_BEFORE_FISHING);
      return;
    }
    player.SetLure(lure);
    L2ItemInstance lure2 = player.getInventory().getPaperdollItem(8);

    if ((lure2 == null) || (lure2.getCount() < 1))
    {
      player.sendPacket(Static.NOT_ENOUGH_BAIT);
    }

    if (player.isInBoat())
    {
      player.sendPacket(Static.CANNOT_FISH_ON_BOAT);
      if (!player.isGM())
        return;
    }
    if ((player.isInCraftMode()) || (player.isInStoreMode()))
    {
      player.sendPacket(Static.CANNOT_FISH_WHILE_USING_RECIPE_BOOK);
      if (!player.isGM()) {
        return;
      }

    }

    int rnd = Rnd.get(150) + 50;
    double angle = Util.convertHeadingToDegree(player.getHeading());
    double radian = Math.toRadians(angle);
    double sin = Math.sin(radian);
    double cos = Math.cos(radian);
    int x = player.getX() + (int)(cos * rnd);
    int y = player.getY() + (int)(sin * rnd);
    int z = player.getZ() + 50;

    L2FishingZone aimingTo = null;
    L2WaterZone water = null;
    boolean canFish = false;
    for (L2ZoneType zone : ZoneManager.getInstance().getZones(x, y))
    {
      if ((zone instanceof L2FishingZone))
      {
        aimingTo = (L2FishingZone)zone;
        continue;
      }
      if ((zone instanceof L2WaterZone))
      {
        water = (L2WaterZone)zone;
      }
    }
    if (aimingTo != null)
    {
      if (water != null)
        z = water.getWaterZ() + 10;
      else
        z = aimingTo.getWaterZ() + 10;
      canFish = true;
    }

    if (!canFish)
    {
      player.sendPacket(Static.CANNOT_FISH_HERE);
      if (!player.isGM()) {
        return;
      }
    }

    lure2 = player.getInventory().destroyItem("Consume", player.getInventory().getPaperdollObjectId(8), 1, player, null);
    InventoryUpdate iu = new InventoryUpdate();
    iu.addModifiedItem(lure2);
    player.sendPacket(iu);

    player.startFishing(x, y, z);
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}