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

import l2.hellknight.gameserver.network.NpcStringId;
import l2.hellknight.gameserver.network.SystemMessageId;

/**
 * @author Kerberos
 */
public class ExShowScreenMessage extends L2GameServerPacket
{
	private final int _type;
	private final int _sysMessageId;
	private final int _unk1;
	private final int _unk2;
	private final int _unk3;
	private final boolean _fade;
	private final int _size;
	private final int _position;
	private boolean _effect;
	private final String _text;
	private final int _time;
	private final int _npcString;
	private List<String> _parameters;
	
	public ExShowScreenMessage(String text, int time)
	{
		_type = 1;
		_sysMessageId = -1;
		_unk1 = 0;
		_unk2 = 0;
		_unk3 = 0;
		_fade = false;
		_position = 0x02;
		_text = text;
		_time = time;
		_size = 0;
		_effect = false;
		_npcString = -1;
	}
	
	public ExShowScreenMessage(NpcStringId npcString, int position, int time) // For npcstring
	{
		_type = 2;
		_sysMessageId = -1;
		_unk1 = 0;
		_unk2 = 0;
		_unk3 = 0;
		_fade = false;
		_position = position;
		_text = null;
		_time = time;
		_size = 0;
		_effect = false;
		_npcString = npcString.getId();
	}
	
	public ExShowScreenMessage(SystemMessageId systemMsg, int position, int time) // For SystemMessage
	{
		_type = 2;
		_sysMessageId = systemMsg.getId();
		_unk1 = 0;
		_unk2 = 0;
		_unk3 = 0;
		_fade = false;
		_position = position;
		_text = null;
		_time = time;
		_size = 0;
		_effect = false;
		_npcString = -1;
	}
	
	public ExShowScreenMessage(int type, int messageId, int position, int unk1, int size, int unk2, int unk3, boolean showEffect, int time, boolean fade, String text, NpcStringId npcString)
	{
		_type = type;
		_sysMessageId = messageId;
		_unk1 = unk1;
		_unk2 = unk2;
		_unk3 = unk3;
		_fade = fade;
		_position = position;
		_text = text;
		_time = time;
		_size = size;
		_effect = showEffect;
		_npcString = npcString.getId();
	}
	
	/**
	 * String parameter for argument S1,S2,.. in npcstring-e.dat
	 * @param text
	 */
	public void addStringParameter(String text)
	{
		if (_parameters == null)
			_parameters = new ArrayList<>();
		_parameters.add(text);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x39);
		writeD(_type); // 0 - system messages, 1 - your defined text, 2 - npcstring
		writeD(_sysMessageId); // system message id (_type must be 0 otherwise no effect)
		writeD(_position); // message position
		writeD(_unk1); // ?
		writeD(_size); // font size 0 - normal, 1 - small
		writeD(_unk2); // ?
		writeD(_unk3); // ?
		writeD(_effect == true ? 1 : 0); // upper effect (0 - disabled, 1 enabled) - _position must be 2 (center) otherwise no effect
		writeD(_time); // time
		writeD(_fade == true ? 1 : 0); // fade effect (0 - disabled, 1 enabled)
		writeD(_npcString); // npcString
		if (_npcString == -1)
		{
			writeS(_text); // your text (_type must be 1, otherwise no effect)
		}
		else
		{
			if (_parameters != null)
			{
				for (String s : _parameters)
					writeS(s);
			}
		}
	}

	public ExShowScreenMessage setUpperEffect(boolean value)
	{
		_effect = value;
		return this;
	}
}
