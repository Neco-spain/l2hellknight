package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Creature;

public class NickNameChanged extends L2GameServerPacket
{
	private final int objectId;
	private final String title;

	public NickNameChanged(Creature cha)
	{
		objectId = cha.getObjectId();
		title = cha.getTitle();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xCC);
		writeD(objectId);
		writeS(title);
	}
}