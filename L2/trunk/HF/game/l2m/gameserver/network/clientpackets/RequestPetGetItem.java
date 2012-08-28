package l2m.gameserver.network.clientpackets;

import l2m.gameserver.ai.CtrlIntention;
import l2m.gameserver.ai.SummonAI;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Summon;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.utils.ItemFunctions;

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