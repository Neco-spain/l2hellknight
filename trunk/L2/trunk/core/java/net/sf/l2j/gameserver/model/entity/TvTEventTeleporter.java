package net.sf.l2j.gameserver.model.entity;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.Rnd;

public class TvTEventTeleporter
  implements Runnable
{
  private L2PcInstance _playerInstance = null;

  private int[] _coordinates = new int[3];

  private boolean _adminRemove = false;
  private static FastMap<Integer, Integer[]> _oldPlayerPos;

  public TvTEventTeleporter(L2PcInstance playerInstance, int[] coordinates, boolean fastSchedule, boolean adminRemove)
  {
    _playerInstance = playerInstance;
    _coordinates = coordinates;
    _adminRemove = adminRemove;

    long delay = (TvTEvent.isStarted() ? Config.TVT_EVENT_RESPAWN_TELEPORT_DELAY : Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY) * 1000;

    ThreadPoolManager.getInstance().scheduleGeneral(this, fastSchedule ? 0L : delay);
  }

  public void run()
  {
    if (_playerInstance == null) 
    {
      return;
    }
    
    if (_playerInstance.getParty() != null)
    {
    	L2Party party = _playerInstance.getParty();
    	party.removePartyMember(_playerInstance);
    }
    
    L2Summon summon = _playerInstance.getPet();

    if (summon != null) 
    {
      summon.unSummon(_playerInstance);
    }
    
    _playerInstance.stopAllEffects();
    
    if (_playerInstance.isInDuel()) 
    {
      _playerInstance.setDuelState(4);
    }

    _playerInstance.doRevive(true);

    int objId = _playerInstance.getObjectId();

    if ((Config.TVT_RESTORE_PLAYER_POS) && (TvTEvent.isStarted()) && (!_adminRemove))
    {
      Integer[] oldCoords = { Integer.valueOf(_playerInstance.getX()), Integer.valueOf(_playerInstance.getY()), Integer.valueOf(_playerInstance.getZ()) };

      _oldPlayerPos.put(Integer.valueOf(objId), oldCoords);
    }

    if ((Config.TVT_RESTORE_PLAYER_POS) && (!TvTEvent.isStarted()))
    {
      Integer[] coor = (Integer[])_oldPlayerPos.get(Integer.valueOf(objId));

      if (coor != null)
      {
        _playerInstance.teleToLocation(coor[0].intValue(), coor[1].intValue(), coor[2].intValue(), false);
      }
      else
      {
        _playerInstance.teleToLocation(_coordinates[0] + Rnd.get(101) - 50, _coordinates[1] + Rnd.get(101) - 50, _coordinates[2], false);
      }
    }
    else {
      _playerInstance.teleToLocation(_coordinates[0] + Rnd.get(101) - 50, _coordinates[1] + Rnd.get(101) - 50, _coordinates[2], false);
    }
    if ((TvTEvent.isStarted()) && (!_adminRemove))
      _playerInstance.setTeam(TvTEvent.getParticipantTeamId(_playerInstance.getObjectId()) + 1);
    else {
      _playerInstance.setTeam(0);
    }
    if (_oldPlayerPos.containsKey(Integer.valueOf(objId))) {
      _oldPlayerPos.remove(Integer.valueOf(objId));
    }

    _playerInstance.setCurrentCp(_playerInstance.getMaxCp());
    _playerInstance.setCurrentHp(_playerInstance.getMaxHp());
    _playerInstance.setCurrentMp(_playerInstance.getMaxMp());

    if (_playerInstance.isMageClass())
    {
      L2Skill skill = SkillTable.getInstance().getInfo(1059, 3);
      skill.getEffects(_playerInstance, _playerInstance);
      skill = SkillTable.getInstance().getInfo(1062, 2);
      skill.getEffects(_playerInstance, _playerInstance);
      skill = SkillTable.getInstance().getInfo(1085, 3);
      skill.getEffects(_playerInstance, _playerInstance);
    }
    if (!_playerInstance.isMageClass())
    {
      L2Skill skill = SkillTable.getInstance().getInfo(1077, 3);
      skill.getEffects(_playerInstance, _playerInstance);
      skill = SkillTable.getInstance().getInfo(1086, 2);
      skill.getEffects(_playerInstance, _playerInstance);
      skill = SkillTable.getInstance().getInfo(1068, 3);
      skill.getEffects(_playerInstance, _playerInstance);
    }
    _playerInstance.broadcastStatusUpdate();
    _playerInstance.broadcastUserInfo();
  }

  public static void initializeRestoreMap()
  {
    if (_oldPlayerPos == null)
      _oldPlayerPos = new FastMap<Integer, Integer[]>();
  }

  public static void clearRestoreMap()
  {
    _oldPlayerPos.clear();
  }
}