package l2.hellknight.gameserver.instancemanager;

import l2.hellknight.ExternalConfig;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.base.ClassId;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.util.Rnd;

public class PcCafePointsManager
{
	private static PcCafePointsManager _instance;
	
	public static PcCafePointsManager getInstance()
	{
		if (_instance == null)
			_instance = new PcCafePointsManager();
		return _instance;
	}
	
	public PcCafePointsManager()
	{
	}
	
	public void givePcCafePoint(final L2PcInstance player, final long givedexp)
	{
		if (!ExternalConfig.PC_BANG_ENABLED)
			return;
		
		if (player.isInsideZone(L2Character.ZONE_PEACE) || player.isInsideZone(L2Character.ZONE_PVP) || player.isInsideZone(L2Character.ZONE_SIEGE) || player.isOnlineInt() == 0 || player.isInJail())
			return;
		
		if (player.getPcBangPoints() >= ExternalConfig.MAX_PC_BANG_POINTS)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_MAXMIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED);
			player.sendPacket(sm);
			return;
		}
		int _points = (int) (givedexp * 0.0001 * ExternalConfig.PC_BANG_POINT_RATE);
		if (player.getActiveClass() == ClassId.archmage.getId() || player.getActiveClass() == ClassId.soultaker.getId() || player.getActiveClass() == ClassId.stormScreamer.getId() || player.getActiveClass() == ClassId.mysticMuse.getId())
			_points /= 2;
		
		if (ExternalConfig.RANDOM_PC_BANG_POINT)
			_points = Rnd.get(_points / 2, _points);
		
		boolean doublepoint = false;
		SystemMessage sm = null;
		if (_points > 0)
		{
			if (ExternalConfig.ENABLE_DOUBLE_PC_BANG_POINTS
					&& Rnd.get(100) < ExternalConfig.DOUBLE_PC_BANG_POINTS_CHANCE)
			{
				_points *= 2;
				sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_PCPOINT_DOUBLE);
				doublepoint = true;
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
			}
			if (player.getPcBangPoints() + _points > ExternalConfig.MAX_PC_BANG_POINTS)
				_points = ExternalConfig.MAX_PC_BANG_POINTS-player.getPcBangPoints();
			sm.addNumber(_points);
			player.sendPacket(sm);
			player.setPcBangPoints(player.getPcBangPoints() + _points);
			player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), _points, true, doublepoint, 1));
		}
	}
}
