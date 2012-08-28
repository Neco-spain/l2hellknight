package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.items.IItemHandler;

public class MysteryPotion
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5234 };
  private static final int BIGHEAD_EFFECT = 8192;
  private static final int MYSTERY_POTION_SKILL = 2103;
  private static final int EFFECT_DURATION = 1200000;

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer())
      return;
    L2PcInstance activeChar = (L2PcInstance)playable;

    activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2103, 1, 0, 0));

    activeChar.startAbnormalEffect(8192);
    activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);

    activeChar.sendPacket(SystemMessage.id(SystemMessageId.USE_S1).addSkillName(2103));
    ThreadPoolManager.getInstance().scheduleEffect(new MysteryPotionStop(playable), 1200000L);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }

  public static class MysteryPotionStop
    implements Runnable
  {
    private L2PlayableInstance _playable;

    public MysteryPotionStop(L2PlayableInstance playable)
    {
      _playable = playable;
    }

    public void run()
    {
      try {
        if (!_playable.isPlayer()) {
          return;
        }
        ((L2PcInstance)_playable).stopAbnormalEffect(8192);
      }
      catch (Throwable t)
      {
      }
    }
  }
}