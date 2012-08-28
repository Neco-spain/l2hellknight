package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.model.security.SecondaryPasswordAuth;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.Ex2ndPasswordAck;

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
    if (!Config.EX_SECOND_PASSWORD) {
      return;
    }
    SecondaryPasswordAuth spa = ((GameClient)getClient()).getSecondaryAuth();
    boolean exVal = false;

    if ((_changePass == 0) && (!spa.passwordExist()))
      exVal = spa.savePassword(_password);
    else if ((_changePass == 2) && (spa.passwordExist())) {
      exVal = spa.changePassword(_password, _newPassword);
    }
    if (exVal)
      ((GameClient)getClient()).sendPacket(new Ex2ndPasswordAck(0));
  }
}