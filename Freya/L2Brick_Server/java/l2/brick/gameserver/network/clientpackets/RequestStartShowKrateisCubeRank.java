package l2.brick.gameserver.network.clientpackets;

import l2.brick.gameserver.model.actor.instance.L2PcInstance;
//import l2.brick.gameserver.network.serverpackets.ExPVPMatchCCRecord;
import l2.brick.gameserver.network.serverpackets.ActionFailed;

public class RequestStartShowKrateisCubeRank extends L2GameClientPacket
{
	private static final String _C__51_REQUESTSTARTSHOWKRATEISCUBERANK = "[C] 51 RequestStartShowKrateisCubeRank";
	@Override
	protected void readImpl()
	{
		// trigger packet
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
        if (player == null) return;

		//sendPacket(new ExPVPMatchCCRecord(2, ExPVPMatchCCRecord.EMPTY_ARRAY));

        player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	@Override
	public String getType()
	{
		return _C__51_REQUESTSTARTSHOWKRATEISCUBERANK;
	}
}