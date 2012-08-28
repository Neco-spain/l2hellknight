package l2p.gameserver.clientpackets;

public class RequestEx2ndPasswordReq extends L2GameClientPacket
{
  private int _changePass;
  private String _password;
  private String _newPassword;

  protected void readImpl()
  {
    _changePass = readC();
    _password = readS();
    if (_changePass == 2)
      _newPassword = readS();
  }

  protected void runImpl()
  {
  }
}