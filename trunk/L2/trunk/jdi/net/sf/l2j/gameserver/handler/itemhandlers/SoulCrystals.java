package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class SoulCrystals
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 4629, 4630, 4631, 4632, 4633, 4634, 4635, 4636, 4637, 4638, 4639, 5577, 5580, 5908, 4640, 4641, 4642, 4643, 4644, 4645, 4646, 4647, 4648, 4649, 4650, 5578, 5581, 5911, 4651, 4652, 4653, 4654, 4655, 4656, 4657, 4658, 4659, 4660, 4661, 5579, 5582, 5914 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance)) {
      return;
    }
    L2PcInstance activeChar = (L2PcInstance)playable;
    L2Object target = activeChar.getTarget();
    if (!(target instanceof L2MonsterInstance))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.INCORRECT_TARGET);
      activeChar.sendPacket(sm);

      activeChar.sendPacket(new ActionFailed());

      return;
    }

    if (((L2MonsterInstance)target).getCurrentHp() > ((L2MonsterInstance)target).getMaxHp() / 2.0D)
    {
      activeChar.sendPacket(new ActionFailed());
      return;
    }

    int crystalId = item.getItemId();

    L2Skill skill = SkillTable.getInstance().getInfo(2096, 1);
    activeChar.useMagic(skill, false, true);

    CrystalFinalizer cf = new CrystalFinalizer(activeChar, target, crystalId);
    ThreadPoolManager.getInstance().scheduleEffect(cf, skill.getHitTime());
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }

  static class CrystalFinalizer
    implements Runnable
  {
    private L2PcInstance _activeChar;
    private L2Attackable _target;
    private int _crystalId;

    CrystalFinalizer(L2PcInstance activeChar, L2Object target, int crystalId)
    {
      _activeChar = activeChar;
      _target = ((L2Attackable)target);
      _crystalId = crystalId;
    }

    public void run()
    {
      if ((_activeChar.isDead()) || (_target.isDead()))
        return;
      _activeChar.enableAllSkills();
      try {
        _target.addAbsorber(_activeChar, _crystalId);
        _activeChar.setTarget(_target);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }
}