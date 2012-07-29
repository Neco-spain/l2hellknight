package l2p.gameserver.loginservercon.gspackets;

import l2p.gameserver.loginservercon.SendablePacket;

public class Player2ndAuthSetAttempts extends SendablePacket {
    private String _login;
    private int _attempts;

    public Player2ndAuthSetAttempts(String login, int attempts) {
        _login = login;
        _attempts = attempts;

    }

    protected void writeImpl() {
        writeC(0x16);
        writeS(_login);
        writeD(_attempts);
    }
}