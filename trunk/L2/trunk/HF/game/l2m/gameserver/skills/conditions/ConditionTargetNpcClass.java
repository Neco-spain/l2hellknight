package l2m.gameserver.skills.conditions;

import java.util.Map;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.scripts.Scripts;
import l2m.gameserver.skills.Env;

public class ConditionTargetNpcClass extends Condition
{
  private final Class<NpcInstance> _npcClass;

  public ConditionTargetNpcClass(String name)
  {
    Class classType = null;
    try
    {
      classType = Class.forName("l2p.gameserver.model.instances." + name + "Instance");
    }
    catch (ClassNotFoundException e)
    {
      classType = (Class)Scripts.getInstance().getClasses().get("npc.model." + name + "Instance");
    }

    if (classType == null) {
      throw new IllegalArgumentException("Not found type class for type: " + name + ".");
    }
    _npcClass = classType;
  }

  protected boolean testImpl(Env env)
  {
    return (env.target != null) && (env.target.getClass() == _npcClass);
  }
}