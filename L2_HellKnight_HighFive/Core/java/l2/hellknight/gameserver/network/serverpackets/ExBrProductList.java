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

import java.util.Collection;

import l2.hellknight.gameserver.datatables.ProductItemTable;
import l2.hellknight.gameserver.model.L2ProductItem;

public class ExBrProductList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xD6);//writeH(0xb9);
		Collection<L2ProductItem> items = ProductItemTable.getInstance().getAllItems();
		writeD(items.size());

		for (L2ProductItem template : items)
		{
			writeD(template.getProductId());  //product id
			writeH(template.getCategory()); //category 1 - enchant 2 - supplies  3 - decoration 4 - package 5 - other
			writeD(template.getPoints()); //points
			writeD(0x00);// show tab 2-th group - 1 ?????????? ?????? ??? ???? 1 - event 2 - best 3 - event & best
			writeD(0x00); //start sale
			writeD(0x00); //end sale
			writeC(0x00);	//day week
			writeC(0x00);   //start hour
			writeC(0x00);   //start min
			writeC(0x00);   //end hour
			writeC(0x00);  //end min
			writeD(0x00); //current stock
			writeD(0x00); //max stock
		}
	}
}