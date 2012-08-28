package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;

public final class CharacterRestore extends L2GameClientPacket
{
  private static final String _C__62_CHARACTERRESTORE = "[C] 62 CharacterRestore";
  private int _charSlot;

  protected void readImpl()
  {
    _charSlot = readD();
  }

  protected void runImpl()
  {
    try
    {
      ((L2GameClient)getClient()).markRestoredChar(_charSlot); } catch (Exception e) {
    }
    CharSelectInfo cl = new CharSelectInfo(((L2GameClient)getClient()).getAccountName(), ((L2GameClient)getClient()).getSessionId().playOkID1, 0);
    sendPacket(cl);
    ((L2GameClient)getClient()).setCharSelection(cl.getCharInfo());
  }

  public String getType()
  {
    return "[C] 62 CharacterRestore";
  }
}