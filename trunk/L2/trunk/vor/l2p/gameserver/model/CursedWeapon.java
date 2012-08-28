package l2p.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import l2p.commons.util.Rnd;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.Earthquake;
import l2p.gameserver.serverpackets.ExRedSky;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Location;

public class CursedWeapon
{
  private final String _name;
  private String _transformationName;
  private final int _itemId;
  private final int _skillMaxLevel;
  private final int _skillId;
  private int _dropRate;
  private int _disapearChance;
  private int _durationMin;
  private int _durationMax;
  private int _durationLost;
  private int _transformationId;
  private int _transformationTemplateId;
  private int _stageKills;
  private int _nbKills = 0; private int _playerKarma = 0; private int _playerPkKills = 0;

  private CursedWeaponState _state = CursedWeaponState.NONE;
  private Location _loc = null;
  private long _endTime = 0L; private long _owner = 0L;
  private ItemInstance _item = null;

  public CursedWeapon(int itemId, int skillId, String name)
  {
    _name = name;
    _itemId = itemId;
    _skillId = skillId;
    _skillMaxLevel = SkillTable.getInstance().getMaxLevel(_skillId);
  }

  public void initWeapon()
  {
    zeroOwner();
    setState(CursedWeaponState.NONE);
    _endTime = 0L;
    _item = null;
    _nbKills = 0;
  }

  public void create(NpcInstance attackable, Player killer)
  {
    _item = ItemFunctions.createItem(_itemId);
    L2GameServerPacket redSky;
    L2GameServerPacket eq;
    if (_item != null)
    {
      zeroOwner();
      setState(CursedWeaponState.DROPPED);

      if (_endTime == 0L) {
        _endTime = (System.currentTimeMillis() + getRndDuration() * 60000);
      }
      _item.dropToTheGround(attackable, Location.findPointToStay(attackable, 100));
      _loc = _item.getLoc();
      _item.setDropTime(0L);

      redSky = new ExRedSky(10);
      eq = new Earthquake(killer.getLoc(), 30, 12);
      for (Player player : GameObjectsStorage.getAllPlayersForIterate())
        player.sendPacket(new IStaticPacket[] { redSky, eq });
    }
  }

  public boolean dropIt(NpcInstance attackable, Player killer, Player owner)
  {
    if (Rnd.chance(_disapearChance)) {
      return false;
    }
    Player player = getOnlineOwner();
    if (player == null)
    {
      if (owner == null)
        return false;
      player = owner;
    }
    ItemInstance oldItem;
    if ((oldItem = player.getInventory().removeItemByItemId(_itemId, 1L)) == null) {
      return false;
    }
    player.setKarma(_playerKarma);
    player.setPkKills(_playerPkKills);
    player.setCursedWeaponEquippedId(0);
    player.setTransformation(0);
    player.setTransformationName(null);
    player.validateLocation(0);

    Skill skill = SkillTable.getInstance().getInfo(_skillId, player.getSkillLevel(Integer.valueOf(_skillId)));
    if (skill != null) {
      for (Skill.AddedSkill s : skill.getAddedSkills())
        player.removeSkillById(Integer.valueOf(s.id));
    }
    player.removeSkillById(Integer.valueOf(_skillId));

    player.abortAttack(true, false);

    zeroOwner();
    setState(CursedWeaponState.DROPPED);

    oldItem.dropToTheGround(player, Location.findPointToStay(player, 100));
    _loc = oldItem.getLoc();

    oldItem.setDropTime(0L);
    _item = oldItem;

    player.sendPacket(new SystemMessage(298).addItemName(oldItem.getItemId()));
    player.broadcastUserInfo(true);
    player.broadcastPacket(new L2GameServerPacket[] { new Earthquake(player.getLoc(), 30, 12) });

    return true;
  }

  private void giveSkill(Player player)
  {
    for (Skill s : getSkills(player))
    {
      player.addSkill(s, false);
      player._transformationSkills.put(Integer.valueOf(s.getId()), s);
    }
    player.sendPacket(new SkillList(player));
  }

  private Collection<Skill> getSkills(Player player)
  {
    int level = 1 + _nbKills / _stageKills;
    if (level > _skillMaxLevel) {
      level = _skillMaxLevel;
    }
    Skill skill = SkillTable.getInstance().getInfo(_skillId, level);
    List ret = new ArrayList();
    ret.add(skill);
    for (Skill.AddedSkill s : skill.getAddedSkills())
      ret.add(SkillTable.getInstance().getInfo(s.id, s.level));
    return ret;
  }

  public boolean reActivate()
  {
    if (getTimeLeft() <= 0L)
    {
      if (getPlayerId() != 0)
        setState(CursedWeaponState.ACTIVATED);
      return false;
    }

    if (getPlayerId() == 0)
    {
      if ((_loc == null) || ((this._item = ItemFunctions.createItem(_itemId)) == null)) {
        return false;
      }
      _item.dropMe(null, _loc);
      _item.setDropTime(0L);

      setState(CursedWeaponState.DROPPED);
    }
    else {
      setState(CursedWeaponState.ACTIVATED);
    }return true;
  }

