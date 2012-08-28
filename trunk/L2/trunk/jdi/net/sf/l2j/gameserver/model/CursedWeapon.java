package net.sf.l2j.gameserver.model;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.Earthquake;
import net.sf.l2j.gameserver.network.serverpackets.ExRedSky;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.util.Point3D;
import net.sf.l2j.util.Rnd;

public class CursedWeapon
{
  private static final Logger _log = Logger.getLogger(CursedWeaponsManager.class.getName());
  private final String _name;
  private final int _itemId;
  private final int _skillId;
  private final int _skillMaxLevel;
  private int _dropRate;
  private int _duration;
  private int _durationLost;
  private int _disapearChance;
  private int _stageKills;
  private boolean _isDropped = false;
  private boolean _isActivated = false;
  private ScheduledFuture _removeTask;
  private int _nbKills = 0;
  private long _endTime = 0L;

  private int _playerId = 0;
  private L2PcInstance _player = null;
  private L2ItemInstance _item = null;
  private int _playerKarma = 0;
  private int _playerPkKills = 0;

  public CursedWeapon(int itemId, int skillId, String name)
  {
    _name = name;
    _itemId = itemId;
    _skillId = skillId;
    _skillMaxLevel = SkillTable.getInstance().getMaxLevel(_skillId, 0);
  }

  public void endOfLife()
  {
    if (_isActivated)
    {
      if ((_player != null) && (_player.isOnline() == 1))
      {
        _log.info(_name + " being removed online.");

        _player.abortAttack();

        _player.setKarma(_playerKarma);
        _player.setPkKills(_playerPkKills);
        _player.setCursedWeaponEquipedId(0);
        removeSkill();

        _player.getInventory().unEquipItemInBodySlotAndRecord(16384);
        _player.store();

        L2ItemInstance removedItem = _player.getInventory().destroyItemByItemId("", _itemId, 1, _player, null);
        if (!Config.FORCE_INVENTORY_UPDATE)
        {
          InventoryUpdate iu = new InventoryUpdate();
          if (removedItem.getCount() == 0) iu.addRemovedItem(removedItem); else {
            iu.addModifiedItem(removedItem);
          }
          _player.sendPacket(iu);
        } else {
          _player.sendPacket(new ItemList(_player, true));
        }
        _player.broadcastUserInfo();
      }
      else
      {
        _log.info(_name + " being removed offline.");

        Connection con = null;
        try
        {
          con = L2DatabaseFactory.getInstance().getConnection();

          PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
          statement.setInt(1, _playerId);
          statement.setInt(2, _itemId);
          if (statement.executeUpdate() != 1)
          {
            _log.warning("Error while deleting itemId " + _itemId + " from userId " + _playerId);
          }
          statement.close();

          statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_id=?");
          statement.setInt(1, _playerKarma);
          statement.setInt(2, _playerPkKills);
          statement.setInt(3, _playerId);
          if (statement.executeUpdate() != 1)
          {
            _log.warning("Error while updating karma & pkkills for userId " + _playerId);
          }

          statement.close();
        }
        catch (Exception e)
        {
          _log.warning("Could not delete : " + e);
        }
        finally {
          try {
            con.close();
          }
          catch (Exception e) {
          }
        }
      }
    }
    else if ((_player != null) && (_player.getInventory().getItemByItemId(_itemId) != null))
    {
      L2ItemInstance removedItem = _player.getInventory().destroyItemByItemId("", _itemId, 1, _player, null);
      if (!Config.FORCE_INVENTORY_UPDATE)
      {
        InventoryUpdate iu = new InventoryUpdate();
        if (removedItem.getCount() == 0) iu.addRemovedItem(removedItem); else {
          iu.addModifiedItem(removedItem);
        }
        _player.sendPacket(iu);
      } else {
        _player.sendPacket(new ItemList(_player, true));
      }
      _player.broadcastUserInfo();
    }
    else if (_item != null)
    {
      _item.decayMe();
      L2World.getInstance().removeObject(_item);
      _log.info(_name + " item has been removed from World.");
    }

    CursedWeaponsManager.removeFromDb(_itemId);

    SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
    sm.addItemName(_itemId);
    CursedWeaponsManager.announce(sm);

    cancelTask();
    _isActivated = false;
    _isDropped = false;
    _endTime = 0L;
    _player = null;
    _playerId = 0;
    _playerKarma = 0;
    _playerPkKills = 0;
    _item = null;
    _nbKills = 0;
  }

  private void cancelTask()
  {
    if (_removeTask != null)
    {
      _removeTask.cancel(true);
      _removeTask = null;
    }
  }

  private void dropIt(L2Attackable attackable, L2PcInstance player)
  {
    dropIt(attackable, player, null, true);
  }

