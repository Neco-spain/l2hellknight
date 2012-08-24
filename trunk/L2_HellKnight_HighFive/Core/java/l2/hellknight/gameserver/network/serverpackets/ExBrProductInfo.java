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

import l2.hellknight.gameserver.datatables.ProductItemTable;
import l2.hellknight.gameserver.model.L2ProductItem;
import l2.hellknight.gameserver.model.L2ProductItemComponent;

public class ExBrProductInfo extends L2GameServerPacket
{
	private L2ProductItem _productId;

	public ExBrProductInfo(int id)
	{
		_productId = ProductItemTable.getInstance().getProduct(id);
	}

	@Override
	protected void writeImpl()
	{
		if(_productId == null)
		{
			return;
		}

		writeC(0xFE);
		writeH(0xD7);//writeH(0xBA);

		writeD(_productId.getProductId());  //product id
		writeD(_productId.getPoints());	  // points
		writeD(_productId.getComponents().size());	   //size

		for (L2ProductItemComponent com : _productId.getComponents())
		{
			writeD(com.getItemId());   //item id
			writeD(com.getCount());  //quality
			writeD(com.getWeight()); //weight
			writeD(com.isDropable() ? 1 : 0); //0 - dont drop/trade
		}
	}
}