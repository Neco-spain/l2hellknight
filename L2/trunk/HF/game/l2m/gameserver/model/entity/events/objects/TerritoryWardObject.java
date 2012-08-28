package l2m.gameserver.model.entity.events.objects;

import l2p.commons.dao.JdbcEntityState;
import l2m.gameserver.data.xml.holder.EventHolder;
import l2m.gameserver.data.xml.holder.NpcHolder;
import l2m.gameserver.idfactory.IdFactory;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.entity.events.EventType;
import l2m.gameserver.model.entity.events.GlobalEvent;
import l2m.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2m.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.model.instances.TerritoryWardInstance;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.model.items.attachment.FlagItemAttachment;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.item.WeaponTemplate;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.ItemFunctions;
import l2m.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;

public class TerritoryWardObject
  implements SpawnableObject, FlagItemAttachment
{
  private static final long serialVersionUID = 1L;
  private final int _itemId;
  private final NpcTemplate _template;
  private final Location _location;
  private NpcInstance _wardNpcInstance;
  private ItemInstance _wardItemInstance;

  public TerritoryWardObject(int itemId, int npcId, Location location)
  {
    _itemId = itemId;
    _template = NpcHolder.getInstance().getTemplate(npcId);
    _location = location;
  }

  public void spawnObject(GlobalEvent event)
  {
    _wardItemInstance = ItemFunctions.createItem(_itemId);
    _wardItemInstance.setAttachment(this);

    _wardNpcInstance = new TerritoryWardInstance(IdFactory.getInstance().getNextId(), _template, this);
    _wardNpcInstance.addEvent(event);
    _wardNpcInstance.setCurrentHpMp(_wardNpcInstance.getMaxHp(), _wardNpcInstance.getMaxMp());
    _wardNpcInstance.spawnMe(_location);
  }

  public void despawnObject(GlobalEvent event)
  {
    if ((_wardItemInstance == null) || (_wardNpcInstance == null)) {
      return;
    }
    Player owner = GameObjectsStorage.getPlayer(_wardItemInstance.getOwnerId());
    if (owner != null)
    {
      owner.getInventory().destroyItem(_wardItemInstance);
      owner.sendDisarmMessage(_wardItemInstance);
    }
    _wardItemInstance.setAttachment(null);
    _wardItemInstance.setJdbcState(JdbcEntityState.UPDATED);
    _wardItemInstance.delete();
    _wardItemInstance.deleteMe();
    _wardItemInstance = null;

    _wardNpcInstance.deleteMe();
    _wardNpcInstance = null;
  }

  public void refreshObject(GlobalEvent event)
  {
  }

  public void onLogout(Player player)
  {
    player.getInventory().removeItem(_wardItemInstance);

    _wardItemInstance.setOwnerId(0);
    _wardItemInstance.setJdbcState(JdbcEntityState.UPDATED);
    _wardItemInstance.update();

    _wardNpcInstance.setCurrentHpMp(_wardNpcInstance.getMaxHp(), _wardNpcInstance.getMaxMp(), true);
    _wardNpcInstance.spawnMe(_location);
  }

  public void onDeath(Player owner, Creature killer)
  {
    Location loc = owner.getLoc();

    owner.getInventory().removeItem(_wardItemInstance);
    owner.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_DROPPED_S1).addName(_wardItemInstance));

    _wardItemInstance.setOwnerId(0);
    _wardItemInstance.setJdbcState(JdbcEntityState.UPDATED);
    _wardItemInstance.update();

    _wardNpcInstance.setCurrentHpMp(_wardNpcInstance.getMaxHp(), _wardNpcInstance.getMaxMp(), true);
    _wardNpcInstance.spawnMe(loc);

    DominionSiegeRunnerEvent runnerEvent = (DominionSiegeRunnerEvent)EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
    runnerEvent.broadcastTo(new SystemMessage2(SystemMsg.THE_CHARACTER_THAT_ACQUIRED_S1S_WARD_HAS_BEEN_KILLED).addResidenceName(getDominionId()));
  }

  public boolean canPickUp(Player player)
  {
    return true;
  }

  public void pickUp(Player player)
  {
    player.getInventory().addItem(_wardItemInstance);
    player.getInventory().equipItem(_wardItemInstance);

    player.sendPacket(SystemMsg.YOUVE_ACQUIRED_THE_WARD);

    DominionSiegeRunnerEvent runnerEvent = (DominionSiegeRunnerEvent)EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
    runnerEvent.broadcastTo(((SystemMessage2)new SystemMessage2(SystemMsg.THE_S1_WARD_HAS_BEEN_DESTROYED_C2_NOW_HAS_THE_TERRITORY_WARD).addResidenceName(getDominionId())).addName(player));
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

  public Location getWardLocation()
  {
    if ((_wardItemInstance == null) || (_wardNpcInstance == null)) {
      return null;
    }
    if (_wardItemInstance.getOwnerId() > 0)
    {
      Player player = GameObjectsStorage.getPlayer(_wardItemInstance.getOwnerId());
      if (player != null) {
        return player.getLoc();
      }
    }
    return _wardNpcInstance.getLoc();
  }

  public NpcInstance getWardNpcInstance()
  {
    return _wardNpcInstance;
  }

  public ItemInstance getWardItemInstance()
  {
    return _wardItemInstance;
  }

  public int getDominionId()
  {
    return _itemId - 13479;
  }

  public DominionSiegeEvent getEvent()
  {
    return (DominionSiegeEvent)_wardNpcInstance.getEvent(DominionSiegeEvent.class);
  }
}