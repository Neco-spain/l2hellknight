package l2p.loginserver.gameservercon.gspackets;


import l2p.loginserver.accounts.SecondaryPasswordAuth;
import l2p.loginserver.gameservercon.ReceivablePacket;

public class Player2ndAuthSetBanTime extends ReceivablePacket {
    private String _account;
    private int _banTime;

    @Override
    protected void readImpl() {
        _account = readS();
        _banTime = readD();
    }

    @Override
    protected void runImpl() {
        SecondaryPasswordAuth.setBanTime(_account, _banTime);
    }
}