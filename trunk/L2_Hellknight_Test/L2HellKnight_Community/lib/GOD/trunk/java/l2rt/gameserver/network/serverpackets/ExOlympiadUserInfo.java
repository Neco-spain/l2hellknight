package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;

public class ExOlympiadUserInfo extends L2GameServerPacket
{
	// cdSddddd
	private int _side, class_id, curHp, maxHp, curCp, maxCp;
	private int obj_id = 0;
	private String _name;

	public ExOlympiadUserInfo(L2Player player, int side)
	{
		_side = side;
		obj_id = player.getObjectId();
		class_id = player.getClassId().getId();
		_name = player.getName();
		curHp = (int) player.getCurrentHp();
		maxHp = player.getMaxHp();
		curCp = (int) player.getCurrentCp();
		maxCp = player.getMaxCp();
		
		if (player.isAwaking())
			class_id = player.getAwakingId();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x7a);
		writeC(_side);
		writeD(obj_id);
		writeS(_name);
		writeD(class_id);
		writeD(curHp);
		writeD(maxHp);
		writeD(curCp);
		writeD(maxCp);
	}
}