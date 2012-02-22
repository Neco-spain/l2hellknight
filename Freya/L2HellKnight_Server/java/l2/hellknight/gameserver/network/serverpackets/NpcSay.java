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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kerberos
 */
public final class NpcSay extends L2GameServerPacket
{
	// cddddS
	private static final String _S__30_NPCSAY = "[S] 30 NpcSay";
	private int _objectId;
	private int _textType;
	private int _npcId;
	private String _text;
	private int _npcString;
	private List<String> _parameters;
	
	/**
	 * @param _characters
	 */
	public NpcSay(int objectId, int messageType, int npcId, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000+npcId;
		_text = text;
	}
	
	public NpcSay(int objectId, int messageType, int npcId, int npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000+npcId;
		_npcString = npcString;
	}
	
	/**
	 * String parameter for argument S1,S2,.. in npcstring-e.dat
	 * @param text
	 */
	public void addStringParameter(String text)
	{
		if (_parameters == null)
			_parameters = new ArrayList<String>();
		_parameters.add(text);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x30);
		writeD(_objectId);
		writeD(_textType);
		writeD(_npcId);
		writeD(_npcString);
		if (_npcString == 0)
			writeS(_text);
		else
		{
			if (_parameters != null)
			{
				for (String s : _parameters)
					writeS(s);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see l2.hellknight.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__30_NPCSAY;
	}
}