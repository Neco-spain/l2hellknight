package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import scripts.items.IItemHandler;

public class Remedy
  implements IItemHandler
{
  private static int[] ITEM_IDS = { 1831, 1832, 1833, 1834, 3889 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    L2PcInstance activeChar;
    if (playable.isPlayer()) {
      activeChar = (L2PcInstance)playable;
    }
    else
    {
      L2PcInstance activeChar;
      if (playable.isPet())
        activeChar = ((L2PetInstance)playable).getOwner();
      else
        return;
    }
    L2PcInstance activeChar;
    if (activeChar.isInOlympiadMode())
    {
      activeChar.sendPacket(Static.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
      return;
    }

    int remId = 0;
    L2Effect e = null;
    switch (item.getItemId())
    {
    case 1831:
      e = activeChar.getFirstEffect(L2Skill.SkillType.POISON);
      if ((e != null) && (e.getLevel() <= 3))
        e.exit();
      remId = 2042;
      break;
    case 1832:
      e = activeChar.getFirstEffect(L2Skill.SkillType.POISON);
      if ((e != null) && (e.getLevel() <= 7))
        e.exit();
      remId = 2043;
      break;
    case 1833:
      e = activeChar.getFirstEffect(L2Skill.SkillType.BLEED);
      if ((e != null) && (e.getLevel() <= 3))
        e.exit();
      remId = 34;
      break;
    case 1834:
      e = activeChar.getFirstEffect(L2Skill.SkillType.BLEED);
      if ((e != null) && (e.getLevel() <= 7))
        e.exit();
      remId = 2045;
      break;
    case 3889:
      e = activeChar.getFirstEffect(4082);
      if (e != null)
        e.exit();
      activeChar.setIsImobilised(false);
      if (activeChar.getFirstEffect(L2Effect.EffectType.ROOT) == null)
        activeChar.stopRooting(null);
      remId = 2042;
    }

    activeChar.broadcastPacket(new MagicSkillUser(playable, playable, remId, 1, 0, 0));
    playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
  }

  public int[] getItemIds() {
    return ITEM_IDS;
  }
}