package npc.model;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.MerchantInstance;
import l2p.gameserver.serverpackets.PackageToList;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.WarehouseFunctions;

/**
 * @author VISTALL
 * @date 20:32/16.05.2011
 */
public class FreightSenderInstance extends MerchantInstance
{
	public FreightSenderInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("deposit_items"))
			player.sendPacket(new PackageToList(player));
		else if(command.equalsIgnoreCase("withdraw_items"))
			WarehouseFunctions.showFreightWindow(player);
		else
			super.onBypassFeedback(player, command);
	}
}
