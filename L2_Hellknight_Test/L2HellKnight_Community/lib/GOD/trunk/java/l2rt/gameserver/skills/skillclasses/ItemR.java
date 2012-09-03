package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;
import l2rt.util.Rnd;

public class ItemR extends L2Skill
{
	private final int _id1;
	private final int _id2;
	private final int _id3;
	private final int _idDel;

	public ItemR(StatsSet set)
	{
		super(set);
		_id1 = set.getInteger("item_r1", 0);
		_id2 = set.getInteger("item_r2", 0);
		_id3 = set.getInteger("item_r3", 0);
		_idDel = set.getInteger("item_del", 0);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		if (activeChar == null)
			return;
		L2Player player = null;
		if (activeChar instanceof L2Player)
			player = (L2Player) activeChar;

		if (_id1 != 0) {
		player.getInventory().destroyItemByItemId(_idDel, 1, true);
			if (Rnd.chance(95))
				player.getInventory().addItem(_id1, 1);
			 else if (Rnd.chance(2)) 
				player.getInventory().addItem(_id2, 1);
			 else 
				player.getInventory().addItem(_id3, 1);
			player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S1).addItemName(_id1));
		} else
			player.sendMessage("Данный итем не реализован, ожидайте.");
		
		
		
	}
}