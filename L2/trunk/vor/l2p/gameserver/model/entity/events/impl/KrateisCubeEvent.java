package l2p.gameserver.model.entity.events.impl;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.lang.ArrayUtils;
import l2p.commons.time.cron.SchedulingPattern;
import l2p.commons.util.Rnd;
import l2p.gameserver.data.xml.holder.EventHolder;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.listener.actor.OnKillListener;
import l2p.gameserver.listener.actor.player.OnPlayerExitListener;
import l2p.gameserver.listener.actor.player.OnTeleportListener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.base.RestartType;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.entity.events.EventType;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.model.entity.events.objects.KrateisCubePlayerObject;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.ExPVPMatchCCMyRecord;
import l2p.gameserver.serverpackets.ExPVPMatchCCRecord;
import l2p.gameserver.serverpackets.ExPVPMatchCCRetire;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.utils.Location;

public class KrateisCubeEvent extends GlobalEvent
{
  private static final SchedulingPattern DATE_PATTERN = new SchedulingPattern("0,30 * * * *");
  private static final Location RETURN_LOC = new Location(-70381, -70937, -1428);
  private static final int[] SKILL_IDS = { 1086, 1204, 1059, 1085, 1078, 1068, 1240, 1077, 1242, 1062, 5739 };
  private static final int[] SKILL_LEVEL = { 2, 2, 3, 3, 6, 3, 3, 3, 3, 2, 1 };
  public static final String PARTICLE_PLAYERS = "particle_players";
  public static final String REGISTERED_PLAYERS = "registered_players";
  public static final String WAIT_LOCS = "wait_locs";
  public static final String TELEPORT_LOCS = "teleport_locs";
  public static final String PREPARE = "prepare";
  private final int _minLevel;
  private final int _maxLevel;
  private Calendar _calendar = Calendar.getInstance();
  private KrateisCubeRunnerEvent _runnerEvent;
  private Listeners _listeners = new Listeners(null);

  public KrateisCubeEvent(MultiValueSet<String> set)
  {
    super(set);
    _minLevel = set.getInteger("min_level");
    _maxLevel = set.getInteger("max_level");
  }

  public void initEvent()
  {
    _runnerEvent = ((KrateisCubeRunnerEvent)EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 2));

