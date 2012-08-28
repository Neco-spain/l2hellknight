package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.CharSchemesTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2BuffInstance extends L2NpcInstance
{
	  public int[] TableId;
	  public int[] TableDialog;
    String _curHtm = null;


    public L2BuffInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		TableId=Config.BUFFS_LIST;
		TableDialog=Config.BUFFER_TABLE_DIALOG;
    }


	public String getHtmlPath(int npcId, int val)
	{
		String pom;

		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "data/html/buff/" + pom + ".htm";
	}


    public void onBypassFeedback(L2PcInstance player, String command)
    {
    	
    	StringTokenizer st = new StringTokenizer(command, " ");
    	String cmd = st.nextToken(); //получаем команду
    	if (cmd.startsWith("chat"))
    	{
    		String file = "data/html/buff/"+getNpcId()+".htm";
    		int cmdChoice = Integer.parseInt(command.substring(5,7).trim());
    		if(cmdChoice>0)
			{
				file = "data/html/buff/"+getNpcId()+"-"+cmdChoice+".htm";
			}
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            _curHtm = file;
		    html.setFile(file);
            sendHtmlMessage(player,html);
		    player.sendPacket(new ActionFailed());
    	}
    	else if (cmd.startsWith("cancel"))
    	{
    		player.stopAllEffects();
    		NpcHtmlMessage html = new NpcHtmlMessage(1);
		    html.setFile("data/html/buff/"+getNpcId()+".htm");
            sendHtmlMessage(player,html);
		    player.sendPacket(new ActionFailed());
    	}
    	//Buff Warior 3 lvl
    	else if (command.startsWith("warior3"))
    	{
    		String file = "data/html/buff/40001-1.htm";
    		
    		if(player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_FIGHTER_3)
			{
				player.sendMessage("Не хватает монет");
	            NpcHtmlMessage html = new NpcHtmlMessage(1);
			    html.setFile(file);
			    player.sendPacket(new ActionFailed());
				return;
				
			}
			player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_FIGHTER_3, player, false);
			SkillTable.getInstance().getInfo(1068,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1040,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1086,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1204,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1077,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1242,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1268,4).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1035,4).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1036,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1045,6).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1388,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1363,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(271,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(275,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(274,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(269,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(264,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(304,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(364,1).getEffects(this,player);
        	player.setCurrentHpMp(player.getMaxHp(),(player.getMaxMp()));
            player.setCurrentCp(player.getMaxCp());
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setFile(file);
            sendHtmlMessage(player,html);
		    player.sendPacket(new ActionFailed());
    	}
    	//Buff Warior 2 lvl
    	else if (cmd.startsWith("warior2"))
    	{
    		String file = "data/html/buff/40001-1.htm";
    		if(player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_FIGHTER_2)
			{
				player.sendMessage("Не хватает монет");
	            NpcHtmlMessage html = new NpcHtmlMessage(1);
			    html.setFile(file);
			    player.sendPacket(new ActionFailed());
				return;
			}
			player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_FIGHTER_2, player, false);
			SkillTable.getInstance().getInfo(1068,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1040,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1086,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1204,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1077,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1242,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1268,4).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1035,4).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1036,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(271,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(275,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(274,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(269,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(264,1).getEffects(this,player);
        	player.setCurrentHpMp(player.getMaxHp(),(player.getMaxMp()));
            player.setCurrentCp(player.getMaxCp());
            NpcHtmlMessage html = new NpcHtmlMessage(1);
		    html.setFile(file);
            sendHtmlMessage(player,html);
		    player.sendPacket(new ActionFailed());
    	}
    	//Buff Warior 1 lvl
    	else if (cmd.startsWith("warior1"))
    	{
    		String file = "data/html/buff/40001-1.htm";
    		if(player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_FIGHTER_1)
			{
				player.sendMessage("Не хватает монет");
	            NpcHtmlMessage html = new NpcHtmlMessage(1);
			    html.setFile(file);
				return;
			}
			player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_FIGHTER_1, player, false);
			SkillTable.getInstance().getInfo(1068,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1040,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1086,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1204,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1077,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1242,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1268,4).getEffects(this,player);		
        	player.setCurrentHpMp(player.getMaxHp(),(player.getMaxMp()));
            player.setCurrentCp(player.getMaxCp());
            NpcHtmlMessage html = new NpcHtmlMessage(1);
		    html.setFile(file);
            sendHtmlMessage(player,html);
		    player.sendPacket(new ActionFailed());
    	}
    	//Buff mage 3 lvl
    	else if (command.startsWith("mage3"))
    	{
    		String file = "data/html/buff/40001-1.htm";
    		
			if(player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_MAGE_3)
			{
				player.sendMessage("Не хватает монет");
	            NpcHtmlMessage html = new NpcHtmlMessage(1);
			    html.setFile(file);
				return;
			}
			player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_MAGE_3, player, false);
			SkillTable.getInstance().getInfo(1085,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1059,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1078,6).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1204,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1048,6).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1397,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1303,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1040,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1035,4).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1062,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(273,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(276,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(349,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(363,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(365,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1413,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1036,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1389,3).getEffects(this,player);					
        	player.setCurrentHpMp(player.getMaxHp(),(player.getMaxMp()));
            player.setCurrentCp(player.getMaxCp());
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setFile(file);
            sendHtmlMessage(player,html);
		    player.sendPacket(new ActionFailed());
    	}
    	//Buff msge 2 lvl
    	else if (cmd.startsWith("mage2"))
    	{
    		String file = "data/html/buff/40001-1.htm";
			if(player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_MAGE_2)
			{
				player.sendMessage("Не хватает монет");
	            NpcHtmlMessage html = new NpcHtmlMessage(1);
			    html.setFile(file);
				return;
			}
			player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_MAGE_2, player, false);
			SkillTable.getInstance().getInfo(1085,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1059,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1078,6).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1204,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1048,6).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1397,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1303,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1040,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1035,4).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1062,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(273,1).getEffects(this,player);					
			SkillTable.getInstance().getInfo(276,1).getEffects(this,player);										
			SkillTable.getInstance().getInfo(349,1).getEffects(this,player);
        	player.setCurrentHpMp(player.getMaxHp(),(player.getMaxMp()));
            player.setCurrentCp(player.getMaxCp());
            NpcHtmlMessage html = new NpcHtmlMessage(1);
		    html.setFile(_curHtm);
            sendHtmlMessage(player,html);
		    player.sendPacket(new ActionFailed());
    	}
    	//Buff mage 1 lvl
    	else if (cmd.startsWith("mage1"))
    	{
    		String file = "data/html/buff/40001-1.htm";
			if(player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_MAGE_1)
			{
				player.sendMessage("Не хватает монет");
	            NpcHtmlMessage html = new NpcHtmlMessage(1);
			    html.setFile(_curHtm);
				return;
			}
			player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_MAGE_1, player, false);
			SkillTable.getInstance().getInfo(1085,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1059,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1078,6).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1204,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1048,6).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1397,3).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1303,2).getEffects(this,player);					
			SkillTable.getInstance().getInfo(1040,3).getEffects(this,player);							
        	player.setCurrentHpMp(player.getMaxHp(),(player.getMaxMp()));
            player.setCurrentCp(player.getMaxCp());
            NpcHtmlMessage html = new NpcHtmlMessage(1);
		    html.setFile(file);
            sendHtmlMessage(player,html);
		    player.sendPacket(new ActionFailed());
    	}
    	else if(cmd.startsWith("regen"))
        {
            if (player.getPvpFlag() != 0)
            {
            	player.sendMessage("Вы не можете использовать функцию восстановления в PvP");
            	return;
            }
            if (player.isDead())
            {
            	player.sendMessage("Вы не можете восстанавливаться когда мертвы");
            	return;
            }
        	player.setCurrentHpMp(player.getMaxHp(),(player.getMaxMp()));
            player.setCurrentCp(player.getMaxCp());
            NpcHtmlMessage html = new NpcHtmlMessage(1);
		    html.setFile("data/html/buff/"+getNpcId()+".htm");
            sendHtmlMessage(player,html);
		    player.sendPacket(new ActionFailed());
        }
    	
    	if(cmd.startsWith("buff"))
        {
    		String filename = "data/html/buff/40001";
    		String v;
            int cmdChoice;
            int id;
            int dialog;
            int level;
            cmdChoice = Integer.parseInt(command.substring(5, 7).trim());
    		if(player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_OTHER)
			{
				player.sendMessage("Не хватает монет");
	            NpcHtmlMessage html = new NpcHtmlMessage(1);
			    html.setFile(filename);
				return;
			}
			player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_OTHER, player, false);

			 if (player.isDead()) 
			 {
	              player.sendMessage("Вы не можете использовать баффера когда мертвы.");
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
	            if (dialog == 0)
	              v = "";
	            else
	              v = "-" + Integer.toString(dialog);
	

	            player.stopSkillEffects(id);

	            if (player.getShowAnim())
	            {
	            	player.broadcastPacket(new MagicSkillUser(player,player,id,level,350,150));
	            }

	            SkillTable.getInstance().getInfo(id, level).getEffects(player, player);

	            NpcHtmlMessage html = new NpcHtmlMessage(1);
	            html.setFile(filename + v + ".htm");
	            sendHtmlMessage(player, html);
	            player.sendPacket(new ActionFailed());
			
           

        }
    	/*else if(cmd.startsWith("donbuff"))
        {
    		int id = Integer.parseInt(command.substring(8).trim());
    		if (player.getInventory().getItemByItemId(4356) == null)
    		{
    			player.sendMessage("У вас нет сертификата баффов");
    			NpcHtmlMessage html = new NpcHtmlMessage(1);
    		    html.setFile("data/html/buff/"+getNpcId()+"-50.htm");
                sendHtmlMessage(player,html);
    			return;
    		}
			int level=SkillTable.getInstance().getMaxLevel(id,0);
            if(id==4554)level=4;
            if(id==4553)level=4;
            if (player.isDead())
            {
            	player.sendMessage("Вы не можете восстанавливаться когда мертвы");
            	return;
            }
            player.stopSkillEffects(id);
            if (menu._showanim)
            {
            	player.broadcastPacket(new MagicSkillUser(player,player,id,level,350,150));
            }
            SkillTable.getInstance().getInfo(id,level).getEffects(player,player);
            NpcHtmlMessage html = new NpcHtmlMessage(1);
		    html.setFile(_curHtm);
            sendHtmlMessage(player,html);
		    player.sendPacket(new ActionFailed());
        }*/
    	else if(cmd.startsWith("save"))
        {
            int cmdChoice = Integer.parseInt(command.substring(5, 6).trim());
            int flag=0;
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			if(cmdChoice>3)
			{
				html.setFile("data/html/buff/"+getNpcId()+"-11.htm");
				flag=1;
			}
            else
            	html.setFile(_curHtm);
			CreateScheme(player,Integer.toString(cmdChoice),flag);
            sendHtmlMessage(player,html);
		    player.sendPacket(new ActionFailed());
        }
        else if(cmd.startsWith("give"))
        {
            int cmdChoice = Integer.parseInt(command.substring(5, 6).trim());
            if((cmdChoice<1)&&(cmdChoice>6))return;
            String key="data/html/buff/"+getNpcId(),sKey=Integer.toString(cmdChoice);
            int flag=0;
            NpcHtmlMessage html = new NpcHtmlMessage(1);

            if(cmdChoice>3)
            {
                flag=1;
                key="data/html/buff/"+getNpcId()+"-8";
            }
            if (player.isDead())
            {
            	player.sendMessage("Вы не можете восстанавливаться когда мертвы");
            	return;
            }
            if (CharSchemesTable.getInstance().getScheme(player.getObjectId(),sKey)!=null)
            {
            	player.stopAllEffects();
                if(flag==0)
                {
                    for (L2Skill sk : CharSchemesTable.getInstance().getScheme(
							player.getObjectId(),sKey))
					{
                        player.stopSkillEffects(sk.getId());
                        sk.getEffects(this, player);
					}
                }
                else
                {
                    for (L2Skill sk : CharSchemesTable.getInstance().getScheme(
							player.getObjectId(),sKey))
					{
                        L2Summon pet = player.getPet();
                        if(pet!=null)
                        {
                            pet.stopSkillEffects(sk.getId());
                            sk.getEffects(this, pet);
                        }
                    }
                }
                html.setFile(key+".htm");
            }
            else
            {
                player.sendMessage("Профиль "+sKey+" не найден");
                return;
            }
            sendHtmlMessage(player,html);
            player.sendPacket(new ActionFailed());
        }
        else if(cmd.startsWith("rebuff"))
        {
            int rebuffChoice = 4;
            String key="data/html/buff/"+getNpcId(),sKey=Integer.toString(rebuffChoice);
            NpcHtmlMessage html = new NpcHtmlMessage(1);

            if (player.isDead())
            {
            	player.sendMessage("Вы не можете ребаффаться когда мертвы");
            	return;
            }
    		if(player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_REBUF)
			{
				player.sendMessage("Не хватает монет");
				return;
			}
			player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_REBUF, player, false);
			
            CreateScheme(player,Integer.toString(4),0);
            if (CharSchemesTable.getInstance().getScheme(
							player.getObjectId(),sKey)!=null)
            {
                    for (L2Skill sk : CharSchemesTable.getInstance().getScheme(
							player.getObjectId(),sKey))
					{
                        player.stopSkillEffects(sk.getId());
                        sk.getEffects(this, player);
					}
               
                html.setFile(key+".htm");
            }
            else
            {
                player.sendMessage("Запрещено");
                return;
            }
            sendHtmlMessage(player,html);
            player.sendPacket(new ActionFailed());
        }
        else
        {
			super.onBypassFeedback(player, command);
		}
    	
    }
    
    private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
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
}

