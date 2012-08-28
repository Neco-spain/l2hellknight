package l2m.gameserver.model.instances;

import l2m.gameserver.templates.npc.NpcTemplate;

public class SpecialMonsterInstance extends MonsterInstance
{
  public static final long serialVersionUID = 1L;

  public SpecialMonsterInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public boolean canChampion()
  {
    return false;
  }
}