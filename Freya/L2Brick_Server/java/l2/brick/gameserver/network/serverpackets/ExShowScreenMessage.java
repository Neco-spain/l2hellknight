package l2.brick.gameserver.network.serverpackets;

public class ExShowScreenMessage extends L2GameServerPacket
{
	// Screen positions for text
	public static final byte POSITION_TOP_LEFT = 0x01;
	public static final byte POSITION_TOP_CENTER = 0x02;
	public static final byte POSITION_TOP_RIGHT = 0x03;
	public static final byte POSITION_MIDDLE_LEFT = 0x04;
	public static final byte POSITION_MIDDLE_CENTER = 0x05;
	public static final byte POSITION_MIDDLE_RIGHT = 0x06;
	public static final byte POSITION_LOW_CENTER = 0x07;
	public static final byte POSITION_LOW_RIGHT = 0x08;
	
	// Font Size
	public static final byte FONT_SIZE_NORMAL = 0;
	public static final byte FONT_SIZE_SMALL = 1;
	
	// Text Type
	public static final byte TEXT_TYPE_SYSTEMMESSAGE = 0;
	public static final byte TEXT_TYPE_CUSTOM = 1;
	
	private int _type;
	private int _sysMessageId;
	private int _unk1;
	private int _unk2;
	private int _unk3;
	private int _unk4;
	private int _size;
	private int _position;
	private boolean _effect;
	private String _text;
	private int _time;
	private int _clientMessageId;
	
	public ExShowScreenMessage(String text, int time)
	{
		_type = TEXT_TYPE_CUSTOM;
		_sysMessageId = -1;
		_unk1 = 0;
		_unk2 = 0;
		_unk3 = 0;
		_unk4 = 0;
		_position = POSITION_TOP_CENTER;
		_text = text;
		_time = time;
		_size = FONT_SIZE_NORMAL;
		_effect = false;
	}

	public ExShowScreenMessage(int type, int messageId, int position, int unk1, int size, int unk2, int unk3, boolean showEffect, int time, int unk4, String text)
	{
		_type = type;
		_sysMessageId = messageId;
		_unk1 = unk1;
		_unk2 = unk2;
		_unk3 = unk3;
		_unk4 = unk4;
		_position = (showEffect ? POSITION_TOP_CENTER : position);
		_text = text;
		_time = time;
		_size = size;
		_effect = showEffect;
	}

    public ExShowScreenMessage(int clientMsgId, int time, int position, boolean big_font, boolean type, int messageId, boolean showEffect)
	{
		_type = type ? 0x00 : 0x01;
		_sysMessageId = messageId;
		_time = time;
		_position = position;
		_size = big_font ? 0x00 : 0x01;
		_effect = showEffect;
        _clientMessageId = clientMsgId;
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
		writeD(_effect == true ? 1 : 0); // upper effect (0 - disabled, 1 enabled) - _position must be 2 (center) otherwise no effect
		writeD(_time); // time
		writeD(_unk4); // ?
		writeD(_clientMessageId); //TODO: npcString // may be done
		
		writeS(_text); // your text (_type must be 1, otherwise no effect)
	}
	
	@Override
	public String getType()
	{
		return "[S]FE:39 ExShowScreenMessage";
	}
}