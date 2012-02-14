package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2SubClass;

public class ExSubjobInfo extends L2GameServerPacket {
	private L2Player player;

	public ExSubjobInfo(L2Player _cha)
	{
		player = _cha;
	}

	protected final void writeImpl() {
		writeC(EXTENDED_PACKET);
		writeH(0xE9);
		writeC(0);
		writeD(player.isAwaking()? player.getAwakingId():player.getClassId().getId());
		writeD(player.getRace().ordinal());
		writeD(player.getSubClasses().size());
		if (player.getSubClasses().size() > 1)
			for (L2SubClass tmp : player.getSubClasses().values()) 
			{
				writeD(player.getClassId().getId()); // WTF??? mb 1
				writeD(tmp.getClassId());
				writeD(tmp.getLevel());
				if (tmp.isBase())
					writeC(0); // 0 - main class, 1 - dual class, 2 - sub class
				else
					writeC(tmp.getDualClass() == 1 ? 1 : 2);
			}

	}

	public final String getType() {
		return "[S] FE:E9 ExSubjobInfo";
	}
}
