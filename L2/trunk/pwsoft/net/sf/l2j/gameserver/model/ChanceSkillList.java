package net.sf.l2j.gameserver.model;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;

public class ChanceSkillList extends FastMap<L2Skill, ChanceCondition>
{
  private static final long serialVersionUID = 1L;
  private L2Character _owner;

  public ChanceSkillList(L2Character owner)
  {
    shared("ChanceSkillList.ChanceSkillList");
    _owner = owner;
  }

  public L2Character getOwner()
  {
    return _owner;
  }

  public void setOwner(L2Character owner)
  {
    _owner = owner;
  }

  public void onHit(L2Character target, boolean ownerWasHit, boolean wasCrit)
  {
    int event;
    if (ownerWasHit)
    {
      int event = 384;
      if (wasCrit)
        event |= 512;
    }
    else
    {
      event = 1;
      if (wasCrit) {
        event |= 2;
      }
    }
    onEvent(event, target);
  }

  public void onSkillHit(L2Character target, boolean ownerWasHit, boolean wasMagic, boolean wasOffensive)
  {
    int event;
    if (ownerWasHit)
    {
      int event = 1024;
      if (wasOffensive)
      {
        event |= 2048;
        event |= 128;
      }
      else
      {
        event |= 4096;
      }
    }
    else
    {
      event = 4;
      event |= (wasMagic ? 16 : 8);
      event |= (wasOffensive ? 64 : 32);
    }

    onEvent(event, target);
  }

  public void onEvent(int event, L2Character target)
  {
    FastMap.Entry e = head(); for (FastMap.Entry end = tail(); (e = e.getNext()) != end; )
    {
      if ((e.getValue() == null) || (!((ChanceCondition)e.getValue()).trigger(event)))
        continue;
      makeCast((L2Skill)e.getKey(), target);
    }
  }

  private void makeCast(L2Skill skill, L2Character target)
  {
    try
    {
      if (skill.getWeaponDependancy(_owner, true))
      {
        L2Skill skillTemp = SkillTable.getInstance().getInfo(skill.getChanceTriggeredId(), skill.getChanceTriggeredLevel());
        if (skillTemp != null)
        {
          skill = skillTemp;
        }
        _owner.setLastTrigger();

        _owner.broadcastPacket(new MagicSkillUser(_owner, target, skill.getDisplayId(), skill.getLevel(), 0, 0));

        if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF) {
          target = _owner;
        }
        SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()).getEffects(target, target);
      }
    }
    catch (Exception e)
    {
    }
  }
}