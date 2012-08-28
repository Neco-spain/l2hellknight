package net.sf.l2j.gameserver.model.entity;

import java.util.Calendar;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.EventManager;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExDuelEnd;
import net.sf.l2j.gameserver.network.serverpackets.ExDuelReady;
import net.sf.l2j.gameserver.network.serverpackets.ExDuelStart;
import net.sf.l2j.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.util.log.AbstractLogger;

public class Duel
{
  protected static final Logger _log = AbstractLogger.getLogger(Duel.class.getName());
  private boolean _isPartyDuel;
  private Calendar _DuelEndTime;
  private int _surrenderRequest = 0;
  private int _countdown = 4;
  private boolean _finished = false;
  FastList<L2PcInstance> _team1 = new FastList();
  FastList<L2PcInstance> _team2 = new FastList();
  private FastMap<L2PcInstance, PlayerCondition> _playerConditions = new FastMap();

  public Duel(L2PcInstance playerA, L2PcInstance playerB, boolean partyDuel)
  {
    _isPartyDuel = partyDuel;

    _team1.add(playerA);
    _team2.add(playerB);

    _DuelEndTime = Calendar.getInstance();
    if (_isPartyDuel)
      _DuelEndTime.add(13, 300);
    else {
      _DuelEndTime.add(13, 120);
    }

    if (_isPartyDuel);
    savePlayerConditions();

    ThreadPoolManager.getInstance().scheduleAi(new ScheduleStartDuelTask(this), 3000L, true);
  }

  void stopFighting()
  {
    FastList.Node n = _team1.head(); for (FastList.Node end = _team1.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance temp = (L2PcInstance)n.getValue();
      if (temp == null) {
        continue;
      }
      stopFighting(temp);
    }

    FastList.Node n = _team2.head(); for (FastList.Node end = _team2.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance temp = (L2PcInstance)n.getValue();
      if (temp == null) {
        continue;
      }
      stopFighting(temp);
    }
  }

  public void stopFighting(L2PcInstance player)
  {
    if (player.isDead() == true) {
      player.setIsKilledAlready(true);
    }
    player.abortCast();
    player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    player.setTarget(null);
    if (player.getPet() != null) {
      player.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }
    player.sendActionFailed();
  }

  public boolean isDuelistInPvp(boolean sendMessage)
  {
    if (_isPartyDuel)
    {
      return false;
    }if ((((L2PcInstance)_team1.get(0)).getPvpFlag() != 0) || (((L2PcInstance)_team2.get(0)).getPvpFlag() != 0)) {
      if (sendMessage) {
        String engagedInPvP = "The duel was canceled because a duelist engaged in PvP combat.";
        ((L2PcInstance)_team1.get(0)).sendMessage(engagedInPvP);
        ((L2PcInstance)_team1.get(0)).sendMessage(engagedInPvP);
      }
      return true;
    }
    return false;
  }

  public void startDuel()
  {
    String name = null;

    FastList.Node n = _team1.head(); for (FastList.Node end = _team1.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance temp = (L2PcInstance)n.getValue();
      if (temp == null) {
        continue;
      }
      if (temp.getDuel() != null) {
        name = temp.getName();
        break;
      }
    }
    FastList.Node n;
    if (name == null) {
      n = _team2.head(); for (FastList.Node end = _team2.tail(); (n = n.getNext()) != end; ) {
        L2PcInstance temp = (L2PcInstance)n.getValue();
        if (temp == null) {
          continue;
        }
        if (temp.getDuel() != null) {
          name = temp.getName();
          break;
        }
      }
    }

    if (name != null) {
      SystemMessage sm = SystemMessage.id(SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD).addString(name);
      FastList.Node n = _team1.head(); for (FastList.Node end = _team1.tail(); (n = n.getNext()) != end; ) {
        L2PcInstance temp = (L2PcInstance)n.getValue();
        if (temp == null) {
          continue;
        }
        temp.sendPacket(sm);
      }
      FastList.Node n = _team2.head(); for (FastList.Node end = _team2.tail(); (n = n.getNext()) != end; ) {
        L2PcInstance temp = (L2PcInstance)n.getValue();
        if (temp == null) {
          continue;
        }
        temp.sendPacket(sm);
      }
      sm = null;
      return;
    }

    FastList.Node n = _team1.head(); for (FastList.Node end = _team1.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance temp = (L2PcInstance)n.getValue();
      if (temp == null) {
        continue;
      }
      temp.cancelActiveTrade();
      temp.setDuel(this);
      getPlayerCondition(temp).setDuelState(DuelState.Fighting);
      temp.setTeam(1);
      temp.broadcastStatusUpdate();
      broadcastToOppositTeam(temp, new ExDuelUpdateUserInfo(temp));
    }
    FastList.Node n = _team2.head(); for (FastList.Node end = _team2.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance temp = (L2PcInstance)n.getValue();
      if (temp == null) {
        continue;
      }
      temp.cancelActiveTrade();
      temp.setDuel(this);
      temp.setTeam(2);
      getPlayerCondition(temp).setDuelState(DuelState.Fighting);
      temp.broadcastStatusUpdate();
      broadcastToOppositTeam(temp, new ExDuelUpdateUserInfo(temp));
    }

    ExDuelReady ready = new ExDuelReady(_isPartyDuel ? 1 : 0);
    ExDuelStart start = new ExDuelStart(_isPartyDuel ? 1 : 0);

    broadcastToTeam1(ready);
    broadcastToTeam2(ready);
    broadcastToTeam1(start);
    broadcastToTeam2(start);

    PlaySound ps = new PlaySound("B04_S01");
    broadcastToTeam1(ps);
    broadcastToTeam2(ps);

    ThreadPoolManager.getInstance().scheduleAi(new ScheduleDuelTask(this), 1000L, true);
  }

