package l2p.gameserver.model.entity.events.objects;

import l2p.commons.dao.JdbcEntityState;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.attachment.FlagItemAttachment;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.item.WeaponTemplate;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FortressCombatFlagObject
  implements SpawnableObject, FlagItemAttachment
{
  private static final Logger _log = LoggerFactory.getLogger(FortressCombatFlagObject.class);
  private ItemInstance _item;
  private Location _location;
  private GlobalEvent _event;

  public FortressCombatFlagObject(Location location)
  {
    _location = location;
  }

  public void spawnObject(GlobalEvent event)
  {
    if (_item != null)
    {
      _log.info("FortressCombatFlagObject: can't spawn twice: " + event);
      return;
    }
    _item = ItemFunctions.createItem(9819);
    _item.setAttachment(this);
    _item.dropMe(null, _location);
    _item.setDropTime(0L);

    _event = event;
  }

  public void despawnObject(GlobalEvent event)
  {
    if (_item == null) {
      return;
    }
    Player owner = GameObjectsStorage.getPlayer(_item.getOwnerId());
    if (owner != null)
    {
      owner.getInventory().destroyItem(_item);
      owner.sendDisarmMessage(_item);
    }

    _item.setAttachment(null);
    _item.setJdbcState(JdbcEntityState.UPDATED);
    _item.delete();

    _item.deleteMe();
    _item = null;

    _event = null;
  }

  public void refreshObject(GlobalEvent event)
  {
  }

  public void onLogout(Player player)
  {
    onDeath(player, null);
  }

  public void onDeath(Player owner, Creature killer)
  {
    owner.getInventory().removeItem(_item);

    _item.setOwnerId(0);
    _item.setJdbcState(JdbcEntityState.UPDATED);
    _item.update();

    owner.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_DROPPED_S1).addItemName(_item.getItemId()));

    _item.dropMe(null, _location);
    _item.setDropTime(0L);
  }

  public boolean canPickUp(Player player)
  {
    if (player.getActiveWeaponFlagAttachment() != null)
      return false;
    FortressSiegeEvent event = (FortressSiegeEvent)player.getEvent(FortressSiegeEvent.class);
    if (event == null)
      return false;
    SiegeClanObject object = event.getSiegeClan("attackers", player.getClan());

    return object != null;
  }

  public void pickUp(Player player)
  {
    player.getInventory().equipItem(_item);

    FortressSiegeEvent event = (FortressSiegeEvent)player.getEvent(FortressSiegeEvent.class);
    event.broadcastTo(new SystemMessage2(SystemMsg.C1_HAS_ACQUIRED_THE_FLAG).addName(player), new String[] { "attackers", "defenders" });
  }

  public boolean canAttack(Player player)
  {
    player.sendPacket(SystemMsg.THAT_WEAPON_CANNOT_PERFORM_ANY_ATTACKS);
    return false;
  }

  public boolean canCast(Player player, Skill skill)
  {
    Skill[] skills = player.getActiveWeaponItem().getAttachedSkills();
    if (!ArrayUtils.contains(skills, skill))
    {
      player.sendPacket(SystemMsg.THAT_WEAPON_CANNOT_USE_ANY_OTHER_SKILL_EXCEPT_THE_WEAPONS_SKILL);
      return false;
    }

    return true;
  }

  public void setItem(ItemInstance item)
  {
  }

  public GlobalEvent getEvent()
  {
    return _event;
  }
}