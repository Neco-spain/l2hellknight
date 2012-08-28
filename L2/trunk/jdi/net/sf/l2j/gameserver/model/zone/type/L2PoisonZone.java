package net.sf.l2j.gameserver.model.zone.type;

import java.util.Collection;
import java.util.concurrent.Future;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.util.Rnd;

public class L2PoisonZone extends L2ZoneType
{
  private int _skillId;
  private int _chance;
  private int _initialDelay;
  private int _skillLvl;
  private int _reuse;
  private boolean _enabled;
  private String _target;
  private Future<?> _task;

  public L2PoisonZone(int id)
  {
    super(id);

    _skillId = 4070;
    _skillLvl = 1;
    _chance = 100;
    _initialDelay = 0;
    _reuse = 30000;
    _enabled = true;
    _target = "pc";
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("skillId"))
    {
      _skillId = Integer.parseInt(value);
    }
    else if (name.equals("skillLvl"))
    {
      _skillLvl = Integer.parseInt(value);
    }
    else if (name.equals("chance"))
    {
      _chance = Integer.parseInt(value);
    }
    else if (name.equals("initialDelay"))
    {
      _initialDelay = Integer.parseInt(value);
    }
    else if (name.equals("default_enabled"))
    {
      _enabled = Boolean.parseBoolean(value);
    }
    else if (name.equals("target"))
    {
      _target = String.valueOf(value);
    }
    else if (name.equals("reuse"))
    {
      _reuse = Integer.parseInt(value);
    }
    else
      super.setParameter(name, value);
  }

  protected void onEnter(L2Character character)
  {
    if ((character instanceof L2PcInstance))
      ((L2PcInstance)character).enterDangerArea();
    if ((((character instanceof L2PlayableInstance)) && (_target.equalsIgnoreCase("pc"))) || (((character instanceof L2PcInstance)) && (_target.equalsIgnoreCase("pc_only"))) || (((character instanceof L2MonsterInstance)) && (_target.equalsIgnoreCase("npc"))))
    {
      if (_task == null)
      {
        _task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ApplySkill(this), _initialDelay, _reuse);
      }
    }
  }

  protected void onExit(L2Character character)
  {
    if ((character instanceof L2PcInstance))
      ((L2PcInstance)character).exitDangerArea();
    if ((_characterList.isEmpty()) && (_task != null))
    {
      _task.cancel(true);
      _task = null;
    }
  }

  public L2Skill getSkill()
  {
    return SkillTable.getInstance().getInfo(_skillId, _skillLvl);
  }

  public String getTargetType()
  {
    return _target;
  }

  public boolean isEnabled()
  {
    return _enabled;
  }

  public int getChance()
  {
    return _chance;
  }

  public void setZoneEnabled(boolean val)
  {
    _enabled = val;
  }

  protected Collection<L2Character> getCharacterList()
  {
    return _characterList.values();
  }
  public void onDieInside(L2Character character) {
  }
  public void onReviveInside(L2Character character) {
  }
  class ApplySkill implements Runnable { private L2PoisonZone _poisonZone;

    ApplySkill(L2PoisonZone zone) { _poisonZone = zone;
    }

    public void run()
    {
      if (isEnabled())
      {
        for (L2Character temp : _poisonZone.getCharacterList())
        {
          if ((temp != null) && (!temp.isDead()))
          {
            if ((((temp instanceof L2PlayableInstance)) && (getTargetType().equalsIgnoreCase("pc"))) || (((temp instanceof L2PcInstance)) && (getTargetType().equalsIgnoreCase("pc_only"))) || (((temp instanceof L2MonsterInstance)) && (getTargetType().equalsIgnoreCase("npc")) && (Rnd.get(100) < getChance())))
            {
              getSkill().getEffects(temp, temp);
            }
          }
        }
      }
    }
  }
}