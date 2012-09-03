package l2rt.gameserver.loginservercon.lspackets;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.loginservercon.AttLS;
import l2rt.gameserver.loginservercon.KickWaitingClientTask;
import l2rt.gameserver.loginservercon.LSConnection;
import l2rt.gameserver.loginservercon.SessionKey;
import l2rt.gameserver.loginservercon.gspackets.PlayerInGame;
import l2rt.gameserver.loginservercon.gspackets.PlayerLogout;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.serverpackets.CharacterSelectionInfo;
import l2rt.gameserver.network.serverpackets.LoginFail;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.Stats;

import java.util.logging.Logger;

public class PlayerAuthResponse extends LoginServerBasePacket
{
	private static final Logger log = Logger.getLogger(PlayerAuthResponse.class.getName());

	public PlayerAuthResponse(byte[] decrypt, AttLS loginserver)
	{
		super(decrypt, loginserver);
	}

	@Override
	public void read()
	{
		String account = readS();
		boolean authed = readC() == 1;
		int playOkId1 = readD();
		int playOkId2 = readD();
		int loginOkId1 = readD();
		int loginOkId2 = readD();
		String s_bonus = readS();
        String s_points = readS();
		String account_fields = readS();
		int bonusExpire = readD();

		float bonus = s_bonus == null || s_bonus.equals("") ? 1 : Float.parseFloat(s_bonus);
        int point = s_points == null || s_points.equals("") ? 0 : Integer.parseInt(s_points);

		L2GameClient client = getLoginServer().getCon().removeWaitingClient(account);

		if(client != null)
		{
			if(client.getState() != L2GameClient.GameClientState.CONNECTED)
			{
				log.severe("Trying to authd allready authed client.");
				client.closeNow(true);
				return;
			}

			if(client.getLoginName() == null || client.getLoginName().isEmpty())
			{
				client.closeNow(true);
				log.warning("PlayerAuthResponse: empty accname for " + client);
				return;
			}

			SessionKey key = client.getSessionId();

			if(authed)
				if(getLoginServer().isLicenseShown())
					authed = key.playOkID1 == playOkId1 && key.playOkID2 == playOkId2 && key.loginOkID1 == loginOkId1 && key.loginOkID2 == loginOkId2;
				else
					authed = key.playOkID1 == playOkId1 && key.playOkID2 == playOkId2;

			if(authed)
			{
				client.account_fields = StatsSet.unserialize(account_fields);
				client.setState(L2GameClient.GameClientState.AUTHED);
				client.setBonus(bonus);
				client.setBonusExpire(bonusExpire);
                client.setPoint(point);
				getLoginServer().getCon().addAccountInGame(client);
				CharacterSelectionInfo csi = new CharacterSelectionInfo(client.getLoginName(), client.getSessionId().playOkID1);
				client.sendPacket(csi);
				client.setCharSelection(csi.getCharInfo());
				sendPacket(new PlayerInGame(client.getLoginName(), Stats.getOnline(true)));
			}
			else
			{
				//log.severe("Cheater? SessionKey invalid! Login: " + client.getLoginName() + ", IP: " + client.getIpAddr());
				client.sendPacket(new LoginFail(LoginFail.INCORRECT_ACCOUNT_INFO_CONTACT_CUSTOMER_SUPPORT));
				ThreadPoolManager.getInstance().scheduleGeneral(new KickWaitingClientTask(client), 1000);
				LSConnection.getInstance().sendPacket(new PlayerLogout(client.getLoginName()));
				LSConnection.getInstance().removeAccount(client);
			}
		}
	}
}