  public void savePlayerConditions()
  {
    FastList.Node n = _team1.head(); for (FastList.Node end = _team1.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance temp = (L2PcInstance)n.getValue();
      if (temp == null) {
        continue;
      }
      _playerConditions.put(temp, new PlayerCondition(temp, _isPartyDuel));
    }
    FastList.Node n = _team2.head(); for (FastList.Node end = _team2.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance temp = (L2PcInstance)n.getValue();
      if (temp == null) {
        continue;
      }
      _playerConditions.put(temp, new PlayerCondition(temp, _isPartyDuel));
    }
  }

  public void restorePlayerConditions(boolean abnormalDuelEnd)
  {
    FastList.Node n = _team1.head(); for (FastList.Node end = _team1.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance temp = (L2PcInstance)n.getValue();
      if (temp == null) {
        continue;
      }
      temp.setDuel(null);
      temp.setTeam(0);
    }
    FastList.Node n = _team2.head(); for (FastList.Node end = _team2.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance temp = (L2PcInstance)n.getValue();
      if (temp == null) {
        continue;
      }
      temp.setDuel(null);
      temp.setTeam(0);
    }

    FastMap.Entry e = _playerConditions.head(); for (FastMap.Entry end = _playerConditions.tail(); (e = e.getNext()) != end; ) {
      PlayerCondition pc = (PlayerCondition)e.getValue();
      if (pc == null) {
        continue;
      }
      pc.RestoreCondition(abnormalDuelEnd);
    }
  }

