package l2m.gameserver.ai;

import l2m.gameserver.model.instances.NpcInstance;

public class Mystic extends DefaultAI
{
  public Mystic(NpcInstance actor)
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
    return _damSkills.length == 0 ? 25 : 0;
  }

  public int getRateDOT()
  {
    return 25;
  }

  public int getRateDEBUFF()
  {
    return 20;
  }

  public int getRateDAM()
  {
    return 100;
  }

  public int getRateSTUN()
  {
    return 10;
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