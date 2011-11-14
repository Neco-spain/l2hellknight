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
package com.l2js.gameserver.network.serverpackets;

import java.util.logging.Logger;

import com.l2js.gameserver.model.L2ItemInstance;
import com.l2js.gameserver.model.entity.Message;
import com.l2js.gameserver.model.itemcontainer.ItemContainer;

/**
 * @author Migi, DS
 */
public class ExShowReceivedPost extends L2GameServerPacket
{
	private static final String _S__FE_AB_EXSHOWRECEIVEDPOST = "[S] FE:AB ExShowReceivedPost";
	private static final Logger _log = Logger.getLogger(ExShowReceivedPost.class.getName());

	private Message _msg;
	private L2ItemInstance[] _items = null;

	public ExShowReceivedPost(Message msg)
	{
		_msg = msg;
		if (msg.hasAttachments())
		{
			final ItemContainer attachments = msg.getAttachments();
			if (attachments != null && attachments.getSize() > 0)
				_items = attachments.getItems();
			else
				_log.warning("Message "+msg.getId()+" has attachments but itemcontainer is empty.");
		}
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0xab);
		writeD(_msg.getId());
		writeD(_msg.isLocked() ? 1 : 0);
		writeD(0x00); //Unknown
		writeS(_msg.getSenderName());
		writeS(_msg.getSubject());
		writeS(_msg.getContent());

		if (_items != null && _items.length > 0)
		{
			writeD(_items.length);
			for (L2ItemInstance item : _items)
			{
				writeH(item.getItem().getType2());
				writeD(0x00); // unknown
				writeD(item.getItemId());
				writeQ(item.getCount());

				writeD(0x00); // unknown
				writeD(item.getEnchantLevel()); 
				writeH(item.getCustomType2());
				writeH(0x00); // unknown
				


				writeD(item.isAugmented() ? item.getAugmentation().getAugmentationId() : 0x00);

				writeD(0x00); // unknown 

				writeH(item.getAttackElementType());
				writeH(item.getAttackElementPower());
				for (byte i = 0; i < 6; i++)
					writeH(item.getElementDefAttr(i));

				writeH(0x00); // Enchant effect 1
				writeH(0x00); // Enchant effect 2
				writeH(0x00); // Enchant effect 3 
			}
			_items = null;
		}
		else
			writeD(0x00);

		writeQ(_msg.getReqAdena());

		writeD(0x01); // Unknown why 1
		writeD(0x00); // Unknown

		_msg = null;
	}

	@Override
	public String getType()
	{
		return _S__FE_AB_EXSHOWRECEIVEDPOST;
	}
}
