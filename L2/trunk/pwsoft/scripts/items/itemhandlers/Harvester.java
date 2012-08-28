package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import scripts.items.IItemHandler;

public class Harvester
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5125 };
  L2PcInstance _activeChar;
  L2MonsterInstance _target;

  public void useItem(L2PlayableInstance playable, L2ItemInstance _item)
  {
    if (!playable.isPlayer()) {
      return;
    }
    if (CastleManorManager.getInstance().isDisabled()) {
      return;
    }
    _activeChar = ((L2PcInstance)playable);

    if ((_activeChar.getTarget() == null) || (!_activeChar.getTarget().isL2Monster())) {
      _activeChar.sendPacket(Static.TARGET_IS_INCORRECT);
      _activeChar.sendActionFailed();
      return;
    }

    _target = ((L2MonsterInstance)_activeChar.getTarget());

    if ((_target == null) || (!_target.isDead())) {
      _activeChar.sendActionFailed();
      return;
    }

    L2Skill skill = SkillTable.getInstance().getInfo(2098, 1);
    _activeChar.useMagic(skill, false, false);
  }

  public int[] getItemIds() {
    return ITEM_IDS;
  }
}