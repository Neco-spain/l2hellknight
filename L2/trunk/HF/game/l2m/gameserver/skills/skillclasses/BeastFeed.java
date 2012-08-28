package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.instances.FeedableBeastInstance;
import l2m.gameserver.templates.StatsSet;

public class BeastFeed extends Skill
{
  public BeastFeed(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets)
    {
      ThreadPoolManager.getInstance().execute(new RunnableImpl(target, activeChar)
      {
        public void runImpl()
          throws Exception
        {
          if ((val$target instanceof FeedableBeastInstance))
            ((FeedableBeastInstance)val$target).onSkillUse((Player)val$activeChar, _id);
        }
      });
    }
  }
}