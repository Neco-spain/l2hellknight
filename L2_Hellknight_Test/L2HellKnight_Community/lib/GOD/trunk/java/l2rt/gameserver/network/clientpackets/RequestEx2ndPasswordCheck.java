package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.gameserver.network.serverpackets.Ex2ndPasswordCheck;

public class RequestEx2ndPasswordCheck extends L2GameClientPacket
{
	private static final String _C__D0_AD_REQUESTEX2NDPASSWORDCHECK = "[C] D0:AD RequestEx2ndPasswordCheck";
	
	@Override
	public void readImpl()
	{
	}
	@Override
	public void runImpl()
	{
		if (!Config.SECOND_AUTH_ENABLED || getClient().getSecondaryAuth().isAuthed())
		{
			sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.PASSWORD_OK));
			return;
		}
		
		getClient().getSecondaryAuth().openDialog();
	}

}
