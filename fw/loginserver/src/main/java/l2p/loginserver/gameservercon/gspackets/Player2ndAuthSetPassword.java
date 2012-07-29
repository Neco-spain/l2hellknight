package l2p.loginserver.gameservercon.gspackets;


import l2p.loginserver.accounts.SecondaryPasswordAuth;
import l2p.loginserver.gameservercon.ReceivablePacket;


public class Player2ndAuthSetPassword extends ReceivablePacket {
    private String _account;
    private String _password;

    @Override
    protected void readImpl() {
        _account = readS();
        _password = readS();
    }

    @Override
    protected void runImpl() {
        SecondaryPasswordAuth.setPassword(_account, _password);
    }
}