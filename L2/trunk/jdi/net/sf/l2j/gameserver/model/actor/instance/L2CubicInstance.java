package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.handler.skillhandlers.Continuous;
import net.sf.l2j.gameserver.handler.skillhandlers.Disablers;
import net.sf.l2j.gameserver.handler.skillhandlers.Mdam;
import net.sf.l2j.gameserver.instancemanager.DuelManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.TvTEventTeam;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillDrain;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.util.Rnd;

public class L2CubicInstance
{
  protected static final Logger _log = Logger.getLogger(L2CubicInstance.class.getName());
  public static final int STORM_CUBIC = 1;
  public static final int VAMPIRIC_CUBIC = 2;
  public static final int LIFE_CUBIC = 3;
  public static final int VIPER_CUBIC = 4;
  public static final int POLTERGEIST_CUBIC = 5;
  public static final int BINDING_CUBIC = 6;
  public static final int AQUA_CUBIC = 7;
  public static final int SPARK_CUBIC = 8;
  public static final int ATTRACT_CUBIC = 9;
  public static final int SMART_CUBIC_EVATEMPLAR = 10;
  public static final int SMART_CUBIC_SHILLIENTEMPLAR = 11;
  public static final int SMART_CUBIC_ARCANALORD = 12;
  public static final int SMART_CUBIC_ELEMENTALMASTER = 13;
  public static final int SMART_CUBIC_SPECTRALMASTER = 14;
  public static final int MAX_MAGIC_RANGE = 900;
  public static final int SKILL_CUBIC_HEAL = 4051;
  public static final int SKILL_CUBIC_CURE = 5579;
  protected L2PcInstance _owner;
  protected L2Character _target;
  protected int _id;
  protected int _matk;
  protected int _activationtime;
  protected int _activationchance;
  protected boolean _active;
  protected List<L2Skill> _skills = new FastList();
  private Future<?> _disappearTask;
  private Future<?> _actionTask;

  public L2CubicInstance(L2PcInstance owner, int id, int level, int mAtk, int activationtime, int activationchance)
  {
    _owner = owner;
    _id = id;
    _matk = mAtk;
    _activationtime = (activationtime * 1000);
    _activationchance = activationchance;
    _active = false;

    switch (_id)
    {
    case 1:
      _skills.add(SkillTable.getInstance().getInfo(4049, level));
      break;
    case 2:
      _skills.add(SkillTable.getInstance().getInfo(4050, level));
      break;
    case 3:
      _skills.add(SkillTable.getInstance().getInfo(4051, level));
      _disappearTask = ThreadPoolManager.getInstance().scheduleGeneral(new Disappear(), 3600000L);
      doAction();
      break;
    case 4:
      _skills.add(SkillTable.getInstance().getInfo(4052, level));
      break;
    case 5:
      _skills.add(SkillTable.getInstance().getInfo(4053, level));
      _skills.add(SkillTable.getInstance().getInfo(4054, level));
      _skills.add(SkillTable.getInstance().getInfo(4055, level));
      break;
    case 6:
      _skills.add(SkillTable.getInstance().getInfo(4164, level));
      break;
    case 7:
      _skills.add(SkillTable.getInstance().getInfo(4165, level));
      break;
    case 8:
      _skills.add(SkillTable.getInstance().getInfo(4166, level));
      break;
    case 9:
      _skills.add(SkillTable.getInstance().getInfo(5115, level));
      _skills.add(SkillTable.getInstance().getInfo(5116, level));
      break;
    case 12:
      _skills.add(SkillTable.getInstance().getInfo(4051, 7));

      _skills.add(SkillTable.getInstance().getInfo(4165, 9));

      break;
    case 13:
      _skills.add(SkillTable.getInstance().getInfo(4049, 8));

      _skills.add(SkillTable.getInstance().getInfo(4166, 9));

      break;
    case 14:
      _skills.add(SkillTable.getInstance().getInfo(4049, 8));

      _skills.add(SkillTable.getInstance().getInfo(4052, 6));

      break;
    case 10:
      _skills.add(SkillTable.getInstance().getInfo(4053, 8));

      _skills.add(SkillTable.getInstance().getInfo(4165, 9));

      break;
    case 11:
      _skills.add(SkillTable.getInstance().getInfo(4049, 8));

      _skills.add(SkillTable.getInstance().getInfo(5115, 4));
    }

    if (_disappearTask == null)
      _disappearTask = ThreadPoolManager.getInstance().scheduleGeneral(new Disappear(), 1200000L);
  }

