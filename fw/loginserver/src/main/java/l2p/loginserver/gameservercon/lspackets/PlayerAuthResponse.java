package l2p.loginserver.gameservercon.lspackets;

import l2p.loginserver.SessionKey;
import l2p.loginserver.accounts.Account;
import l2p.loginserver.accounts.SecondaryPasswordAuth;
import l2p.loginserver.accounts.SessionManager.Session;
import l2p.loginserver.gameservercon.SendablePacket;

public class PlayerAuthResponse extends SendablePacket {
    private String login;
    private boolean authed;
    private int playOkID1;
    private int playOkID2;
    private int loginOkID1;
    private int loginOkID2;
    private double bonus;
    private int bonusExpire;
    private String _2ndPassword;
    private int _2ndWrongAttempts;
    private long _2ndUnbanTime;

    public PlayerAuthResponse(Session session, boolean authed) {
        Account account = session.getAccount();
        this.login = account.getLogin();
        this.authed = authed;
        if (authed) {
            SessionKey skey = session.getSessionKey();
            playOkID1 = skey.playOkID1;
            playOkID2 = skey.playOkID2;
            loginOkID1 = skey.loginOkID1;
            loginOkID2 = skey.loginOkID2;
            bonus = account.getBonus();
            bonusExpire = account.getBonusExpire();
            _2ndPassword = SecondaryPasswordAuth.getPassword(login);
            _2ndWrongAttempts = SecondaryPasswordAuth.getLoginAttempts(login);
            _2ndUnbanTime = SecondaryPasswordAuth.getBanTime(login);
        }
    }

    public PlayerAuthResponse(String account) {
        this.login = account;
        authed = false;
    }

    @Override
    protected void writeImpl() {
        writeC(0x02);
        writeS(login);
        writeC(authed ? 1 : 0);
        if (authed) {
            writeD(playOkID1);
            writeD(playOkID2);
            writeD(loginOkID1);
            writeD(loginOkID2);
            writeF(bonus);
            writeD(bonusExpire);
            writeS(_2ndPassword);
            writeD(_2ndWrongAttempts);
            writeQ(_2ndUnbanTime);
        }
    }
}
