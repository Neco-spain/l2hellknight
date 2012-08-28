//L2DDT
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class MysteryPotion implements IItemHandler
{
    private static final int[] ITEM_IDS = { 5234 };
    private static final int BIGHEAD_EFFECT = 0x2000;
    private static final int MYSTERY_POTION_SKILL = 2103;
    private static final int EFFECT_DURATION = 1200000;

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance)playable;
		MagicSkillUser MSU = new MagicSkillUser(playable, playable, 2103, 1, 0, 0);
		activeChar.sendPacket(MSU);
		activeChar.broadcastPacket(MSU);

		activeChar.startAbnormalEffect(BIGHEAD_EFFECT);
		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);

		SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
		sm.addSkillName(MYSTERY_POTION_SKILL);
		activeChar.sendPacket(sm);

		MysteryPotionStop mp = new MysteryPotionStop(playable);
		ThreadPoolManager.getInstance().scheduleEffect(mp, EFFECT_DURATION);
    }

	public class MysteryPotionStop implements Runnable
	{
		private L2PlayableInstance _playable;

		public MysteryPotionStop (L2PlayableInstance playable)
		{
			_playable = playable;
		}

		public void run()
		{
			try	{
				if (!(_playable instanceof L2PcInstance))
					return;

				((L2PcInstance)_playable).stopAbnormalEffect(BIGHEAD_EFFECT);
			}
			catch (Throwable t) {}
		}
	}

    public int[] getItemIds()
    {
        return ITEM_IDS;
    }
}
