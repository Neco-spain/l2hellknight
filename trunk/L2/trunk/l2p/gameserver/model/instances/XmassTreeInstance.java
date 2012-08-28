package l2p.gameserver.model.instances;

import l2p.gameserver.model.Creature;
import l2p.gameserver.templates.npc.NpcTemplate;

public class XmassTreeInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;

  public XmassTreeInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public boolean isAttackable(Creature attacker)
  {
    return false;
  }

  public boolean isAutoAttackable(Creature attacker)
  {
    return false;
  }

  public boolean hasRandomWalk()
  {
    return false;
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