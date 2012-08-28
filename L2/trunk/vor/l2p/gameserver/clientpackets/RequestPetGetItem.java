package l2p.gameserver.clientpackets;

import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.SummonAI;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Summon;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.utils.ItemFunctions;

public class RequestPetGetItem extends L2GameClientPacket
{
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (activeChar.isOutOfControl())
    {
      activeChar.sendActionFailed();
      return;
    }

    Summon summon = activeChar.getPet();
    if ((summon == null) || (!summon.isPet()) || (summon.isDead()) || (summon.isActionsDisabled()))
    {
      activeChar.sendActionFailed();
      return;
    }

    ItemInstance item = (ItemInstance)activeChar.getVisibleObject(_objectId);
    if (item == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    if (!ItemFunctions.checkIfCanPickup(summon, item))
    {
      SystemMessage sm;
      if (item.getItemId() == 57)
      {
        SystemMessage sm = new SystemMessage(55);
        sm.addNumber(item.getCount());
      }
      else
      {
        sm = new SystemMessage(56);
        sm.addItemName(item.getItemId());
      }
      sendPacket(sm);
      activeChar.sendActionFailed();
      return;
    }

    summon.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item, null);
  }
}