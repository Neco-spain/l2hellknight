package net.sf.l2j.gameserver.model.actor.instance;

import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.util.PeaceZone;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.skills.ISkillHandler;
import scripts.skills.SkillHandler;

public class L2CubicInstance
{
  protected static final Logger _log = AbstractLogger.getLogger(L2CubicInstance.class.getName());
  public static final int STORM_CUBIC = 1;
  public static final int VAMPIRIC_CUBIC = 2;
  public static final int LIFE_CUBIC = 3;
  public static final int VIPER_CUBIC = 4;
  public static final int POLTERGEIST_CUBIC = 5;
  public static final int BINDING_CUBIC = 6;
  public static final int AQUA_CUBIC = 7;
  public static final int SPARK_CUBIC = 8;
  public static final int ATTRACT_CUBIC = 9;
  protected L2PcInstance _owner;
  protected L2Character _target;
  protected int _id;
  protected int _level = 1;

  protected List<Integer> _skills = new FastList();
  private Future<?> _disappearTask;
  private Future<?> _actionTask;

  public L2CubicInstance(L2PcInstance owner, int id, int level)
  {
    _owner = owner;
    _id = id;
    _level = level;

    switch (_id)
    {
    case 1:
      _skills.add(Integer.valueOf(4049));
      break;
    case 2:
      _skills.add(Integer.valueOf(4050));
      break;
    case 3:
      _skills.add(Integer.valueOf(4051));
      _disappearTask = ThreadPoolManager.getInstance().scheduleEffect(new Disappear(), 3600000L);
      doAction(_owner);
      break;
    case 4:
      _skills.add(Integer.valueOf(4052));
      break;
    case 5:
      _skills.add(Integer.valueOf(4053));
      _skills.add(Integer.valueOf(4054));
      _skills.add(Integer.valueOf(4055));
      break;
    case 6:
      _skills.add(Integer.valueOf(4164));
      break;
    case 7:
      _skills.add(Integer.valueOf(4165));
      break;
    case 8:
      _skills.add(Integer.valueOf(4166));
      break;
    case 9:
      _skills.add(Integer.valueOf(5115));
      _skills.add(Integer.valueOf(5116));
    }

    if (_disappearTask == null)
      _disappearTask = ThreadPoolManager.getInstance().scheduleEffect(new Disappear(), 1200000L);
  }

  public void doAction(L2Character target)
  {
    if (_target == target) return;
    stopAction();
    _target = target;
    switch (_id)
    {
    case 1:
      _actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(17), 0L, 10000L);
      break;
    case 2:
      _actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(15), 0L, 15000L);
      break;
    case 4:
      _actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(35), 0L, 20000L);
      break;
    case 5:
      _actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(35), 0L, 8000L);
      break;
    case 6:
    case 7:
    case 8:
      _actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(35), 0L, 8000L);
      break;
    case 3:
      _actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Heal(55), 0L, 30000L);
      break;
    case 9:
      _actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(35), 0L, 8000L);
    }
  }

  public int getId()
  {
    return _id;
  }

  public void setLevel(int level)
  {
    _level = level;
  }

  public void stopAction()
  {
    _target = null;
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
    private int _chance;

    Heal(int chance)
    {
      _chance = chance;
    }

    public void run()
    {
      if (_owner.isDead())
      {
        stopAction();
        _owner.delCubic(_id);
        _owner.broadcastUserInfo();
        cancelDisappear();
        return;
      }
      try
      {
        if (Rnd.get(1, 100) < _chance)
        {
          L2Skill skill = SkillTable.getInstance().getInfo(((Integer)_skills.get(Rnd.get(_skills.size()))).intValue(), _level);

          if (skill != null)
          {
            L2Character target = null;
            if (_owner.isInParty())
            {
              L2Character caster = _owner;
              L2PcInstance player = _owner;
              L2Party party = player.getParty();
              double percentleft = 100.0D;
              if (party != null)
              {
                List partyList = party.getPartyMembers();
                L2Character partyMember = null;

                int range = 400;
                for (int i = 0; i < partyList.size(); i++)
                {
                  partyMember = (L2Character)partyList.get(i);
                  if (partyMember.isDead()) {
                    continue;
                  }
                  int x = caster.getX() - partyMember.getX();
                  int y = caster.getY() - partyMember.getY();
                  int z = caster.getZ() - partyMember.getZ();
                  if (x * x + y * y + z * z > range * range) {
                    continue;
                  }
                  if (partyMember.getCurrentHp() >= partyMember.getMaxHp())
                    continue;
                  if (percentleft <= partyMember.getCurrentHp() / partyMember.getMaxHp())
                    continue;
                  percentleft = partyMember.getCurrentHp() / partyMember.getMaxHp();
                  target = partyMember;
                }

              }

            }
            else if (_owner.getCurrentHp() < _owner.getMaxHp()) { target = _owner;
            }
            if (target != null)
            {
              FastList targets = new FastList();
              targets.add(_target);
              ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());

              if (handler != null)
                handler.useSkill(_owner, skill, targets);
              else {
                skill.useSkill(_owner, targets);
              }
              _owner.broadcastPacket(new MagicSkillUser(_owner, target, skill.getId(), _level, 0, 0));
              targets.clear();
              targets = null;
            }
          }
        }
      }
      catch (Exception e)
      {
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
      if (_target == null)
      {
        stopAction();
        return;
      }

      if ((_owner.isDead()) || (_target.isDead()) || (_owner.getTarget() != _target))
      {
        stopAction();
        if (_owner.isDead())
        {
          _owner.delCubic(_id);
          _owner.broadcastUserInfo();
          cancelDisappear();
        }
        return;
      }

      try
      {
        if (Rnd.get(1, 100) < _chance)
        {
          L2Skill skill = SkillTable.getInstance().getInfo(((Integer)_skills.get(Rnd.get(_skills.size()))).intValue(), _level);
          if (skill != null)
          {
            if ((skill.isOffensive()) && (PeaceZone.getInstance().inPeace(_owner, _target))) {
              return;
            }
            FastList targets = new FastList();
            targets.add(_target);

            ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());

            int range = _target.getTemplate().collisionRadius + 400;

            int x = _owner.getX() - _target.getX();
            int y = _owner.getY() - _target.getY();
            int z = _owner.getZ() - _target.getZ();
            if (x * x + y * y + z * z <= range * range)
            {
              if (handler != null)
                handler.useSkill(_owner, skill, targets);
              else {
                skill.useSkill(_owner, targets);
              }
              _owner.broadcastPacket(new MagicSkillUser(_owner, _target, skill.getId(), _level, 0, 0));
              targets.clear();
              targets = null;
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