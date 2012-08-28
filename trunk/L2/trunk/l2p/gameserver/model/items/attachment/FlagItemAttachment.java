package l2p.gameserver.model.items.attachment;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;

public abstract interface FlagItemAttachment extends PickableAttachment
{
  public abstract void onLogout(Player paramPlayer);

  public abstract void onDeath(Player paramPlayer, Creature paramCreature);

  public abstract boolean canAttack(Player paramPlayer);

  public abstract boolean canCast(Player paramPlayer, Skill paramSkill);
}