//L2DDT
package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;

public final class RequestGiveItemToPet extends L2GameClientPacket
{
	private static final String REQUESTCIVEITEMTOPET__C__8B = "[C] 8B RequestGiveItemToPet";
	private static Logger _log = Logger.getLogger(RequestGetItemFromPet.class.getName());

	private int _objectId;
	private int _amount;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount   = readD();
	}

	@SuppressWarnings("unused")
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null || player.getPet() == null || !(player.getPet() instanceof L2PetInstance)) return;

        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && player.getKarma() > 0) return;
        if (player.getPrivateStoreType() != 0)
        {
            player.sendMessage("Cannot exchange items while trading");
            return;
        }
		
		// Exploit fix for hero weapons uses pet inventory to buy new one.
		L2ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		if (player != null && item.isAugmented()) 
		{ 
			player.sendMessage("You can't give Augmented items to pet!"); 
			return; 
		}
		
		if (item == null)
			return;
			
		if (player != null && item.isHeroItem())
		{
			player.sendMessage("You can't give Hero items to pet!");
			return;
		}
			
		if (player.getActiveTradeList() != null)
		{
		    player.sendMessage("You can't give items to pet while trading.");
			return;
		}
			
		if(player.isCastingNow())
		{
			return;
		}
		
		if(player.getActiveEnchantItem() != null)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " Tried To Use Enchant Exploit And Got Banned!", IllegalPlayerAction.PUNISH_KICKBAN);
			return;
		}
		
		if (!item.isDropable() || !item.isDestroyable() || !item.isTradeable())
		{
			sendPacket(new SystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS));
			return;
		}

		
		
        L2PetInstance pet = (L2PetInstance)player.getPet();

		if (pet.isDead())
		{
			sendPacket(new SystemMessage(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET));
			return;
		}
		
		if (item.getObjectId() == pet.getControlItemId())
	    {
	      return;
	    }
		
		if(_amount < 0)
		{
			return;
		}

		if (player.transferItem("Transfer", _objectId, _amount, pet.getInventory(), pet) == null)
		{
			_log.warning("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
	}

	@Override
	public String getType()
	{
		return REQUESTCIVEITEMTOPET__C__8B;
	}
}
