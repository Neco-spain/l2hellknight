package scripts.ai;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.network.serverpackets.CharMoveToLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public class QueenAntNurse extends L2MonsterInstance
{
  private static QueenAnt aq = null;
  private static QueenAntLarva larva = null;

  private boolean process = false;

  public QueenAntNurse(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onSpawn()
  {
    setRunning();
    ThreadPoolManager.getInstance().scheduleAi(new Heal(), 10000L, false);
  }

  public void setAq(QueenAnt aq)
  {
    aq = aq;
  }

  public void setLarva(QueenAntLarva larva)
  {
    larva = larva;
  }

  public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
  {
    super.reduceCurrentHp(damage, attacker, awake);
  }

  public boolean doDie(L2Character killer)
  {
    super.doDie(killer);
    if (getSpawn() != null)
      getSpawn().setLastKill(System.currentTimeMillis());
    return true;
  }

  public void deleteMe()
  {
    super.deleteMe();
  }

  private void doHeal(L2Object trg)
  {
    if ((aq == null) || (aq.isDead()))
    {
      deleteMe();
      return;
    }

    if (!Util.checkIfInRange(800, this, trg, false))
    {
      moveToLocationm(trg.getX() + Rnd.get(150), trg.getY() + Rnd.get(150), trg.getZ(), 0);
      broadcastPacket(new CharMoveToLocation(this));
    }
    else
    {
      setTarget(trg);
      addUseSkillDesire(4020, 1);
    }
    process = false;
  }

  class Heal
    implements Runnable
  {
    Heal()
    {
    }

    public void run()
    {
      if ((QueenAntNurse.aq == null) || (QueenAntNurse.aq.isDead()))
      {
        deleteMe();
        return;
      }
      if (process)
      {
        ThreadPoolManager.getInstance().scheduleAi(new Heal(QueenAntNurse.this), 10000L, false);
        return;
      }

      if ((QueenAntNurse.larva != null) && (QueenAntNurse.larva.getCurrentHp() < QueenAntNurse.larva.getMaxHp()))
      {
        QueenAntNurse.access$102(QueenAntNurse.this, true);
        QueenAntNurse.this.doHeal(QueenAntNurse.larva);
      }
      else if (QueenAntNurse.aq.getCurrentHp() < QueenAntNurse.aq.getMaxHp())
      {
        QueenAntNurse.access$102(QueenAntNurse.this, true);
        QueenAntNurse.this.doHeal(QueenAntNurse.aq);
      }
      ThreadPoolManager.getInstance().scheduleAi(new Heal(QueenAntNurse.this), 10000L, false);
    }
  }
}