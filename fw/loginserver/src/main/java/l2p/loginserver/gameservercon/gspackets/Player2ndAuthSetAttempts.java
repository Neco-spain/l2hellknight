package l2p.loginserver.gameservercon.gspackets;


import l2p.loginserver.accounts.SecondaryPasswordAuth;
import l2p.loginserver.gameservercon.ReceivablePacket;


public class Player2ndAuthSetAttempts extends ReceivablePacket {
    private String _account;
    private int _attempts;

    @Override
    protected void readImpl() {
        _account = readS();
        _attempts = readD();
    }

    @Override
    protected void runImpl() {
        SecondaryPasswordAuth.setLoginAttempts(_account, _attempts);
    }
}