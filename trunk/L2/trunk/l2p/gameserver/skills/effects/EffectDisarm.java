package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.stats.Env;

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