package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2ShortCut;
import l2rt.gameserver.network.serverpackets.ShortCutRegister;

public class RequestShortCutReg extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _type, _id, _slot, _page, unk1, unk2;

	/**
	 * packet type id 0x3D
	 * format:		cddddd
	 */
	@Override
	public void readImpl()
	{
		_type = readD();
		int slot = readD();
		_id = readD();
		unk1 = readD();
		unk2 = readD(); // UserShortCut

		_slot = slot % 12;
		_page = slot / 12;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_slot < 0 || _slot > 11 || _page < 0 || _page > 10)
		{
			activeChar.sendActionFailed();
			return;
		}

		switch(_type)
		{
			case 0x01: // item
			case 0x03: // action
			case 0x04: // macro
			case 0x05: // recipe
			{
				L2ShortCut sc = new L2ShortCut(_slot, _page, _type, _id, -1);
				sendPacket(new ShortCutRegister(sc));
				activeChar.registerShortCut(sc);
				break;
			}
			case 0x02: // skill
			{
				int level = activeChar.getSkillDisplayLevel(_id);
				if(level > 0)
				{
					L2ShortCut sc = new L2ShortCut(_slot, _page, _type, _id, level);
					sendPacket(new ShortCutRegister(sc));
					activeChar.registerShortCut(sc);
				}
				break;
			}
		}
	}
}