package l2p.gameserver.ai;

import l2p.gameserver.model.instances.NpcInstance;

public class Fighter extends DefaultAI
{
  public Fighter(NpcInstance actor)
  {
    super(actor);
  }

  protected boolean thinkActive()
  {
    return (super.thinkActive()) || (defaultThinkBuff(10));
  }

  protected boolean createNewTask()
  {
    return defaultFightTask();
  }

  public int getRatePHYS()
  {
    return 30;
  }

  public int getRateDOT()
  {
    return 20;
  }

  public int getRateDEBUFF()
  {
    return 20;
  }

  public int getRateDAM()
  {
    return 15;
  }

  public int getRateSTUN()
  {
    return 30;
  }

  public int getRateBUFF()
  {
    return 10;
  }

  public int getRateHEAL()
  {
    return 20;
  }
}