package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2ShortCut;

public class ShortCutRegister extends L2GameServerPacket
{
	private L2ShortCut sc;

	public ShortCutRegister(L2ShortCut _sc)
	{
		sc = _sc;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x44);

		writeD(sc.type);
		writeD(sc.slot + sc.page * 12); // номер слота

		switch(sc.type)
		{
			case L2ShortCut.TYPE_ITEM:
				writeD(sc.getId());
                writeD(sc.getCharacterType());
                writeD(sc.getSharedReuseGroup());
                writeD(0x00); //reuse per second
                writeD(0x00); //?
                writeD(0x00); //?
				break;
			case L2ShortCut.TYPE_SKILL:
				writeD(sc.getId());
                writeD(sc.getLevel());
                writeD(0x00);
                writeD(sc.getCharacterType());
				break;
			default:
				writeD(sc.getId());
                writeD(sc.getCharacterType());
				break;
		}
	}
}