  private void dropIt(L2Attackable attackable, L2PcInstance player, L2Character killer, boolean fromMonster) {
    _isActivated = false;
    ExRedSky packet;
    Earthquake eq;
    if (fromMonster)
    {
      _item = attackable.dropItem(player, _itemId, 1);
      _item.setDropTime(0L);

      packet = new ExRedSky(10);
      eq = new Earthquake(player.getX(), player.getY(), player.getZ(), 14, 3);
      for (L2PcInstance aPlayer : L2World.getInstance().getAllPlayers()) {
        aPlayer.sendPacket(packet);
        aPlayer.sendPacket(eq);
      }
    }
    else {
      _player.dropItem("DieDrop", _item, killer, true);
      _player.setKarma(_playerKarma);
      _player.setPkKills(_playerPkKills);
      _player.setCursedWeaponEquipedId(0);
      removeSkill();
      _player.abortAttack();
    }

    _isDropped = true;
    SystemMessage sm = new SystemMessage(SystemMessageId.S2_WAS_DROPPED_IN_THE_S1_REGION);
    if (player != null)
      sm.addZoneName(player.getX(), player.getY(), player.getZ());
    else if (_player != null)
      sm.addZoneName(_player.getX(), _player.getY(), _player.getZ());
    else
      sm.addZoneName(killer.getX(), killer.getY(), killer.getZ());
    sm.addItemName(_itemId);
    CursedWeaponsManager.announce(sm);
  }

  public void giveSkill()
  {
    int level = 1 + _nbKills / _stageKills;
    if (level > _skillMaxLevel) {
      level = _skillMaxLevel;
    }
    L2Skill skill = SkillTable.getInstance().getInfo(_skillId, level);

    _player.addSkill(skill, false);

    skill = SkillTable.getInstance().getInfo(3630, 1);
    _player.addSkill(skill, false);
    skill = SkillTable.getInstance().getInfo(3631, 1);
    _player.addSkill(skill, false);

    if (Config.DEBUG)
      System.out.println("Player " + _player.getName() + " has been awarded with skill " + skill);
    _player.sendSkillList();
  }

  public void removeSkill()
  {
    _player.removeSkill(SkillTable.getInstance().getInfo(_skillId, _player.getSkillLevel(_skillId)), false);
    _player.removeSkill(SkillTable.getInstance().getInfo(3630, 1), false);
    _player.removeSkill(SkillTable.getInstance().getInfo(3631, 1), false);
    _player.sendSkillList();
  }

