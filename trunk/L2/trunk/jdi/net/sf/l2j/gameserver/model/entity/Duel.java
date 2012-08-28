package net.sf.l2j.gameserver.model.entity;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.instancemanager.DuelManager;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExDuelEnd;
import net.sf.l2j.gameserver.network.serverpackets.ExDuelReady;
import net.sf.l2j.gameserver.network.serverpackets.ExDuelStart;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class Duel
{
  protected static final Logger _log = Logger.getLogger(Duel.class.getName());
  public static final int DUELSTATE_NODUEL = 0;
  public static final int DUELSTATE_DUELLING = 1;
  public static final int DUELSTATE_DEAD = 2;
  public static final int DUELSTATE_WINNER = 3;
  public static final int DUELSTATE_INTERRUPTED = 4;
  private int _duelId;
  private L2PcInstance _playerA;
  private L2PcInstance _playerB;
  private boolean _partyDuel;
  private Calendar _duelEndTime;
  private int _surrenderRequest = 0;
  private int _countdown = 4;
  private boolean _finished = false;
  private FastList<PlayerCondition> _playerConditions;

  public Duel(L2PcInstance playerA, L2PcInstance playerB, int partyDuel, int duelId)
  {
    _duelId = duelId;
    _playerA = playerA;
    _playerB = playerB;
    _partyDuel = (partyDuel == 1);

    _duelEndTime = Calendar.getInstance();
    if (_partyDuel) _duelEndTime.add(13, 300); else {
      _duelEndTime.add(13, 120);
    }
    _playerConditions = new FastList();

    setFinished(false);

    if (_partyDuel)
    {
      _countdown += 1;

      SystemMessage sm = new SystemMessage(SystemMessageId.IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE);
      broadcastToTeam1(sm);
      broadcastToTeam2(sm);
    }

    ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartDuelTask(this), 3000L);
  }

  private void stopFighting()
  {
    ActionFailed af = new ActionFailed();
    if (_partyDuel)
    {
      for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
      {
        temp.abortCast();
        temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        temp.setTarget(null);
        temp.sendPacket(af);
      }
      for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
      {
        temp.abortCast();
        temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        temp.setTarget(null);
        temp.sendPacket(af);
      }
    }
    else
    {
      _playerA.abortCast();
      _playerB.abortCast();
      _playerA.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      _playerA.setTarget(null);
      _playerB.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      _playerB.setTarget(null);
      _playerA.sendPacket(af);
      _playerB.sendPacket(af);
    }
  }

  public boolean isDuelistInPvp(boolean sendMessage)
  {
    if (_partyDuel)
    {
      return false;
    }
    if ((_playerA.getPvpFlag() != 0) || (_playerB.getPvpFlag() != 0))
    {
      if (sendMessage)
      {
        String engagedInPvP = "The duel was canceled because a duelist engaged in PvP combat.";
        _playerA.sendMessage(engagedInPvP);
        _playerB.sendMessage(engagedInPvP);
      }
      return true;
    }
    return false;
  }

  public void startDuel()
  {
    savePlayerConditions();

    if ((_playerA.isInDuel()) || (_playerB.isInDuel()))
    {
      _playerConditions.clear();
      _playerConditions = null;
      DuelManager.getInstance().removeDuel(this);
      return;
    }

    if (_partyDuel)
    {
      for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
      {
        temp.cancelActiveTrade();
        temp.setIsInDuel(_duelId);
        temp.setTeam(1);
        temp.broadcastStatusUpdate();
        temp.broadcastUserInfo();
      }
      for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
      {
        temp.cancelActiveTrade();
        temp.setIsInDuel(_duelId);
        temp.setTeam(2);
        temp.broadcastStatusUpdate();
        temp.broadcastUserInfo();
      }

      ExDuelReady ready = new ExDuelReady(1);
      ExDuelStart start = new ExDuelStart(1);

      broadcastToTeam1(ready);
      broadcastToTeam2(ready);
      broadcastToTeam1(start);
      broadcastToTeam2(start);
    }
    else
    {
      _playerA.setIsInDuel(_duelId);
      _playerA.setTeam(1);
      _playerB.setIsInDuel(_duelId);
      _playerB.setTeam(2);

      ExDuelReady ready = new ExDuelReady(0);
      ExDuelStart start = new ExDuelStart(0);

      broadcastToTeam1(ready);
      broadcastToTeam2(ready);
      broadcastToTeam1(start);
      broadcastToTeam2(start);

      _playerA.broadcastStatusUpdate();
      _playerB.broadcastStatusUpdate();
      _playerA.broadcastUserInfo();
      _playerB.broadcastUserInfo();
    }

    PlaySound ps = new PlaySound(1, "B04_S01", 0, 0, 0, 0, 0);
    broadcastToTeam1(ps);
    broadcastToTeam2(ps);

    ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleDuelTask(this), 1000L);
  }

  public void savePlayerConditions()
  {
    if (_partyDuel)
    {
      for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
      {
        _playerConditions.add(new PlayerCondition(temp, _partyDuel));
      }
      for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
      {
        _playerConditions.add(new PlayerCondition(temp, _partyDuel));
      }
    }
    else
    {
      _playerConditions.add(new PlayerCondition(_playerA, _partyDuel));
      _playerConditions.add(new PlayerCondition(_playerB, _partyDuel));
    }
  }

  public void restorePlayerConditions(boolean abnormalDuelEnd)
  {
    if (_partyDuel)
    {
      for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
      {
        temp.setIsInDuel(0);
        temp.setTeam(0);
        temp.broadcastUserInfo();
      }
      for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
      {
        temp.setIsInDuel(0);
        temp.setTeam(0);
        temp.broadcastUserInfo();
      }
    }
    else
    {
      _playerA.setIsInDuel(0);
      _playerA.setTeam(0);
      _playerA.broadcastUserInfo();
      _playerB.setIsInDuel(0);
      _playerB.setTeam(0);
      _playerB.broadcastUserInfo();
    }

    if (abnormalDuelEnd) return;

    FastList.Node e = _playerConditions.head(); for (FastList.Node end = _playerConditions.tail(); (e = e.getNext()) != end; )
    {
      ((PlayerCondition)e.getValue()).restoreCondition();
    }
  }

  public int getId()
  {
    return _duelId;
  }

  public int getRemainingTime()
  {
    return (int)(_duelEndTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
  }

  public L2PcInstance getPlayerA()
  {
    return _playerA;
  }

  public L2PcInstance getPlayerB()
  {
    return _playerB;
  }

  public boolean isPartyDuel()
  {
    return _partyDuel;
  }

  public void setFinished(boolean mode)
  {
    _finished = mode;
  }

  public boolean getFinished()
  {
    return _finished;
  }

  public void teleportPlayers(int x, int y, int z)
  {
    if (!_partyDuel) return;
    int offset = 0;

    for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
    {
      temp.teleToLocation(x + offset - 180, y - 150, z);
      offset += 40;
    }
    offset = 0;
    for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
    {
      temp.teleToLocation(x + offset - 180, y + 150, z);
      offset += 40;
    }
  }

  public void broadcastToTeam1(L2GameServerPacket packet)
  {
    if (_playerA == null) return;

    if ((_partyDuel) && (_playerA.getParty() != null))
    {
      for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
        temp.sendPacket(packet);
    }
    else _playerA.sendPacket(packet);
  }

  public void broadcastToTeam2(L2GameServerPacket packet)
  {
    if (_playerB == null) return;

    if ((_partyDuel) && (_playerB.getParty() != null))
    {
      for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
        temp.sendPacket(packet);
    }
    else _playerB.sendPacket(packet);
  }

  public L2PcInstance getWinner()
  {
    if ((!getFinished()) || (_playerA == null) || (_playerB == null)) return null;
    if (_playerA.getDuelState() == 3) return _playerA;
    if (_playerB.getDuelState() == 3) return _playerB;
    return null;
  }

  public L2PcInstance getLooser()
  {
    if ((!getFinished()) || (_playerA == null) || (_playerB == null)) return null;
    if (_playerA.getDuelState() == 3) return _playerB;
    if (_playerA.getDuelState() == 3) return _playerA;
    return null;
  }

  public void playKneelAnimation()
  {
    L2PcInstance looser = getLooser();

    if (looser == null) return;

    if ((_partyDuel) && (looser.getParty() != null))
    {
      for (L2PcInstance temp : looser.getParty().getPartyMembers())
        temp.broadcastPacket(new SocialAction(temp.getObjectId(), 7));
    }
    else looser.broadcastPacket(new SocialAction(looser.getObjectId(), 7));
  }

  public int countdown()
  {
    _countdown -= 1;

    if (_countdown > 3) return _countdown;

    SystemMessage sm = null;
    if (_countdown > 0)
    {
      sm = new SystemMessage(SystemMessageId.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS);
      sm.addNumber(_countdown);
    } else {
      sm = new SystemMessage(SystemMessageId.LET_THE_DUEL_BEGIN);
    }
    broadcastToTeam1(sm);
    broadcastToTeam2(sm);

    return _countdown;
  }

  public void endDuel(DuelResultEnum result)
  {
    if ((_playerA == null) || (_playerB == null))
    {
      _playerConditions.clear();
      _playerConditions = null;
      DuelManager.getInstance().removeDuel(this);
      return;
    }

    SystemMessage sm = null;
    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$entity$Duel$DuelResultEnum[result.ordinal()])
    {
    case 1:
      restorePlayerConditions(false);

      if (_partyDuel) sm = new SystemMessage(SystemMessageId.S1S_PARTY_HAS_WON_THE_DUEL); else
        sm = new SystemMessage(SystemMessageId.S1_HAS_WON_THE_DUEL);
      sm.addString(_playerA.getName());

      broadcastToTeam1(sm);
      broadcastToTeam2(sm);
      break;
    case 2:
      restorePlayerConditions(false);

      if (_partyDuel) sm = new SystemMessage(SystemMessageId.S1S_PARTY_HAS_WON_THE_DUEL); else
        sm = new SystemMessage(SystemMessageId.S1_HAS_WON_THE_DUEL);
      sm.addString(_playerB.getName());

      broadcastToTeam1(sm);
      broadcastToTeam2(sm);
      break;
    case 3:
      restorePlayerConditions(false);

      if (_partyDuel) sm = new SystemMessage(SystemMessageId.SINCE_S1S_PARTY_WITHDREW_FROM_THE_DUEL_S1S_PARTY_HAS_WON); else
        sm = new SystemMessage(SystemMessageId.SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON);
      sm.addString(_playerA.getName());
      sm.addString(_playerB.getName());

      broadcastToTeam1(sm);
      broadcastToTeam2(sm);
      break;
    case 4:
      restorePlayerConditions(false);

      if (_partyDuel) sm = new SystemMessage(SystemMessageId.SINCE_S1S_PARTY_WITHDREW_FROM_THE_DUEL_S1S_PARTY_HAS_WON); else
        sm = new SystemMessage(SystemMessageId.SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON);
      sm.addString(_playerB.getName());
      sm.addString(_playerA.getName());

      broadcastToTeam1(sm);
      broadcastToTeam2(sm);
      break;
    case 5:
      stopFighting();

      restorePlayerConditions(true);

      sm = new SystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);

      broadcastToTeam1(sm);
      broadcastToTeam2(sm);
      break;
    case 6:
      stopFighting();

      restorePlayerConditions(false);

      sm = new SystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);

      broadcastToTeam1(sm);
      broadcastToTeam2(sm);
    }

    ExDuelEnd duelEnd = null;
    if (_partyDuel) duelEnd = new ExDuelEnd(1); else {
      duelEnd = new ExDuelEnd(0);
    }
    broadcastToTeam1(duelEnd);
    broadcastToTeam2(duelEnd);

    _playerConditions.clear();
    _playerConditions = null;
    DuelManager.getInstance().removeDuel(this);
  }

  public DuelResultEnum checkEndDuelCondition()
  {
    if ((_playerA == null) || (_playerB == null)) return DuelResultEnum.Canceled;

    if (_surrenderRequest != 0)
    {
      if (_surrenderRequest == 1) return DuelResultEnum.Team1Surrender;
      return DuelResultEnum.Team2Surrender;
    }

    if (getRemainingTime() <= 0)
    {
      return DuelResultEnum.Timeout;
    }

    if (_playerA.getDuelState() == 3)
    {
      stopFighting();
      return DuelResultEnum.Team1Win;
    }
    if (_playerB.getDuelState() == 3)
    {
      stopFighting();
      return DuelResultEnum.Team2Win;
    }

    if (!_partyDuel)
    {
      if ((_playerA.getDuelState() == 4) || (_playerB.getDuelState() == 4)) {
        return DuelResultEnum.Canceled;
      }

      if (!_playerA.isInsideRadius(_playerB, 1600, false, false)) return DuelResultEnum.Canceled;

      if (isDuelistInPvp(true)) return DuelResultEnum.Canceled;

      if ((_playerA.isInsideZone(2)) || (_playerB.isInsideZone(2)) || (_playerA.isInsideZone(4)) || (_playerB.isInsideZone(4)) || (_playerA.isInsideZone(1)) || (_playerB.isInsideZone(1)))
      {
        return DuelResultEnum.Canceled;
      }
    }
    return DuelResultEnum.Continue;
  }

  public void doSurrender(L2PcInstance player)
  {
    if (_surrenderRequest != 0) return;

    stopFighting();

    if (_partyDuel)
    {
      if (_playerA.getParty().getPartyMembers().contains(player))
      {
        _surrenderRequest = 1;
        for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
        {
          temp.setDuelState(2);
        }
        for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
        {
          temp.setDuelState(3);
        }
      }
      else if (_playerB.getParty().getPartyMembers().contains(player))
      {
        _surrenderRequest = 2;
        for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
        {
          temp.setDuelState(2);
        }
        for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
        {
          temp.setDuelState(3);
        }

      }

    }
    else if (player == _playerA)
    {
      _surrenderRequest = 1;
      _playerA.setDuelState(2);
      _playerB.setDuelState(3);
    }
    else if (player == _playerB)
    {
      _surrenderRequest = 2;
      _playerB.setDuelState(2);
      _playerA.setDuelState(3);
    }
  }

  public void onPlayerDefeat(L2PcInstance player)
  {
    player.setDuelState(2);

    if (_partyDuel)
    {
      boolean teamdefeated = true;
      for (L2PcInstance temp : player.getParty().getPartyMembers())
      {
        if (temp.getDuelState() == 1)
        {
          teamdefeated = false;
          break;
        }
      }

      if (teamdefeated)
      {
        L2PcInstance winner = _playerA;
        if (_playerA.getParty().getPartyMembers().contains(player)) winner = _playerB;

        for (L2PcInstance temp : winner.getParty().getPartyMembers())
        {
          temp.setDuelState(3);
        }
      }
    }
    else
    {
      if ((player != _playerA) && (player != _playerB)) _log.warning("Error in onPlayerDefeat(): player is not part of this 1vs1 duel");

      if (_playerA == player) _playerB.setDuelState(3); else
        _playerA.setDuelState(3);
    }
  }

  public void onRemoveFromParty(L2PcInstance player)
  {
    if (!_partyDuel) return;

    if ((player == _playerA) || (player == _playerB))
    {
      FastList.Node e = _playerConditions.head(); for (FastList.Node end = _playerConditions.tail(); (e = e.getNext()) != end; )
      {
        ((PlayerCondition)e.getValue()).teleportBack();
        ((PlayerCondition)e.getValue()).getPlayer().setIsInDuel(0);
      }

      _playerA = null; _playerB = null;
    }
    else
    {
      FastList.Node e = _playerConditions.head(); for (FastList.Node end = _playerConditions.tail(); (e = e.getNext()) != end; )
      {
        if (((PlayerCondition)e.getValue()).getPlayer() != player)
          continue;
        ((PlayerCondition)e.getValue()).teleportBack();
        _playerConditions.remove(e.getValue());
      }

      player.setIsInDuel(0);
    }
  }

  public void onBuff(L2PcInstance player, L2Effect debuff)
  {
    FastList.Node e = _playerConditions.head(); for (FastList.Node end = _playerConditions.tail(); (e = e.getNext()) != end; )
    {
      if (((PlayerCondition)e.getValue()).getPlayer() != player)
        continue;
      ((PlayerCondition)e.getValue()).registerDebuff(debuff);
      return;
    }
  }

  public class ScheduleEndDuelTask
    implements Runnable
  {
    private Duel _duel;
    private Duel.DuelResultEnum _result;

    public ScheduleEndDuelTask(Duel duel, Duel.DuelResultEnum result)
    {
      _duel = duel;
      _result = result;
    }

    public void run()
    {
      try
      {
        _duel.endDuel(_result);
      }
      catch (Throwable t)
      {
      }
    }
  }

  public class ScheduleStartDuelTask
    implements Runnable
  {
    private Duel _duel;

    public ScheduleStartDuelTask(Duel duel)
    {
      _duel = duel;
    }

    public void run()
    {
      try
      {
        int count = _duel.countdown();

        if (count == 4)
        {
          _duel.teleportPlayers(Config.DUEL_SPAWN_X, Config.DUEL_SPAWN_Y, Config.DUEL_SPAWN_Z);

          ThreadPoolManager.getInstance().scheduleGeneral(this, 20000L);
        }
        else if (count > 0)
        {
          ThreadPoolManager.getInstance().scheduleGeneral(this, 1000L);
        } else {
          _duel.startDuel();
        }
      }
      catch (Throwable t)
      {
      }
    }
  }

  public class ScheduleDuelTask
    implements Runnable
  {
    private Duel _duel;

    public ScheduleDuelTask(Duel duel)
    {
      _duel = duel;
    }

    public void run()
    {
      try
      {
        Duel.DuelResultEnum status = _duel.checkEndDuelCondition();

        if (status == Duel.DuelResultEnum.Canceled)
        {
          setFinished(true);
          _duel.endDuel(status);
        }
        else if (status != Duel.DuelResultEnum.Continue)
        {
          setFinished(true);
          playKneelAnimation();
          ThreadPoolManager.getInstance().scheduleGeneral(new Duel.ScheduleEndDuelTask(Duel.this, _duel, status), 5000L);
        } else {
          ThreadPoolManager.getInstance().scheduleGeneral(this, 1000L);
        }
      }
      catch (Throwable t)
      {
      }
    }
  }

  public class PlayerCondition
  {
    private L2PcInstance _player;
    private double _hp;
    private double _mp;
    private double _cp;
    private boolean _paDuel;
    private int _x;
    private int _y;
    private int _z;
    private FastList<L2Effect> _debuffs;

    public PlayerCondition(L2PcInstance player, boolean partyDuel)
    {
      if (player == null) return;
      _player = player;
      _hp = _player.getCurrentHp();
      _mp = _player.getCurrentMp();
      _cp = _player.getCurrentCp();
      _paDuel = partyDuel;

      if (_paDuel)
      {
        _x = _player.getX();
        _y = _player.getY();
        _z = _player.getZ();
      }
    }

    public void restoreCondition()
    {
      if (_player == null) return;
      _player.setCurrentHp(_hp);
      _player.setCurrentMp(_mp);
      _player.setCurrentCp(_cp);

      if (_paDuel)
      {
        teleportBack();
      }
      if (_debuffs != null)
      {
        for (L2Effect temp : _debuffs)
          if (temp != null) temp.exit();
      }
    }

    public void registerDebuff(L2Effect debuff)
    {
      if (_debuffs == null) {
        _debuffs = new FastList();
      }
      _debuffs.add(debuff);
    }

    public void teleportBack()
    {
      if (_paDuel) _player.teleToLocation(_x, _y, _z);
    }

    public L2PcInstance getPlayer()
    {
      return _player;
    }
  }

  public static enum DuelResultEnum
  {
    Continue, 
    Team1Win, 
    Team2Win, 
    Team1Surrender, 
    Team2Surrender, 
    Canceled, 
    Timeout;
  }
}