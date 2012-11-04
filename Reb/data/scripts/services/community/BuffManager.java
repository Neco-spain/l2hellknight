package services.community;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;
import java.util.concurrent.ScheduledFuture;

import javolution.text.TextBuilder;
import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.dao.BuffManagerDAO;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.skills.effects.EffectTemplate;
import l2r.gameserver.stats.Env;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.AutoBan;
import l2r.gameserver.utils.GCSArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KilRoy
 * Community Board v2.0 Buff (Player/Pet + Sheme) MOD
 */
public class BuffManager implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(BuffManager.class);

	/**
	 * Имплементированые методы скриптов
	 */
	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED && Config.BBS_PVP_BUFFER_ENABLED)
		{
			_log.info("CommunityBoard: Manage Buffer service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED && Config.BBS_PVP_BUFFER_ENABLED)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown()
	{}

	public class CBBuffGroups
	{
		public int GpId = 0; // ID группы
		public String GpName = ""; // Имя группы
		public int PlayerId = 0; // ID владельца
	}

	public class CBBuffGroup
	{
		public int OneId = 0; // Уникальный ID группы в таблице communitybuff_grp_buffs
		public int OneGpId = 0; // ID группы
		public int OnePlayerId = 0; // ID владельца
		public int OneBuffId = 0; // ID баффа
	}

	public class CBBuffAllowedBuffs
	{
		public int BuffLvL = 0; // LvL баффа
		public int BuffId = 0; // ID баффа
	}

	/**
	 * Регистратор команд
	 */
	@Override
	public String[] getBypassCommands()
	{
		return new String[] {
				"_bbsbuff;",
				"_bbsbuff;buff;",
				"_bbsbuff;cancel",
				"_bbsbuff;restore",
				"_bbsbuff;buffgrp;",
				"_bbsbuff;bufffixedgrp;",
				"_bbsbuff;addgrp; ",
				"_bbsbuff;dellGrp;",
				"_bbsbuff;use;",
				"_bbsbuff;usefixed;",
				"_bbsbuff;editegrp;",
				"_bbsbuff;addbuffin;",
				"_bbsbuff;addbuffingrp;",
				"_bbsbuff;dellbufffrom;",
				"_bbsbuff;buffgrpauto;",
				"_bbsbuff;buffgrpautostop;",
				"_bbsbuff;buffgrpautopet;",
				"_bbsbuff;buffgrpautopetstop;" };
	}

	@Override
	public void onBypassCommand(Player activeChar, String command)
	{
		activeChar.setSessionVar("add_fav", null);
		Summon pet = activeChar.getPet();

		if(command.equals("_bbsbuff;"))
			showBuffIndexPage(activeChar);
		else if(command.startsWith("_bbsbuff;buff;"))
		{
			StringTokenizer buffOne = new StringTokenizer(command, ";");
			buffOne.nextToken();
			buffOne.nextToken();
			int BuffIdUse = Integer.parseInt(buffOne.nextToken());
			int BuffLvL = Integer.parseInt(buffOne.nextToken());
			BuffOne(activeChar, BuffIdUse, BuffLvL);
			UseFixedBuffGrp(activeChar, activeChar.getVar("GrpName"), Integer.parseInt(activeChar.getVar("page")));

		}
		else if(command.startsWith("_bbsbuff;cancel"))
		{
			if(activeChar.getEffectList().getEffectsBySkillId(Skill.SKILL_RAID_CURSE) == null)
				activeChar.getEffectList().stopAllEffects();
			if(pet != null && pet.getEffectList().getEffectsBySkillId(Skill.SKILL_RAID_CURSE) == null)
				pet.getEffectList().stopAllEffects();
			showBuffIndexPage(activeChar);
		}
		if(command.startsWith("_bbsbuff;restore"))
		{
			if(!chekCondition(activeChar))
				return;
			
			activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
			activeChar.setCurrentCp(activeChar.getMaxCp());
			onBypassCommand(activeChar, "_bbsbuff;");
			return;
		}
		else if(command.startsWith("_bbsbuff;buffgrp;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int BuffIdUse = Integer.parseInt(st.nextToken());
			String target = st.nextToken();
			BuffGrp(activeChar, BuffIdUse, target);
			showBuffIndexPage(activeChar);
		}
		else if(command.startsWith("_bbsbuff;bufffixedgrp;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			String GrpName = st.nextToken();
			String target = st.nextToken();
			BuffFixedGrp(activeChar, GrpName, target);
			showBuffIndexPage(activeChar);
		}
		else if(command.startsWith("_bbsbuff;addgrp; "))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			String GrpNameAdd = st.nextToken();
			AddBuffGrp(activeChar, GrpNameAdd);
			showBuffIndexPage(activeChar);
		}
		else if(command.startsWith("_bbsbuff;dellGrp;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int GpNameDell = Integer.parseInt(st.nextToken());
			DellBuffGrp(activeChar, GpNameDell);
			showBuffIndexPage(activeChar);
		}
		else if(command.startsWith("_bbsbuff;use;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int GpIdUse = Integer.parseInt(st.nextToken());
			UseBuffGrp(activeChar, GpIdUse);
		}
		else if(command.startsWith("_bbsbuff;usefixed;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			String GrpName = st.nextToken();
			int page = Integer.parseInt(st.nextToken());
			UseFixedBuffGrp(activeChar, GrpName, page);
			activeChar.setVar("GrpName", GrpName, -1);
			activeChar.setVar("page", String.valueOf(page), -1);
		}
		else if(command.startsWith("_bbsbuff;editegrp;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int GpIdUse = Integer.parseInt(st.nextToken());
			EditeBuffGrp(activeChar, GpIdUse);
		}
		else if(command.startsWith("_bbsbuff;addbuffin;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int GpIdUse = Integer.parseInt(st.nextToken());
			int page = Integer.parseInt(st.nextToken());
			EditeAddBuffInGrp(activeChar, GpIdUse, page);
		}
		else if(command.startsWith("_bbsbuff;addbuffingrp;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int GpIdUse = Integer.parseInt(st.nextToken());
			int Buff = Integer.parseInt(st.nextToken());
			int BuffLvL = Integer.parseInt(st.nextToken());
			AddBuffInGrp(activeChar, GpIdUse, Buff, BuffLvL);
		}
		else if(command.startsWith("_bbsbuff;dellbufffrom;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int GpIdUse = Integer.parseInt(st.nextToken());
			int Buff = Integer.parseInt(st.nextToken());
			DellBuffFromGrp(activeChar, GpIdUse, Buff);
		}
		else if(command.startsWith("_bbsbuff;buffgrpauto;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int GpIdUse = Integer.parseInt(st.nextToken());
			String GpNameUse = st.nextToken();
			int price = Integer.parseInt(st.nextToken());
			StartAutoBuff(activeChar, GpIdUse, GpNameUse, price);//Запуск задачи.
		}
		else if(command.startsWith("_bbsbuff;buffgrpautostop;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int GpIdUse = Integer.parseInt(st.nextToken());
			String GpNameUse = st.nextToken();
			StopAutoBuff(activeChar, GpIdUse, GpNameUse);//Остановка задачи.
		}
		else if(command.startsWith("_bbsbuff;buffgrpautopet;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int GpIdUse = Integer.parseInt(st.nextToken());
			String GpNameUse = st.nextToken();
			int price = Integer.parseInt(st.nextToken());
			StartAutoBuffPet(activeChar, GpIdUse, GpNameUse, price);//Запуск задачи.
		}
		else if(command.startsWith("_bbsbuff;buffgrpautopetstop;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int GpIdUse = Integer.parseInt(st.nextToken());
			String GpNameUse = st.nextToken();
			StopAutoBuffPet(activeChar, GpIdUse, GpNameUse);//Остановка задачи.
		}
	}

	/**
	 * Бафаем группу баффов составленную администратором сервера.<br>
	 * Формируется переменная со списком всех баффов из группы и передается в StartBuffGrp  
	 * 
	 * @param
	 */
	private void BuffFixedGrp(Player activeChar, String GrpName, String target)
	{
		Connection con = null;
		CBBuffGroup bgrp;
		String allbuff = "";
		int price = 0;
		GCSArray<String> skillIds = new GCSArray<String>();
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM communitybuff_grp_allowed_buffs WHERE (locate( ? ,skillgrp)>0)");
			statement.setString(1, GrpName);
			ResultSet rcln = statement.executeQuery();

			while(rcln.next())
			{
				bgrp = new CBBuffGroup();
				bgrp.OneBuffId = rcln.getInt("skillID");
				skillIds.add("" + bgrp.OneBuffId + "");
			}
			DbUtils.closeQuietly(statement, rcln);

			for(int j = 0; j < skillIds.size(); j++)
				allbuff = new StringBuilder().append(allbuff).append(skillIds.get(j) + ";").toString();

			if(!chekCondition(activeChar)){ return; }

			price = skillIds.size() * Config.BBS_PVP_BUFFER_PRICE_ONE;

			if(price > 0 && activeChar.getAdena() < price)
			{
				activeChar.sendMessage("Недостаточно денег.");
				return;
			}

			if(price > 0)
				activeChar.reduceAdena(price, true);

			StartBuffGrp(activeChar, allbuff, target);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}

	/**
	 * Бафаем группу баффов.<br>
	 * Формируется переменная со списком всех баффов из группы и передается в StartBuffGrp  
	 * 
	 * @param
	 */
	private void BuffGrp(Player activeChar, int buffIdUse, String target)
	{
		Connection con = null;
		CBBuffGroup bgrp;
		String allbuff = "";
		int price = 0;
		GCSArray<String> skillIds = new GCSArray<String>();
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM communitybuff_grp_buffs WHERE GpId=?;");
			statement.setInt(1, buffIdUse);
			ResultSet rcln = statement.executeQuery();

			while(rcln.next())
			{
				bgrp = new CBBuffGroup();
				bgrp.OneBuffId = rcln.getInt("buffid");
				skillIds.add("" + bgrp.OneBuffId + "");
			}
			DbUtils.closeQuietly(statement, rcln);

			for(int j = 0; j < skillIds.size(); j++)
				allbuff = new StringBuilder().append(allbuff).append(skillIds.get(j) + ";").toString();

			if(!chekCondition(activeChar)){ return; }

			price = skillIds.size() * Config.BBS_PVP_BUFFER_PRICE_ONE;

			if(price > 0 && activeChar.getAdena() < price)
			{
				activeChar.sendMessage("Недостаточно денег.");
				return;
			}

			if(price > 0)
				activeChar.reduceAdena(price, true);

			StartBuffGrp(activeChar, allbuff, target);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}

	/**
	 * Запуск баффа для группы.
	 * 
	 * @param allbuff - переменная со списком всех баффов из группы. Значение устанавливается в методах BuffGrp и BuffFixedGrp
	 * @param
	 */
	private void StartBuffGrp(Player activeChar, String allbuff, String target)
	{
		Connection con = null;
		Skill skill = null;
		Skill skillTmp = null;
		Summon pet = activeChar.getPet();
		StringTokenizer stBuff = new StringTokenizer(allbuff, ";");

		while(stBuff.hasMoreTokens())
		{
			int skilltoresatore = Integer.parseInt(stBuff.nextToken());

			skillTmp = SkillTable.getInstance().getInfo(skilltoresatore, 1);

			skill = SkillTable.getInstance().getInfo(skilltoresatore, skillTmp.getBaseLevel());

			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM communitybuff_grp_allowed_buffs WHERE skillID=?;");
				st.setInt(1, skilltoresatore);
				ResultSet rs = st.executeQuery();
				if(rs.next() && rs.getInt(1) != 0)
				{
					if(target.startsWith(" Player"))
						for(EffectTemplate et : skill.getEffectTemplates())
						{
							Env env = new Env(activeChar, activeChar, skill);
							Effect effect = et.getEffect(env);
							effect.setPeriod(Config.BBS_PVP_BUFFER_ALT_TIME);
							activeChar.getEffectList().addEffect(effect);
						}
					if(target.startsWith(" Pet"))
					{
						if(pet == null)
							return;

						for(EffectTemplate et : skill.getEffectTemplates())
						{
							Env env = new Env(pet, pet, skill);
							Effect effect = et.getEffect(env);
							effect.setPeriod(Config.BBS_PVP_BUFFER_ALT_TIME);
							pet.getEffectList().addEffect(effect);
						}
					}
					DbUtils.closeQuietly(st, rs);
				}
				else
					activeChar.sendMessage("Бафф: " + skill.getName() + " (" + skill.getId() + "), не может быть использован!");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				DbUtils.closeQuietly(con);
			}
		}
	}

	/**
	 * Бафаем один бафф
	 * 
	 * @param
	 */
	private void BuffOne(Player activeChar, int buffIdUse, int buffLvL)
	{
		Summon pet = activeChar.getPet();
		if(!chekCondition(activeChar))
			return;

		if(activeChar.getEffectList().getEffectsBySkillId(1363) != null && buffIdUse == 1413 || buffIdUse == 1356 || buffIdUse == 1357 || buffIdUse == 1355)
			activeChar.getEffectList().stopEffect(1363);

		if(Config.BBS_PVP_BUFFER_PRICE_ONE > 0 && activeChar.getAdena() < Config.BBS_PVP_BUFFER_PRICE_ONE)
		{
			activeChar.sendMessage("Недостаточно денег.");
			return;
		}

		if(Config.BBS_PVP_BUFFER_PRICE_ONE > 0)
			activeChar.reduceAdena(Config.BBS_PVP_BUFFER_PRICE_ONE, true);

		if(BuffManagerDAO.getInstance().getAllowedBuffs().contains(buffIdUse))
		{
			Skill skill = SkillTable.getInstance().getInfo(buffIdUse, buffLvL);
			for(EffectTemplate et : skill.getEffectTemplates())
			{
				Env env = new Env(activeChar, activeChar, skill);
				Effect effect = et.getEffect(env);
				effect.setPeriod(Config.BBS_PVP_BUFFER_ALT_TIME);
				activeChar.getEffectList().addEffect(effect);
				if(Config.BBS_PVP_BUFER_ONE_BUFF_PET && pet != null)
				{
					Env envPet = new Env(pet, pet, skill);
					Effect effectPet = et.getEffect(envPet);
					effectPet.setPeriod(Config.BBS_PVP_BUFFER_ALT_TIME);
					pet.getEffectList().addEffect(effectPet);
				}
			}
		}
		else
		{
			activeChar.sendMessage("Ну и глупый же ты...");
			_log.warn("Player: " + activeChar + " used not allow buff: " + buffIdUse + " - Player: " + activeChar + " BANNED!!!");
			activeChar.setAccessLevel(-100); // Без временный бан на плеера
			AutoBan.Banned(activeChar, 999999, "Cheater detected! Auto ban. Buffer CB", "REBELLION"); // Ban this player
			activeChar.kick(); // Выбрасываем плеера из мира
		}
	}

	/**
	 * Удаляем бафф из набора и возвращаем список.
	 * 
	 * @param
	 */
	private void DellBuffFromGrp(Player activeChar, int gpIdUse, int buff)
	{
		Connection conDel = null;
		try
		{
			conDel = DatabaseFactory.getInstance().getConnection();
			PreparedStatement stDel = conDel.prepareStatement("DELETE FROM communitybuff_grp_buffs WHERE GpId=? AND buffid=?;");
			stDel.setInt(1, gpIdUse);
			stDel.setInt(2, buff);
			stDel.execute();
			DbUtils.closeQuietly(stDel);
			EditeBuffGrp(activeChar, gpIdUse);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conDel);
		}

	}

	/**
	 * Добавляем бафф в набор и возвращаем список.
	 * 
	 * @param
	 */
	private void AddBuffInGrp(Player activeChar, int gpIdUse, int buff, int lvl)
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM communitybuff_grp_buffs WHERE charId=? AND GpId=?;");
			st.setInt(1, activeChar.getObjectId());
			st.setInt(2, gpIdUse);
			ResultSet rs = st.executeQuery();
			rs.next();
			if(rs.getInt(1) <= (Config.BBS_PVP_BUFFER_BUFFS_PER_SET-1))
			{
				PreparedStatement stAdd = con.prepareStatement("INSERT INTO communitybuff_grp_buffs (charId,GpId,buffid,bufflvl) VALUES (?,?,?,?)");
				stAdd.setInt(1, activeChar.getObjectId());
				stAdd.setInt(2, gpIdUse);
				stAdd.setInt(3, buff);
				stAdd.setInt(4, lvl);
				stAdd.execute();
				EditeBuffGrp(activeChar, gpIdUse);
				DbUtils.closeQuietly(stAdd);
			}
			else
				activeChar.sendMessage("Набор не может содержать более " + Config.BBS_PVP_BUFFER_BUFFS_PER_SET + " баффов");
			DbUtils.closeQuietly(st, rs);
		}
		catch(Exception e)
		{}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}

	/**
	 * Страница редактора набора. Список баффов которые можно добавить в набор.
	 * 
	 * @param
	 */
	private void EditeAddBuffInGrp(Player activeChar, int gpIdUse, int page)
	{
		CBBuffAllowedBuffs bgrpAlw;
		CBBuffGroups bgrpOne;
		String GrpName = null;
		int GrpID = 0;
		double numpages = 0;
		GCSArray<String> AlwBuffIds = new GCSArray<String>();
		int offset = Config.BBS_PVP_BUFFER_BUFFS_PER_PAGE * (page - 1);
		TextBuilder html = new TextBuilder();
		TextBuilder htmltoppanel = new TextBuilder();
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			PreparedStatement stC = con.prepareStatement("SELECT * FROM communitybuff_grp WHERE GpId=?;");
			stC.setInt(1, gpIdUse);
			ResultSet rsC = stC.executeQuery();

			while(rsC.next())
			{
				bgrpOne = new CBBuffGroups();
				bgrpOne.GpId = rsC.getInt("GpId");
				bgrpOne.GpName = rsC.getString("GpName");
				bgrpOne.PlayerId = rsC.getInt("charId");
				GrpName = bgrpOne.GpName;
				GrpID = bgrpOne.GpId;
			}
			DbUtils.closeQuietly(stC, rsC);

			PreparedStatement stAlwCount = con.prepareStatement("SELECT COUNT(*) FROM communitybuff_grp_allowed_buffs");
			ResultSet rsAlwCount = stAlwCount.executeQuery();
			rsAlwCount.next();
			if(rsAlwCount.getInt(1) > Config.BBS_PVP_BUFFER_BUFFS_PER_PAGE - 1)
				numpages = Math.ceil((double) rsAlwCount.getInt(1) / Config.BBS_PVP_BUFFER_BUFFS_PER_PAGE);
			DbUtils.closeQuietly(stAlwCount, rsAlwCount);

			PreparedStatement stAlw = con.prepareStatement("SELECT * FROM communitybuff_grp_allowed_buffs WHERE NOT EXISTS (SELECT * FROM communitybuff_grp_buffs WHERE communitybuff_grp_buffs.buffid = communitybuff_grp_allowed_buffs.skillID AND communitybuff_grp_buffs.GpId =?) LIMIT " + offset + ", " + Config.BBS_PVP_BUFFER_BUFFS_PER_PAGE + "");
			stAlw.setInt(1, gpIdUse);
			ResultSet rsAlw = stAlw.executeQuery();
			while(rsAlw.next())
			{
				bgrpAlw = new CBBuffAllowedBuffs();
				bgrpAlw.BuffId = rsAlw.getInt("skillID");
				bgrpAlw.BuffLvL = rsAlw.getInt("skillLvl");
				AlwBuffIds.add("" + bgrpAlw.BuffId + "");
			}
			DbUtils.closeQuietly(stAlw, rsAlw);

			html.append(buildTable(AlwBuffIds, 3, 3, gpIdUse, null));
			htmltoppanel.append("<table width=600>");
			htmltoppanel.append("<tr>");
			htmltoppanel.append("<td><font color=30D249>Редактирование: " + GrpName + "</font></td>");
			htmltoppanel.append("<td></td>");
			htmltoppanel.append(page_list(numpages, page, GrpID, null));
			htmltoppanel.append("<td><button value=\"Назад\" action=\"bypass _bbsbuff;use;" + GrpID + "\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			htmltoppanel.append("</tr>");
			htmltoppanel.append("</table>");

			String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/buffer/buff_1.htm", activeChar); // 601
			content = content.replace("%buffgrptoppanel%", htmltoppanel.toString());
			content = content.replace("%buffgrp%", html.toString());
			ShowBoard.separateAndSend(content, activeChar);
			return;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}

	}

	/**
	 * Страница редактора набора. Список уже имеющихся баффов в наборе.
	 * 
	 * @param
	 */
	private void EditeBuffGrp(Player activeChar, int gpIdUse)
	{
		CBBuffGroup bgrp;
		CBBuffGroups bgrpOne;
		String GrpName = null;
		int GrpID = 0;
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			PreparedStatement stC = con.prepareStatement("SELECT * FROM communitybuff_grp WHERE GpId=?;");
			stC.setInt(1, gpIdUse);
			ResultSet rsC = stC.executeQuery();

			while(rsC.next())
			{
				bgrpOne = new CBBuffGroups();
				bgrpOne.GpId = rsC.getInt("GpId");
				bgrpOne.GpName = rsC.getString("GpName");
				bgrpOne.PlayerId = rsC.getInt("charId");
				GrpName = bgrpOne.GpName;
				GrpID = bgrpOne.GpId;
			}

			PreparedStatement st = con.prepareStatement("SELECT * FROM communitybuff_grp_buffs WHERE GpId=?;");
			st.setInt(1, gpIdUse);
			ResultSet rs = st.executeQuery();
			TextBuilder html = new TextBuilder();

			GCSArray<String> buffIds = new GCSArray<String>();

			while(rs.next())
			{
				bgrp = new CBBuffGroup();
				bgrp.OneId = rs.getInt("Id");
				bgrp.OneGpId = rs.getInt("GpId");
				bgrp.OnePlayerId = rs.getInt("charId");
				bgrp.OneBuffId = rs.getInt("buffid");
				buffIds.add("" + bgrp.OneBuffId + "");
			}

			DbUtils.closeQuietly(st, rs);
			DbUtils.closeQuietly(stC, rsC);

			html.append(buildTable(buffIds, 3, 2, gpIdUse, null));

			TextBuilder htmltoppanel = new TextBuilder();
			htmltoppanel.append("<table width=600>");
			htmltoppanel.append("<tr>");
			htmltoppanel.append("<td><font color=30D249>Редактирование: " + GrpName + "</font></td>");
			htmltoppanel.append("<td></td>");
			htmltoppanel.append("<td><button value=\"Добавить бафф\" action=\"bypass _bbsbuff;addbuffin;" + GrpID + ";1\" width=140 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			htmltoppanel.append("<td><button value=\"Удалить набор\" action=\"bypass _bbsbuff;dellGrp;" + GrpID + "\" width=140 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			htmltoppanel.append("<td><button value=\"Назад\" action=\"bypass _bbsbuff;use;" + GrpID + "\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			htmltoppanel.append("</tr>");
			htmltoppanel.append("</table>");

			String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/buffer/buff_1.htm", activeChar); // 601
			content = content.replace("%buffgrptoppanel%", htmltoppanel.toString());
			content = content.replace("%buffgrp%", html.toString());
			ShowBoard.separateAndSend(content, activeChar);
			return;

		}
		catch(Exception e)
		{}
		finally
		{
			DbUtils.closeQuietly(con);
		}

	}

	/**
	 * Показываем страницу фиксированной группы.<br> 
	 * Группа составляется администратором и не может быть отредактированна игроком. 
	 * 
	 * @param
	 */
	private void UseFixedBuffGrp(Player activeChar, String GrpName, int page)
	{
		CBBuffGroup bgrp;
		int price = 0;
		double numpages = 0;
		Summon l2Summon = activeChar.getPet();
		int offset = Config.BBS_PVP_BUFFER_BUFFS_PER_PAGE * (page - 1);
		Connection con = null;
		PreparedStatement statement = null;
		GCSArray<String> buffIds = new GCSArray<String>();
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			PreparedStatement stAlwCount = con.prepareStatement("SELECT COUNT(*) FROM communitybuff_grp_allowed_buffs WHERE (locate( ? ,skillgrp)>0)");
			stAlwCount.setString(1, GrpName);
			ResultSet rsAlwCount = stAlwCount.executeQuery();
			rsAlwCount.next();
			if(rsAlwCount.getInt(1) > Config.BBS_PVP_BUFFER_BUFFS_PER_PAGE - 1)
				numpages = Math.ceil((double) rsAlwCount.getInt(1) / Config.BBS_PVP_BUFFER_BUFFS_PER_PAGE);
			DbUtils.closeQuietly(stAlwCount, rsAlwCount);

			statement = con.prepareStatement("SELECT * FROM communitybuff_grp_allowed_buffs WHERE (locate( ? ,skillgrp)>0) LIMIT " + offset + ", " + Config.BBS_PVP_BUFFER_BUFFS_PER_PAGE + "");
			statement.setString(1, GrpName);
			ResultSet rcln = statement.executeQuery();

			while(rcln.next())
			{
				bgrp = new CBBuffGroup();
				bgrp.OneBuffId = rcln.getInt("skillID");
				buffIds.add("" + bgrp.OneBuffId + "");
			}
			DbUtils.closeQuietly(statement, rcln);

			price = buffIds.size() * Config.BBS_PVP_BUFFER_PRICE_ONE;

			TextBuilder html = new TextBuilder();
			html.append(buildTable(buffIds, 3, 1, 0, GrpName));

			TextBuilder htmltoppanel = new TextBuilder();
			htmltoppanel.append("<table width=600>");
			htmltoppanel.append("<tr>");
			htmltoppanel.append("<td><font color=30D249>[Все: " + price + " адена]</font>");
			htmltoppanel.append("</td>");
			if(numpages <= 1)
			{
				htmltoppanel.append("<td>Себе:</td>");
				htmltoppanel.append("<td><button value=\"Все\" action=\"bypass _bbsbuff;bufffixedgrp;" + GrpName + "; Player\" width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				if(activeChar.getVar("autoBuff@") == null && activeChar.getVar("autoFixedBuff@") == null)
					htmltoppanel.append("<td><button value=\"Включить авто-бафф\" action=\"bypass _bbsbuff;buffgrpauto;0;" + GrpName + ";" + price + "\" width=140 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				else
					htmltoppanel.append("<td><button value=\"Отключить авто-бафф\" action=\"bypass _bbsbuff;buffgrpautostop;0;" + GrpName + "\" width=140 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			}
			else
				htmltoppanel.append(page_list(numpages, page, 0, GrpName));
			htmltoppanel.append("</tr>");

			if(l2Summon != null)
			{
				htmltoppanel.append("<tr>");
				htmltoppanel.append("<td></td>");
				if(numpages <= 1)
				{
					htmltoppanel.append("<td>Питомец:</td>");
					htmltoppanel.append("<td><button value=\"Все\" action=\"bypass _bbsbuff;bufffixedgrp;" + GrpName + "; Pet\" width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
					if(activeChar.getVar("autoBuffPet@") == null && activeChar.getVar("autoFixedBuffPet@") == null)
						htmltoppanel.append("<td><button value=\"Включить авто-бафф\" action=\"bypass _bbsbuff;buffgrpautopet;0;" + GrpName + ";" + price + "\" width=140 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
					else
						htmltoppanel.append("<td><button value=\"Отключить авто-бафф\" action=\"bypass _bbsbuff;buffgrpautopetstop;0;" + GrpName + "\" width=140 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				}
				htmltoppanel.append("<td></td>");
				htmltoppanel.append("</tr>");
			}

			htmltoppanel.append("</table>");

			String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/buffer/buff_1.htm", activeChar); // 601
			content = content.replace("%buffgrptoppanel%", htmltoppanel.toString());
			content = content.replace("%buffgrp%", html.toString());
			ShowBoard.separateAndSend(content, activeChar);
			return;

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}

	}

	/**
	 * Показываем страницу группы
	 * 
	 * @param
	 */
	private void UseBuffGrp(Player activeChar, int gpIdUse)
	{
		CBBuffGroup bgrp;
		CBBuffGroups bgrpOne;
		String GrpName = null;
		int GrpID = 0;
		int price = 0;
		Summon l2Summon = activeChar.getPet();
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			PreparedStatement stC = con.prepareStatement("SELECT * FROM communitybuff_grp WHERE GpId=?;");
			stC.setInt(1, gpIdUse);
			ResultSet rsC = stC.executeQuery();

			while(rsC.next())
			{
				bgrpOne = new CBBuffGroups();
				bgrpOne.GpId = rsC.getInt("GpId");
				bgrpOne.GpName = rsC.getString("GpName");
				bgrpOne.PlayerId = rsC.getInt("charId");
				GrpID = bgrpOne.GpId;
				GrpName = bgrpOne.GpName;
			}
			DbUtils.closeQuietly(stC, rsC);

			PreparedStatement st = con.prepareStatement("SELECT * FROM communitybuff_grp_buffs WHERE GpId=?;");
			st.setInt(1, gpIdUse);
			ResultSet rs = st.executeQuery();
			TextBuilder html = new TextBuilder();

			GCSArray<String> buffIds = new GCSArray<String>();

			while(rs.next())
			{
				bgrp = new CBBuffGroup();
				bgrp.OneId = rs.getInt("Id");
				bgrp.OneGpId = rs.getInt("GpId");
				bgrp.OnePlayerId = rs.getInt("charId");
				bgrp.OneBuffId = rs.getInt("buffid");
				buffIds.add("" + bgrp.OneBuffId + "");
			}
			DbUtils.closeQuietly(st, rs);

			price = buffIds.size() * Config.BBS_PVP_BUFFER_PRICE_ONE;

			html.append(buildTable(buffIds, 3, 1, gpIdUse, null));

			TextBuilder htmltoppanel = new TextBuilder();
			htmltoppanel.append("<table width=600>");
			htmltoppanel.append("<tr>");
			htmltoppanel.append("<td><font color=30D249>" + GrpName + " [Все: " + price + " адена]</font>");
			htmltoppanel.append("</td>");
			htmltoppanel.append("<td>Себе:</td>");
			htmltoppanel.append("<td><button value=\"Все\" action=\"bypass _bbsbuff;buffgrp;" + GrpID + "; Player\" width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");

			if(activeChar.getVar("autoBuff@") == null && activeChar.getVar("autoFixedBuff@") == null)
				htmltoppanel.append("<td><button value=\"Включить авто-бафф\" action=\"bypass _bbsbuff;buffgrpauto;" + GrpID + ";null;" + price + "\" width=140 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			else
				htmltoppanel.append("<td><button value=\"Отключить авто-бафф\" action=\"bypass _bbsbuff;buffgrpautostop;" + GrpID + ";null\" width=140 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			htmltoppanel.append("<td><button value=\"Редактировать\" action=\"bypass _bbsbuff;editegrp;" + GrpID + "\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			htmltoppanel.append("</tr>");

			if(l2Summon != null)
			{
				htmltoppanel.append("<tr>");
				htmltoppanel.append("<td></td>");
				htmltoppanel.append("<td>Питомец:</td>");
				htmltoppanel.append("<td><button value=\"Все\" action=\"bypass _bbsbuff;buffgrp;" + GrpID + "; Pet\" width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				if(activeChar.getVar("autoBuffPet@") == null && activeChar.getVar("autoFixedBuffPet@") == null)
					htmltoppanel.append("<td><button value=\"Включить авто-бафф\" action=\"bypass _bbsbuff;buffgrpautopet;" + GrpID + ";null;" + price + "\" width=140 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				else
					htmltoppanel.append("<td><button value=\"Отключить авто-бафф\" action=\"bypass _bbsbuff;buffgrpautopetstop;" + GrpID + ";null\" width=140 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				htmltoppanel.append("<td></td>");
				htmltoppanel.append("</tr>");
			}

			htmltoppanel.append("</table>");

			String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/buffer/buff_1.htm", activeChar); // 601
			content = content.replace("%buffgrptoppanel%", htmltoppanel.toString());
			content = content.replace("%buffgrp%", html.toString());
			ShowBoard.separateAndSend(content, activeChar);
			return;

		}
		catch(Exception e)
		{}
		finally
		{
			DbUtils.closeQuietly(con);
		}

	}

	/**
	 * Удаляем группу баффов.
	 * 
	 * @param
	 */
	private void DellBuffGrp(Player activeChar, int gpNameDell)
	{
		Connection conDel = null;
		try
		{
			conDel = DatabaseFactory.getInstance().getConnection();

			//Удаляем группу бафов.
			PreparedStatement stDel = conDel.prepareStatement("DELETE FROM communitybuff_grp WHERE charId=? AND GpId=?;");
			stDel.setInt(1, activeChar.getObjectId());
			stDel.setInt(2, gpNameDell);
			stDel.execute();
			//Удаляем баффы которые были в удаляемой группе.
			PreparedStatement stDelin = conDel.prepareStatement("DELETE FROM communitybuff_grp_buffs WHERE charId=? AND GpId=?;");
			stDelin.setInt(1, activeChar.getObjectId());
			stDelin.setInt(2, gpNameDell);
			stDelin.execute();
		}
		catch(Exception e)
		{}
		finally
		{
			DbUtils.closeQuietly(conDel);
		}

	}

	/**
	 * Создаем группу баффов.
	 * 
	 * @param
	 */
	private void AddBuffGrp(Player activeChar, String grpNameAdd)
	{
		if(grpNameAdd.equals("") || grpNameAdd.equals(null))
		{
			activeChar.sendMessage("Вы не ввели Имя группы");
			return;
		}

		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			//Получаем кол-во групп текущего чара из таблицы и если кол-во их не превышает установленное добавляем новую.
			PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM communitybuff_grp WHERE charId=?;");
			st.setLong(1, activeChar.getObjectId());
			ResultSet rs = st.executeQuery();
			rs.next();
			if(rs.getInt(1) <= 4)
			{
				//Проверяем существует ли группа с именем которое передано в параметре
				PreparedStatement st1 = con.prepareStatement("SELECT COUNT(*) FROM communitybuff_grp WHERE charId=? AND GpName=?;");
				st1.setLong(1, activeChar.getObjectId());
				st1.setString(2, grpNameAdd);
				ResultSet rs1 = st1.executeQuery();
				rs1.next();
				if(rs1.getInt(1) == 0)
				{
					//Если группы нет, создаем.
					PreparedStatement stAdd = con.prepareStatement("INSERT INTO communitybuff_grp (charId,GpName) VALUES(?,?)");
					stAdd.setInt(1, activeChar.getObjectId());
					stAdd.setString(2, grpNameAdd);
					stAdd.execute();
				}
				else
				{
					//Если группа есть, просто обновляем ее имя.
					PreparedStatement stAdd = con.prepareStatement("UPDATE communitybuff_grp SET GpName=? WHERE charId=? AND GpName=?;");
					stAdd.setInt(1, activeChar.getObjectId());
					stAdd.setString(2, grpNameAdd);
					stAdd.execute();
				}
			}
			else
			{
				activeChar.sendMessage("Вы не можете сохранить более 5 групп");
				return;
			}

		}
		catch(Exception e)
		{}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}

	/**
	 * Показываем чару первую страницу баффера со списком его персональных групп.
	 * 
	 * @param
	 */
	private void showBuffIndexPage(Player activeChar)
	{
		CBBuffGroups bgrp;
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT * FROM communitybuff_grp WHERE charId=?;");
			st.setLong(1, activeChar.getObjectId());
			ResultSet rs = st.executeQuery();
			TextBuilder html = new TextBuilder();
			html.append("<table width=220>");
			while(rs.next())
			{
				bgrp = new CBBuffGroups();
				bgrp.GpId = rs.getInt("GpId");
				bgrp.GpName = rs.getString("GpName");
				bgrp.PlayerId = rs.getInt("charId");
				html.append("<tr>");
				html.append("<td>");
				html.append("<button value=\"" + bgrp.GpName + "\" action=\"bypass _bbsbuff;use;" + bgrp.GpId + "\" width=190 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>");
				html.append("</tr>");
			}
			html.append("</table>");

			DbUtils.closeQuietly(st, rs);

			String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/buffer/buff.htm", activeChar); // 60
			content = content.replace("%buffgrps%", html.toString());
			ShowBoard.separateAndSend(content, activeChar);
			return;

		}
		catch(Exception e)
		{}
		finally
		{
			DbUtils.closeQuietly(con);
		}

	}

	public static String buildTable(GCSArray<String> buffIds, int cols_number, int type, int grpId, String grpName)
	{
		Skill skill;
		String bottom = null;
		String res = "<table width=600>";
		double rows = Math.ceil((double) buffIds.size() / cols_number);
		int c = 0;
		for(int i = 0; i < (int) rows; i++)
		{
			res += "<tr>";

			for(int j = 0; j < cols_number; j++)
			{
				if(buffIds.size() > c)
				{
					skill = SkillTable.getInstance().getInfo(Integer.parseInt(buffIds.get(c)), 1);
					if(type == 1)
						bottom = "<td width=25><button value=\"$\" action=\"bypass _bbsbuff;buff;" + buffIds.get(c) + ";" + skill.getBaseLevel() + "\" width=25 height=32 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>";
					else if(type == 2)
						bottom = "<td width=25><button value=\">\" action=\"bypass _bbsbuff;dellbufffrom;" + grpId + ";" + buffIds.get(c) + "\" width=25 height=32 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>";
					else if(type == 3)
						bottom = "<td width=25><button value=\"<\" action=\"bypass _bbsbuff;addbuffingrp;" + grpId + ";" + buffIds.get(c) + ";" + skill.getBaseLevel() + "\" width=25 height=32 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>";
					res += "<td width=200>" + "<center>" + "<table width=200 height=32>" + "<tr>" + "<td width=32><center><img src=icon." + skill.getIcon() + " width=32 height=32></center></td>" + bottom + "<td width=128>" + "<table width=128><tr><td><font color=3293F3>" + skill.getName() + "</font></td></tr><tr><td><font color=F2C202>Уровень: " + skill.getBaseLevel() + "</font></td></tr></table>" + "</td>" + "</tr>" + "</table></center></td>";
				}
				else
					res += "<td width=150><center></center></td>";
				c++;
			}

			res += "</tr>";
		}
		res += "</table><br>";
		return res;
	}

	public static String page_list(double numpages, int page, int GrpID, String GrpName)
	{
		String index;
		String cmd;
		if(GrpID == 0 && GrpName != null)
		{
			index = GrpName;
			cmd = "usefixed";
		}
		else
		{
			index = Integer.toString(GrpID);
			cmd = "addbuffin";
		}
		String pager = "";
		pager += page > 1 ? "<td width=50><center><button value=\"<<<\" action=\"bypass _bbsbuff;" + cmd + ";" + index + ";" + (page - 1) + "\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>" : "<td width=50></td>";
		pager += "<td width=50><center>Page №" + page + "</center></td>";
		pager += page < numpages ? "<td width=50><center><button value=\">>>\" action=\"bypass _bbsbuff;" + cmd + ";" + index + ";" + (page + 1) + "\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>" : "<td width=50></td>";

		return pager;
	}

	public boolean chekCondition(Player activeChar)
	{
		if(activeChar == null || !activeChar.isConnected() || activeChar.isInOfflineMode())
			return false;
		if(activeChar.getLevel() > Config.BBS_PVP_BUFFER_MAX_LVL || activeChar.getLevel() < Config.BBS_PVP_BUFFER_MIN_LVL)
		{
			activeChar.sendMessage("Ваш уровень не отвечает требованиям!");
			return false;
		}
		if(activeChar.isCursedWeaponEquipped() || activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isFlying() || activeChar.isTerritoryFlagEquipped())
		{
			activeChar.sendMessage("Невозможно использовать в данный момент!");
			return false;
		}
		//Можно ли юзать бафера в инстансах и в зонах эпик боссов?
		if(!Config.BBS_PVP_BUFFER_ALOWED_INST_BUFF)
			if(activeChar.getReflectionId() != 0 || activeChar.isInZone(ZoneType.epic))
			{
				activeChar.sendMessage(activeChar.isLangRus() ? "Невозможно использовать в данных зонах!" : "Can not be used in these areas!");
				return false;
			}
		//Можно ли юзать бафера во время осады?
		if(!Config.BBS_PVP_BUFFER_ALLOW_SIEGE)
			if(activeChar.isInZone(ZoneType.SIEGE))
			{
				activeChar.sendMessage(activeChar.isLangRus() ? "Невозможно использовать во время осад!" : "Can not be used during the siege!");
				return false;
			}
		//Можно ли юзать бафера во время пвп флага?
		if(!Config.BBS_PVP_BUFFER_ALLOW_PVP_FLAG)
			if(activeChar.isInCombat() || activeChar.getPvpFlag() != 0)
			{
				activeChar.sendMessage(activeChar.isLangRus() ? "Невозможно использовать в данное время!" : "Can not be used at this time!");
				return false;
			}
		return true;
	}

	/**
	 * Запускаем задачу автобафа
	 * @param
	 */
	private void StartAutoBuff(Player activeChar, int gpIdUse, String gpUseName, int price)
	{
		if(!chekCondition(activeChar))
			return;

		if(price > 0 && activeChar.getAdena() < price)
		{
			activeChar.sendMessage("Недостаточно денег для запуска автобаффа!");
			return;
		}

		if(gpIdUse == 0 && gpUseName != null)
		{
			activeChar.setVar("autoFixedBuff@", "" + gpUseName + "", -1);

			BuffFixedGrp(activeChar, gpUseName, " Player");
			new buffTask(activeChar, price, true);
			UseFixedBuffGrp(activeChar, gpUseName, 1);
		}
		else
		{
			activeChar.setVar("autoBuff@", "" + gpIdUse + "", -1);

			BuffGrp(activeChar, gpIdUse, " Player");
			new buffTask(activeChar, price, false);
			UseBuffGrp(activeChar, gpIdUse);
		}
	}

	/**
	 * Останавливаем задачу автобафа 
	 * @param
	 */
	private void StopAutoBuff(Player activeChar, int gpIdUse, String gpUseName)
	{
		if(gpIdUse == 0 && gpUseName != null)
		{
			activeChar.unsetVar("autoFixedBuff@");
			UseFixedBuffGrp(activeChar, gpUseName, 1);
		}
		else
		{
			activeChar.unsetVar("autoBuff@");
			UseBuffGrp(activeChar, gpIdUse);
		}
	}

	/**
	 * Запускаем задачу автобафа для пета
	 * @param
	 */
	private void StartAutoBuffPet(Player activeChar, int gpIdUse, String gpUseName, int price)
	{
		Summon l2Summon = activeChar.getPet();

		if(l2Summon == null)
		{
			activeChar.sendMessage("Призавите питомца для запуска!");
			return;
		}

		if(!chekCondition(activeChar))
			return;

		if(price > 0 && activeChar.getAdena() < price)
		{
			activeChar.sendMessage("Недостаточно денег для запуска автобаффа питомца!");
			return;
		}

		if(gpIdUse == 0 && gpUseName != null)
		{
			activeChar.setVar("autoFixedBuffPet@", "" + gpUseName + "", -1);

			BuffFixedGrp(activeChar, gpUseName, " Pet");
			new buffTaskPet(activeChar, price, true);
			UseFixedBuffGrp(activeChar, gpUseName, 1);
		}
		else
		{
			activeChar.setVar("autoBuffPet@", "" + gpIdUse + "", -1);

			BuffGrp(activeChar, gpIdUse, " Pet");
			new buffTaskPet(activeChar, price, false);
			UseBuffGrp(activeChar, gpIdUse);
		}
	}

	/**
	 * Останавливаем задачу автобафа для пета 
	 * @param
	 */
	private void StopAutoBuffPet(Player activeChar, int gpIdUse, String gpUseName)
	{
		if(gpIdUse == 0 && gpUseName != null)
		{
			activeChar.unsetVar("autoFixedBuffPet@");
			UseFixedBuffGrp(activeChar, gpUseName, 1);
		}
		else
		{
			activeChar.unsetVar("autoBuffPet@");
			UseBuffGrp(activeChar, gpIdUse);
		}

	}

	/**
	 * Задача автобафа
	 * @param
	 */
	public class buffTask
	{
		private ScheduledFuture<?> buffTask = null;

		class buff implements Runnable
		{
			Player activeChartoBuff;
			int pricetoBuff;
			boolean fixedBuff;

			public buff(Player _activeChar, int _price, boolean _fixed)
			{
				activeChartoBuff = _activeChar;
				pricetoBuff = _price;
				fixedBuff = _fixed;
			}

			@Override
			public void run()
			{
				try
				{
					if(pricetoBuff > 0 && activeChartoBuff.getAdena() < pricetoBuff)
					{
						activeChartoBuff.sendMessage("Недостаточно денег для запуска автобаффа! Задача остановлена!");

						stopBuffTask(true);
						return;
					}

					if(activeChartoBuff.getVar("autoBuff@") == null && !fixedBuff)
					{
						stopBuffTask(true);
						return;
					}

					if(activeChartoBuff.getVar("autoFixedBuff@") == null && fixedBuff)
					{
						stopBuffTask(true);
						return;
					}

					if(activeChartoBuff == null || !activeChartoBuff.isConnected() || activeChartoBuff.isInOfflineMode())
					{
						stopBuffTask(true);
						return;
					}

					if(activeChartoBuff.isDead() || activeChartoBuff.isAlikeDead() || activeChartoBuff.isCastingNow() || activeChartoBuff.isInCombat() || activeChartoBuff.isAttackingNow() || activeChartoBuff.isInOlympiadMode() || activeChartoBuff.isFlying() || activeChartoBuff.isTerritoryFlagEquipped() || activeChartoBuff.isInZone(ZoneType.SIEGE))
					{
						activeChartoBuff.sendMessage("Невозможно запустить автобафф в данный момент! Задача остановлена!");
						stopBuffTask(true);
						return;
					}

					if(activeChartoBuff.getVar("autoBuff@") != null && activeChartoBuff.getVar("autoFixedBuff@") == null)
						BuffGrp(activeChartoBuff, Integer.parseInt(activeChartoBuff.getVar("autoBuff@")), " Player");
					else if(activeChartoBuff.getVar("autoBuff@") == null && activeChartoBuff.getVar("autoFixedBuff@") != null)
						BuffFixedGrp(activeChartoBuff, activeChartoBuff.getVar("autoFixedBuff@"), " Player");
					else
					{
						stopBuffTask(true);
						return;
					}

					buffTask = ThreadPoolManager.getInstance().schedule(new buff(activeChartoBuff, pricetoBuff, fixedBuff), Config.BBS_PVP_BUFFER_TASK_DELAY);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}

		}

		public void Shutdown(Player _activeChar)
		{
			stopBuffTask(false);
		}

		public void stopBuffTask(final boolean mayInterruptIfRunning)
		{
			if(buffTask != null)
			{
				buffTask.cancel(mayInterruptIfRunning);
				buffTask = null;
			}
		}

		private buffTask(Player activeChar, int price, boolean fixed)
		{
			buffTask = ThreadPoolManager.getInstance().schedule(new buff(activeChar, price, fixed), Config.BBS_PVP_BUFFER_TASK_DELAY);
		}
	}

	/**
	 * Задача автобафа для петов 
	 * @param
	 */
	public class buffTaskPet
	{
		private ScheduledFuture<?> buffTaskPet = null;

		class buffPet implements Runnable
		{
			Player activeChartoBuff;
			long pricetoBuff;
			boolean fixedBuff;

			public buffPet(Player _activeChar, long _price, boolean _fixed)
			{
				activeChartoBuff = _activeChar;
				pricetoBuff = _price;
				fixedBuff = _fixed;
			}

			@Override
			public void run()
			{
				try
				{
					Summon l2Summon = activeChartoBuff.getPet();

					if(l2Summon == null)
					{
						activeChartoBuff.sendMessage("Призавите питомца для запуска автобаффа! Задача остановлена!");
						stopPetBuffTask(true);
						return;
					}

					if(pricetoBuff > 0 && activeChartoBuff.getAdena() < pricetoBuff)
					{
						activeChartoBuff.sendMessage("Недостаточно денег для запуска автобаффа питомца! Задача остановлена!");
						stopPetBuffTask(true);
						return;
					}

					if(activeChartoBuff.getVar("autoBuffPet@") == null && !fixedBuff)
					{
						stopPetBuffTask(true);
						return;
					}

					if(activeChartoBuff.getVar("autoFixedBuffPet@") == null && fixedBuff)
					{
						stopPetBuffTask(true);
						return;
					}

					if(activeChartoBuff == null || !activeChartoBuff.isConnected() || activeChartoBuff.isInOfflineMode())
					{
						stopPetBuffTask(true);
						return;
					}

					if(activeChartoBuff.isDead() || activeChartoBuff.isAlikeDead() || activeChartoBuff.isCastingNow() || activeChartoBuff.isInCombat() || activeChartoBuff.isAttackingNow() || activeChartoBuff.isInOlympiadMode() || activeChartoBuff.isFlying() || activeChartoBuff.isTerritoryFlagEquipped() || activeChartoBuff.isInZone(ZoneType.SIEGE))
					{
						activeChartoBuff.sendMessage("Невозможно запустить автобафф питомца в данном! Задача остановлена!");
						stopPetBuffTask(true);
						return;
					}

					if(activeChartoBuff.getVar("autoBuffPet@") != null && activeChartoBuff.getVar("autoFixedBuffPet@") == null)
						BuffGrp(activeChartoBuff, Integer.parseInt(activeChartoBuff.getVar("autoBuffPet@")), " Pet");
					else if(activeChartoBuff.getVar("autoBuffPet@") == null && activeChartoBuff.getVar("autoFixedBuffPet@") != null)
						BuffFixedGrp(activeChartoBuff, activeChartoBuff.getVar("autoFixedBuffPet@"), " Pet");
					else
					{
						stopPetBuffTask(true);
						return;
					}

					buffTaskPet = ThreadPoolManager.getInstance().schedule(new buffPet(activeChartoBuff, pricetoBuff, fixedBuff), Config.BBS_PVP_BUFFER_TASK_DELAY);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}

		}

		public void Shutdown(Player _activeChar)
		{
			stopPetBuffTask(false);
		}

		public void stopPetBuffTask(final boolean mayInterruptIfRunning)
		{
			if(buffTaskPet != null)
			{
				buffTaskPet.cancel(mayInterruptIfRunning);
				buffTaskPet = null;
			}
		}

		private buffTaskPet(Player activeChar, long price, boolean fixed)
		{
			buffTaskPet = ThreadPoolManager.getInstance().schedule(new buffPet(activeChar, price, fixed), Config.BBS_PVP_BUFFER_TASK_DELAY);
		}
	}

	/**
	 * Не используемый, но вызываемый метод имплемента
	 */
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{}
}