  public int getRemainingTime()
  {
    return (int)(_DuelEndTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
  }

  public L2PcInstance getPlayerA()
  {
    return (L2PcInstance)_team1.get(0);
  }

  public L2PcInstance getPlayerB()
  {
    return (L2PcInstance)_team2.get(0);
  }

  public boolean isPartyDuel()
  {
    return _isPartyDuel;
  }

  public void setFinished(boolean mode) {
    _finished = mode;
  }

  public boolean isFinished() {
    return _finished;
  }

  public void teleportPlayers(int x, int y, int z)
  {
    if (!_isPartyDuel) {
      return;
    }
    int offset = 0;

    FastList.Node n = _team1.head(); for (FastList.Node end = _team1.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance temp = (L2PcInstance)n.getValue();
      if (temp == null) {
        continue;
      }
      temp.teleToLocation(x + offset - 180, y - 150, z);
      offset += 40;
    }
    offset = 0;
    FastList.Node n = _team2.head(); for (FastList.Node end = _team2.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance temp = (L2PcInstance)n.getValue();
      if (temp == null) {
        continue;
      }
      temp.teleToLocation(x + offset - 180, y + 150, z);
      offset += 40;
    }
  }

  public void broadcastToTeam1(L2GameServerPacket packet)
  {
    FastList.Node n = _team1.head(); for (FastList.Node end = _team1.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance temp = (L2PcInstance)n.getValue();
      if (temp == null) {
        continue;
      }
      temp.sendPacket(packet);
    }
  }

  public void broadcastToTeam2(L2GameServerPacket packet)
  {
    FastList.Node n = _team2.head(); for (FastList.Node end = _team2.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance temp = (L2PcInstance)n.getValue();
      if (temp == null) {
        continue;
      }
      temp.sendPacket(packet);
    }
  }

  public L2PcInstance getWinner()
  {
    if ((!isFinished()) || (_team1.size() == 0) || (_team2.size() == 0)) {
      return null;
    }
    if (((PlayerCondition)_playerConditions.get(_team1.get(0))).getDuelState() == DuelState.Winner) {
      return (L2PcInstance)_team1.get(0);
    }
    if (((PlayerCondition)_playerConditions.get(_team2.get(0))).getDuelState() == DuelState.Winner) {
      return (L2PcInstance)_team2.get(0);
    }
    return null;
  }

  public FastList<L2PcInstance> getLoosers()
  {
    if ((!isFinished()) || (_team1.get(0) == null) || (_team2.get(0) == null)) {
      return null;
    }
    if (((PlayerCondition)_playerConditions.get(_team1.get(0))).getDuelState() == DuelState.Winner) {
      return _team2;
    }
    if (((PlayerCondition)_playerConditions.get(_team2.get(0))).getDuelState() == DuelState.Winner) {
      return _team1;
    }
    return null;
  }

  public void playKneelAnimation()
  {
    FastList loosers = getLoosers();
    if ((loosers == null) || (loosers.size() == 0)) {
      return;
    }

    FastList.Node n = loosers.head(); for (FastList.Node end = loosers.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance looser = (L2PcInstance)n.getValue();
      if (looser == null) {
        continue;
      }
      looser.broadcastPacket(new SocialAction(looser.getObjectId(), 7));
    }
  }

  public int Countdown()
  {
    _countdown -= 1;

    if (_countdown > 3) {
      return _countdown;
    }

    SystemMessage sm = null;
    if (_countdown > 0)
      sm = SystemMessage.id(SystemMessageId.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS).addNumber(_countdown);
    else {
      sm = Static.LET_THE_DUEL_BEGIN;
    }

    broadcastToTeam1(sm);
    broadcastToTeam2(sm);
    sm = null;
    return _countdown;
  }

  public void endDuel(DuelResultEnum result)
  {
    if ((_team1.get(0) == null) || (_team2.get(0) == null))
    {
      _log.warning("Duel: Duel end with null players.");
      _playerConditions.clear();
      _playerConditions = null;
      return;
    }

    SystemMessage sm = null;
    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$entity$Duel$DuelResultEnum[result.ordinal()]) {
    case 2:
      restorePlayerConditions(false);

      if (_isPartyDuel)
        sm = SystemMessage.id(SystemMessageId.S1S_PARTY_HAS_WON_THE_DUEL);
      else {
        sm = SystemMessage.id(SystemMessageId.S1_HAS_WON_THE_DUEL);
      }
      sm.addString(((L2PcInstance)_team1.get(0)).getName());

      broadcastToTeam1(sm);
      broadcastToTeam2(sm);
      break;
    case 3:
      restorePlayerConditions(false);

      if (_isPartyDuel)
        sm = SystemMessage.id(SystemMessageId.S1S_PARTY_HAS_WON_THE_DUEL);
      else {
        sm = SystemMessage.id(SystemMessageId.S1_HAS_WON_THE_DUEL);
      }
      sm.addString(((L2PcInstance)_team2.get(0)).getName());

      broadcastToTeam1(sm);
      broadcastToTeam2(sm);
      break;
    case 4:
      restorePlayerConditions(false);

      if (_isPartyDuel)
        sm = SystemMessage.id(SystemMessageId.SINCE_S1S_PARTY_WITHDREW_FROM_THE_DUEL_S1S_PARTY_HAS_WON);
      else {
        sm = SystemMessage.id(SystemMessageId.SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON);
      }
      sm.addString(((L2PcInstance)_team2.get(0)).getName()).addString(((L2PcInstance)_team1.get(0)).getName());

      broadcastToTeam1(sm);
      broadcastToTeam2(sm);
      break;
    case 5:
      restorePlayerConditions(false);

      if (_isPartyDuel)
        sm = SystemMessage.id(SystemMessageId.SINCE_S1S_PARTY_WITHDREW_FROM_THE_DUEL_S1S_PARTY_HAS_WON);
      else {
        sm = SystemMessage.id(SystemMessageId.SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON);
      }
      sm.addString(((L2PcInstance)_team2.get(0)).getName()).addString(((L2PcInstance)_team1.get(0)).getName());

      broadcastToTeam1(sm);
      broadcastToTeam2(sm);
      break;
    case 6:
      restorePlayerConditions(true);

      sm = Static.THE_DUEL_HAS_ENDED_IN_A_TIE;

      broadcastToTeam1(sm);
      broadcastToTeam2(sm);
      break;
    case 7:
      stopFighting();

      restorePlayerConditions(false);

      sm = Static.THE_DUEL_HAS_ENDED_IN_A_TIE;

      broadcastToTeam1(sm);
      broadcastToTeam2(sm);
    }

    sm = null;

    ExDuelEnd duelEnd = new ExDuelEnd(_isPartyDuel ? 1 : 0);

    broadcastToTeam1(duelEnd);
    broadcastToTeam2(duelEnd);

    _playerConditions.clear();
    _playerConditions = null;
  }

