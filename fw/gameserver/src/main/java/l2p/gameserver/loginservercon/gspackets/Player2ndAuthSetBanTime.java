package l2p.gameserver.loginservercon.gspackets;

import l2p.gameserver.loginservercon.SendablePacket;

public class Player2ndAuthSetBanTime extends SendablePacket {
    private String _login;
    private int _banTime;

    public Player2ndAuthSetBanTime(String login, int banTime) {
        _login = login;
        _banTime = banTime;

    }

    protected void writeImpl() {
        writeC(0x17);
        writeS(_login);
        writeD(_banTime);
    }
}