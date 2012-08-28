package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class ChristmasTree
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5560, 5561 };

  private static final int[] NPC_IDS = { 13006, 13007 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    L2PcInstance activeChar = (L2PcInstance)playable;
    L2NpcTemplate template1 = null;

    int itemId = item.getItemId();
    for (int i = 0; i < ITEM_IDS.length; i++)
    {
      if (ITEM_IDS[i] != itemId)
        continue;
      template1 = NpcTable.getInstance().getTemplate(NPC_IDS[i]);
      break;
    }

    if (template1 == null) {
      return;
    }
    L2Object target = activeChar.getTarget();
    if (target == null) {
      target = activeChar;
    }
    try
    {
      L2Spawn spawn = new L2Spawn(template1);
      spawn.setId(IdFactory.getInstance().getNextId());
      spawn.setLocx(target.getX());
      spawn.setLocy(target.getY());
      spawn.setLocz(target.getZ());
      L2World.getInstance().storeObject(spawn.spawnOne(true));

      activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);

      SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
      sm.addString("Created " + template1.name + " at x: " + spawn.getLocx() + " y: " + spawn.getLocy() + " z: " + spawn.getLocz());
      activeChar.sendPacket(sm);
    }
    catch (Exception e)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
      sm.addString("Target is not ingame.");
      activeChar.sendPacket(sm);
    }
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}