  public DuelResultEnum checkEndDuelCondition()
  {
    if ((_team1.get(0) == null) || (_team2.get(0) == null)) {
      return DuelResultEnum.Canceled;
    }

    if (_surrenderRequest != 0) {
      if (_surrenderRequest == 1) {
        return DuelResultEnum.Team1Surrender;
      }
      return DuelResultEnum.Team2Surrender;
    }
    if (getRemainingTime() <= 0) {
      return DuelResultEnum.Timeout;
    }

    if (((PlayerCondition)_playerConditions.get(_team1.get(0))).getDuelState() == DuelState.Winner) {
      return DuelResultEnum.Team1Win;
    }
    if (((PlayerCondition)_playerConditions.get(_team2.get(0))).getDuelState() == DuelState.Winner) {
      return DuelResultEnum.Team2Win;
    }
    if (!_isPartyDuel)
    {
      if ((((PlayerCondition)_playerConditions.get(_team1.get(0))).getDuelState() == DuelState.Interrupted) || (((PlayerCondition)_playerConditions.get(_team2.get(0))).getDuelState() == DuelState.Interrupted)) {
        return DuelResultEnum.Canceled;
      }

      if (!((L2PcInstance)_team1.get(0)).isInsideRadius((L2Object)_team2.get(0), 1600, false, false)) {
        return DuelResultEnum.Canceled;
      }

      if (isDuelistInPvp(true)) {
        return DuelResultEnum.Canceled;
      }

      if ((((L2PcInstance)_team1.get(0)).isInZonePeace()) || (((L2PcInstance)_team2.get(0)).isInZonePeace())) {
        return DuelResultEnum.Canceled;
      }
    }

    return DuelResultEnum.Continue;
  }

  public void doSurrender(L2PcInstance player)
  {
    if (_surrenderRequest != 0) {
      return;
    }

    if (getTeamForPlayer(player) == null) {
      _log.warning("Error handling duel surrender request by " + player.getName());
      return;
    }

    if (_team1.contains(player)) {
      _surrenderRequest = 1;
      for (L2PcInstance temp : _team1) {
        setDuelState(temp, DuelState.Dead);
      }

      for (L2PcInstance temp : _team2)
        setDuelState(temp, DuelState.Winner);
    }
    else if (_team2.contains(player)) {
      _surrenderRequest = 2;
      for (L2PcInstance temp : _team2) {
        setDuelState(temp, DuelState.Dead);
      }

      for (L2PcInstance temp : _team1)
        setDuelState(temp, DuelState.Winner);
    }
  }

  public void onPlayerDefeat(L2PcInstance player)
  {
    setDuelState(player, DuelState.Dead);

    if (_isPartyDuel) {
      boolean teamdefeated = true;
      FastList team = getTeamForPlayer(player);
      for (L2PcInstance temp : getTeamForPlayer(player)) {
        if (getDuelState(temp) == DuelState.Fighting) {
          teamdefeated = false;
          break;
        }
      }

      if (teamdefeated)
      {
        team = team == _team1 ? _team2 : _team1;
        for (L2PcInstance temp : team)
          setDuelState(temp, DuelState.Winner);
      }
    }
    else {
      if ((player != _team1.get(0)) && (player != _team2.get(0))) {
        _log.warning("Error in onPlayerDefeat(): player is not part of this 1vs1 duel");
      }

      if (_team1.get(0) == player)
        setDuelState((L2PcInstance)_team2.get(0), DuelState.Winner);
      else
        setDuelState((L2PcInstance)_team1.get(0), DuelState.Winner);
    }
  }

