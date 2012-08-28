/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;

import javolution.util.FastList;
import javolution.util.FastMap;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.CharSchemesTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CustomZoneManager;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class TopBBSBuffManager extends BaseBBSManager
{
	  public int[] TableId=Config.BUFFS_LIST;
	  public int[] TableDialog=Config.BUFFER_TABLE_DIALOG;

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
	 */
	@SuppressWarnings("unused")
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if(command.equals("_bbstop"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/index.htm");
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/index.htm' </center></body></html>";
			}
			separateAndSend(content,activeChar);
		}
	   	else if (command.equals("_bbsbuff_warior3"))
    	{
	   		String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
    		
    		if(activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_FIGHTER_3)
			{
    			activeChar.sendMessage("Не хватает монет");
			    activeChar.sendPacket(new ActionFailed());
				return;
				
			}
            if (activeChar.isDead())
            {
            	activeChar.sendMessage("Вы не можете бафаться когда мертвы");
            	return;
            }
            if (Olympiad.getInstance().isRegisteredInComp(activeChar) || activeChar.getOlympiadGameId() > 0)
            {
            	activeChar.sendMessage("You cant not buff in olympiad mode");
                return;
            }
            if (activeChar.getPvpFlag() != 0)
            {
            	activeChar.sendMessage("Вы не можете использовать функцию восстановления в PvP");
            	return;
            }
    		if (activeChar.isMounted()||activeChar.getActiveTradeList() != null||activeChar.isCastingNow()||activeChar.getActiveEnchantItem() != null || TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
    		{
    			activeChar.sendPacket(SystemMessage.sendString("неблагоприятные условия для использования бафера"));
    		    return;
    		}
            if(CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
            {
            	activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
            	activeChar.sendPacket(new ActionFailed());
                return;
            }
    		activeChar.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_FIGHTER_3, activeChar, false);
			SkillTable.getInstance().getInfo(1068,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1040,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1086,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1204,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1077,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1242,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1268,4).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1035,4).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1036,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1045,6).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1388,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1363,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(271,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(275,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(274,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(269,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(264,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(304,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(364,1).getEffects(activeChar,activeChar);
			activeChar.setCurrentHpMp(activeChar.getMaxHp(),(activeChar.getMaxMp()));
			activeChar.setCurrentCp(activeChar.getMaxCp());
            separateAndSend(content,activeChar);
            activeChar.sendPacket(new ActionFailed());
            return;
    	}
    	//Buff Warior 2 lvl
    	else if (command.startsWith("_bbsbuff_warior2"))
    	{
    		String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
    		if(activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_FIGHTER_2)
			{
    			activeChar.sendMessage("Не хватает монет");
			    activeChar.sendPacket(new ActionFailed());
				return;
			}
            if (activeChar.isDead())
            {
            	activeChar.sendMessage("Вы не можете бафаться когда мертвы");
            	return;
            }
            if (activeChar.getPvpFlag() != 0)
            {
            	activeChar.sendMessage("Вы не можете использовать функцию восстановления в PvP");
            	return;
            }
            if (Olympiad.getInstance().isRegisteredInComp(activeChar) || activeChar.getOlympiadGameId() > 0)
            {
            	activeChar.sendMessage("You cant not buff in olympiad mode");
                return;
            }
    		if (activeChar.isMounted()||activeChar.getActiveTradeList() != null||activeChar.isCastingNow()||activeChar.getActiveEnchantItem() != null || TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
    		{
    			activeChar.sendPacket(SystemMessage.sendString("неблагоприятные условия для использования бафера"));
    		    return;
    		}
            if(CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
            {
            	activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
            	activeChar.sendPacket(new ActionFailed());
                return;
            }
    		activeChar.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_FIGHTER_2, activeChar, false);
			SkillTable.getInstance().getInfo(1068,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1040,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1086,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1204,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1077,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1242,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1268,4).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1035,4).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1036,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(271,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(275,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(274,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(269,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(264,1).getEffects(activeChar,activeChar);
			activeChar.setCurrentHpMp(activeChar.getMaxHp(),(activeChar.getMaxMp()));
			activeChar.setCurrentCp(activeChar.getMaxCp());
		    separateAndSend(content,activeChar);
            activeChar.sendPacket(new ActionFailed());
            return;
    	}
    	//Buff Warior 1 lvl
    	else if (command.startsWith("_bbsbuff_warior1"))
    	{
    		String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
    		if(activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_FIGHTER_1)
			{
    			activeChar.sendMessage("Не хватает монет");
			    separateAndSend(content,activeChar);
				return;
			}
            if (activeChar.isDead())
            {
            	activeChar.sendMessage("Вы не можете бафаться когда мертвы");
            	return;
            }
            if (activeChar.getPvpFlag() != 0)
            {
            	activeChar.sendMessage("Вы не можете использовать функцию восстановления в PvP");
            	return;
            }
            if (Olympiad.getInstance().isRegisteredInComp(activeChar) || activeChar.getOlympiadGameId() > 0)
            {
            	activeChar.sendMessage("You cant not buff in olympiad mode");
                return;
            }
    		if (activeChar.isMounted()||activeChar.getActiveTradeList() != null||activeChar.isCastingNow()||activeChar.getActiveEnchantItem() != null || TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
    		{
    			activeChar.sendPacket(SystemMessage.sendString("неблагоприятные условия для использования бафера"));
    		    return;
    		}
            if(CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
            {
            	activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
            	activeChar.sendPacket(new ActionFailed());
                return;
            }
    		activeChar.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_FIGHTER_1, activeChar, false);
			SkillTable.getInstance().getInfo(1068,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1040,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1086,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1204,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1077,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1242,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1268,4).getEffects(activeChar,activeChar);		
			activeChar.setCurrentHpMp(activeChar.getMaxHp(),(activeChar.getMaxMp()));
			activeChar.setCurrentCp(activeChar.getMaxCp());
		    separateAndSend(content,activeChar);
		    activeChar.sendPacket(new ActionFailed());
		    return;
    	}
		//Buff mage 3 lvl
    	else if (command.startsWith("_bbsbuff_mage3"))
    	{
    		String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
    		
			if(activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_MAGE_3)
			{
				activeChar.sendMessage("Не хватает монет");
				separateAndSend(content,activeChar);
				return;
			}
            if (activeChar.isDead())
            {
            	activeChar.sendMessage("Вы не можете бафаться когда мертвы");
            	return;
            }
            if (activeChar.getPvpFlag() != 0)
            {
            	activeChar.sendMessage("Вы не можете использовать функцию восстановления в PvP");
            	return;
            }
            if (Olympiad.getInstance().isRegisteredInComp(activeChar) || activeChar.getOlympiadGameId() > 0)
            {
            	activeChar.sendMessage("You cant not buff in olympiad mode");
                return;
            }
    		if (activeChar.isMounted()||activeChar.getActiveTradeList() != null||activeChar.isCastingNow()||activeChar.getActiveEnchantItem() != null || TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
    		{
    			activeChar.sendPacket(SystemMessage.sendString("неблагоприятные условия для использования бафера"));
    		    return;
    		}
            if(CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
            {
            	activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
            	activeChar.sendPacket(new ActionFailed());
                return;
            }
			activeChar.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_MAGE_3, activeChar, false);
			SkillTable.getInstance().getInfo(1085,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1059,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1078,6).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1204,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1048,6).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1397,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1303,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1040,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1035,4).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1062,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(273,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(276,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(349,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(363,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(365,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1413,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1036,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1389,3).getEffects(activeChar,activeChar);					
			activeChar.setCurrentHpMp(activeChar.getMaxHp(),(activeChar.getMaxMp()));
			activeChar.setCurrentCp(activeChar.getMaxCp());
			separateAndSend(content,activeChar);
			activeChar.sendPacket(new ActionFailed());
			return;
    	}
    	//Buff msge 2 lvl
    	else if (command.startsWith("_bbsbuff_mage2"))
    	{
    		String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
			if(activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_MAGE_2)
			{
				activeChar.sendMessage("Не хватает монет");
				separateAndSend(content,activeChar);
				return;
			}
	           if (activeChar.isDead())
	            {
	            	activeChar.sendMessage("Вы не можете бафаться когда мертвы");
	            	return;
	            }
	            if (activeChar.getPvpFlag() != 0)
	            {
	            	activeChar.sendMessage("Вы не можете использовать функцию восстановления в PvP");
	            	return;
	            }
	            if (Olympiad.getInstance().isRegisteredInComp(activeChar) || activeChar.getOlympiadGameId() > 0)
	            {
	            	activeChar.sendMessage("You cant not buff in olympiad mode");
	                return;
	            }
	    		if (activeChar.isMounted()||activeChar.getActiveTradeList() != null||activeChar.isCastingNow()||activeChar.getActiveEnchantItem() != null || TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
	    		{
	    			activeChar.sendPacket(SystemMessage.sendString("неблагоприятные условия для использования бафера"));
	    		    return;
	    		}
	            if(CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
	            {
	            	activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
	            	activeChar.sendPacket(new ActionFailed());
	                return;
	            }
			activeChar.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_MAGE_2, activeChar, false);
			SkillTable.getInstance().getInfo(1085,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1059,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1078,6).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1204,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1048,6).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1397,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1303,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1040,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1035,4).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1062,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(273,1).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(276,1).getEffects(activeChar,activeChar);										
			SkillTable.getInstance().getInfo(349,1).getEffects(activeChar,activeChar);
			activeChar.setCurrentHpMp(activeChar.getMaxHp(),(activeChar.getMaxMp()));
			activeChar.setCurrentCp(activeChar.getMaxCp());
            separateAndSend(content,activeChar);
            activeChar.sendPacket(new ActionFailed());
            return;
    	}
    	//Buff mage 1 lvl
    	else if (command.startsWith("_bbsbuff_mage1"))
    	{
    		String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
    		
			if(activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_MAGE_1)
			{
				activeChar.sendMessage("Не хватает монет");
				separateAndSend(content,activeChar);
				return;
			}
	           if (activeChar.isDead())
	            {
	            	activeChar.sendMessage("Вы не можете бафаться когда мертвы");
	            	return;
	            }
	            if (activeChar.getPvpFlag() != 0)
	            {
	            	activeChar.sendMessage("Вы не можете использовать функцию восстановления в PvP");
	            	return;
	            }
	            if (Olympiad.getInstance().isRegisteredInComp(activeChar) || activeChar.getOlympiadGameId() > 0)
	            {
	            	activeChar.sendMessage("You cant not buff in olympiad mode");
	                return;
	            }
	    		if (activeChar.isMounted()||activeChar.getActiveTradeList() != null||activeChar.isCastingNow()||activeChar.getActiveEnchantItem() != null || TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
	    		{
	    			activeChar.sendPacket(SystemMessage.sendString("неблагоприятные условия для использования бафера"));
	    		    return;
	    		}
	            if(CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
	            {
	            	activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
	            	activeChar.sendPacket(new ActionFailed());
	                return;
	            }
	            activeChar.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_MAGE_1, activeChar, false);
			SkillTable.getInstance().getInfo(1085,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1059,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1078,6).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1204,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1048,6).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1397,3).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1303,2).getEffects(activeChar,activeChar);					
			SkillTable.getInstance().getInfo(1040,3).getEffects(activeChar,activeChar);							
			activeChar.setCurrentHpMp(activeChar.getMaxHp(),(activeChar.getMaxMp()));
			activeChar.setCurrentCp(activeChar.getMaxCp());
			separateAndSend(content,activeChar);
			activeChar.sendPacket(new ActionFailed());
			return;
    	}
	   	if(command.startsWith("_bbsbuff_buff"))
        {
    		String filename = "data/html/CommunityBoard/buff/4000";
    		//String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001");
    		String v;
            int cmdChoice;
            int id;
            int dialog;
            int level;
            cmdChoice = Integer.parseInt(command.substring(14, 16).trim());
    		if(activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_OTHER)
			{
    			activeChar.sendMessage("Не хватает монет");
				return;
			}
    		activeChar.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_OTHER, activeChar, false);

			 if (activeChar.isDead()) 
			 {
				 activeChar.sendMessage("Вы не можете использовать баффера когда мертвы.");
	              return;
	         }
	            id = this.TableId[cmdChoice];
	            dialog = this.TableDialog[cmdChoice];
	            level = SkillTable.getInstance().getMaxLevel(id, 0);
	            if (id == 4554)
	              level = 4;

	            if (id == 4553)
	              level = 4;
	            if (id == 4551)
	              level = 4;
	            if (id == 4552)
	              level = 4;

	              v = Integer.toString(dialog);
	

	            activeChar.stopSkillEffects(id);

	            if (activeChar.getShowAnim())
	            {
	            	activeChar.broadcastPacket(new MagicSkillUser(activeChar,activeChar,id,level,350,150));
	            }

	            SkillTable.getInstance().getInfo(id, level).getEffects(activeChar, activeChar);
	            String content = HtmCache.getInstance().getHtm(filename + v + ".htm");
	            separateAndSend(content,activeChar);
	            activeChar.sendPacket(new ActionFailed());
	            return;
        }
    	else if(command.startsWith("_bbsbuff_save"))
        {
    		String content;
            int cmdChoice = Integer.parseInt(command.substring(14, 15).trim());
            int flag=0;
			if(cmdChoice>3)
			{
				content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/400011.htm");
				flag=1;
			}
            else
            	content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
			CreateScheme(activeChar,Integer.toString(cmdChoice),flag);
			separateAndSend(content,activeChar);
            activeChar.sendPacket(new ActionFailed());
            return;
        }
        else if(command.startsWith("_bbsbuff_give"))
        {
        	String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/400011.htm");
            int cmdChoice = Integer.parseInt(command.substring(14, 15).trim());
            if((cmdChoice<1)&&(cmdChoice>6))return;
            String key=HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/400011"),sKey=Integer.toString(cmdChoice);
            int flag=0;
            NpcHtmlMessage html = new NpcHtmlMessage(1);

            if(cmdChoice>3)
            {
                flag=1;
                key=HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40008");
            }
            if (activeChar.isDead())
            {
            	activeChar.sendMessage("Вы не можете восстанавливаться когда мертвы");
            	separateAndSend(content,activeChar);
            	return;
            }
            if (CharSchemesTable.getInstance().getScheme(activeChar.getObjectId(),sKey)!=null)
            {
            	activeChar.stopAllEffects();
                if(flag==0)
                {
                    for (L2Skill sk : CharSchemesTable.getInstance().getScheme(
                    		activeChar.getObjectId(),sKey))
					{
                    	activeChar.stopSkillEffects(sk.getId());
                        sk.getEffects(activeChar, activeChar);
					}
                }
                else
                {
                    for (L2Skill sk : CharSchemesTable.getInstance().getScheme(
                    		activeChar.getObjectId(),sKey))
					{
                        L2Summon pet = activeChar.getPet();
                        if(pet!=null)
                        {
                            pet.stopSkillEffects(sk.getId());
                            sk.getEffects(activeChar, pet);
                        }
                    }
                }
                content = HtmCache.getInstance().getHtm(key+".htm");
            }
            else
            {
            	activeChar.sendMessage("Профиль "+sKey+" не найден");
                return;
            }
            separateAndSend(content,activeChar);
            activeChar.sendPacket(new ActionFailed());
            return;
        }
    	else if(command.startsWith("_bbsbuff_regen"))
        {
    		String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
            if (activeChar.getPvpFlag() != 0)
            {
            	activeChar.sendMessage("Вы не можете использовать функцию восстановления в PvP");
            	return;
            }
            if (activeChar.isDead())
            {
            	activeChar.sendMessage("Вы не можете бафаться когда мертвы");
            	return;
            }
            if (Olympiad.getInstance().isRegisteredInComp(activeChar) || activeChar.getOlympiadGameId() > 0)
            {
            	activeChar.sendMessage("You cant not buff in olympiad mode");
                return;
            }
    		if (activeChar.isMounted()||activeChar.getActiveTradeList() != null||activeChar.isCastingNow()||activeChar.getActiveEnchantItem() != null || TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
    		{
    			activeChar.sendPacket(SystemMessage.sendString("неблагоприятные условия для использования бафера"));
    		    return;
    		}
            if(CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
            {
            	activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
            	activeChar.sendPacket(new ActionFailed());
                return;
            }
            activeChar.setCurrentHpMp(activeChar.getMaxHp(),(activeChar.getMaxMp()));
            activeChar.setCurrentCp(activeChar.getMaxCp());
		    separateAndSend(content,activeChar);
            activeChar.sendPacket(new ActionFailed());
            return;
        }
      	else if (command.startsWith("_bbsbuff_cancel"))
    	{
            if (activeChar.getPvpFlag() != 0)
            {
            	activeChar.sendMessage("Вы не можете использовать функцию восстановления в PvP");
            	return;
            }
            if (activeChar.isDead())
            {
            	activeChar.sendMessage("Вы не можете бафаться когда мертвы");
            	return;
            }
            if (Olympiad.getInstance().isRegisteredInComp(activeChar) || activeChar.getOlympiadGameId() > 0)
            {
            	activeChar.sendMessage("You cant not buff in olympiad mode");
                return;
            }
    		if (activeChar.isMounted()||activeChar.getActiveTradeList() != null||activeChar.isCastingNow()||activeChar.getActiveEnchantItem() != null || TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
    		{
    			activeChar.sendPacket(SystemMessage.sendString("неблагоприятные условия для использования бафера"));
    		    return;
    		}
            if(CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
            {
            	activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
            	activeChar.sendPacket(new ActionFailed());
                return;
            }
      		String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
      		activeChar.stopAllEffects();
      		separateAndSend(content,activeChar);
      		activeChar.sendPacket(new ActionFailed());
      		return;
    	}
		else if(command.startsWith("_bbsbuff;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			int idp = Integer.parseInt(st.nextToken());
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/"+idp+".htm");
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/buff/"+idp+".htm' </center></body></html>";
			}
			separateAndSend(content,activeChar);
		}
		else
		{
		ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: "+command+" is not implemented yet</center><br><br></body></html>","101");
		activeChar.sendPacket(sb);
		activeChar.sendPacket(new ShowBoard(null,"102"));
		activeChar.sendPacket(new ShowBoard(null,"103"));
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsewrite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		// TODO Auto-generated method stub

	}
	
    private void CreateScheme(L2PcInstance player,String name,int flag)
	{
			if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) != null
			&& CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).containsKey(name))
			{
				CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).remove(name);
			}
			if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) == null)
                {
                    CharSchemesTable.getInstance().getSchemesTable().put(player.getObjectId(),
                            new FastMap<String, FastList<L2Skill>>(6));
                }
				CharSchemesTable.getInstance().setScheme(player.getObjectId(),name.trim(),
						new FastList<L2Skill>(69));
             L2Effect[] s;
            if (flag==0)
            {
                 s= player.getAllEffects();
                 
            }
			else
			{
			L2Summon pet=player.getPet();
            s=pet.getAllEffects();
			}
            int Id;
            Boolean Ok=false;
        int i = 0;
        while (i < s.length) {
            L2Effect value = s[i];
            Id = value.getSkill().getId();
            int k = 0;
            while (k < TableId.length) {
                if (Id == TableId[k]) {
                    Ok = true;
                    break;
                }
                k++;
            }
            if (Ok)
                CharSchemesTable.getInstance().getScheme(
                        player.getObjectId(), name).add(
                        SkillTable.getInstance().getInfo(Id, value.getSkill().getLevel()));

            Ok = false;
            i++;
        }
        if (name.equals(Integer.toString(4)))
        {
        	player.sendMessage("Текущие баффы успешно обновлены");
        }
        else
        {
        player.sendMessage("Профиль "+name+" успешно сохранён");
        }
    }

	private static TopBBSBuffManager _instance = new TopBBSBuffManager();

	/**
	 * @return
	 */
	public static TopBBSBuffManager getInstance()
	{
		return _instance;
	}

}