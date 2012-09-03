package services.NPCBuffer;

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Summon;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.MagicSkillLaunched;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.effects.EffectTemplate;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.Files;

public class Buffer extends Functions implements ScriptFile
{
	/** Количество бафов в группах */
	public static int priceBuff = 6000;//Цена за один бафф
	public static int creatSchema = 100000;//Цена за создание схемы
	public static int priceBuffNabor = 60000;



	public void onLoad()
	{
		

	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	/**
	 * Бафает группу баффов, снимает плату за бафф, отображает диалог с кнопкой возврата к списку бафов
	 * @param args массив строк, где элемент 0 - id группы бафов
	 */
	

	/**
	 * Бафает один бафф, снимает плату за бафф, отображает диалог с кнопкой возврата к списку бафов
	 * @param args массив строк: элемент 0 - id скида, элемент 1 - уровень скила
	 */
	public void doBuff(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		String page = args[1];
		int petorplayer = Integer.valueOf(args[2]);
		if(!checkCondition(player, npc))
			return;

		if(player.getAdena() < priceBuff)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		try
		{
			int skill_id = Integer.valueOf(args[0]);
			
			L2Skill skill = SkillTable.getInstance().getInfo(skill_id, SkillTable.getInstance().getBaseLevel(skill_id));
			if (petorplayer == 0)
			{
					skill.getEffects(player, player, false, false);
					getNpc().broadcastPacket(new MagicSkillUse(getNpc(), player, skill.getDisplayId(), skill.getLevel(), skill.getHitTime(), 0));
					for(EffectTemplate et : skill.getEffectTemplates())
					   {
					    Env env = new Env(player,player, skill);
					    L2Effect effect = et.getEffect(env);
					    effect.setPeriod(60 * 60 * 1000);
					    player.getEffectList().addEffect(effect);
					  }
			}
			else
			{
				if (player.getPet() == null)
				{
					player.sendMessage("У вас нету питомца");
				return;
				}
				L2Character pets = player.getPet();
				skill.getEffects(player, pets, false, false);
				getNpc().broadcastPacket(new MagicSkillUse(getNpc(), pets, skill.getDisplayId(), skill.getLevel(), skill.getHitTime(), 0));
				for(EffectTemplate et : skill.getEffectTemplates())
				   {
				    Env env = new Env(player,pets, skill);
				    L2Effect effect = et.getEffect(env);
				    effect.setPeriod(60 * 60 * 1000);
				    pets.getEffectList().addEffect(effect);
				  }	
			}
			player.reduceAdena(priceBuff, true);
		}
		catch(Exception e)
		{
			player.sendMessage("Invalid skill!");
		}

		show(Files.read("data/scripts/services/NPCBuffer/" + page + ".htm", player), player, npc);
	}

	/**
	 * Проверяет возможность бафа персонажа.<BR>
	 * В случае невозможности бафа показывает игроку html с ошибкой и возвращает false.
	 * @param player персонаж
	 * @return true, если можно бафать персонажа
	 */
	public boolean checkCondition(L2Player player, L2NpcInstance npc)
	{
		if(player == null || npc == null)
			return false;

		if(!L2NpcInstance.canBypassCheck(player, npc))
			return false;

		return true;
	}

	/* Выбор меню */
	
	public void showHtml(String[] args)
	{
		
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		String page = args[0];
		show(Files.read("data/scripts/services/NPCBuffer/" + page + ".htm", player), player, npc);
	}
	
	/* Показывает страницу с выбором кого бафать. */
	public void SelectBuffs()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance lastNpc = player.getLastNpc();

		if(!checkCondition(player, lastNpc))
			return;

		show(Files.read("data/scripts/services/NPCBuffer/buffs.htm", player), player, lastNpc);
	}

