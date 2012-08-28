package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.skills.Env;

public class ConditionTargetClan extends Condition
{
  private final boolean _test;

  public ConditionTargetClan(String param)
  {
    _test = Boolean.valueOf(param).booleanValue();
  }

  protected boolean testImpl(Env env)
  {
    Creature Char = env.character;
    Creature target = env.target;
    if ((Char.getPlayer() != null) && (target.getPlayer() != null)) if (Char.getPlayer().getClanId() != 0) if ((Char.getPlayer().getClanId() == target.getPlayer().getClanId()) == _test) break label90;  label90: return (Char.getPlayer().getParty() != null) && (Char.getPlayer().getParty() == target.getPlayer().getParty());
  }
}