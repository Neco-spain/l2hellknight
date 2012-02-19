package l2.brick.gameserver.network.serverpackets;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.util.BotPunish;

public final class MoveToLocation extends L2GameServerPacket
{
	private static final String _S__01_CHARMOVETOLOCATION = "[S] 2f MoveToLocation";
	private int _charObjId, _x, _y, _z, _xDst, _yDst, _zDst;
	private L2Character _cha;
	
	public MoveToLocation(L2Character cha)
	{
		_cha = cha;
		_charObjId = cha.getObjectId();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_xDst = cha.getXdestination();
		_yDst = cha.getYdestination();
		_zDst = cha.getZdestination();
	}
	
	@Override
	protected final void writeImpl()
	{
		// Bot punishment restriction
		if(_cha instanceof L2PcInstance && Config.ENABLE_BOTREPORT)
		{
			L2PcInstance actor = (L2PcInstance) _cha;
			if(actor.isBeingPunished())
			{
				if(actor.getPlayerPunish().canWalk() && actor.getPlayerPunish().getBotPunishType() == BotPunish.Punish.MOVEBAN)
				{
					actor.endPunishment();
				}
				else
				{
					actor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REPORTED_120_MINS_WITHOUT_MOVE));
					return;
				}
			}
		}
		writeC(0x2f);
		
		writeD(_charObjId);
		
		writeD(_xDst);
		writeD(_yDst);
		writeD(_zDst);
		
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
	
	/* (non-Javadoc)
	 * @see l2.brick.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__01_CHARMOVETOLOCATION;
	}
	
}
