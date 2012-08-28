package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.instancemanager.FishingZoneManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Fishing;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.type.L2FishingZone;
import net.sf.l2j.gameserver.model.zone.type.L2WaterZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public class Fishing
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.FISHING };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    if (!(activeChar instanceof L2PcInstance)) {
      return;
    }
    L2PcInstance player = (L2PcInstance)activeChar;

    if ((!Config.ALLOWFISHING) && (!player.isGM()))
    {
      player.sendMessage("Fishing server is currently offline");
      return;
    }
    if (player.isFishing())
    {
      if (player.GetFishCombat() != null)
        player.GetFishCombat().doDie(false);
      else {
        player.EndFishing(false);
      }
      player.sendPacket(new SystemMessage(SystemMessageId.FISHING_ATTEMPT_CANCELLED));
      return;
    }
    L2Weapon weaponItem = player.getActiveWeaponItem();
    if ((weaponItem == null) || (weaponItem.getItemType() != L2WeaponType.ROD))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.FISHING_POLE_NOT_EQUIPPED));
      return;
    }
    L2ItemInstance lure = player.getInventory().getPaperdollItem(8);
    if (lure == null)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING));
      return;
    }
    player.SetLure(lure);
    L2ItemInstance lure2 = player.getInventory().getPaperdollItem(8);

    if ((lure2 == null) || (lure2.getCount() < 1))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_BAIT));
      return;
    }
    if (player.isInBoat())
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_ON_BOAT));
      if (!player.isGM())
        return;
    }
    if ((player.isInCraftMode()) || (player.isInStoreMode()))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK));
      if (!player.isGM()) {
        return;
      }

    }

    int rnd = Rnd.get(200) + 200;
    double angle = Util.convertHeadingToDegree(player.getHeading());
    double radian = Math.toRadians(angle);
    double sin = Math.sin(radian);
    double cos = Math.cos(radian);
    int x1 = (int)(cos * rnd);
    int y1 = (int)(sin * rnd);
    int x = player.getX() + x1;
    int y = player.getY() + y1;
    int z = player.getZ() - 30;

    L2FishingZone aimingTo = FishingZoneManager.getInstance().isInsideFishingZone(x, y, z);
    L2WaterZone water = FishingZoneManager.getInstance().isInsideWaterZone(x, y, z);
    if ((aimingTo != null) && (water != null) && (GeoData.getInstance().canSeeTarget(player.getX(), player.getY(), player.getZ() + 50, x, y, water.getWaterZ() - 50)))
    {
      z = water.getWaterZ() + 10;
    }
    else if ((aimingTo != null) && (GeoData.getInstance().canSeeTarget(player.getX(), player.getY(), player.getZ() + 50, x, y, aimingTo.getWaterZ() - 50))) {
      z = aimingTo.getWaterZ() + 10;
    }
    else
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_HERE));
      if (!player.isGM())
      {
        return;
      }

    }

    if ((player.getZ() <= -3800) || (player.getZ() < z - 32))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_UNDER_WATER));
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