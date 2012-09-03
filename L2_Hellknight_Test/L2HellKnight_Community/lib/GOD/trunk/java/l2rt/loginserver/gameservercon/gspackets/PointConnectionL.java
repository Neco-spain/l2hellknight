package l2rt.loginserver.gameservercon.gspackets;

import java.sql.SQLException;

import l2rt.loginserver.L2LoginClient;
import l2rt.loginserver.LoginController;
import l2rt.loginserver.gameservercon.AttGS;

public class PointConnectionL extends ClientBasePacket
{

    public PointConnectionL(byte decrypt[], AttGS gameserver)
    {
        super(decrypt, gameserver);
    }

    public void read()
    {
        String acc = readS();
        int point = readD();
        LoginController lc = LoginController.getInstance();
        L2LoginClient client = lc.getAuthedClient(acc);
        try {
			client.setPointL(point);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
