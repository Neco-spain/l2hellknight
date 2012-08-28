package l2m.gameserver.model.instances;

import l2m.gameserver.model.Creature;
import l2m.gameserver.templates.npc.NpcTemplate;

public final class ArtefactInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;

  public ArtefactInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
    setHasChatWindow(false);
  }

  public boolean isArtefact()
  {
    return true;
  }

  public boolean isAutoAttackable(Creature attacker)
  {
    return false;
  }

  public boolean isAttackable(Creature attacker)
  {
    return false;
  }

  public boolean isInvul()
  {
    return true;
  }

  public boolean isFearImmune()
  {
    return true;
  }

  public boolean isParalyzeImmune()
  {
    return true;
  }

  public boolean isLethalImmune()
  {
    return true;
  }
}