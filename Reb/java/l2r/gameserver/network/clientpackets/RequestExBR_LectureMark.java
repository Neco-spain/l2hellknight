package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;

/**
 * @author VISTALL
 */
public class RequestExBR_LectureMark extends L2GameClientPacket
{
	public static final int INITIAL_MARK = 1;
	public static final int EVANGELIST_MARK = 2;
	public static final int OFF_MARK = 3;

	private int _mark;

	@Override
	protected void readImpl() throws Exception
	{
		_mark = readC();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || !Config.EX_LECTURE_MARK)
			return;

		switch(_mark)
		{
			case INITIAL_MARK:
			case EVANGELIST_MARK:
			case OFF_MARK:
				//TODO [VISTALL] проверить ли можно включать - от первого чара 6 месяцев
				player.setLectureMark(_mark);
				player.broadcastUserInfo(true);
				break;
		}
	}
}