	/**
	 * Генерит ссылку, которая в дальнейшем аппендится эвент менеждерам
	 * @return html код ссылки
	 */
	public String OutDia()
	{
		L2Player activeChar = (L2Player) getSelf();
		String append = Files.read("data/scripts/services/NPCBuffer/buffs.htm");
		String schema = "";
		String one = "Первая";
		String two = "Вторая";
		String three = "Третья";
		
		for (int i = 1; i < 4; ++i) 
		{
			if (i < 4)
			{
			String setname = "buffset" + i;
			if (activeChar.getVar(setname) == null)	{
				schema +=("<tr><td width=189><button value=\"Создать\" action=\"bypass -h scripts_services.NPCBuffer.Buffer:newSchema " + setname + " " + i + "\" width=195 height=23 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			} else if  (activeChar.getVar(setname) != null)
			{
				String namebutton = "";
				if (i==1)
					namebutton = one;
				else if (i==2)
					namebutton = two;
				else
					namebutton = three;
				schema+=("<tr><td width=189><button value=\"" + namebutton + "\" action=\"bypass -h scripts_services.NPCBuffer.Buffer:showSchema " + setname + "\" width=195 height=23 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			}
			}
		}
		append = append.replaceFirst("%buffset%", schema);
		return append;
	}

	public void newSchema(String[] args)
	{
		L2Player activeChar = (L2Player) getSelf();
		if (activeChar.getAdena()<creatSchema)
		{
			activeChar.sendMessage("Не достаточно адены для создания схемы.");
			return;
		}
		String buffset = args[0];
		String SchemaName = args[1];
		activeChar.setVar(buffset, SchemaName + ";");
		String adrg[] = {buffset} ;
		showSchema(adrg);
		activeChar.reduceAdena(creatSchema, true);
	}
	
	public void delSchema(String[] args)
	{
		L2Player activeChar = (L2Player) getSelf();
		activeChar.unsetVar(args[0]);
	}
	
	public void editSchema(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		String setname = args[0];
		String html = "";
		html = Files.read("data/scripts/services/NPCBuffer/schemaedit.htm", player);
		html = html.replaceAll("%buffsetname%", setname);
	
		StringTokenizer st = new StringTokenizer(player.getVar(setname), ";");
		st.nextToken();
		int page = Integer.valueOf(args[1]);
		int lenghtarray = st.countTokens();
		int ipage = 0;
		ipage = (page)*28-28;
		int len = (page)*28;
		
		
		int i = 0;
		boolean closed = true;
		String SkillId = "";
		String icon = "";
		int count = 0;
		while (st.hasMoreTokens())
		{
			count++;
			if (ipage>=count || len<count)
			{
				st.nextToken();
				continue;
			}
			try
			{	
			int skillid = Integer.parseInt(st.nextToken());

			if ((++i == 1) || (i == 8) || (i == 15) || (i == 22) || (i == 29)) {
				closed = false;
				icon+=("<td><table width=\"60\" border=\"0\">");
			}
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if (skill.getId() < 1000)
			{
				SkillId = "0" + skill.getId();
			}
			else
			{
				SkillId = "" + skill.getId();
			}
			if (skill.getId() == 4700 || skill.getId() == 4699)
			{
				SkillId = "1331 ";
			}
			else if (skill.getId() == 4703 || skill.getId() == 4702)
			{
				SkillId = "1332 ";
			}
			icon+=("<tr><td><img src=icon.skill" + SkillId + " width=32 height=32></td><td width=25><button value=- action=\"bypass -h scripts_services.NPCBuffer.Buffer:removeOneBuff " + skillid + " " + setname + " " + page + "\" width=25 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			if ((i == 7) || (i == 14) || (i == 21) || (i == 28) || (i == 35)) {
				closed = true;
				icon+=("</table></td>");
				
			}

			}
			catch (Exception e)
			{continue;
			}
		}
		if (!(closed))
			icon+=("</table></td>");
		if (icon.length() == 0)
			icon+=("<td>Баффов нету</td>");
		
		TextBuilder sb = new TextBuilder();
		int pages = Math.max(1, lenghtarray / 28 + 1);
		 sb.append("<table><tr>");
		 if (pages!=1)
			for (int ii = 1; ii <= pages; ii++)
	         {
	         if (ii != page)
	        	 sb.append("<td width=20 height=20><a action=\"bypass -h scripts_services.NPCBuffer.Buffer:editSchema " + setname + " " + ii + "\"><font color=F2C202>[" + ii + "]</font></a></td>");
	         else
	        	 sb.append("<td width=10 height=20><font color=FFFFFF>" + ii + "</font></a></td>");
	         }
			sb.append("</tr></table>");
        html += sb.toString();
		//icon+=("<td><img src=icon.skill" + SkillId + " width=32 height=32></td><td width=25><button value=- action=\"bypass -h scripts_services.NPCBuffer.Buffer:removeOneBuff " + skillid + " " + setname + "\" width=25 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		
		html = html.replaceAll("%buff%", icon);
		
		show(html, player, npc);
		
		
	}
	
	public void addBuffSchema(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		String setname = args[0];
		int page = Integer.valueOf(args[1]);
		int lenghtarray = BUFF_ADD.length;
		int ipage = 0;
		ipage = (page)*24-24;
		int len = (page)*24;
		String html = "";
		html = Files.read("data/scripts/services/NPCBuffer/schemaadd.htm", player);
		html = html.replaceAll("%buffsetname%", setname);
	

		int i = 0;
		boolean closed = true;
		String SkillId = "";
		String icon = "";
	
		 for (; ipage < len; ipage++)
         {
			 if (lenghtarray == ipage)
	           	 break;
			/*if (!checkSchema(player,setname,BUFF_ADD[ipage]))
			{
				len++;
				continue;
			}*/
			
			if ((++i == 1) || (i == 7) || (i == 13) || (i == 19) || (i == 25)) {
				closed = false;
				icon+=("<td><table width=\"60\" border=\"0\">");
			}
			L2Skill skill = SkillTable.getInstance().getInfo(BUFF_ADD[ipage], SkillTable.getInstance().getBaseLevel(BUFF_ADD[ipage]));
			if (skill.getId() < 1000)
			{
				SkillId = "0" + skill.getId();
			}
			else
			{
				SkillId = "" + skill.getId();
			}
			if (skill.getId() == 4700 || skill.getId() == 4699)
			{
				SkillId = "1331 ";
			}
			else if (skill.getId() == 4703 || skill.getId() == 4702)
			{
				SkillId = "1332 ";
			}
			icon+=("<tr><td><img src=icon.skill" + SkillId + " width=32 height=32></td><td width=25><button value=+ action=\"bypass -h scripts_services.NPCBuffer.Buffer:addOneBuff " + BUFF_ADD[ipage] + " " + setname + " " + page + "\" width=25 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			if ((i == 6) || (i == 12) || (i == 18) || (i == 24) || (i == 30)) {
				closed = true;
				icon+=("</table></td>");
			}
		}
		if (!(closed))
			icon+=("</table></td>");
		if (icon.length() == 0)
			icon+=("<td>Баффов нету</td>");
		 
		TextBuilder sb = new TextBuilder();
		int pages = Math.max(1, lenghtarray / 24 + 1);
		 sb.append("<table><tr>");
		 if (pages!=1)
			for (int ii = 1; ii <= pages; ii++)
	         {
	         if (ii != page)
	        	 sb.append("<td width=20 height=20><a action=\"bypass -h scripts_services.NPCBuffer.Buffer:addBuffSchema " + setname + " " + ii + "\"><font color=F2C202>[" + ii + "]</font></a></td>");
	         else
	        	 sb.append("<td width=10 height=20><font color=FFFFFF>" + ii + "</font></a></td>");
	         }
			sb.append("</tr></table>");
        html += sb.toString();
		
		//icon+=("<td><img src=icon.skill" + SkillId + " width=32 height=32></td><td width=25><button value=- action=\"bypass -h scripts_services.NPCBuffer.Buffer:removeOneBuff " + skillid + " " + setname + "\" width=25 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		
		html = html.replaceAll("%buff%", icon);
		
		show(html, player, npc);
		
		
	}
	
	public boolean checkSchema(L2Player player, String buffset, int buffId)
	{
		StringTokenizer st = new StringTokenizer(player.getVar(buffset), ";");
		st.nextToken();
	
		while (st.hasMoreTokens())
		{
			int skillid = Integer.parseInt(st.nextToken());
			if (buffId == skillid)
				return false;
		}
			
		return true;
	}
	
	public void addOneBuff (String[] args)
	{
		L2Player player = (L2Player) getSelf();
		int buffid = Integer.valueOf(args[0]);;
		String buffset = args[1];
		String page = args[2];
		String adrg[] = {buffset,page} ;
		if ((player.getVar(buffset) == null))
			return;
		if (!checkSchema(player,buffset,buffid))
		{
			player.sendMessage("Этот бафф вы уже добавили");
			addBuffSchema(adrg);
			return;
		}
		
		if (new StringTokenizer(player.getVar(buffset), ";").countTokens() < 61)
		{
		player.setVar(buffset, player.getVar(buffset) + buffid + ";");
	    }
		
		
		addBuffSchema(adrg);
	}
	
	public void removeOneBuff (String[] args)
	{
		L2Player player = (L2Player) getSelf();
		String page = args[2];
		String buffid = args[0];
		String buffset = args[1];
		if ((player.getVar(buffset) == null))
			return;
		
		player.setVar(buffset, player.getVar(buffset).replaceFirst(buffid + ";", ""));
		
		String adrg[] = {buffset,page} ;
		editSchema(adrg);
		
		
	}
	
	public void showSchema(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		String setname = args[0];
		String html = "";
		html = Files.read("data/scripts/services/NPCBuffer/schema.htm", player);
		html = html.replaceAll("%buffsetname%", setname);
		StringTokenizer st = new StringTokenizer(player.getVar(setname), ";");
		st.nextToken();
		
		int i = 0;
		boolean closed = true;
		String SkillId = "";
		String icon = "";
	
		while (st.hasMoreTokens())
		{
			try
			{
			int skillid = Integer.parseInt(st.nextToken());
			
			if ((++i == 1) || (i == 7) || (i == 13) || (i == 19) || (i == 25) || (i == 31) || (i == 37) || (i == 43) || (i == 49) || (i == 55)) {
				closed = false;
				icon+=("<tr>");
			}
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if (skill.getId() < 1000)
			{
				SkillId = "0" + skill.getId();
			}
			else
			{
				SkillId = "" + skill.getId();
			}
			if (skill.getId() == 4700 || skill.getId() == 4699)
			{
				SkillId = "1331 ";
			}
			else if (skill.getId() == 4703 || skill.getId() == 4702)
			{
				SkillId = "1332 ";
			}
			icon+=("<td><img src=icon.skill" + SkillId + " width=32 height=32></td>");
			if ((i == 6) || (i == 12) || (i == 18) || (i == 24) || (i == 30) || (i == 36) || (i == 42) || (i == 48) || (i == 54) || (i == 60)) {
				closed = true;
				icon+=("</tr>");
			}
			}
			catch (Exception e)
			{continue;
			}
		}
		if (!(closed))
			icon+=("</tr>");
		
		html = html.replaceAll("%icon%", icon);
		show(html, player, npc);
		
	}
	
	public void buffSchema(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		String setname = args[0];
		
		StringTokenizer st = new StringTokenizer(player.getVar(setname), ";");
		if (player.getAdena()< st.countTokens()*priceBuff)
		{
			player.sendMessage("У вас не хватает адены");
			return;
		}
		st.nextToken();
		
        while (st.hasMoreTokens()) 
        {
			int skill_id = Integer.valueOf(st.nextToken()).intValue();
				L2Skill skill = SkillTable.getInstance().getInfo(skill_id, SkillTable.getInstance().getBaseLevel(skill_id));
				skill.getEffects(player, player, false, false);
				for(EffectTemplate et : skill.getEffectTemplates())
				   {
				    Env env = new Env(player,player, skill);
				    L2Effect effect = et.getEffect(env);
				    effect.setPeriod(60 * 60 * 1000);
				    player.getEffectList().addEffect(effect);
				   }
		}
        player.reduceAdena(st.countTokens()*priceBuff, true);
        String adrg[] = {setname} ;
		showSchema(adrg);
	}		
	
	public void buffSchemaPet(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		String setname = args[0];
		
		StringTokenizer st = new StringTokenizer(player.getVar(setname), ";");
		if (player.getAdena()< st.countTokens()*priceBuff)
		{
			player.sendMessage("У вас не хватает адены");
			return;
		}
		st.nextToken();
		if (player.getPet() == null)
		{
			player.sendMessage("У вас нету питомца");
			String adrg[] = {setname} ;
			showSchema(adrg);
			return;
		}
		L2Character pet = player.getPet();
		
        while (st.hasMoreTokens()) 
        {
			int skill_id = Integer.valueOf(st.nextToken()).intValue();
				L2Skill skill = SkillTable.getInstance().getInfo(skill_id, SkillTable.getInstance().getBaseLevel(skill_id));
				skill.getEffects(player, pet, false, false);
				for(EffectTemplate et : skill.getEffectTemplates())
				   {
				    Env env = new Env(player,pet, skill);
				    L2Effect effect = et.getEffect(env);
				    effect.setPeriod(60 * 60 * 1000);
				    pet.getEffectList().addEffect(effect);
				   }
		}
        player.reduceAdena(st.countTokens()*priceBuff, true);
        String adrg[] = {setname} ;
		showSchema(adrg);
        
	}	
		

	
	

	public String DialogAppend_50005(Integer val)
	{
		if(val != 0)
			return "";
		return OutDia();
	}

	public class BeginBuff implements Runnable
	{
		L2Character _buffer;
		L2Skill _skill;
		L2Player _target;

		public BeginBuff(L2Character buffer, L2Skill skill, L2Player target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		public void run()
		{
			if(_target.isInOlympiadMode())
				return;
			_buffer.broadcastPacket(new MagicSkillUse(_buffer, _target, _skill.getDisplayId(), _skill.getLevel(), _skill.getHitTime(), 0));
			ThreadPoolManager.getInstance().scheduleGeneral(new EndBuff(_buffer, _skill, _target), _skill.getHitTime());
		}
	}

	public class EndBuff implements Runnable
	{
		L2Character _buffer;
		L2Skill _skill;
		L2Player _target;

		public EndBuff(L2Character buffer, L2Skill skill, L2Player target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		public void run()
		{
			_skill.getEffects(_buffer, _target, false, false);
			_buffer.broadcastPacket(new MagicSkillLaunched(_buffer.getObjectId(), _skill.getId(), _skill.getLevel(), _target, _skill.isOffensive()));
		}
	}

	public class BeginPetBuff implements Runnable
	{
		L2Character _buffer;
		L2Skill _skill;
		L2Summon _target;

		public BeginPetBuff(L2Character buffer, L2Skill skill, L2Summon target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		public void run()
		{
			_buffer.broadcastPacket(new MagicSkillUse(_buffer, _target, _skill.getDisplayId(), _skill.getLevel(), _skill.getHitTime(), 0));
			ThreadPoolManager.getInstance().scheduleGeneral(new EndPetBuff(_buffer, _skill, _target), _skill.getHitTime());
		}
	}

	public class EndPetBuff implements Runnable
	{
		L2Character _buffer;
		L2Skill _skill;
		L2Summon _target;

		public EndPetBuff(L2Character buffer, L2Skill skill, L2Summon target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		public void run()
		{
			_skill.getEffects(_buffer, _target, false, false);
			_buffer.broadcastPacket(new MagicSkillLaunched(_buffer.getObjectId(), _skill.getId(), _skill.getLevel(), _target, _skill.isOffensive()));
		}
	}
	
	
	public static final int[] BUFF_ADD = 
	  {	   
		1479,
		1068,
		1059,
		1036,
		1045,
		1048,
		1086,
		1268,
		1085,
		1303,
		1242,
		1077,
		1204,
		1087,
		1240,
		1304,
		1243,
		1352,
		1353,
		1354,
		1259,
		1035,
		1078,
		1392,
		1393,
		1191,
		1189,
		1182,
		1033,
		1032,
		1073,
		4699,
		4700,
		4702,
		4703,
		1356,
		1357,
		1355,
		1414,
		1363,
		1413,
		1388,
		1389,
		1062,
		264,
		265,
		266,
		267,
		268,
		269,
		270,
		304,
		305,
		306,
		308,
		349,
		363,
		364,
		529,
		834,
		1444,
		1442,
		1443,
		1476,
		1477,
		1478,
		1479,
		1519,
		1499,
		1500,
		1501,
		1502,
		1504,
		1503,
		271,
		274,
		275,
		272,
		273,
		276,
		277,
		1307,
		309,
		310,
		311,
		365,
		366,
		530,
		825,
		826,
		827,
		828,
		829,
		830,
		915,
	   1542
		
		   
	  };
	  
	  public static final int[] ALL_NORM = 
	  {	   
	   1040,
	   1068,
	   1059,
	   1036,
	   1045,
	   1047,
	   1086,
	   1268,
	   1085,
	   1303,
	   1242,
	   1077,
	   1204,
	   1087,
	   1240,
	   1304,
	   1243,
	   1304
	  };
	  
	  public static final int[] ALL_RESIST = 
	  {	   
	   1352,
	   1353,
	   1354,
	   1259,
	   1035,
	   1078,
	   1392,
	   1393,
	   1191,
	   1189,
	   1182,
	   1033,
	   1032,
	   1073
	  };
	  
	  public static final int[] ALL_HEAL = 
	  {	   
	   1307,
	   1311,
	   1043,
	   1044,
	   1397,
	   1460,
	   1257
	  };
	  
	  public static final int[] ALL_WARC = 
	  {	   
	   1007,
	   1009,
	   1002,
	   1006,
	   1251,
	   1252,
	   1253,
	   1284,
	   1308,
	   1309,
	   1310,
	   1362,
	   1461,
	   1390,
	   1391
	  };
	  
	  public static final int[] ALL_OVER = 
	  {	   
	   1003,
	   1004,
	   1005,
	   1008,
	   1249,
	   1250,
	   1260,
	   1261,
	   1282,
	   1364,
	   1365,
	   1416,
	   1415
	  };
	  
	  public static final int[] ALL_IMP = 
	  {	   
	   1499,
	   1519,
	   1500,
	   1501,
	   1502,
	   1504,
	   1503
	  };
	  
	  public static final int[] ALL_WARS = 
	  {	   
	   825,
	   826,
	   827,
	   828,
	   829,
	   830
	  };
	  
	  public static final int[] ALL_DANCE = 
	  {	   
	   271,
	   274,
	   275,
	   272,
	   273,
	   276,
	   277,
	   307,
	   309,
	   310,
	   311,
	   365,
	   530,
	   915
	  };
	  
	  public static final int[] ALL_SONG = 
	  {	   
	   264,
	   265,
	   266,
	   267,
	   268,
	   269,
	   270,
	   304,
	   305,
	   306,
	   308,
	   349,
	   363,
	   364,
	   529
	  };
	
	public static final int[] BUFF_FIGHT = 
	  {	   
	   1501,//Improved Condition +HP +MP
	   1499,//Improved Combat +pAtk +pDef
	   1519,//Chant of Blood Awakening +pAtkSpd +hpDrain
	   1500,//Improved Magic +mAtk +mDef
	   1502,//Improved cAttack +rCrit +CrtPower
	   1504,//Improved Movement +speed +rEvas
	   1284,//Chant of Revenge +reflectDam
	   1461,//Chant of Protection -critPower
	   1362,//Chant of Spirit +resist cancel,debuff
	   1240,//Guidance
	   1542,//Counter Critical
	   1035,//Mental Shield
	   1388,//Greater Might
	   1259,//Resist Shock
	   1397,//Clarity
	   274,//Dance of Fire
	   271,//Dance of the Warrior
	   275,//Dance of Fury
	   915,//Dance of Berserker
	   310,//Dance of the Vampire
	   269,//Song of Hunter
	   264,//Song of Earth
	   267,//Song of Warding
	   304,//Song of Vitality
	   305,//Song of Vengeance +reflectDam
	   364,//Song of Champion -10%otkat
	   4699,//Blessing of Queen
	   1363//Chant of Victory
	   
	  };
	  
	  public static final int[] BUFF_MAGE = 
	  {
	   1501,//Improved Condition +HP +MP
	   1499,//Improved Combat +pAtk +pDef
	   1500,//Improved Magic +mAtk +mDef
	   1504,//Improved Movement +speed +rEvas
	   1284,//Chant of Revenge +reflectDam
	   1461,//Chant of Protection -critPower
	   1362,//Chant of Spirit +resist cancel,debuff
	   1035,//Mental Shield
	   1078,//Concentration
	   1062,//Berserker Spirit
	   1389,//Greater Shield
	   1259,//Resist Shock
	   1397,//Clarity
	   1303,//Wild Magic
	   1413,//Magnus' Chant
	   273,//Dance of the Mystic
	   276,//Dance of Concentration
	   365,//Siren Dance
	   267,//Song of Warding
	   268,//Song of Wind
	   915,//Dance of Berserker
	   264,//Song of Earth
	   305,//Song of Vengeance +reflectDam
	   349,//Song of Renewal -20%otkat
	   4703,//Gift of Seraph
	   304//Song of Vitality
	};
	  
	public void removeBuff ()
	{
		L2Player activeChar = (L2Player) getSelf();
		activeChar.getEffectList().stopAllEffects();
	}
	
	public void recoveryHPMP ()
	{
		L2Player activeChar = (L2Player) getSelf();
		activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
	}
	
	public void allWars()
	{
		L2Player activeChar = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
		pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor) 
		{
			activeChar.sendMessage("У вас не хватает адены");
			return;
		}
		
		
		L2Summon summon = activeChar.getPet();
		for(int skillid : ALL_WARS)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget()&& pet == true)) 
			{
				skill.getEffects(activeChar, summon, false, false);
			} 
			else 
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
			for(EffectTemplate et : skill.getEffectTemplates())
			   {
			    Env env = new Env(activeChar,activeChar, skill);
			    L2Effect effect = et.getEffect(env);
			    effect.setPeriod(60 * 60 * 1000);
			    activeChar.getEffectList().addEffect(effect);
			    activeChar.updateEffectIcons();
			   }
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		show(Files.read("data/scripts/services/NPCBuffer/buffswars.htm", activeChar), activeChar, npc);
	}
	
	public void allImp()
	{
		L2Player activeChar = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
		pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor) 
		{
			activeChar.sendMessage("У вас не хватает адены");
			return;
		}
		
		
		L2Summon summon = activeChar.getPet();
		for(int skillid : ALL_IMP)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget()&& pet == true)) 
			{
				skill.getEffects(activeChar, summon, false, false);
			} 
			else 
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
			for(EffectTemplate et : skill.getEffectTemplates())
			   {
			    Env env = new Env(activeChar,activeChar, skill);
			    L2Effect effect = et.getEffect(env);
			    effect.setPeriod(60 * 60 * 1000);
			    activeChar.getEffectList().addEffect(effect);
			    activeChar.updateEffectIcons();
			   }
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		show(Files.read("data/scripts/services/NPCBuffer/buffsimp.htm", activeChar), activeChar, npc);
	}
	
	public void allOver()
	{
		L2Player activeChar = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
		pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor) 
		{
			activeChar.sendMessage("У вас не хватает адены");
			return;
		}
		
		
		L2Summon summon = activeChar.getPet();
		for(int skillid : ALL_OVER)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget()&& pet == true)) 
			{
				skill.getEffects(activeChar, summon, false, false);
			} 
			else 
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
			for(EffectTemplate et : skill.getEffectTemplates())
			   {
			    Env env = new Env(activeChar,activeChar, skill);
			    L2Effect effect = et.getEffect(env);
			    effect.setPeriod(60 * 60 * 1000);
			    activeChar.getEffectList().addEffect(effect);
			    activeChar.updateEffectIcons();
			   }
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		show(Files.read("data/scripts/services/NPCBuffer/buffsover.htm", activeChar), activeChar, npc);
	}
	
	public void allWarc()
	{
		L2Player activeChar = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
		pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor) 
		{
			activeChar.sendMessage("У вас не хватает адены");
			return;
		}
		
		
		L2Summon summon = activeChar.getPet();
		for(int skillid : ALL_WARC)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget()&& pet == true)) 
			{
				skill.getEffects(activeChar, summon, false, false);
			} 
			else 
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
			for(EffectTemplate et : skill.getEffectTemplates())
			   {
			    Env env = new Env(activeChar,activeChar, skill);
			    L2Effect effect = et.getEffect(env);
			    effect.setPeriod(60 * 60 * 1000);
			    activeChar.getEffectList().addEffect(effect);
			    activeChar.updateEffectIcons();
			   }
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		show(Files.read("data/scripts/services/NPCBuffer/buffswarc.htm", activeChar), activeChar, npc);
	}
	
	public void allHeal()
	{
		L2Player activeChar = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
		pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor) 
		{
			activeChar.sendMessage("У вас не хватает адены");
			return;
		}
		
		
		L2Summon summon = activeChar.getPet();
		for(int skillid : ALL_HEAL)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget()&& pet == true)) 
			{
				skill.getEffects(activeChar, summon, false, false);
			} 
			else 
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
			for(EffectTemplate et : skill.getEffectTemplates())
			   {
			    Env env = new Env(activeChar,activeChar, skill);
			    L2Effect effect = et.getEffect(env);
			    effect.setPeriod(60 * 60 * 1000);
			    activeChar.getEffectList().addEffect(effect);
			    activeChar.updateEffectIcons();
			   }
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		show(Files.read("data/scripts/services/NPCBuffer/buffsheal.htm", activeChar), activeChar, npc);
	}
	
	public void allResist()
	{
		L2Player activeChar = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
		pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor) 
		{
			activeChar.sendMessage("У вас не хватает адены");
			return;
		}
		
		
		L2Summon summon = activeChar.getPet();
		for(int skillid : ALL_RESIST)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget()&& pet == true)) 
			{
				skill.getEffects(activeChar, summon, false, false);
			} 
			else 
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
			for(EffectTemplate et : skill.getEffectTemplates())
			   {
			    Env env = new Env(activeChar,activeChar, skill);
			    L2Effect effect = et.getEffect(env);
			    effect.setPeriod(60 * 60 * 1000);
			    activeChar.getEffectList().addEffect(effect);
			    activeChar.updateEffectIcons();
			   }
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		show(Files.read("data/scripts/services/NPCBuffer/buffsresist.htm", activeChar), activeChar, npc);
	}
	
	
	
	public void allNorm()
	{
		L2Player activeChar = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
		pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor) 
		{
			activeChar.sendMessage("У вас не хватает адены");
			return;
		}
		
		
		L2Summon summon = activeChar.getPet();
		for(int skillid : ALL_NORM)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget()&& pet == true)) 
			{
				skill.getEffects(activeChar, summon, false, false);
			} 
			else 
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
			for(EffectTemplate et : skill.getEffectTemplates())
			   {
			    Env env = new Env(activeChar,activeChar, skill);
			    L2Effect effect = et.getEffect(env);
			    effect.setPeriod(60 * 60 * 1000);
			    activeChar.getEffectList().addEffect(effect);
			    activeChar.updateEffectIcons();
			   }
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		show(Files.read("data/scripts/services/NPCBuffer/buffsnorm.htm", activeChar), activeChar, npc);
	}
	  
	public void allDance()
	{
		L2Player activeChar = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
		pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor) 
		{
			activeChar.sendMessage("У вас не хватает адены");
			return;
		}
		
		
		L2Summon summon = activeChar.getPet();
		for(int skillid : ALL_DANCE)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget()&& pet == true)) 
			{
				skill.getEffects(activeChar, summon, false, false);
			} 
			else 
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
			for(EffectTemplate et : skill.getEffectTemplates())
			   {
			    Env env = new Env(activeChar,activeChar, skill);
			    L2Effect effect = et.getEffect(env);
			    effect.setPeriod(60 * 60 * 1000);
			    activeChar.getEffectList().addEffect(effect);
			    activeChar.updateEffectIcons();
			   }
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		show(Files.read("data/scripts/services/NPCBuffer/buffsdance.htm", activeChar), activeChar, npc);
	}
	
	public void allSong()
	{
		L2Player activeChar = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
		pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor) 
		{
			activeChar.sendMessage("У вас не хватает адены");
			return;
		}
		
		
		L2Summon summon = activeChar.getPet();
		for(int skillid : ALL_SONG)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget()&& pet == true)) 
			{
				skill.getEffects(activeChar, summon, false, false);
			} 
			else 
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
			for(EffectTemplate et : skill.getEffectTemplates())
			   {
			    Env env = new Env(activeChar,activeChar, skill);
			    L2Effect effect = et.getEffect(env);
			    effect.setPeriod(60 * 60 * 1000);
			    activeChar.getEffectList().addEffect(effect);
			    activeChar.updateEffectIcons();
			   }
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		show(Files.read("data/scripts/services/NPCBuffer/buffsdance.htm", activeChar), activeChar, npc);
	}
	  
	public void buffFight()
	{
		L2Player activeChar = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
		pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor) 
		{
			activeChar.sendMessage("У вас не хватает адены");
			return;
		}
		
		
		L2Summon summon = activeChar.getPet();
		for(int skillid : BUFF_FIGHT)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget()&& pet == true)) 
			{
				skill.getEffects(activeChar, summon, false, false);
			} 
			else 
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
			for(EffectTemplate et : skill.getEffectTemplates())
			   {
			    Env env = new Env(activeChar,activeChar, skill);
			    L2Effect effect = et.getEffect(env);
			    effect.setPeriod(60 * 60 * 1000);
			    activeChar.getEffectList().addEffect(effect);
			    activeChar.updateEffectIcons();
			   }
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		show(Files.read("data/scripts/services/NPCBuffer/buffs.htm", activeChar), activeChar, npc);
	}
	
	public void buffMage()
	{
		L2Player activeChar = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
		pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor) 
		{
			activeChar.sendMessage("У вас не хватает адены");
			return;
		}
		L2Summon summon = activeChar.getPet();
		for(int skillid : BUFF_MAGE)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget()&& pet == true)) 
			{
				skill.getEffects(activeChar, summon, false, false);
			} 
			else 
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
			for(EffectTemplate et : skill.getEffectTemplates())
			   {
			    Env env = new Env(activeChar,activeChar, skill);
			    L2Effect effect = et.getEffect(env);
			    effect.setPeriod(60 * 60 * 1000);
			    activeChar.getEffectList().addEffect(effect);
			    activeChar.updateEffectIcons();
			   }
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		show(Files.read("data/scripts/services/NPCBuffer/buffs.htm", activeChar), activeChar, npc);
	}
	
}