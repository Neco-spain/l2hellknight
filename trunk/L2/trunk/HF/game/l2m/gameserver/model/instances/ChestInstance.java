package l2m.gameserver.model.instances;

import l2m.gameserver.ai.CharacterAI;
import l2m.gameserver.ai.CtrlEvent;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.templates.npc.NpcTemplate;

public class ChestInstance extends MonsterInstance
{
  public static final long serialVersionUID = 1L;

  public ChestInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public void tryOpen(Player opener, Skill skill)
  {
    getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, opener, Integer.valueOf(100));
  }

  public boolean canChampion()
  {
    return false;
  }
}