  public void activate(Player player, ItemInstance item)
  {
    if ((isDropped()) || (getPlayerId() != player.getObjectId()))
    {
      _playerKarma = player.getKarma();
      _playerPkKills = player.getPkKills();
    }

    setPlayer(player);
    setState(CursedWeaponState.ACTIVATED);

    player.leaveParty();
    if (player.isMounted()) {
      player.setMount(0, 0, 0);
    }
    _item = item;

    player.getInventory().setPaperdollItem(8, null);
    player.getInventory().setPaperdollItem(7, null);
    player.getInventory().setPaperdollItem(7, _item);

    player.sendPacket(new SystemMessage(49).addItemName(_item.getItemId()));

    player.setTransformation(0);
    player.setCursedWeaponEquippedId(_itemId);
    player.setTransformation(_transformationId);
    player.setTransformationName(_transformationName);
    player.setTransformationTemplate(_transformationTemplateId);
    player.setKarma(9999999);
    player.setPkKills(_nbKills);

    if (_endTime == 0L) {
      _endTime = (System.currentTimeMillis() + getRndDuration() * 60000);
    }
    giveSkill(player);

    player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
    player.setCurrentCp(player.getMaxCp());
    player.broadcastUserInfo(true);
  }

  public void increaseKills()
  {
    Player player = getOnlineOwner();
    if (player == null) {
      return;
    }
    _nbKills += 1;
    player.setPkKills(_nbKills);
    player.updateStats();
    if ((_nbKills % _stageKills == 0) && (_nbKills <= _stageKills * (_skillMaxLevel - 1)))
      giveSkill(player);
    _endTime -= _durationLost * 60000;
  }

  public void setDisapearChance(int disapearChance)
  {
    _disapearChance = disapearChance;
  }

  public void setDropRate(int dropRate)
  {
    _dropRate = dropRate;
  }

  public void setDurationMin(int duration)
  {
    _durationMin = duration;
  }

  public void setDurationMax(int duration)
  {
    _durationMax = duration;
  }

  public void setDurationLost(int durationLost)
  {
    _durationLost = durationLost;
  }

  public void setStageKills(int stageKills)
  {
    _stageKills = stageKills;
  }

  public void setTransformationId(int transformationId)
  {
    _transformationId = transformationId;
  }

  public int getTransformationId()
  {
    return _transformationId;
  }

  public void setTransformationTemplateId(int transformationTemplateId)
  {
    _transformationTemplateId = transformationTemplateId;
  }

  public void setTransformationName(String name)
  {
    _transformationName = name;
  }

  public void setNbKills(int nbKills)
  {
    _nbKills = nbKills;
  }

  public void setPlayerId(int playerId)
  {
    _owner = (playerId == 0 ? 0L : GameObjectsStorage.objIdNoStore(playerId));
  }

  public void setPlayerKarma(int playerKarma)
  {
    _playerKarma = playerKarma;
  }

  public void setPlayerPkKills(int playerPkKills)
  {
    _playerPkKills = playerPkKills;
  }

  public void setState(CursedWeaponState state)
  {
    _state = state;
  }

  public void setEndTime(long endTime)
  {
    _endTime = endTime;
  }

  public void setPlayer(Player player)
  {
    if (player != null)
      _owner = player.getStoredId().longValue();
    else if (_owner != 0L)
      setPlayerId(getPlayerId());
  }

  private void zeroOwner()
  {
    _owner = 0L;
    _playerKarma = 0;
    _playerPkKills = 0;
  }

  public void setItem(ItemInstance item)
  {
    _item = item;
  }

  public void setLoc(Location loc)
  {
    _loc = loc;
  }

  public CursedWeaponState getState()
  {
    return _state;
  }

  public boolean isActivated()
  {
    return getState() == CursedWeaponState.ACTIVATED;
  }

  public boolean isDropped()
  {
    return getState() == CursedWeaponState.DROPPED;
  }

  public long getEndTime()
  {
    return _endTime;
  }

  public String getName()
  {
    return _name;
  }

  public int getItemId()
  {
    return _itemId;
  }

  public ItemInstance getItem()
  {
    return _item;
  }

  public int getSkillId()
  {
    return _skillId;
  }

  public int getDropRate()
  {
    return _dropRate;
  }

  public int getPlayerId()
  {
    return _owner == 0L ? 0 : GameObjectsStorage.getStoredObjectId(_owner);
  }

  public Player getPlayer()
  {
    return _owner == 0L ? null : GameObjectsStorage.getAsPlayer(_owner);
  }

  public int getPlayerKarma()
  {
    return _playerKarma;
  }

  public int getPlayerPkKills()
  {
    return _playerPkKills;
  }

  public int getNbKills()
  {
    return _nbKills;
  }

  public int getStageKills()
  {
    return _stageKills;
  }

  public Location getLoc()
  {
    return _loc;
  }

  public int getRndDuration()
  {
    if (_durationMin > _durationMax)
      _durationMax = (2 * _durationMin);
    return Rnd.get(_durationMin, _durationMax);
  }

  public boolean isActive()
  {
    return (isActivated()) || (isDropped());
  }

  public int getLevel()
  {
    return Math.min(1 + _nbKills / _stageKills, _skillMaxLevel);
  }

  public long getTimeLeft()
  {
    return _endTime - System.currentTimeMillis();
  }

  public Location getWorldPosition()
  {
    if (isActivated())
    {
      Player player = getOnlineOwner();
      if (player != null)
        return player.getLoc();
    }
    else if ((isDropped()) && 
      (_item != null)) {
      return _item.getLoc();
    }
    return null;
  }

  public Player getOnlineOwner()
  {
    Player player = getPlayer();
    return (player != null) && (player.isOnline()) ? player : null;
  }

  public boolean isOwned()
  {
    return _owner != 0L;
  }

  public static enum CursedWeaponState
  {
    NONE, 
    ACTIVATED, 
    DROPPED;
  }
}