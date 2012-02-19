package l2.brick.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;

public class ExShowScreenMessageNPCString extends L2GameServerPacket
{
	private static final int _type = 1;
	private static final int _sysMessageId = 0;
	private final int _position;
	private final int _unk1;
	private final int _size;
	private final int _unk2;
	private final int _unk3;
	private final boolean _effect;
	private final int _time;
	private final boolean _fade;
	private final int _npcString;
	private final String _text = "";
	private List<String> _parameters;
	
	public static final int
		TOP_LEFT = 1, TOP_CENTER = 2, TOP_RIGHT = 3, MIDDLE_LEFT = 4, MIDDLE_CENTER = 5, MIDDLE_RIGHT = 6, BOTTOM_CENTER = 7, BOTTOM_RIGHT = 8;
	public static final int
		SIZE_LARGE = 0, SIZE_SMALL = 1;
	
	public ExShowScreenMessageNPCString(int npcStringId, int time)
	{
		_position = ExShowScreenMessageNPCString.TOP_CENTER;
		_unk1 = 0;
		_size = ExShowScreenMessageNPCString.SIZE_LARGE;
		_unk2 = 0;
		_unk3 = 0;
		_effect = false;
		_time = time;
		_fade = false;
		_npcString = npcStringId;
	}
	
	public ExShowScreenMessageNPCString(int type, int messageId, int position, int unk1, int size, int unk2, int unk3, boolean showEffect, int time, boolean fade, int npcStringId)
	{
		_position = position;
		_unk1 = unk1;
		_size = size;
		_unk2 = unk2;
		_unk3 = unk3;
		_effect = showEffect;
		_time = time;
		_fade = fade;
		_npcString = npcStringId;
	}
	
	public ExShowScreenMessageNPCString addStringParameter(String text)
	{
		if (_parameters == null)
			_parameters = new ArrayList<String>();
		_parameters.add(text);
		return this;
	}
	public ExShowScreenMessageNPCString addString(String text)
	{
		return addStringParameter(text);
	}
	public ExShowScreenMessageNPCString addPcName(L2PcInstance pc)
	{
		return addStringParameter(pc.getName());
	}
	public ExShowScreenMessageNPCString addNpcName(L2Npc npc)
	{
		return addStringParameter(npc.getName());
	}
	public ExShowScreenMessageNPCString addNumber(int number)
	{
		return addStringParameter(String.valueOf(number));
	}
	
	@Override
	public String getType()
	{
		return "[S]FE:39 ExShowScreenMessage";
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x39);
		writeD(_type); // 0 - system messages, 1 - your defined text
		writeD(_sysMessageId); // system message id (_type must be 0 otherwise no effect)
		writeD(_position); // message position
		writeD(_unk1); // ?
		writeD(_size); // font size 0 - normal, 1 - small
		writeD(_unk2); // ?
		writeD(_unk3); // ?
		writeD(_effect ? 1 : 0); // upper effect (0 - disabled, 1 enabled) - _position must be 2 (center) otherwise no effect
		writeD(_time); // time
		writeD(_fade ? 1 : 0); // 1 - fade in/out, 0 - no fade
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
}
