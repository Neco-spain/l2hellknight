package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class Harvester
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5125 };
  L2PcInstance _activeChar;
  L2MonsterInstance _target;

  public void useItem(L2PlayableInstance playable, L2ItemInstance _item)
  {
    if (!(playable instanceof L2PcInstance)) {
      return;
    }
    if (CastleManorManager.getInstance().isDisabled()) {
      return;
    }
    _activeChar = ((L2PcInstance)playable);

    if ((_activeChar.getTarget() == null) || (!(_activeChar.getTarget() instanceof L2MonsterInstance))) {
      _activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
      _activeChar.sendPacket(new ActionFailed());
      return;
    }

    _target = ((L2MonsterInstance)_activeChar.getTarget());

    if ((_target == null) || (!_target.isDead())) {
      _activeChar.sendPacket(new ActionFailed());
      return;
    }

    L2Skill skill = SkillTable.getInstance().getInfo(2098, 1);
    _activeChar.useMagic(skill, false, false);
  }

  public int[] getItemIds() {
    return ITEM_IDS;
  }
}