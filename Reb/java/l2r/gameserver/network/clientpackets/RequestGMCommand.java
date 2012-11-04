package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ExGMViewQuestItemList;
import l2r.gameserver.network.serverpackets.GMHennaInfo;
import l2r.gameserver.network.serverpackets.GMViewCharacterInfo;
import l2r.gameserver.network.serverpackets.GMViewItemList;
import l2r.gameserver.network.serverpackets.GMViewPledgeInfo;
import l2r.gameserver.network.serverpackets.GMViewQuestInfo;
import l2r.gameserver.network.serverpackets.GMViewSkillInfo;
import l2r.gameserver.network.serverpackets.GMViewWarehouseWithdrawList;

public class RequestGMCommand extends L2GameClientPacket
{
	private String _targetName;
	private int _command;

	@Override
	protected void readImpl()
	{
		_targetName = readS();
		_command = readD();
		// readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		Player target = World.getPlayer(_targetName);
		if(player == null || target == null)
			return;
		if(!player.getPlayerAccess().CanViewChar)
			return;

		switch(_command)
		{
			case 1:
				player.sendPacket(new GMViewCharacterInfo(target));
				player.sendPacket(new GMHennaInfo(target));
				break;
			case 2:
				if(target.getClan() != null)
					player.sendPacket(new GMViewPledgeInfo(target));
				break;
			case 3:
				player.sendPacket(new GMViewSkillInfo(target));
				break;
			case 4:
				player.sendPacket(new GMViewQuestInfo(target));
				break;
			case 5:
				ItemInstance[] items = target.getInventory().getItems();
				int questSize = 0;
				for(ItemInstance item : items)
					if(item.getTemplate().isQuest())
						questSize ++;
				player.sendPacket(new GMViewItemList(target, items, items.length - questSize));
				player.sendPacket(new ExGMViewQuestItemList(target, items, questSize));

				player.sendPacket(new GMHennaInfo(target));
				break;
			case 6:
				player.sendPacket(new GMViewWarehouseWithdrawList(target));
				break;
		}
	}
}