  public void onRemoveFromParty(L2PcInstance player)
  {
    if (!_isPartyDuel)
      return;
    FastMap.Entry e;
    if ((player == _team1.get(0)) || (player == _team2.get(0))) {
      e = _playerConditions.head(); for (FastMap.Entry end = _playerConditions.tail(); (e = e.getNext()) != end; ) {
        PlayerCondition pc = (PlayerCondition)e.getValue();
        if (pc == null) {
          continue;
        }
        pc.TeleportBack();
        pc.getPlayer().setDuel(null);
      }
    }
    else {
      PlayerCondition pc = (PlayerCondition)_playerConditions.get(player);

      if (pc == null) {
        _log.warning("Duel: Error, can't get player condition from list.");
        return;
      }

      pc.TeleportBack();
      _playerConditions.remove(player);

      if (_team1.contains(player))
        _team1.remove(player);
      else if (_team2.contains(player)) {
        _team2.remove(player);
      }

      player.setDuel(null);
    }
  }

  public PlayerCondition getPlayerCondition(L2PcInstance player)
  {
    return (PlayerCondition)_playerConditions.get(player);
  }

  public DuelState getDuelState(L2PcInstance player)
  {
    return ((PlayerCondition)_playerConditions.get(player)).getDuelState();
  }

  public void setDuelState(L2PcInstance player, DuelState state)
  {
    ((PlayerCondition)_playerConditions.get(player)).setDuelState(state);
  }

  public void broadcastToOppositTeam(L2PcInstance player, L2GameServerPacket packet)
  {
    if (_team1.contains(player))
      broadcastToTeam2(packet);
    else if (_team2.contains(player))
      broadcastToTeam1(packet);
    else
      _log.warning("Duel: Broadcast by player who is not in duel");
  }

  public FastList<L2PcInstance> getTeamForPlayer(L2PcInstance p)
  {
    if (_team1.contains(p))
      return _team1;
    if (_team2.contains(p)) {
      return _team2;
    }
    _log.warning("Duel: got request for player team who is not duel participant");
    return null;
  }

  public static void createDuel(L2PcInstance playerA, L2PcInstance playerB, int partyDuel)
  {
    if ((playerA == null) || (playerB == null) || (playerA.getDuel() != null) || (playerB.getDuel() != null)) {
      return;
    }

    String engagedInPvP = "The duel was canceled because a duelist engaged in PvP combat.";
    if (partyDuel == 1) {
      boolean playerInPvP = false;
      for (L2PcInstance temp : playerA.getParty().getPartyMembers()) {
        if (temp.getPvpFlag() != 0) {
          playerInPvP = true;
          break;
        }
      }
      if (!playerInPvP) {
        for (L2PcInstance temp : playerB.getParty().getPartyMembers()) {
          if (temp.getPvpFlag() != 0) {
            playerInPvP = true;
            break;
          }
        }
      }

      if (playerInPvP) {
        for (L2PcInstance temp : playerA.getParty().getPartyMembers()) {
          temp.sendMessage(engagedInPvP);
        }
        for (L2PcInstance temp : playerB.getParty().getPartyMembers()) {
          temp.sendMessage(engagedInPvP);
        }
        return;
      }
    } else if ((playerA.getPvpFlag() != 0) || (playerB.getPvpFlag() != 0)) {
      playerA.sendMessage(engagedInPvP);
      playerB.sendMessage(engagedInPvP);
      return;
    }

    new Duel(playerA, playerB, partyDuel == 1);
  }