  public void reActivate()
  {
    _isActivated = true;
    if (_endTime - System.currentTimeMillis() <= 0L)
      endOfLife();
    else
      _removeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RemoveTask(), _durationLost * 12000L, _durationLost * 12000L);
  }

  public boolean checkDrop(L2Attackable attackable, L2PcInstance player)
  {
    if (Rnd.get(100000) < _dropRate)
    {
      dropIt(attackable, player);

      _endTime = (System.currentTimeMillis() + _duration * 60000L);
      _removeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RemoveTask(), _durationLost * 12000L, _durationLost * 12000L);

      return true;
    }

    return false;
  }

  public void activate(L2PcInstance player, L2ItemInstance item)
  {
    if (player.isMounted())
    {
      if (_player.setMountType(0))
      {
        Ride dismount = new Ride(_player.getObjectId(), 0, 0);
        _player.broadcastPacket(dismount);
        _player.setMountObjectID(0);
      }
      else
      {
        player.sendMessage("You may not pick up this item while riding in this territory");
        return;
      }
    }

    _isActivated = true;

    _player = player;
    _playerId = _player.getObjectId();
    _playerKarma = _player.getKarma();
    _playerPkKills = _player.getPkKills();
    saveData();

    _player.setCursedWeaponEquipedId(_itemId);
    _player.setKarma(9999999);
    _player.setPkKills(0);
    if (_player.isInParty()) {
      _player.getParty().oustPartyMember(_player);
    }

    giveSkill();

    _item = item;

    _player.getInventory().equipItemAndRecord(_item);
    SystemMessage sm = new SystemMessage(SystemMessageId.S1_EQUIPPED);
    sm.addItemName(_item.getItemId());
    _player.sendPacket(sm);

    _player.setCurrentHpMp(_player.getMaxHp(), _player.getMaxMp());
    _player.setCurrentCp(_player.getMaxCp());

    if (!Config.FORCE_INVENTORY_UPDATE)
    {
      InventoryUpdate iu = new InventoryUpdate();
      iu.addItem(_item);

      _player.sendPacket(iu);
    } else {
      _player.sendPacket(new ItemList(_player, false));
    }

    _player.broadcastUserInfo();

    SocialAction atk = new SocialAction(_player.getObjectId(), 17);

    _player.broadcastPacket(atk);

    sm = new SystemMessage(SystemMessageId.THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION);
    sm.addZoneName(_player.getX(), _player.getY(), _player.getZ());
    sm.addItemName(_item.getItemId());
    CursedWeaponsManager.announce(sm);
  }

  public void saveData()
  {
    if (Config.DEBUG) {
      System.out.println("CursedWeapon: Saving data to disk.");
    }
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
      statement.setInt(1, _itemId);
      statement.executeUpdate();

      if (_isActivated)
      {
        statement = con.prepareStatement("INSERT INTO cursed_weapons (itemId, playerId, playerKarma, playerPkKills, nbKills, endTime) VALUES (?, ?, ?, ?, ?, ?)");
        statement.setInt(1, _itemId);
        statement.setInt(2, _playerId);
        statement.setInt(3, _playerKarma);
        statement.setInt(4, _playerPkKills);
        statement.setInt(5, _nbKills);
        statement.setLong(6, _endTime);
        statement.executeUpdate();
      }

      statement.close();
      con.close();
    }
    catch (SQLException e)
    {
      _log.severe("CursedWeapon: Failed to save data: " + e);
    }
    finally
    {
      try
      {
        statement.close();
      }
      catch (Exception e) {
      }
      try {
        con.close();
      }
      catch (Exception e) {
      }
    }
  }

  public void dropIt(L2Character killer) {
    if (Rnd.get(100) <= _disapearChance)
    {
      endOfLife();
    }
    else
    {
      dropIt(null, null, killer, false);

      _player.setKarma(_playerKarma);
      _player.setPkKills(_playerPkKills);
      _player.setCursedWeaponEquipedId(0);
      removeSkill();

      _player.abortAttack();

      _player.broadcastUserInfo();
    }
  }

  public void increaseKills()
  {
    _nbKills += 1;

    _player.setPkKills(_nbKills);
    _player.broadcastUserInfo();

    if ((_nbKills % _stageKills == 0) && (_nbKills <= _stageKills * (_skillMaxLevel - 1)))
    {
      giveSkill();
    }

    _endTime -= _durationLost * 60000L;
    saveData();
  }

  public void setDisapearChance(int disapearChance)
  {
    _disapearChance = disapearChance;
  }

  public void setDropRate(int dropRate) {
    _dropRate = dropRate;
  }

  public void setDuration(int duration) {
    _duration = duration;
  }

  public void setDurationLost(int durationLost) {
    _durationLost = durationLost;
  }

  public void setStageKills(int stageKills) {
    _stageKills = stageKills;
  }

  public void setNbKills(int nbKills) {
    _nbKills = nbKills;
  }

  public void setPlayerId(int playerId) {
    _playerId = playerId;
  }

  public void setPlayerKarma(int playerKarma) {
    _playerKarma = playerKarma;
  }

  public void setPlayerPkKills(int playerPkKills) {
    _playerPkKills = playerPkKills;
  }

  public void setActivated(boolean isActivated) {
    _isActivated = isActivated;
  }

  public void setDropped(boolean isDropped) {
    _isDropped = isDropped;
  }

  public void setEndTime(long endTime) {
    _endTime = endTime;
  }

  public void setPlayer(L2PcInstance player) {
    _player = player;
  }

  public void setItem(L2ItemInstance item) {
    _item = item;
  }

  public boolean isActivated()
  {
    return _isActivated;
  }

  public boolean isDropped() {
    return _isDropped;
  }

  public long getEndTime() {
    return _endTime;
  }

  public String getName() {
    return _name;
  }

  public int getItemId() {
    return _itemId;
  }

  public int getSkillId() {
    return _skillId;
  }

  public int getPlayerId() {
    return _playerId;
  }

  public L2PcInstance getPlayer() {
    return _player;
  }

  public int getPlayerKarma() {
    return _playerKarma;
  }

  public int getPlayerPkKills() {
    return _playerPkKills;
  }

  public int getNbKills() {
    return _nbKills;
  }

  public int getStageKills() {
    return _stageKills;
  }

  public boolean isActive()
  {
    return (_isActivated) || (_isDropped);
  }

  public int getLevel() {
    if (_nbKills > _stageKills * _skillMaxLevel)
    {
      return _skillMaxLevel;
    }

    return _nbKills / _stageKills;
  }

  public long getTimeLeft()
  {
    return _endTime - System.currentTimeMillis();
  }

  public void goTo(L2PcInstance player) {
    if (player == null) return;

    if (_isActivated)
    {
      player.teleToLocation(_player.getX(), _player.getY(), _player.getZ() + 20, true);
    } else if (_isDropped)
    {
      player.teleToLocation(_item.getX(), _item.getY(), _item.getZ() + 20, true);
    }
    else
      player.sendMessage(_name + " isn't in the World.");
  }

  public Point3D getWorldPosition()
  {
    if ((_isActivated) && (_player != null)) {
      return _player.getPosition().getWorldPosition();
    }
    if ((_isDropped) && (_item != null)) {
      return _item.getPosition().getWorldPosition();
    }
    return null;
  }

  private class RemoveTask
    implements Runnable
  {
    protected RemoveTask()
    {
    }

    public void run()
    {
      if (System.currentTimeMillis() >= getEndTime())
        endOfLife();
    }
  }
}