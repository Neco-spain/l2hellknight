package ai;

import java.util.logging.Level;
import java.util.logging.Logger;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.SpawnTable;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class ZealotOfShilen extends Fighter
{
  static final Location[] points = { new Location(-148162, 255173, -184), new Location(-148607, 254347, -184), new Location(-148781, 254206, -184), new Location(-149090, 254012, -184), new Location(-149093, 254183, -184) };

  private int _npc = 18782;
  private L2Character i;
  int[] targets = { 32628, 32629 };

  public ZealotOfShilen(L2Character actor) { super(actor);
    this.i = actor;
  }

  protected void onEvtAttacked(L2Character attacker, int damage)
  {
    onEvtAggression(attacker, damage);
    super.onEvtAttacked(attacker, damage);
  }

  protected void onEvtAggression(L2Character attacker, int aggro)
  {
    L2NpcInstance actor = getActor();
    if ((attacker == null) || (actor == null)) {
      return;
    }
    actor.setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
    setGlobalAggro(0L);

    if (!actor.isRunning()) {
      startRunningTask(1000);
    }
    if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK) {
      setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
    }
    super.onEvtAggression(attacker, aggro);
  }

  protected boolean thinkActive()
  {
    for (L2NpcInstance npc : getActor().getAroundNpc(getActor().getAggroRange(), 200))
      if (isTargetNPC(npc.getNpcId()))
      {
        getActor().startConfused();
        getActor().setTarget(npc);
        getActor().addDamageHate(npc, 0, 500);
        setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc, null);
        break;
      }
    return super.thinkActive();
  }

  private boolean isTargetNPC(int id)
  {
    for (int n : this.targets) {
      if (n == id)
        return true;
    }
    return false;
  }

  protected void onEvtDead(L2Character killer)
  {
    super.onEvtDead(killer);
    ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(), 300000L);
  }

  public class Spawn implements Runnable
  {
    public Location[] points = { new Location(-148162, 255173, -184), new Location(-148607, 254347, -184), new Location(-148781, 254206, -184), new Location(-149090, 254012, -184), new Location(-149093, 254183, -184) };

    public Spawn()
    {
    }

    public void run()
    {
      try {
        ZealotOfShilen.this.i.deleteMe();
        L2Spawn spawnDat = new L2Spawn(18782);
        spawnDat.setAmount(1);
        spawnDat.setLoc(this.points[Rnd.get(this.points.length)]);
        spawnDat.setRespawnDelay(600);
        spawnDat.setReflection(0L);
        spawnDat.setRespawnTime(0);
        spawnDat.init();
      } catch (ClassNotFoundException ex) {
        Logger.getLogger(SpawnTable.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
}