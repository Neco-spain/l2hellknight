package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2CubicInstance;
import l2rt.gameserver.model.instances.L2CubicInstance.CubicType;

public class RequestTargetCanceld extends L2GameClientPacket
{
	private int _unselect;

	/**
	 * packet type id 0x48
	 * format:		ch
	 */
	@Override
	public void readImpl()
	{
		_unselect = readH();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		for(L2CubicInstance cubic : activeChar.getCubics())
			if(cubic.getType() != CubicType.LIFE_CUBIC)
				cubic.stopAttackAction();

		if(activeChar.getAgathion() != null)
			activeChar.getAgathion().stopAction();

		if(_unselect == 0)
		{
			if(activeChar.isCastingNow())
			{
				L2Skill skill = activeChar.getCastingSkill();
				activeChar.abortCast(skill != null && (skill.isHandler() || skill.getHitTime() > 5000));
			}
			else if(activeChar.getTarget() != null)
				activeChar.setTarget(null);
		}
		else if(activeChar.getTarget() != null)
			activeChar.setTarget(null);
	}
}