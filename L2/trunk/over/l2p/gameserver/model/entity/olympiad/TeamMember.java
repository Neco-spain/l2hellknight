package l2p.gameserver.model.entity.olympiad;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Summon;
import l2p.gameserver.model.base.TeamType;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.entity.events.impl.DuelEvent;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.serverpackets.ExAutoSoulShot;
import l2p.gameserver.serverpackets.ExOlympiadMatchEnd;
import l2p.gameserver.serverpackets.ExOlympiadMode;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.Revive;
import l2p.gameserver.serverpackets.SkillCoolTime;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.skills.EffectType;
import l2p.gameserver.skills.TimeStamp;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.InstantZone;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamMember
{
  private static final Logger _log = LoggerFactory.getLogger(TeamMember.class);

  private String _name = "";
  private String _clanName = "";
  private int _classId;
  private double _damage;
  private boolean _isDead;
  private final int _objId;
  private final OlympiadGame _game;
  private final CompType _type;
  private final int _side;
  private Player _player;
  private Location _returnLoc = null;

  public boolean isDead()
  {
    return _isDead;
  }

  public void doDie()
  {
    _isDead = true;
  }

  public TeamMember(int obj_id, String name, Player player, OlympiadGame game, int side)
  {
    _objId = obj_id;
    _name = name;
    _game = game;
    _type = game.getType();
    _side = side;

    _player = player;
    if (_player == null) {
      return;
    }
    _clanName = (player.getClan() == null ? "" : player.getClan().getName());
    _classId = player.getActiveClassId();

    player.setOlympiadSide(side);
    player.setOlympiadGame(game);
  }

  public StatsSet getStat()
  {
    return (StatsSet)Olympiad._nobles.get(Integer.valueOf(_objId));
  }

  public void incGameCount()
  {
    StatsSet set = getStat();
    switch (1.$SwitchMap$l2p$gameserver$model$entity$olympiad$CompType[_type.ordinal()])
    {
    case 1:
      set.set("game_team_count", set.getInteger("game_team_count") + 1);
      break;
    case 2:
      set.set("game_classes_count", set.getInteger("game_classes_count") + 1);
      break;
    case 3:
      set.set("game_noclasses_count", set.getInteger("game_noclasses_count") + 1);
    }
  }

  public void takePointsForCrash()
  {
    if (!checkPlayer())
    {
      StatsSet stat = getStat();
      int points = stat.getInteger("olympiad_points");
      int diff = Math.min(10, points / _type.getLooseMult());
      stat.set("olympiad_points", points - diff);
      Log.add("Olympiad Result: " + _name + " lost " + diff + " points for crash", "olympiad");

      Player player = _player;
      if (player == null) {
        Log.add("Olympiad info: " + _name + " crashed coz player == null", "olympiad");
      }
      else {
        if (player.isLogoutStarted())
          Log.add("Olympiad info: " + _name + " crashed coz player.isLogoutStarted()", "olympiad");
        if (!player.isConnected())
          Log.add("Olympiad info: " + _name + " crashed coz !player.isOnline()", "olympiad");
        if (player.getOlympiadGame() == null)
          Log.add("Olympiad info: " + _name + " crashed coz player.getOlympiadGame() == null", "olympiad");
        if (player.getOlympiadObserveGame() != null)
          Log.add("Olympiad info: " + _name + " crashed coz player.getOlympiadObserveGame() != null", "olympiad");
      }
    }
  }

  public boolean checkPlayer()
  {
    Player player = _player;

    return (player != null) && (!player.isLogoutStarted()) && (player.getOlympiadGame() != null) && (!player.isInObserverMode());
  }

  public void portPlayerToArena()
  {
    Player player = _player;
    if ((!checkPlayer()) || (player.isTeleporting()))
    {
      _player = null;
      return;
    }

    DuelEvent duel = (DuelEvent)player.getEvent(DuelEvent.class);
    if (duel != null) {
      duel.abortDuel(player);
    }
    _returnLoc = (player._stablePoint == null ? player.getReflection().getReturnLoc() : player.getReflection().getReturnLoc() == null ? player.getLoc() : player._stablePoint);

    if (player.isDead())
      player.setPendingRevive(true);
    if (player.isSitting()) {
      player.standUp();
    }
    player.setTarget(null);
    player.setIsInOlympiadMode(true);

    player.leaveParty();

    Reflection ref = _game.getReflection();
    InstantZone instantZone = ref.getInstancedZone();

    Location tele = Location.findPointToStay((Location)instantZone.getTeleportCoords().get(_side - 1), 50, 50, ref.getGeoIndex());

    player._stablePoint = _returnLoc;
    player.teleToLocation(tele, ref);

    if (_type == CompType.TEAM) {
      player.setTeam(_side == 1 ? TeamType.BLUE : TeamType.RED);
    }
    player.sendPacket(new ExOlympiadMode(_side));
  }

  public void portPlayerBack()
  {
    Player player = _player;
    if (player == null) {
      return;
    }
    if (_returnLoc == null) {
      return;
    }
    player.setIsInOlympiadMode(false);
    player.setOlympiadSide(-1);
    player.setOlympiadGame(null);

    if (_type == CompType.TEAM) {
      player.setTeam(TeamType.NONE);
    }

    for (Effect e : player.getEffectList().getAllEffects()) {
      if ((e.getEffectType() != EffectType.Cubic) || (player.getSkillLevel(Integer.valueOf(e.getSkill().getId())) <= 0))
        e.exit();
    }
    if (player.getPet() != null) {
      player.getPet().getEffectList().stopAllEffects();
    }
    player.setCurrentCp(player.getMaxCp());
    player.setCurrentMp(player.getMaxMp());

    if (player.isDead())
    {
      player.setCurrentHp(player.getMaxHp(), true);
      player.broadcastPacket(new L2GameServerPacket[] { new Revive(player) });
    }
    else {
      player.setCurrentHp(player.getMaxHp(), false);
    }

    if ((player.getClan() != null) && (player.getClan().getReputationScore() >= 0)) {
      player.getClan().enableSkills(player);
    }

    if (player.isHero()) {
      Hero.addSkills(player);
    }

    player.sendPacket(new SkillList(player));
    player.sendPacket(new ExOlympiadMode(0));
    player.sendPacket(new ExOlympiadMatchEnd());

    player._stablePoint = null;
    player.teleToLocation(_returnLoc, ReflectionManager.DEFAULT);
  }

  public void preparePlayer()
  {
    Player player = _player;
    if (player == null) {
      return;
    }
    if (player.isInObserverMode()) {
      if (player.getOlympiadObserveGame() != null)
        player.leaveOlympiadObserverMode(true);
      else {
        player.leaveObserverMode();
      }
    }
    if (player.getClan() != null) {
      player.getClan().disableSkills(player);
    }

    if (player.isHero()) {
      Hero.removeSkills(player);
    }

    if (player.isCastingNow()) {
      player.abortCast(true, true);
    }

    for (Effect e : player.getEffectList().getAllEffects()) {
      if ((e.getEffectType() != EffectType.Cubic) || (player.getSkillLevel(Integer.valueOf(e.getSkill().getId())) <= 0)) {
        e.exit();
      }
    }
    if (player.getPet() != null)
    {
      Summon summon = player.getPet();
      if (summon.isPet())
        summon.unSummon();
      else {
        summon.getEffectList().stopAllEffects();
      }
    }

    if (player.getAgathionId() > 0) {
      player.setAgathion(0);
    }

    for (TimeStamp sts : player.getSkillReuses())
    {
      if (sts == null)
        continue;
      Skill skill = SkillTable.getInstance().getInfo(sts.getId(), sts.getLevel());
      if (skill == null)
        continue;
      if (sts.getReuseBasic() <= 900000L) {
        player.enableSkill(skill);
      }
    }

    player.sendPacket(new SkillList(player));

    player.sendPacket(new SkillCoolTime(player));

    ItemInstance wpn = player.getActiveWeaponInstance();
    if ((wpn != null) && (wpn.isHeroWeapon()))
    {
      player.getInventory().unEquipItem(wpn);
      player.abortAttack(true, true);
    }

    Set activeSoulShots = player.getAutoSoulShot();
    for (Iterator i$ = activeSoulShots.iterator(); i$.hasNext(); ) { int itemId = ((Integer)i$.next()).intValue();

      player.removeAutoSoulShot(Integer.valueOf(itemId));
      player.sendPacket(new ExAutoSoulShot(itemId, false));
    }

    ItemInstance weapon = player.getActiveWeaponInstance();
    if (weapon != null)
    {
      weapon.setChargedSpiritshot(0);
      weapon.setChargedSoulshot(0);
    }

    player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
    player.setCurrentCp(player.getMaxCp());
    player.broadcastUserInfo(true);
  }

  public void saveNobleData()
  {
    OlympiadDatabase.saveNobleData(_objId);
  }

  public void logout()
  {
    _player = null;
  }

  public Player getPlayer()
  {
    return _player;
  }

  public String getName()
  {
    return _name;
  }

  public void addDamage(double d)
  {
    _damage += d;
  }

  public double getDamage()
  {
    return _damage;
  }

  public String getClanName()
  {
    return _clanName;
  }

  public int getClassId()
  {
    return _classId;
  }

  public int getObjectId()
  {
    return _objId;
  }
}