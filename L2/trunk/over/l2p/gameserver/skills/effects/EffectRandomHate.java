package l2p.gameserver.skills.effects;

import java.util.List;
import l2p.commons.util.Rnd;
import l2p.gameserver.model.AggroList;
import l2p.gameserver.model.AggroList.AggroInfo;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.instances.MonsterInstance;
import l2p.gameserver.stats.Env;

public class EffectRandomHate extends Effect
{
  public EffectRandomHate(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean checkCondition()
  {
    return (getEffected().isMonster()) && (Rnd.chance(_template.chance(100)));
  }

  public void onStart()
  {
    MonsterInstance monster = (MonsterInstance)getEffected();
    Creature mostHated = monster.getAggroList().getMostHated();
    if (mostHated == null) {
      return;
    }
    AggroList.AggroInfo mostAggroInfo = monster.getAggroList().get(mostHated);
    List hateList = monster.getAggroList().getHateList();
    hateList.remove(mostHated);

    if (!hateList.isEmpty())
    {
      AggroList.AggroInfo newAggroInfo = monster.getAggroList().get((Creature)hateList.get(Rnd.get(hateList.size())));
      int oldHate = newAggroInfo.hate;

      newAggroInfo.hate = mostAggroInfo.hate;
      mostAggroInfo.hate = oldHate;
    }
  }

  public boolean isHidden()
  {
    return true;
  }

  protected boolean onActionTime()
  {
    return false;
  }
}