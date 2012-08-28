package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.commons.collections.LazyArrayList;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.util.Rnd;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Fishing;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Territory;
import l2p.gameserver.model.World;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.Zone.ZoneType;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.tables.FishTable;
import l2p.gameserver.templates.FishTemplate;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.templates.item.WeaponTemplate;
import l2p.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.PositionUtils;

public class FishingSkill extends Skill
{
  public FishingSkill(StatsSet set)
  {
    super(set);
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    Player player = (Player)activeChar;

    if (player.getSkillLevel(Integer.valueOf(1315)) == -1) {
      return false;
    }
    if (player.isFishing())
    {
      player.stopFishing();
      player.sendPacket(Msg.CANCELS_FISHING);
      return false;
    }

    if (player.isInBoat())
    {
      activeChar.sendPacket(Msg.YOU_CANT_FISH_WHILE_YOU_ARE_ON_BOARD);
      return false;
    }

    if (player.getPrivateStoreType() != 0)
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_FISH_WHILE_USING_A_RECIPE_BOOK_PRIVATE_MANUFACTURE_OR_PRIVATE_STORE);
      return false;
    }

    if ((!player.isInZone(Zone.ZoneType.FISHING)) || (player.isInWater()))
    {
      player.sendPacket(Msg.YOU_CANT_FISH_HERE);
      return false;
    }

    WeaponTemplate weaponItem = player.getActiveWeaponItem();
    if ((weaponItem == null) || (weaponItem.getItemType() != WeaponTemplate.WeaponType.ROD))
    {
      player.sendPacket(Msg.FISHING_POLES_ARE_NOT_INSTALLED);
      return false;
    }

    ItemInstance lure = player.getInventory().getPaperdollItem(8);
    if ((lure == null) || (lure.getCount() < 1L))
    {
      player.sendPacket(Msg.BAITS_ARE_NOT_PUT_ON_A_HOOK);
      return false;
    }

    int rnd = Rnd.get(50) + 150;
    double angle = PositionUtils.convertHeadingToDegree(player.getHeading());
    double radian = Math.toRadians(angle - 90.0D);
    double sin = Math.sin(radian);
    double cos = Math.cos(radian);
    int x1 = -(int)(sin * rnd);
    int y1 = (int)(cos * rnd);
    int x = player.getX() + x1;
    int y = player.getY() + y1;

    int z = GeoEngine.getHeight(x, y, player.getZ(), player.getGeoIndex()) + 1;

    boolean isInWater = false;
    LazyArrayList zones = LazyArrayList.newInstance();
    World.getZones(zones, new Location(x, y, z), player.getReflection());
    for (Zone zone : zones)
      if (zone.getType() == Zone.ZoneType.water)
      {
        z = zone.getTerritory().getZmax();
        isInWater = true;
        break;
      }
    LazyArrayList.recycle(zones);

    if (!isInWater)
    {
      player.sendPacket(Msg.YOU_CANT_FISH_HERE);
      return false;
    }

    player.getFishing().setFishLoc(new Location(x, y, z));

    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature caster, List<Creature> targets)
  {
    if ((caster == null) || (!caster.isPlayer())) {
      return;
    }
    Player player = (Player)caster;

    ItemInstance lure = player.getInventory().getPaperdollItem(8);
    if ((lure == null) || (lure.getCount() < 1L))
    {
      player.sendPacket(Msg.BAITS_ARE_NOT_PUT_ON_A_HOOK);
      return;
    }
    Zone zone = player.getZone(Zone.ZoneType.FISHING);
    if (zone == null) {
      return;
    }
    int distributionId = zone.getParams().getInteger("distribution_id");

    int lureId = lure.getItemId();

    int group = Fishing.getFishGroup(lure.getItemId());
    int type = Fishing.getRandomFishType(lureId);
    int lvl = Fishing.getRandomFishLvl(player);

    List fishs = FishTable.getInstance().getFish(group, type, lvl);
    if ((fishs == null) || (fishs.size() == 0))
    {
      player.sendPacket(Msg.SYSTEM_ERROR);
      return;
    }

    if (!player.getInventory().destroyItemByObjectId(player.getInventory().getPaperdollObjectId(8), 1L))
    {
      player.sendPacket(Msg.NOT_ENOUGH_BAIT);
      return;
    }

    int check = Rnd.get(fishs.size());
    FishTemplate fish = (FishTemplate)fishs.get(check);

    player.startFishing(fish, lureId);
  }
}