package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.HennaTable;
import net.sf.l2j.gameserver.datatables.HennaTreeTable;
import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.templates.L2Henna;

public final class RequestHennaEquip extends L2GameClientPacket
{
  private int _symbolId;

  protected void readImpl()
  {
    try
    {
      _symbolId = readD();
    } catch (BufferUnderflowException e) {
      _symbolId = -1;
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    L2Henna template = HennaTable.getInstance().getTemplate(_symbolId);
    if (template == null) {
      return;
    }

    L2HennaInstance dye = new L2HennaInstance(template);
    int _count = 0;

    boolean c = true;
    for (L2HennaInstance h : HennaTreeTable.getInstance().getAvailableHenna(player.getClassId())) {
      if (h.getSymbolId() == dye.getSymbolId()) {
        c = false;
        break;
      }
    }

    if (c) {
      player.sendPacket(Static.CANT_DRAW_SYMBOL);
      player.sendActionFailed();
      return;
    }
    try
    {
      _count = player.getInventory().getItemByItemId(dye.getItemIdDye()).getCount();
    }
    catch (Exception e) {
    }
    if ((player.getAdena() < dye.getPrice()) || (_count < dye.getAmountDyeRequire())) {
      player.sendMessage("\u041F\u0440\u043E\u0432\u0435\u0440\u044C \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u0438 \u043A\u043E\u043B\u0438\u0447\u0435\u0441\u0442\u0432\u043E \u043A\u0440\u0430\u0441\u043E\u043A");
      player.sendActionFailed();
      return;
    }

    if (player.addHenna(dye)) {
      player.getInventory().reduceAdena("Henna", dye.getPrice(), player, player.getLastFolkNPC());
      player.destroyItemByItemId("HennaEquip", dye.getItemIdDye(), dye.getAmountDyeRequire(), player.getLastFolkNPC(), true);

      player.sendPacket(Static.SYMBOL_ADDED);
      player.sendItems(false);
      player.sendChanges();
    }
  }
}