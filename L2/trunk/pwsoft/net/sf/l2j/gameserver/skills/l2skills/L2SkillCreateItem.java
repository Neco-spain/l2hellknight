package net.sf.l2j.gameserver.skills.l2skills;

import javolution.util.FastList;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.Rnd;

public class L2SkillCreateItem extends L2Skill
{
  private final int[] _createItemId;
  private final int _createItemCount;
  private final int _randomCount;

  public L2SkillCreateItem(StatsSet set)
  {
    super(set);
    _createItemId = set.getIntegerArray("create_item_id");
    _createItemCount = set.getInteger("create_item_count", 0);
    _randomCount = set.getInteger("random_count", 1);
  }

  public void useSkill(L2Character activeChar, FastList<L2Object> targets)
  {
    if (activeChar.isAlikeDead()) return;
    if ((_createItemId == null) || (_createItemCount == 0))
    {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.SKILL_NOT_AVAILABLE));
      return;
    }

    if (activeChar.isPlayer())
    {
      int rnd = Rnd.nextInt(_randomCount) + 1;
      int count = _createItemCount * rnd;
      int rndid = Rnd.nextInt(_createItemId.length);
      giveItems(activeChar.getPlayer(), _createItemId[rndid], count);
    }
  }

  public void giveItems(L2PcInstance activeChar, int itemId, int count)
  {
    L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
    if (item == null) return;
    item.setCount(count);
    activeChar.getInventory().addItem("Skill", item, activeChar, activeChar);

    if (count > 1)
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(item.getItemId()).addNumber(count));
    else
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(item.getItemId()));
    activeChar.sendItems(false);
  }
}