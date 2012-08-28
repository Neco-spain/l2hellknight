package l2p.gameserver.model.instances;

import l2p.gameserver.templates.npc.NpcTemplate;

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