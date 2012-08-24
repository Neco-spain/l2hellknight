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
package l2.hellknight.gameserver.network.serverpackets;

import java.util.List;

import javolution.util.FastList;
import javolution.util.FastMap;

import l2.hellknight.gameserver.instancemanager.CastleManorManager.CropProcure;
import l2.hellknight.gameserver.model.L2Manor;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author l3x
 */

public class ExShowSellCropList extends L2GameServerPacket
{
	private int _manorId = 1;
	private final FastMap<Integer, L2ItemInstance> _cropsItems;
	private final FastMap<Integer, CropProcure> _castleCrops;
	
	public ExShowSellCropList(L2PcInstance player, int manorId, List<CropProcure> crops)
	{
		_manorId = manorId;
		_castleCrops = new FastMap<>();
		_cropsItems = new FastMap<>();
		
		FastList<Integer> allCrops = L2Manor.getInstance().getAllCrops();
		for (int cropId : allCrops)
		{
			L2ItemInstance item = player.getInventory().getItemByItemId(cropId);
			if (item != null)
			{
				_cropsItems.put(cropId, item);
			}
		}
		
		for (CropProcure crop : crops)
		{
			if (_cropsItems.containsKey(crop.getId()) && crop.getAmount() > 0)
			{
				_castleCrops.put(crop.getId(), crop);
			}
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x2c);
		
		writeD(_manorId); // manor id
		writeD(_cropsItems.size()); // size
		
		for (L2ItemInstance item : _cropsItems.values())
		{
			writeD(item.getObjectId()); // Object id
			writeD(item.getDisplayId()); // crop id
			writeD(L2Manor.getInstance().getSeedLevelByCrop(item.getItemId())); // seed level
			writeC(0x01);
			writeD(L2Manor.getInstance().getRewardItem(item.getItemId(), 1)); // reward 1 id
			writeC(0x01);
			writeD(L2Manor.getInstance().getRewardItem(item.getItemId(), 2)); // reward 2 id
			
			if (_castleCrops.containsKey(item.getItemId()))
			{
				CropProcure crop = _castleCrops.get(item.getItemId());
				writeD(_manorId); // manor
				writeQ(crop.getAmount()); // buy residual
				writeQ(crop.getPrice()); // buy price
				writeC(crop.getReward()); // reward
			}
			else
			{
				writeD(0xFFFFFFFF); // manor
				writeQ(0x00); // buy residual
				writeQ(0x00); // buy price
				writeC(0x00); // reward
			}
			writeQ(item.getCount()); // my crops
		}
	}
}
