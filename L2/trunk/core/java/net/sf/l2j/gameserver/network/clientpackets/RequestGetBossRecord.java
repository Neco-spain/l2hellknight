
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.instancemanager.RaidBossPointsManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExGetBossRecord;

/**
 * Format: (ch) d
 * 
 * @author -Wooden-
 */
public class RequestGetBossRecord extends L2GameClientPacket
{
	private static final String _C__D0_18_REQUESTGETBOSSRECORD = "[C] D0:18 RequestGetBossRecord";
	@SuppressWarnings("unused")
	private int _bossId;

	@Override
	protected void readImpl()
	{
		_bossId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		int points = RaidBossPointsManager.getPointsByOwnerId(activeChar.getObjectId());
		int ranking = RaidBossPointsManager.calculateRanking(activeChar.getObjectId());

		// trigger packet
		activeChar.sendPacket(new ExGetBossRecord(ranking, points, RaidBossPointsManager.getList(activeChar)));
		sendPacket(new ActionFailed());
	}

	@Override
	public String getType()
	{
		return _C__D0_18_REQUESTGETBOSSRECORD;
	}

}
