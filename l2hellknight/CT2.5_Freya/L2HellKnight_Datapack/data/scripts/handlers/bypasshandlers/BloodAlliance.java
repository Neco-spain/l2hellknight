
package handlers.bypasshandlers;

import java.util.StringTokenizer;

import l2.hellknight.gameserver.handler.IBypassHandler;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2CastleWarehouseInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;

public class BloodAlliance implements IBypassHandler
{

        private static final String[] COMMANDS = {
                "HonoraryItem",
                "Receive",
                "Exchange"
        };

        @Override
        public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
        {
                if (!(target instanceof L2Npc))
                        return false;
                
                L2CastleWarehouseInstance npc = ((L2CastleWarehouseInstance)target);
                
                try
                {
                        NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
                        StringTokenizer st = new StringTokenizer(command, " ");
                        String actualCommand = st.nextToken(); // Get actual command
                        
                        if (actualCommand.equalsIgnoreCase(COMMANDS[0]))
                        {
                                if (activeChar.isClanLeader())
                                {
                                        html.setFile(activeChar.getHtmlPrefix(), "data/html/castlewarehouse/castlewarehouse-4.htm");
                                        html.replace("%blood%", Integer.toString(npc.getCastle().getBloodAlliance()));
                                }
                                else
                                {
                                        html.setFile(activeChar.getHtmlPrefix(), "data/html/castlewarehouse/castlewarehouse-3.htm");
                                }
                        }
                        else if (actualCommand.equalsIgnoreCase(COMMANDS[1]))
                        {
                                int count = npc.getCastle().getBloodAlliance();
                                if (count == 0)
                                {
                                        html.setFile(activeChar.getHtmlPrefix(), "data/html/castlewarehouse/castlewarehouse-5.htm");
                                }
                                else
                                {
                                        activeChar.addItem("BloodAlliance", 9911, count, activeChar, true);
                                        npc.getCastle().setBloodAlliance(0);
                                        html.setFile(activeChar.getHtmlPrefix(), "data/html/castlewarehouse/castlewarehouse-6.htm");
                                }
                        }
                        else if (actualCommand.equalsIgnoreCase(COMMANDS[2]))
                        {
                                if (activeChar.getInventory().getInventoryItemCount(9911, -1) > 0)
                                {
                                        activeChar.destroyItemByItemId("BloodAllianceExchange", 9911, 1, activeChar, true);
                                        activeChar.addItem("BloodAllianceExchange", 9910, 30, activeChar, true);
                                        html.setFile(activeChar.getHtmlPrefix(), "data/html/castlewarehouse/castlewarehouse-7.htm");
                                }
                                else
                                {
                                        html.setFile(activeChar.getHtmlPrefix(), "data/html/castlewarehouse/castlewarehouse-8.htm");    
                                }
                                
                        }
                        html.replace("%objectId%", String.valueOf(npc.getObjectId()));
                        activeChar.sendPacket(html);
                        return true;
                }
                catch (Exception e)
                {
                        _log.info("Exception in " + getClass().getSimpleName());
                }
                return false;
        }

        @Override
        public String[] getBypassList()
        {
                return COMMANDS ;
        }
}