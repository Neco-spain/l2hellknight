package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.skills.Env;

public final class EffectDisarm extends Effect
{
  public EffectDisarm(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean checkCondition()
  {
    if (!_effected.isPlayer())
      return false;
    Player player = _effected.getPlayer();

    if ((player.isCursedWeaponEquipped()) || (player.getActiveWeaponFlagAttachment() != null))
      return false;
    return super.checkCondition();
  }

  public void onStart()
  {
    super.onStart();
    Player player = (Player)_effected;

    ItemInstance wpn = player.getActiveWeaponInstance();
    if (wpn != null)
    {
      player.getInventory().unEquipItem(wpn);
      player.sendDisarmMessage(wpn);
    }
    player.startWeaponEquipBlocked();
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopWeaponEquipBlocked();
  }

  public boolean onActionTime()
  {
    return false;
  }
}