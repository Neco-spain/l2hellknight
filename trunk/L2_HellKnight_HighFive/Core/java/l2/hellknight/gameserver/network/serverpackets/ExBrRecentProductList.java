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

/*public class ExBrRecentProductList extends L2GameClientPacket
{
    List<PrimeShopManager.ItemMallItem> list;

    public ExBrRecentProductList(int objId) {
        list = PrimeShopManager.getInstance().getRecentListByOID(objId);
    }

    @Override
    protected void writeImpl() {
        writeC(0xFE);
        writeH(0xD1);
        writeD(list.size());
        for (PrimeShopManager.ItemMallItem item : list) {
            writeD(item.template.brId);
            writeH(item.template.category);
            writeD(item.price);
            int cat = 0;
            if (item.iSale >= 2) {
                switch (item.iCategory2) {
                    case 0:
                    case 2:
                        cat = 2;
                        break;
                    case 1:
                        cat = 3;
                        break;
                }
            }
            writeD(cat);
            if (item.iStartSale > 0 && item.iEndSale > 0) {
                writeD(item.iStartSale);
                writeD(item.iEndSale);
            } else {
                writeD(0x12CEDE40);
                writeD(0x7ECE3CD0);
            }
            writeC(0x7F);
            writeC(item.iStartHour);
            writeC(item.iStartMin);
            writeC(item.iEndHour);
            writeC(item.iEndMin);
            writeD(item.iStock);
            writeD(item.iMaxStock);
        }
    }
}*/