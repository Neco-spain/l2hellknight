package l2rt.gameserver.network.serverpackets;

public class Say2 extends L2GameServerPacket
{
	private int _objectId;
	private int _textType;
	private String _charName;
	private String _text;
	private int _charId;
	private int _msgId = -1;

	public Say2(int objectId, int messageType, String charName, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_text = text;
	}

	public Say2(int objectId, int messageType, int charId, int msgId)
	{
		_objectId = objectId;
		_textType = messageType;
		_charId = charId;
		_msgId = msgId;
	}

	protected final void writeImpl()
	{
		writeC(74);
		writeD(this._objectId);
		writeD(this._textType);
		if (this._charName != null)
			writeS(this._charName);
		else
			writeD(this._charId);
		writeD(this._msgId);
		if (this._text != null)
			writeS(this._text);
	}
}