package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.CharDeleteFail;
import net.sf.l2j.gameserver.network.serverpackets.CharDeleteOk;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;

public final class CharacterDelete extends L2GameClientPacket
{
  private static final String _C__0C_CHARACTERDELETE = "[C] 0C CharacterDelete";
  private static Logger _log = Logger.getLogger(CharacterDelete.class.getName());
  private int _charSlot;

  protected void readImpl()
  {
    _charSlot = readD();
  }

  protected void runImpl()
  {
    if (Config.DEBUG) _log.fine("deleting slot:" + _charSlot);

    L2PcInstance character = null;
    try
    {
      if (Config.DELETE_DAYS == 0)
        character = ((L2GameClient)getClient()).deleteChar(_charSlot);
      else
        character = ((L2GameClient)getClient()).markToDeleteChar(_charSlot);
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Error:", e);
    }

    if (character == null)
    {
      sendPacket(new CharDeleteOk());
    }
    else
    {
      if (character.isClanLeader())
      {
        sendPacket(new CharDeleteFail(3));
      }
      else
      {
        sendPacket(new CharDeleteFail(2));
      }
      if (character.isChatBanned())
      {
        sendPacket(new CharDeleteFail(1));
      }
    }

    CharSelectInfo cl = new CharSelectInfo(((L2GameClient)getClient()).getAccountName(), ((L2GameClient)getClient()).getSessionId().playOkID1, 0);
    sendPacket(cl);
    ((L2GameClient)getClient()).setCharSelection(cl.getCharInfo());
  }

  public String getType()
  {
    return "[C] 0C CharacterDelete";
  }
}