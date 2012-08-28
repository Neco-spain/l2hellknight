package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Summon;
import l2m.gameserver.model.World;
import l2m.gameserver.model.base.InvisibleType;
import l2m.gameserver.skills.Env;

public final class EffectInvisible extends Effect
{
  private InvisibleType _invisibleType = InvisibleType.NONE;

  public EffectInvisible(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean checkCondition()
  {
    if (!_effected.isPlayer())
      return false;
    Player player = (Player)_effected;
    if (player.isInvisible())
      return false;
    if (player.getActiveWeaponFlagAttachment() != null)
      return false;
    return super.checkCondition();
  }

  public void onStart()
  {
    super.onStart();
    Player player = (Player)_effected;

    _invisibleType = player.getInvisibleType();

    player.setInvisibleType(InvisibleType.EFFECT);

    World.removeObjectFromPlayers(player);
  }

  public void onExit()
  {
    super.onExit();
    Player player = (Player)_effected;
    if (!player.isInvisible()) {
      return;
    }
    player.setInvisibleType(_invisibleType);

    player.broadcastUserInfo(true);
    if (player.getPet() != null)
      player.getPet().broadcastCharInfo();
  }

  public boolean onActionTime()
  {
    return false;
  }
}