package net.sf.l2j.gameserver.network.clientpackets;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CharSelected;

public class CharacterSelected extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(CharacterSelected.class.getName());
  private int _charSlot;
  private int _unk1;
  private int _unk2;
  private int _unk3;
  private int _unk4;

  protected void readImpl()
  {
    _charSlot = readD();
    _unk1 = readH();
    _unk2 = readD();
    _unk3 = readD();
    _unk4 = readD();
  }

  protected void runImpl()
  {
    if (((L2GameClient)getClient()).getActiveCharLock().tryLock())
    {
      try
      {
        if (((L2GameClient)getClient()).getActiveChar() == null)
        {
          L2PcInstance cha = ((L2GameClient)getClient()).loadCharFromDisk(_charSlot);
          if (cha == null) { _log.severe("Character could not be loaded (slot:" + _charSlot + ")");
            sendPacket(new ActionFailed());
            return; }
          if (cha.getAccessLevel() < 0) {
            cha.closeNetConnection();
            return;
          }
          cha.setClient((L2GameClient)getClient());
          ((L2GameClient)getClient()).setActiveChar(cha);

          ((L2GameClient)getClient()).sendGameGuardRequest();
          ((L2GameClient)getClient()).setState(L2GameClient.GameClientState.IN_GAME);

          sendPacket(new CharSelected(cha, ((L2GameClient)getClient()).getSessionId().playOkID1));
        }
      }
      finally
      {
        ((L2GameClient)getClient()).getActiveCharLock().unlock();
      }
    }
  }
}