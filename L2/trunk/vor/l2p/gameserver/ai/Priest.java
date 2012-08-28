package l2p.gameserver.ai;

import l2p.gameserver.model.instances.NpcInstance;

public class Priest extends DefaultAI
{
  public Priest(NpcInstance actor)
  {
    super(actor);
  }

  protected boolean thinkActive()
  {
    return (super.thinkActive()) || (defaultThinkBuff(10, 5));
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
    return 40;
  }

  public int getRateDAM()
  {
    return 75;
  }

  public int getRateSTUN()
  {
    return 10;
  }

  public int getRateBUFF()
  {
    return 25;
  }

  public int getRateHEAL()
  {
    return 90;
  }
}