package net.sf.l2j.gameserver.model.entity;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.Rnd;

public class TvTEventTeleporter
  implements Runnable
{
  private L2PcInstance _playerInstance;
  private int[] _coordinates = new int[3];
  private boolean _adminRemove;

  public TvTEventTeleporter(L2PcInstance playerInstance, int[] coordinates, boolean fastSchedule, boolean adminRemove)
  {
    _playerInstance = playerInstance;
    _coordinates = coordinates;
    _adminRemove = adminRemove;

    long delay = (TvTEvent.isStarted() ? Config.TVT_EVENT_RESPAWN_TELEPORT_DELAY : Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY) * 1000;

    if (fastSchedule) {
      delay = 0L;
    }
    ThreadPoolManager.getInstance().scheduleGeneral(this, delay);
  }

  public void run()
  {
    if (_playerInstance == null) {
      return;
    }
    L2Summon summon = _playerInstance.getPet();

    if (summon != null) {
      summon.unSummon(_playerInstance);
    }
    _playerInstance.stopAllEffects();
    _playerInstance.doRevive();

    if ((TvTEvent.isStarted()) && (!_adminRemove))
    {
      _playerInstance.setChannel(8);
      _playerInstance.setTeam(TvTEvent.getParticipantTeamId(_playerInstance.getName()) + 1);
    }
    else
    {
      _playerInstance.setChannel(1);
      _playerInstance.setTeam(0);
    }

    _playerInstance.setCurrentCp(_playerInstance.getMaxCp());
    _playerInstance.setCurrentHp(_playerInstance.getMaxHp());
    _playerInstance.setCurrentMp(_playerInstance.getMaxMp());

    _playerInstance.teleToLocationEvent(_coordinates[0] + Rnd.get(100), _coordinates[1] + Rnd.get(100), _coordinates[2], false);

    _playerInstance.broadcastStatusUpdate();
  }
}