package l2p.gameserver.loginservercon.gspackets;

import l2p.gameserver.loginservercon.SendablePacket;

public class Player2ndAuthSetPassword extends SendablePacket {
    private String _login;
    private String _password;

    public Player2ndAuthSetPassword(String login, String pwd) {
        _login = login;
        _password = pwd;

    }

    protected void writeImpl() {
        writeC(0x15);
        writeS(_login);
        writeS(_password);
    }
}