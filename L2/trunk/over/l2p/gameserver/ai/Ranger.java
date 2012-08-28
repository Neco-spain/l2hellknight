package l2p.gameserver.ai;

import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.instances.NpcInstance;

public class Ranger extends DefaultAI
{
  public Ranger(NpcInstance actor)
  {
    super(actor);
  }

  protected boolean thinkActive()
  {
    return (super.thinkActive()) || (defaultThinkBuff(10));
  }

  protected void onEvtAttacked(Creature attacker, int damage)
  {
    super.onEvtAttacked(attacker, damage);
    NpcInstance actor = getActor();
    if ((actor.isDead()) || (attacker == null) || (actor.getDistance(attacker) > 200.0D)) {
      return;
    }
    if (actor.isMoving) {
      return;
    }
    int posX = actor.getX();
    int posY = actor.getY();
    int posZ = actor.getZ();

    int old_posX = posX;
    int old_posY = posY;
    int old_posZ = posZ;

    int signx = posX < attacker.getX() ? -1 : 1;
    int signy = posY < attacker.getY() ? -1 : 1;

    int range = (int)(0.71D * actor.calculateAttackDelay() / 1000.0D * actor.getMoveSpeed());

    posX += signx * range;
    posY += signy * range;
    posZ = GeoEngine.getHeight(posX, posY, posZ, actor.getGeoIndex());

    if (GeoEngine.canMoveToCoord(old_posX, old_posY, old_posZ, posX, posY, posZ, actor.getGeoIndex()))
    {
      addTaskMove(posX, posY, posZ, false);
      addTaskAttack(attacker);
    }
  }

  protected boolean createNewTask()
  {
    return defaultFightTask();
  }

  public int getRatePHYS()
  {
    return 25;
  }

  public int getRateDOT()
  {
    return 40;
  }

  public int getRateDEBUFF()
  {
    return 25;
  }

  public int getRateDAM()
  {
    return 50;
  }

  public int getRateSTUN()
  {
    return 50;
  }

  public int getRateBUFF()
  {
    return 5;
  }

  public int getRateHEAL()
  {
    return 50;
  }
}