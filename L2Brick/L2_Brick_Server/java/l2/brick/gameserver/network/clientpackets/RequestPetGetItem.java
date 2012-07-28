/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2.brick.gameserver.network.clientpackets;

import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.instancemanager.FortSiegeManager;
import l2.brick.gameserver.instancemanager.MercTicketManager;
import l2.brick.gameserver.model.L2World;
import l2.brick.gameserver.model.actor.instance.L2PetInstance;
import l2.brick.gameserver.model.actor.instance.L2SummonInstance;
import l2.brick.gameserver.model.item.instance.L2ItemInstance;
import l2.brick.gameserver.network.serverpackets.ActionFailed;

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.4 $ $Date: 2005/03/29 23:15:33 $
 */
public final class RequestPetGetItem extends L2GameClientPacket
{
	private static final String _C__98_REQUESTPETGETITEM = "[C] 98 RequestPetGetItem";
	//private static Logger _log = Logger.getLogger(RequestPetGetItem.class.getName());
	
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2World world = L2World.getInstance();
		L2ItemInstance item = (L2ItemInstance)world.findObject(_objectId);
		if (item == null || getClient().getActiveChar() == null)
			return;
		
		int castleId = MercTicketManager.getInstance().getTicketCastleId(item.getItemId());
		if (castleId > 0) {
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if(getClient().getActiveChar().getPet() instanceof L2SummonInstance)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2PetInstance pet = (L2PetInstance)getClient().getActiveChar().getPet();
		if (pet == null || pet.isDead() || pet.isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if(FortSiegeManager.getInstance().isCombat(item.getItemId()) )
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		pet.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item);
	}
	
	@Override
	public String getType()
	{
		return _C__98_REQUESTPETGETITEM;
	}
}