  public static boolean checkIfCanDuel(L2PcInstance requestor, L2PcInstance target, boolean sendMessage) {
    SystemMessage _noDuelReason = null;
    if ((target.isInCombat()) || (target.isInJail()))
    {
      _noDuelReason = Static.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
    } else if ((target.isDead()) || (target.isAlikeDead()))
    {
      _noDuelReason = Static.S1_CANNOT_DUEL_BECAUSE_S1S_HP_OR_MP_IS_BELOW_50_PERCENT;
    } else if (target.isInDuel())
      _noDuelReason = Static.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL;
    else if (target.isInOlympiadMode())
      _noDuelReason = Static.S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
    else if ((target.isCursedWeaponEquiped()) || (target.getKarma() > 0) || (target.getPvpFlag() > 0))
      _noDuelReason = Static.S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE;
    else if (target.getPrivateStoreType() != 0)
      _noDuelReason = Static.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
    else if ((target.isMounted()) || (target.isInBoat()))
      _noDuelReason = Static.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER;
    else if (target.isFishing())
      _noDuelReason = Static.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING;
    else if ((target.isInsideZone(1)) || (target.isInsideZone(2)) || (target.isInsideZone(4)))
      _noDuelReason = Static.S1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
    else if (!requestor.isInsideRadius(target, 250, false, false))
      _noDuelReason = Static.S1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_S1_IS_TOO_FAR_AWAY;
    else if ((!EventManager.getInstance().checkPlayer(requestor)) || (!EventManager.getInstance().checkPlayer(target))) {
      _noDuelReason = Static.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL;
    }

    if ((sendMessage) && (_noDuelReason != null)) {
      if (requestor != target)
        requestor.sendPacket(_noDuelReason);
      else {
        requestor.sendPacket(Static.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
      }
    }
    return _noDuelReason == null;
  }

  public void onBuff(L2PcInstance player, L2Effect debuff) {
    PlayerCondition pcon = (PlayerCondition)_playerConditions.get(player);

    if (pcon != null)
      pcon.registerDebuff(debuff);
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

    public void run() {
      try {
        _duel.endDuel(_result);
      } catch (Throwable t) {
        Duel._log.warning("Duel: Can't end duel " + t);
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
      try {
        int count = _duel.Countdown();

        if (count == 4)
        {
          _duel.teleportPlayers(-102495, -209023, -3326);

          ThreadPoolManager.getInstance().scheduleAi(this, 20000L, true);
        } else if (count > 0) {
          ThreadPoolManager.getInstance().scheduleAi(this, 1000L, true);
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

    public void run() {
      try {
        Duel.DuelResultEnum status = _duel.checkEndDuelCondition();

        switch (Duel.1.$SwitchMap$net$sf$l2j$gameserver$model$entity$Duel$DuelResultEnum[status.ordinal()]) {
        case 1:
          ThreadPoolManager.getInstance().scheduleAi(this, 1000L, true);
          break;
        case 2:
        case 3:
        case 4:
        case 5:
          setFinished(true);
          playKneelAnimation();
        case 6:
        case 7:
          setFinished(true);

          for (L2PcInstance p : _team1) {
            p.setTeam(0);
          }
          for (L2PcInstance p : _team2) {
            p.setTeam(0);
          }

          ThreadPoolManager.getInstance().scheduleGeneral(new Duel.ScheduleEndDuelTask(Duel.this, _duel, status), 5000L);
          stopFighting();

          break;
        default:
          Duel._log.info("Error with duel end.");
        }
      } catch (Throwable t) {
        Duel._log.warning("Can't continue duel" + t);
        t.printStackTrace(System.out);
      }
    }
  }

  public static class PlayerCondition
  {
    private L2PcInstance _player;
    private double _hp;
    private double _mp;
    private double _cp;
    private boolean _paDuel;
    private int _x;
    private int _y;
    private int _z;
    private Duel.DuelState _duelState;
    private FastList<L2Effect> _debuffs;

    public PlayerCondition(L2PcInstance player, boolean partyDuel)
    {
      if (player == null) {
        return;
      }
      _player = player;
      _hp = _player.getCurrentHp();
      _mp = _player.getCurrentMp();
      _cp = _player.getCurrentCp();
      _paDuel = partyDuel;

      if (_paDuel) {
        _x = _player.getX();
        _y = _player.getY();
        _z = _player.getZ();
      }
    }

    public void registerDebuff(L2Effect debuff) {
      if (_debuffs == null) {
        _debuffs = new FastList();
      }

      _debuffs.add(debuff);
    }

    public void RestoreCondition(boolean abnormalEnd) {
      if (_player == null) {
        return;
      }

      if (_debuffs != null) {
        for (L2Effect e : _debuffs) {
          if (e != null) {
            e.exit();
          }
        }
      }

      for (L2Effect e : _player.getAllEffects()) {
        if (e == null)
        {
          continue;
        }
        if (e.getSkill().isOffensive()) {
          e.exit();
        }

      }

      if ((!abnormalEnd) && (!_player.isDead())) {
        _player.setCurrentHp(_hp);
        _player.setCurrentMp(_mp);
        _player.setCurrentCp(_cp);
      }

      if (_paDuel)
        TeleportBack();
    }

    public void TeleportBack()
    {
      if (_paDuel)
        _player.teleToLocation(_x, _y, _z);
    }

    public L2PcInstance getPlayer()
    {
      return _player;
    }

    public void setDuelState(Duel.DuelState d) {
      _duelState = d;
    }

    public Duel.DuelState getDuelState() {
      return _duelState;
    }
  }

  public static enum DuelState
  {
    Winner, 
    Looser, 
    Fighting, 
    Dead, 
    Interrupted;
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