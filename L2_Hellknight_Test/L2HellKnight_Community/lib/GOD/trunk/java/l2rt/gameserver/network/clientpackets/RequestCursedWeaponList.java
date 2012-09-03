package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.instancemanager.CursedWeaponsManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.network.serverpackets.ExCursedWeaponList;
import l2rt.util.GArray;

public class RequestCursedWeaponList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Character activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		GArray<Integer> list = new GArray<Integer>();
		for(int id : CursedWeaponsManager.getInstance().getCursedWeaponsIds())
			list.add(id);

		activeChar.sendPacket(new ExCursedWeaponList(list));
	}
}