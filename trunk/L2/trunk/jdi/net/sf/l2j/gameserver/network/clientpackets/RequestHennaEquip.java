package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.HennaTable;
import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Henna;

public final class RequestHennaEquip extends L2GameClientPacket
{
  private static final String _C__BC_RequestHennaEquip = "[C] bc RequestHennaEquip";
  private int _symbolId;

  protected void readImpl()
  {
    _symbolId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    L2Henna template = HennaTable.getInstance().getTemplate(_symbolId);

    if (template == null) {
      return;
    }
    L2HennaInstance temp = new L2HennaInstance(template);
    int _count = 0;
    try
    {
      _count = activeChar.getInventory().getItemByItemId(temp.getItemIdDye()).getCount();
    }
    catch (Exception e) {
    }
    if ((_count >= temp.getAmountDyeRequire()) && (activeChar.getAdena() >= temp.getPrice()) && (activeChar.addHenna(temp)))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
      sm.addNumber(temp.getItemIdDye());
      activeChar.sendPacket(sm);
      sm = null;
      activeChar.sendPacket(new SystemMessage(SystemMessageId.SYMBOL_ADDED));

      activeChar.getInventory().reduceAdena("Henna", temp.getPrice(), activeChar, activeChar.getLastFolkNPC());
      L2ItemInstance dyeToUpdate = activeChar.getInventory().destroyItemByItemId("Henna", temp.getItemIdDye(), temp.getAmountDyeRequire(), activeChar, activeChar.getLastFolkNPC());

      InventoryUpdate iu = new InventoryUpdate();
      iu.addModifiedItem(activeChar.getInventory().getAdenaInstance());
      iu.addModifiedItem(dyeToUpdate);
      activeChar.sendPacket(iu);

      ItemList il = new ItemList(((L2GameClient)getClient()).getActiveChar(), true);
      sendPacket(il);
    }
    else
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_DRAW_SYMBOL));
    }
  }

  public String getType()
  {
    return "[C] bc RequestHennaEquip";
  }
}