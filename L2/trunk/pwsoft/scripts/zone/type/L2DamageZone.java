package scripts.zone.type;

import java.util.Collection;
import java.util.concurrent.Future;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import scripts.zone.L2ZoneType;

public class L2DamageZone extends L2ZoneType
{
  private int _damagePerSec;
  private Future<?> _task;

  public L2DamageZone(int id)
  {
    super(id);

    _damagePerSec = 100;
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("dmgSec"))
    {
      _damagePerSec = Integer.parseInt(value);
    }
    else super.setParameter(name, value);
  }

  protected void onEnter(L2Character character)
  {
    if (_task == null)
    {
      _task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ApplyDamage(this), 10L, 1000L);
    }
  }

  protected void onExit(L2Character character)
  {
    if (_characterList.isEmpty())
    {
      _task.cancel(true);
      _task = null;
    }
  }

  protected Collection<L2Character> getCharacterList()
  {
    return _characterList.values();
  }

  protected int getDamagePerSecond()
  {
    return _damagePerSec;
  }
  protected void onDieInside(L2Character character) {
  }
  protected void onReviveInside(L2Character character) {  }

  private static class ApplyDamage implements Runnable { private L2DamageZone _dmgZone;

    ApplyDamage(L2DamageZone zone) { _dmgZone = zone;
    }

    public void run()
    {
      for (L2Character temp : _dmgZone.getCharacterList())
      {
        if ((temp != null) && (!temp.isDead()))
        {
          temp.reduceCurrentHp(_dmgZone.getDamagePerSecond(), null);
        }
      }
    }
  }
}