  public void doAction()
  {
    if (_active) return;
    _active = true;

    switch (_id)
    {
    case 1:
    case 2:
    case 4:
    case 5:
    case 6:
    case 7:
    case 8:
    case 9:
    case 10:
    case 11:
    case 12:
    case 13:
    case 14:
      _actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(_activationchance), 0L, _activationtime);
      break;
    case 3:
      _actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Heal(13, 33, 53), 0L, _activationtime);
    }
  }

  public int getId()
  {
    return _id;
  }

  public L2PcInstance getOwner() {
    return _owner;
  }

  public final int getMCriticalHit(L2Character target, L2Skill skill) {
    return _owner.getTemplate().baseMCritRate;
  }

  public int getMAtk() {
    return _matk;
  }

  public void stopAction()
  {
    _target = null;
    _active = false;
    if (_actionTask != null)
    {
      _actionTask.cancel(true);
      _actionTask = null;
    }
  }

  public void cancelDisappear()
  {
    if (_disappearTask != null)
    {
      _disappearTask.cancel(true);
      _disappearTask = null;
    }
  }

  public void getCubicTarget()
  {
    try
    {
      if ((TvTEvent.isStarted()) && (TvTEvent.isPlayerParticipant(_owner.getObjectId())))
      {
        TvTEventTeam enemyTeam = TvTEvent.getParticipantEnemyTeam(_owner.getObjectId());

        if ((_owner.getTarget() instanceof L2PcInstance))
        {
          if ((enemyTeam.containsPlayer(_owner.getTarget().getObjectId())) && (!((L2PcInstance)_owner.getTarget()).isDead()))
          {
            _target = ((L2Character)_owner.getTarget());
            return;
          }
        }
        else if ((_owner.getTarget() instanceof L2Summon))
        {
          if ((enemyTeam.containsPlayer(((L2Summon)_owner.getTarget()).getOwner().getObjectId())) && (!((L2Summon)_owner.getTarget()).isDead()))
          {
            _target = ((L2Character)_owner.getTarget());
            return;
          }
        }
        List potentialTarget = new FastList();

        for (L2PcInstance enemy : enemyTeam.getParticipatedPlayers().values())
        {
          if ((isInCubicRange(_owner, enemy)) && (!enemy.isDead()))
            potentialTarget.add(enemy);
          if ((enemy.getPet() != null) && 
            (isInCubicRange(_owner, enemy.getPet())) && (!enemy.getPet().isDead()))
            potentialTarget.add(enemy.getPet());
        }
        if (potentialTarget.size() == 0)
        {
          _target = null;
          return;
        }

        int choice = Rnd.nextInt(potentialTarget.size());
        _target = ((L2Character)potentialTarget.get(choice));
        return;
      }

      if (_owner.isInDuel())
      {
        L2PcInstance PlayerA = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerA();
        L2PcInstance PlayerB = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerB();
        _target = null;

        if (DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
        {
          L2Party partyA = PlayerA.getParty();
          L2Party partyB = PlayerB.getParty();
          L2Party partyEnemy = null;

          if (partyA != null)
          {
            if (partyA.getPartyMembers().contains(_owner)) {
              if (partyB != null)
                partyEnemy = partyB;
              else
                _target = PlayerB;
            }
            else partyEnemy = partyA;

          }
          else if (PlayerA == _owner) {
            if (partyB != null)
              partyEnemy = partyB;
            else
              _target = PlayerB;
          }
          else _target = PlayerA;

          if ((_target == PlayerA) || (_target == PlayerB)) return;
          if (partyEnemy != null)
          {
            if (partyEnemy.getPartyMembers().contains(_owner.getTarget()))
              _target = ((L2Character)_owner.getTarget());
            else
              _target = ((L2Character)partyEnemy.getPartyMembers().get(Rnd.get(partyEnemy.getPartyMembers().size())));
            return;
          }
        }
        if (PlayerA != _owner)
        {
          _target = PlayerA;
          return;
        }

        _target = PlayerB;
        return;
      }

      if (_owner.isInOlympiadMode())
      {
        _target = null;
        if (_owner.isOlympiadStart())
        {
          L2PcInstance[] players = Olympiad.getInstance().getPlayers(_owner.getOlympiadGameId());
          if (players != null)
          {
            if (_owner.getOlympiadSide() == 1)
              _target = players[1];
            else
              _target = players[0];
          }
        }
        return;
      }
      L2PcInstance enemy = null;
      if ((_owner.getTarget() != null) && 
        ((_owner.getTarget() instanceof L2Character)) && (_owner.getTarget() != _owner.getPet()) && (_owner.getTarget() != _owner))
      {
        if ((_owner.getTarget() instanceof L2Attackable))
        {
          if ((((L2Attackable)_owner.getTarget()).getAggroListRP().get(_owner) != null) && (!((L2Attackable)_owner.getTarget()).isDead()))
          {
            _target = ((L2Character)_owner.getTarget());
            return;
          }
          if ((_owner.getPet() != null) && 
            (((L2Attackable)_owner.getTarget()).getAggroListRP().get(_owner.getPet()) != null) && (!((L2Attackable)_owner.getTarget()).isDead()))
          {
            _target = ((L2Character)_owner.getTarget());
            return;
          }
        }

        enemy = null;

        if (((_owner.getPvpFlag() > 0) && (!_owner.isInsideZone(2))) || (_owner.isInsideZone(4)) || (_owner.isInsideZone(1)))
        {
          if ((_owner.getTarget() instanceof L2Summon))
          {
            if (!((L2Summon)_owner.getTarget()).isDead())
              enemy = ((L2Summon)_owner.getTarget()).getOwner();
          }
          else if ((_owner.getTarget() instanceof L2PcInstance))
          {
            if (!((L2PcInstance)_owner.getTarget()).isDead()) {
              enemy = (L2PcInstance)_owner.getTarget();
            }
          }
          if (enemy != null)
          {
            boolean targetIt = true;

            if (_owner.getParty() != null)
            {
              if (_owner.getParty().getPartyMembers().contains(enemy))
                targetIt = false;
              else if (_owner.getParty().getCommandChannel() != null)
              {
                if (_owner.getParty().getCommandChannel().getMembers().contains(enemy))
                  targetIt = false;
              }
            }
            if ((_owner.getClan() != null) && (!_owner.isInsideZone(1)))
            {
              if (_owner.getClan().isMember(enemy.getName()))
                targetIt = false;
              if ((_owner.getAllyId() > 0) && (enemy.getAllyId() > 0))
              {
                if (_owner.getAllyId() == enemy.getAllyId())
                  targetIt = false;
              }
            }
            if ((enemy.getPvpFlag() == 0) && (!enemy.isInsideZone(4)) && (!enemy.isInsideZone(1)))
              targetIt = false;
            if (enemy.isInsideZone(2))
              targetIt = false;
            if ((_owner.getSiegeState() == enemy.getSiegeState()) && (_owner.getSiegeState() > 0)) {
              targetIt = false;
            }
            if (targetIt)
            {
              _target = ((L2Character)_owner.getTarget());
              return;
            }
          }
        }
      }
      List potentialTarget = new FastList();
      List potentialPvPTarget = new FastList();
      Collection knownTarget = _owner.getKnownList().getKnownCharactersInRadius(900L);

      for (L2Character tgMob : knownTarget)
      {
        if ((tgMob instanceof L2Attackable))
        {
          if (!((L2Attackable)tgMob).isDead()) {
            if (((L2Attackable)tgMob).getAggroListRP().get(_owner) != null)
              potentialTarget.add(tgMob);
            if ((_owner.getPet() != null) && 
              (((L2Attackable)tgMob).getAggroListRP().get(_owner.getPet()) != null))
              potentialTarget.add(tgMob);
          }
        } else if (((_owner.getPvpFlag() > 0) && (!_owner.isInsideZone(2))) || (_owner.isInsideZone(4)) || (_owner.isInsideZone(1)))
        {
          enemy = null;
          if ((tgMob instanceof L2Summon))
          {
            if (!((L2Summon)tgMob).isDead())
              enemy = ((L2Summon)tgMob).getOwner();
          }
          else if ((tgMob instanceof L2PcInstance))
          {
            if (!((L2PcInstance)tgMob).isDead()) {
              enemy = (L2PcInstance)tgMob;
            }
          }
          if (enemy != null)
          {
            boolean targetIt = true;
            if (_owner.getParty() != null)
            {
              if (_owner.getParty().getPartyMembers().contains(enemy))
                targetIt = false;
              else if (_owner.getParty().getCommandChannel() != null)
              {
                if (_owner.getParty().getCommandChannel().getMembers().contains(enemy)) {
                  targetIt = false;
                }
              }
            }
            if ((_owner.getClan() != null) && (!_owner.isInsideZone(1)))
            {
              if (_owner.getClan().isMember(enemy.getName()))
                targetIt = false;
              if ((_owner.getAllyId() > 0) && (enemy.getAllyId() > 0))
              {
                if (_owner.getAllyId() == enemy.getAllyId())
                  targetIt = false;
              }
            }
            if ((enemy.getPvpFlag() == 0) && (!enemy.isInsideZone(4)) && (!enemy.isInsideZone(1)))
              targetIt = false;
            if (enemy.isInsideZone(2))
              targetIt = false;
            if ((_owner.getSiegeState() == enemy.getSiegeState()) && (_owner.getSiegeState() > 0)) {
              targetIt = false;
            }
            if (targetIt)
              potentialPvPTarget.add(tgMob);
          }
        }
      }
      if (potentialPvPTarget.size() > 0)
      {
        int choice = Rnd.nextInt(potentialPvPTarget.size());
        _target = ((L2Character)potentialPvPTarget.get(choice));
        return;
      }
      if (potentialTarget.size() == 0)
      {
        _target = null;
        return;
      }
      int choice = Rnd.nextInt(potentialTarget.size());
      _target = ((L2Character)potentialTarget.get(choice));
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "", e);
    }
  }

  public boolean isInCubicRange(L2Character owner, L2Character target)
  {
    if ((owner == null) || (target == null)) return false;

    int range = 900;

    int x = owner.getX() - target.getX();
    int y = owner.getY() - target.getY();
    int z = owner.getZ() - target.getZ();

    return x * x + y * y + z * z <= range * range;
  }

  public void CubicTargetForHeal()
  {
    L2Character target = null;
    double percentleft = 100.0D;
    L2Party party = _owner.getParty();

    if ((_owner.isInDuel()) && 
      (!DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())) {
      party = null;
    }
    if ((party != null) && (!_owner.isInOlympiadMode()))
    {
      List partyList = party.getPartyMembers();
      for (L2Character partyMember : partyList)
      {
        if (!partyMember.isDead())
        {
          if (isInCubicRange(_owner, partyMember))
          {
            if (partyMember.getCurrentHp() < partyMember.getMaxHp())
            {
              if (percentleft > partyMember.getCurrentHp() / partyMember.getMaxHp())
              {
                percentleft = partyMember.getCurrentHp() / partyMember.getMaxHp();
                target = partyMember;
              }
            }
          }
        }
        if (partyMember.getPet() != null)
        {
          if ((partyMember.getPet().isDead()) || 
            (!isInCubicRange(_owner, partyMember.getPet())))
            continue;
          if (partyMember.getPet().getCurrentHp() < partyMember.getPet().getMaxHp())
          {
            if (percentleft > partyMember.getPet().getCurrentHp() / partyMember.getPet().getMaxHp())
            {
              percentleft = partyMember.getPet().getCurrentHp() / partyMember.getPet().getMaxHp();
              target = partyMember.getPet();
            }
          }
        }
      }
    }
    else
    {
      if (_owner.getCurrentHp() < _owner.getMaxHp())
      {
        percentleft = _owner.getCurrentHp() / _owner.getMaxHp();
        target = _owner;
      }
      if ((_owner.getPet() != null) && 
        (!_owner.getPet().isDead()) && (_owner.getPet().getCurrentHp() < _owner.getPet().getMaxHp()) && (percentleft > _owner.getPet().getCurrentHp() / _owner.getPet().getMaxHp()) && (isInCubicRange(_owner, _owner.getPet())))
      {
        target = _owner.getPet();
        percentleft = _owner.getPet().getCurrentHp() / _owner.getPet().getMaxHp();
      }
    }

    _target = target;
  }

  private class Disappear
    implements Runnable
  {
    Disappear()
    {
    }

    public void run()
    {
      stopAction();
      _owner.delCubic(_id);
      _owner.broadcastUserInfo();
    }
  }

  private class Heal
    implements Runnable
  {
    private int _chanceAb60;
    private int _chance3060;
    private int _chanceBe30;

    Heal(int chance60, int chance3060, int chance30)
    {
      _chanceAb60 = chance60;
      _chance3060 = chance3060;
      _chanceBe30 = chance30;
    }

    public void run()
    {
      if ((_owner.isDead()) && (_owner.isOnline() == 0))
      {
        stopAction();
        _owner.delCubic(_id);
        _owner.broadcastUserInfo();
        cancelDisappear();
        return;
      }
      try
      {
        L2Skill skill = null;
        for (L2Skill sk : _skills)
        {
          if (sk.getId() == 4051)
          {
            skill = sk;
            break;
          }
        }

        if (skill != null)
        {
          CubicTargetForHeal();
          L2Character target = _target;
          if ((target != null) && (!target.isDead()))
          {
            double percentleft = target.getCurrentHp() / target.getMaxHp() * 100.0D;

            int typeHeal = 1;

            if ((percentleft <= 60.0D) && (percentleft > 30.0D))
              typeHeal = 2;
            else if (percentleft <= 30.0D)
              typeHeal = 3;
            int chance;
            switch (typeHeal) { case 1:
              chance = _chanceAb60;
              break;
            case 2:
              chance = _chance3060;
              break;
            case 3:
              chance = _chanceBe30;
              break;
            default:
              chance = _chanceAb60;
            }

            if (Rnd.get(1, 100) < chance)
            {
              L2Character[] targets = { target };
              ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
              if (handler != null)
              {
                handler.useSkill(_owner, skill, targets);
              }
              else
              {
                skill.useSkill(_owner, targets);
              }

              MagicSkillUser msu = new MagicSkillUser(_owner, target, skill.getId(), skill.getLevel(), 0, 0);

              _owner.broadcastPacket(msu);
            }
          }
        }
      }
      catch (Exception e)
      {
        L2CubicInstance._log.log(Level.SEVERE, "", e);
      }
    }
  }

  private class Action
    implements Runnable
  {
    private int _chance;

    Action(int chance)
    {
      _chance = chance;
    }

    public void run()
    {
      try
      {
        if ((_owner.isDead()) && (_owner.isOnline() == 0))
        {
          stopAction();
          _owner.delCubic(_id);
          _owner.broadcastUserInfo();
          cancelDisappear();
          return;
        }
        if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(_owner))
        {
          if (_owner.getPet() != null)
          {
            if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(_owner.getPet()))
            {
              stopAction();
              return;
            }
          }
          else
          {
            stopAction();
            return;
          }
        }
        boolean UseCubicCure = false;
        L2Skill skill = null;

        if ((_id >= 10) && (_id <= 14))
        {
          L2Effect[] effects = _owner.getAllEffects();

          for (L2Effect e : effects)
          {
            if (e.getSkill().getSkillType() != L2Skill.SkillType.DEBUFF)
              continue;
            UseCubicCure = true;
            e.exit();
          }

        }

        if (UseCubicCure)
        {
          MagicSkillUser msu = new MagicSkillUser(_owner, _owner, 5579, 1, 0, 0);
          _owner.broadcastPacket(msu);
        }
        else if (Rnd.get(1, 100) < _chance)
        {
          skill = (L2Skill)_skills.get(Rnd.get(_skills.size()));
          if (skill != null)
          {
            if (skill.getId() == 4051)
            {
              CubicTargetForHeal();
            }
            else
            {
              getCubicTarget();
              if (!isInCubicRange(_owner, _target)) _target = null;
            }
            if ((_target != null) && (!_target.isDead()))
            {
              if (Config.DEBUG)
              {
                L2CubicInstance._log.info("L2CubicInstance: Action.run();");
                L2CubicInstance._log.info("Cubic Id: " + _id + " Target: " + _target.getName() + " distance: " + Math.sqrt(_target.getDistanceSq(_owner.getX(), _owner.getY(), _owner.getZ())));
              }

              L2Skill.SkillType type = skill.getSkillType();
              ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
              L2Character[] targets = { _target };

              if (handler != null)
              {
                if ((type == L2Skill.SkillType.PARALYZE) || (type == L2Skill.SkillType.STUN))
                {
                  if (Config.DEBUG)
                    L2CubicInstance._log.info("L2CubicInstance: Action.run() handler " + type);
                  ((Disablers)handler).useCubicSkill(L2CubicInstance.this, skill, targets);
                }
                else if (type == L2Skill.SkillType.MDAM) {
                  if (Config.DEBUG)
                    L2CubicInstance._log.info("L2CubicInstance: Action.run() handler " + type);
                  ((Mdam)handler).useCubicSkill(L2CubicInstance.this, skill, targets);
                }
                else if ((type == L2Skill.SkillType.POISON) || (type == L2Skill.SkillType.DEBUFF) || (type == L2Skill.SkillType.DOT)) {
                  if (Config.DEBUG)
                    L2CubicInstance._log.info("L2CubicInstance: Action.run() handler " + type);
                  ((Continuous)handler).useCubicSkill(L2CubicInstance.this, skill, targets);
                }
                else {
                  handler.useSkill(_owner, skill, targets);
                  if (Config.DEBUG) {
                    L2CubicInstance._log.info("L2CubicInstance: Action.run(); other handler");
                  }
                }

              }
              else if (type == L2Skill.SkillType.DRAIN) {
                if (Config.DEBUG)
                  L2CubicInstance._log.info("L2CubicInstance: Action.run() skill " + type);
                ((L2SkillDrain)skill).useCubicSkill(L2CubicInstance.this, targets);
              }
              else {
                skill.useSkill(_owner, targets);
                if (Config.DEBUG) {
                  L2CubicInstance._log.info("L2CubicInstance: Action.run(); other skill");
                }
              }

              MagicSkillUser msu = new MagicSkillUser(_owner, _target, skill.getId(), skill.getLevel(), 0, 0);

              _owner.broadcastPacket(msu);
            }
          }
        }
      }
      catch (Exception e)
      {
        L2CubicInstance._log.log(Level.SEVERE, "", e);
      }
    }
  }
}