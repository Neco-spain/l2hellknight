package l2m.gameserver.loginservercon.gspackets;

import l2m.gameserver.loginservercon.SendablePacket;

public class ChangePassword extends SendablePacket
{
  private String _account;
  private String _oldPass;
  private String _newPass;
  private String _hwid;

  public ChangePassword(String account, String oldPass, String newPass, String hwid)
  {
    _account = account;
    _oldPass = oldPass;
    _newPass = newPass;
    _hwid = hwid;
  }

  protected void writeImpl()
  {
    writeC(8);
    writeS(_account);
    writeS(_oldPass);
    writeS(_newPass);
    writeS(_hwid);
  }
}