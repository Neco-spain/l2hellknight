package l2rt.gameserver.network.serverpackets;

public class ExShowScreenMessage extends L2GameServerPacket
{
	public static enum ScreenMessageAlign
	{
		TOP_LEFT,
		TOP_CENTER,
		TOP_RIGHT,
		MIDDLE_LEFT,
		MIDDLE_CENTER,
		MIDDLE_RIGHT,
		BOTTOM_CENTER,
		BOTTOM_RIGHT,
	}

	private int _type, _sysMessageId;
	private boolean _big_font, _effect;
	private ScreenMessageAlign _text_align;
	private int _time;
	private String _text;

	private int _unk1 = 0;
	private int _unk2 = 0;
	private int _unk3 = 0;
	private int _unk4 = 1;

	public ExShowScreenMessage(String text, int time, ScreenMessageAlign text_align, boolean big_font)
	{
		_type = 1;
		_sysMessageId = -1;
		_text = text;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = false;
	}

	public ExShowScreenMessage(String text, int time, ScreenMessageAlign text_align)
	{
		this(text, time, text_align, true);
	}

	public ExShowScreenMessage(String text, int time)
	{
		this(text, time, ScreenMessageAlign.TOP_CENTER);
	}

	public ExShowScreenMessage(String text, int time, ScreenMessageAlign text_align, boolean big_font, int type, int messageId, boolean showEffect)
	{
		_type = type;
		_sysMessageId = messageId;
		_text = text;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = showEffect;
	}
	
	public ExShowScreenMessage(int messageId, int time, ScreenMessageAlign text_align, boolean big_font, int type, boolean showEffect)
	{
		_sysMessageId = messageId;
		_type = type;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = showEffect;
	}

	public ExShowScreenMessage(String text, int time, ScreenMessageAlign text_align, boolean big_font, int type, int messageId, boolean showEffect, int unk1, int unk2, int unk3, int unk4)
	{
		this(text, time, text_align, big_font, type, messageId, showEffect);
		_unk1 = unk1;
		_unk2 = unk2;
		_unk3 = unk3;
		_unk4 = unk4;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x39);

		writeD(_type); // 0 - system messages, 1 - your defined text
		writeD(_sysMessageId); // system message id (_type must be 0 otherwise no effect)
		writeD(_text_align.ordinal() + 1); // размещение текста
		writeD(_unk1); // ?
		writeD(_big_font ? 0 : 1); // размер текста
		writeD(_unk2); // ?
		writeD(_unk3); // ?
		writeD(_effect == true ? 1 : 0); // upper effect (0 - disabled, 1 enabled) - _position must be 2 (center) otherwise no effect
		writeD(_time); // время отображения сообщения в милисекундах
		writeD(_unk4); // ?
		writeD(-1);// NpcStrings
		writeS(_text); // текст сообщения
	}
}