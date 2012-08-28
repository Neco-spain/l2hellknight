package net.sf.l2j.gameserver.model.actor.instance;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2ControllableMobAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2ControllableMobInstance extends L2MonsterInstance
{
  private boolean _isInvul;
  private L2ControllableMobAI _aiBackup;

  public boolean isAggressive()
  {
    return true;
  }

  public int getAggroRange()
  {
    return 500;
  }

  public L2ControllableMobInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public L2CharacterAI getAI()
  {
    if (_ai == null)
    {
      synchronized (this)
      {
        if ((_ai == null) && (_aiBackup == null))
        {
          _ai = new L2ControllableMobAI(new ControllableAIAcessor());
          _aiBackup = ((L2ControllableMobAI)_ai);
        }
        else
        {
          _ai = _aiBackup;
        }
      }
    }
    return _ai;
  }

  public boolean isInvul()
  {
    return _isInvul;
  }

  public void setInvul(boolean isInvul)
  {
    _isInvul = isInvul;
  }

  public void reduceCurrentHp(double i, L2Character attacker, boolean awake)
  {
    if ((isInvul()) || (isDead())) {
      return;
    }
    if (awake) {
      stopSleeping(null);
    }
    i = getCurrentHp() - i;

    if (i < 0.0D) {
      i = 0.0D;
    }
    setCurrentHp(i);

    if (isDead())
    {
      if (Config.DEBUG) _log.fine("char is dead.");

      stopMove(null);

      doDie(attacker);

      setCurrentHp(0.0D);
    }
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }
    removeAI();
    return true;
  }

  public void deleteMe()
  {
    removeAI();
    super.deleteMe();
  }

  protected void removeAI()
  {
    synchronized (this)
    {
      if (_aiBackup != null)
      {
        _aiBackup.setIntention(CtrlIntention.AI_INTENTION_IDLE);
        _aiBackup = null;
        _ai = null;
      }
    }
  }

  protected class ControllableAIAcessor extends L2Character.AIAccessor
  {
    protected ControllableAIAcessor()
    {
      super();
    }

    public void detachAI()
    {
    }
  }
}