    super.initEvent();
  }

  public void prepare()
  {
    NpcInstance npc = _runnerEvent.getNpc();
    List registeredPlayers = removeObjects("registered_players");
    List waitLocs = getObjects("wait_locs");
    for (KrateisCubePlayerObject k : registeredPlayers)
    {
      if (npc.getDistance(k.getPlayer()) > 800.0D) {
        continue;
      }
      addObject("particle_players", k);

      Player player = k.getPlayer();

      player.teleToLocation((Location)Rnd.get(waitLocs), ReflectionManager.DEFAULT);
    }
  }

  public void startEvent()
  {
    super.startEvent();

    List players = getObjects("particle_players");
    List teleportLocs = getObjects("teleport_locs");

    for (int i = 0; i < players.size(); i++)
    {
      KrateisCubePlayerObject k = (KrateisCubePlayerObject)players.get(i);
      Player player = k.getPlayer();

      player.getEffectList().stopAllEffects();

      giveEffects(player);

      player.teleToLocation((Location)teleportLocs.get(i));
      player.addEvent(this);

      player.sendPacket(new IStaticPacket[] { new ExPVPMatchCCMyRecord(k), SystemMsg.THE_MATCH_HAS_STARTED });
    }
  }

  public void stopEvent()
  {
    super.stopEvent();
    reCalcNextTime(false);

    double dif = 0.05D;
    int pos = 0;

    List players = removeObjects("particle_players");
    for (KrateisCubePlayerObject krateisPlayer : players)
    {
      Player player = krateisPlayer.getPlayer();
      pos++;
      if (krateisPlayer.getPoints() >= 10)
      {
        int count = (int)(krateisPlayer.getPoints() * dif * (1.0D + players.size() / pos * 0.04D));
        dif -= 0.0016D;
        if (count > 0)
        {
          Functions.addItem(player, 13067, count);

          int exp = count * 2880;
          int sp = count * 288;
          player.addExpAndSp(exp, sp);
        }
      }

      player.removeEvent(this);

      player.sendPacket(new IStaticPacket[] { ExPVPMatchCCRetire.STATIC, SystemMsg.END_MATCH });
      player.teleToLocation(RETURN_LOC);
    }
  }

  private void giveEffects(Player player)
  {
    player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
    player.setCurrentCp(player.getMaxCp());

    for (int j = 0; j < SKILL_IDS.length; j++)
    {
      Skill skill = SkillTable.getInstance().getInfo(SKILL_IDS[j], SKILL_LEVEL[j]);
      if (skill != null)
        skill.getEffects(player, player, false, false);
    }
  }

  public void reCalcNextTime(boolean onInit)
  {
    clearActions();

    _calendar.setTimeInMillis(DATE_PATTERN.next(System.currentTimeMillis()));

    registerActions();
  }

  protected long startTimeMillis()
  {
    return _calendar.getTimeInMillis();
  }

  public boolean canRessurect(Player resurrectPlayer, Creature creature, boolean force)
  {
    resurrectPlayer.sendPacket(SystemMsg.INVALID_TARGET);
    return false;
  }

  public KrateisCubePlayerObject getRegisteredPlayer(Player player)
  {
    List registeredPlayers = getObjects("registered_players");
    for (KrateisCubePlayerObject p : registeredPlayers)
      if (p.getPlayer() == player)
        return p;
    return null;
  }

  public KrateisCubePlayerObject getParticlePlayer(Player player)
  {
    List registeredPlayers = getObjects("particle_players");
    for (KrateisCubePlayerObject p : registeredPlayers)
      if (p.getPlayer() == player)
        return p;
    return null;
  }

  public void showRank(Player player)
  {
    KrateisCubePlayerObject particlePlayer = getParticlePlayer(player);
    if ((particlePlayer == null) || (particlePlayer.isShowRank())) {
      return;
    }
    particlePlayer.setShowRank(true);

    player.sendPacket(new ExPVPMatchCCRecord(this));
  }

  public void closeRank(Player player)
  {
    KrateisCubePlayerObject particlePlayer = getParticlePlayer(player);
    if ((particlePlayer == null) || (!particlePlayer.isShowRank())) {
      return;
    }
    particlePlayer.setShowRank(false);
  }

  public void updatePoints(KrateisCubePlayerObject k)
  {
    k.getPlayer().sendPacket(new ExPVPMatchCCMyRecord(k));

    ExPVPMatchCCRecord p = new ExPVPMatchCCRecord(this);

    List players = getObjects("particle_players");
    for (KrateisCubePlayerObject $player : players)
    {
      if ($player.isShowRank())
        $player.getPlayer().sendPacket(p);
    }
  }

  public KrateisCubePlayerObject[] getSortedPlayers()
  {
    List players = getObjects("particle_players");
    KrateisCubePlayerObject[] array = (KrateisCubePlayerObject[])players.toArray(new KrateisCubePlayerObject[players.size()]);
    ArrayUtils.eqSort(array);
    return array;
  }

  public void exitCube(Player player, boolean teleport)
  {
    KrateisCubePlayerObject krateisCubePlayer = getParticlePlayer(player);
    krateisCubePlayer.stopRessurectTask();

    getObjects("particle_players").remove(krateisCubePlayer);

    player.sendPacket(ExPVPMatchCCRetire.STATIC);
    player.removeEvent(this);

    if (teleport)
      player.teleToLocation(RETURN_LOC);
  }

  public void announce(int a)
  {
    IStaticPacket p = null;
    if (a > 0)
      p = new SystemMessage2(SystemMsg.S1_SECONDS_TO_GAME_END).addInteger(a);
    else {
      p = new SystemMessage2(SystemMsg.THE_MATCH_WILL_START_IN_S1_SECONDS).addInteger(-a);
    }
    List players = getObjects("particle_players");
    for (KrateisCubePlayerObject $player : players)
      $player.getPlayer().sendPacket(p);
  }

  public boolean isParticle(Player player)
  {
    return getParticlePlayer(player) != null;
  }

  public void onAddEvent(GameObject o)
  {
    if (o.isPlayer())
      o.getPlayer().addListener(_listeners);
  }

  public void onRemoveEvent(GameObject o)
  {
    if (o.isPlayer())
      o.getPlayer().removeListener(_listeners);
  }

  public void action(String name, boolean start)
  {
    if (name.equalsIgnoreCase("prepare"))
      prepare();
    else
      super.action(name, start);
  }

  public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
  {
    r.clear();
  }

  public int getMinLevel()
  {
    return _minLevel;
  }

  public int getMaxLevel()
  {
    return _maxLevel;
  }

  public boolean isInProgress()
  {
    return _runnerEvent.isInProgress();
  }

  public boolean isRegistrationOver()
  {
    return _runnerEvent.isRegistrationOver();
  }

  private class Listeners
    implements OnKillListener, OnPlayerExitListener, OnTeleportListener
  {
    private Listeners()
    {
    }

    public void onKill(Creature actor, Creature victim)
    {
      if (!victim.isPlayer()) {
        return;
      }
      KrateisCubeEvent cubeEvent2 = (KrateisCubeEvent)victim.getEvent(KrateisCubeEvent.class);
      if (cubeEvent2 != KrateisCubeEvent.this) {
        return;
      }
      KrateisCubePlayerObject winnerPlayer = getParticlePlayer((Player)actor);

      winnerPlayer.setPoints(winnerPlayer.getPoints() + 5);
      updatePoints(winnerPlayer);

      KrateisCubePlayerObject looserPlayer = getParticlePlayer((Player)victim);

      looserPlayer.startRessurectTask();
    }

    public boolean ignorePetOrSummon()
    {
      return true;
    }

    public void onPlayerExit(Player player)
    {
      exitCube(player, false);
    }

    public void onTeleport(Player player, int x, int y, int z, Reflection reflection)
    {
      List waitLocs = getObjects("wait_locs");
      for (Location loc : waitLocs) {
        if ((loc.x == x) && (loc.y == y))
          return;
      }
      waitLocs = getObjects("teleport_locs");

      for (Location loc : waitLocs) {
        if ((loc.x == x) && (loc.y == y))
          return;
      }
      exitCube(player, false);
    }
  }
}