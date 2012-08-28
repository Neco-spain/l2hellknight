package l2p.gameserver.model.entity.events.impl;

import java.util.Iterator;
import java.util.List;
import l2p.commons.collections.JoinedIterator;
import l2p.commons.collections.MultiValueSet;
import l2p.gameserver.listener.actor.player.OnPlayerExitListener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Zone.ZoneType;
import l2p.gameserver.model.base.TeamType;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.model.entity.events.objects.DuelSnapshotObject;
import l2p.gameserver.serverpackets.ExDuelStart;
import l2p.gameserver.serverpackets.ExDuelUpdateUserInfo;
import l2p.gameserver.serverpackets.PlaySound;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;

public abstract class DuelEvent extends GlobalEvent
  implements Iterable<DuelSnapshotObject>
{
  public static final String RED_TEAM = TeamType.RED.name();
  public static final String BLUE_TEAM = TeamType.BLUE.name();

  protected OnPlayerExitListener _playerExitListener = new OnPlayerExitListenerImpl(null);
  protected TeamType _winner = TeamType.NONE;
  protected boolean _aborted;

  public DuelEvent(MultiValueSet<String> set)
  {
    super(set);
  }

  protected DuelEvent(int id, String name)
  {
    super(id, name);
  }
  public void initEvent() {
  }

  public abstract boolean canDuel(Player paramPlayer1, Player paramPlayer2, boolean paramBoolean);

  public abstract void askDuel(Player paramPlayer1, Player paramPlayer2);

  public abstract void createDuel(Player paramPlayer1, Player paramPlayer2);

  public abstract void playerExit(Player paramPlayer);

  public abstract void packetSurrender(Player paramPlayer);

  public abstract void onDie(Player paramPlayer);

  public abstract int getDuelType();

  public void startEvent() {
    updatePlayers(true, false);

    sendPackets(new IStaticPacket[] { new ExDuelStart(this), PlaySound.B04_S01, SystemMsg.LET_THE_DUEL_BEGIN });

    for (DuelSnapshotObject $snapshot : this)
      sendPacket(new ExDuelUpdateUserInfo($snapshot.getPlayer()), new String[] { $snapshot.getTeam().revert().name() });
  }

  public void sendPacket(IStaticPacket packet, String[] ar)
  {
    for (String a : ar)
    {
      List objs = getObjects(a);

      for (DuelSnapshotObject obj : objs)
        obj.getPlayer().sendPacket(packet);
    }
  }

  public void sendPacket(IStaticPacket packet)
  {
    sendPackets(new IStaticPacket[] { packet });
  }

  public void sendPackets(IStaticPacket[] packet)
  {
    for (DuelSnapshotObject d : this)
      d.getPlayer().sendPacket(packet);
  }

  public void abortDuel(Player player)
  {
    _aborted = true;
    _winner = TeamType.NONE;

    stopEvent();
  }

  protected IStaticPacket canDuel0(Player requestor, Player target)
  {
    IStaticPacket packet = null;
    if (target.isInCombat())
      packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE).addName(target);
    else if ((target.isDead()) || (target.isAlikeDead()) || (target.getCurrentHpPercents() < 50.0D) || (target.getCurrentMpPercents() < 50.0D) || (target.getCurrentCpPercents() < 50.0D))
      packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1S_HP_OR_MP_IS_BELOW_50).addName(target);
    else if (target.getEvent(DuelEvent.class) != null)
      packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL).addName(target);
    else if ((target.getEvent(ClanHallSiegeEvent.class) != null) || (target.getEvent(ClanHallNpcSiegeEvent.class) != null))
      packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_A_CLAN_HALL_WAR).addName(target);
    else if (target.getEvent(SiegeEvent.class) != null)
      packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_A_SIEGE_WAR).addName(target);
    else if (target.isInOlympiadMode())
      packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD).addName(target);
    else if ((target.isCursedWeaponEquipped()) || (target.getKarma() > 0) || (target.getPvpFlag() > 0))
      packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE).addName(target);
    else if (target.isInStoreMode())
      packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE).addName(target);
    else if ((target.isMounted()) || (target.isInBoat()))
      packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_STEED_OR_STRIDER).addName(target);
    else if (target.isFishing())
      packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING).addName(target);
    else if ((target.isInCombatZone()) || (target.isInPeaceZone()) || (target.isInWater()) || (target.isInZone(Zone.ZoneType.no_restart)))
      packet = new SystemMessage2(SystemMsg.C1_CANNOT_MAKE_A_CHALLENGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUELPROHIBITED_AREA_PEACEFUL_ZONE__SEVEN_SIGNS_ZONE__NEAR_WATER__RESTART_PROHIBITED_AREA).addName(target);
    else if (!requestor.isInRangeZ(target, 1200L))
      packet = new SystemMessage2(SystemMsg.C1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_C1_IS_TOO_FAR_AWAY).addName(target);
    else if (target.getTransformation() != 0)
      packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_POLYMORPHED).addName(target);
    return packet;
  }

  protected void updatePlayers(boolean start, boolean teleport)
  {
    for (DuelSnapshotObject $snapshot : this)
    {
      if (teleport) {
        $snapshot.teleport();
      }
      else if (start)
      {
        $snapshot.getPlayer().addEvent(this);
        $snapshot.getPlayer().setTeam($snapshot.getTeam());
      }
      else
      {
        $snapshot.getPlayer().removeEvent(this);
        $snapshot.restore(_aborted);

        $snapshot.getPlayer().setTeam(TeamType.NONE);
      }
    }
  }

  public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
  {
    if ((target.getTeam() == TeamType.NONE) || (attacker.getTeam() == TeamType.NONE) || (target.getTeam() == attacker.getTeam())) {
      return SystemMsg.INVALID_TARGET;
    }
    DuelEvent duelEvent = (DuelEvent)target.getEvent(DuelEvent.class);
    if ((duelEvent == null) || (duelEvent != this)) {
      return SystemMsg.INVALID_TARGET;
    }
    return null;
  }

  public boolean canAttack(Creature target, Creature attacker, Skill skill, boolean force)
  {
    if ((target.getTeam() == TeamType.NONE) || (attacker.getTeam() == TeamType.NONE) || (target.getTeam() == attacker.getTeam())) {
      return false;
    }
    DuelEvent duelEvent = (DuelEvent)target.getEvent(DuelEvent.class);
    return (duelEvent != null) && (duelEvent == this);
  }

  public void onAddEvent(GameObject o)
  {
    if (o.isPlayer())
      o.getPlayer().addListener(_playerExitListener);
  }

  public void onRemoveEvent(GameObject o)
  {
    if (o.isPlayer())
      o.getPlayer().removeListener(_playerExitListener);
  }

  public Iterator<DuelSnapshotObject> iterator()
  {
    List blue = getObjects(BLUE_TEAM);
    List red = getObjects(RED_TEAM);
    return new JoinedIterator(new Iterator[] { blue.iterator(), red.iterator() });
  }

  public void reCalcNextTime(boolean onInit)
  {
    registerActions();
  }

  public void announce(int i)
  {
    sendPacket(new SystemMessage2(SystemMsg.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS).addInteger(i));
  }

  private class OnPlayerExitListenerImpl
    implements OnPlayerExitListener
  {
    private OnPlayerExitListenerImpl()
    {
    }

    public void onPlayerExit(Player player)
    {
      playerExit(player);
    }
  }
}