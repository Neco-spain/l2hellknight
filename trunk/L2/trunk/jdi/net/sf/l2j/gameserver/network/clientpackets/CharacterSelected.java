package net.sf.l2j.gameserver.network.clientpackets;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CharSelected;
import net.sf.protection.nProtect;

public class CharacterSelected extends L2GameClientPacket
{
  private static final String _C__0D_CHARACTERSELECTED = "[C] 0D CharacterSelected";
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
          if (Config.DEBUG)
          {
            _log.fine("selected slot:" + _charSlot);
          }

          L2PcInstance cha = ((L2GameClient)getClient()).loadCharFromDisk(_charSlot);
          if (cha == null) { _log.severe("Character could not be loaded (slot:" + _charSlot + ")");
            sendPacket(new ActionFailed());
            return; }
          if (cha.getAccessLevel() < 0)
          {
            cha.closeNetConnection(true);
            return;
          }
          cha.setClient((L2GameClient)getClient());
          ((L2GameClient)getClient()).setActiveChar(cha);
          ((L2GameClient)getClient()).setEnterWorld(true);

          if ((cha.isGM()) && (Config.CHECK_IS_GM_BY_ID))
          {
            boolean isOk = false;
            for (int GmId : Config.GM_LIST_ID) {
              if (GmId != cha.getObjectId())
                continue;
              _log.info(cha.getName() + " enter GM access");
              isOk = true;
              break;
            }
            if (!isOk)
            {
              cha.setAccessLevel(0);
              cha.setIsGM(false);
              _log.warning(cha.getName() + " bad login GM, change access to 0!");
            }

          }

          nProtect.getInstance().sendRequest((L2GameClient)getClient());
          ((L2GameClient)getClient()).setState(L2GameClient.GameClientState.IN_GAME);
          CharSelected cs = new CharSelected(cha, ((L2GameClient)getClient()).getSessionId().playOkID1);
          sendPacket(cs);
        }

      }
      finally
      {
        ((L2GameClient)getClient()).getActiveCharLock().unlock();
      }
    }
  }

  public String getType()
  {
    return "[C] 0D CharacterSelected";
  }
}