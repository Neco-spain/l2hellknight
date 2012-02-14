package l2rt.gameserver.communitybbs.Manager;

import l2rt.common.ThreadPoolManager;
import l2rt.config.ConfigSystem;
import l2rt.database.*;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.Config;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.TownManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.entity.residence.Residence;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.network.serverpackets.MagicSkillLaunched;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.network.serverpackets.ShowBoard;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.effects.EffectTemplate;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.ResultSet;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class BuffBBSManager extends BaseBBSManager
{
	static final Logger _log = Logger.getLogger(BuffBBSManager.class.getName());
	private static BuffBBSManager _Instance = null;
	private static int grpCount1;
	private static int grpCount2;
	private static int grpCount3;
	private static int grpCount4;
	private static int grpCount5;
	private static int[][] buffs = { { 1251, 2, 5 }, { 1252, 3, 5 }, { 1253, 3, 5 }, { 1284, 3, 5 }, { 1308, 3, 5 }, { 1309, 3, 5 }, { 1310, 4, 5 }, { 1362, 1, 5 }, { 1363, 1, 5 }, { 1390, 3, 5 }, { 1391, 3, 5 }, { 264, 1, 4 }, { 265, 1, 4 }, { 266, 1, 4 }, { 267, 1, 4 }, { 268, 1, 4 }, { 269, 1, 4 }, { 270, 1, 4 }, { 304, 1, 4 }, { 305, 1, 4 }, { 306, 1, 4 }, { 308, 1, 4 }, { 349, 1, 4 }, { 363, 1, 4 }, { 364, 1, 4 }, { 271, 1, 3 }, { 272, 1, 3 }, { 273, 1, 3 }, { 274, 1, 3 }, { 275, 1, 3 }, { 276, 1, 3 }, { 277, 1, 3 }, { 307, 1, 3 }, { 309, 1, 3 }, { 310, 1, 3 }, { 311, 1, 3 }, { 365, 1, 3 }, { 7059, 1, 2 }, { 4356, 3, 2 }, { 4355, 3, 2 }, { 4352, 1, 2 }, { 4346, 4, 2 }, { 4351, 6, 2 }, { 4342, 2, 2 }, { 4347, 6, 2 }, { 4348, 6, 2 }, { 4344, 3, 2 }, { 7060, 1, 2 }, { 4350, 4, 2 }, { 7057, 1, 1 }, { 4345, 3, 1 }, { 4344, 3, 1 }, { 4349, 2, 1 }, { 4342, 2, 1 }, { 4347, 6, 1 }, { 4357, 2, 1 }, { 4359, 3, 1 }, { 4358, 3, 1 }, { 4360, 3, 1 }, { 4354, 4, 1 }, { 4346, 4, 1 } };

	public static BuffBBSManager getInstance()
	{
		if (_Instance == null)
			_Instance = new BuffBBSManager();
		return _Instance;
	}

	BuffBBSManager()
	{
		Load();
	}

	public void Load()
	{
		for (int[] buff : buffs)
			switch (buff[2])
			{
				case 1:
					grpCount1 += 1;
					break;
				case 2:
					grpCount2 += 1;
					break;
				case 3:
					grpCount3 += 1;
					break;
				case 4:
					grpCount4 += 1;
					break;
				case 5:
					grpCount5 += 1;
			}
	}

	private boolean confirmBuff(int path, int skill_id, int skill_lvl)
	{
		LineNumberReader lnr = null;
		boolean conf = false;
		try
		{
			File data = new File(Config.DATAPACK_ROOT, "data/html/CommunityBoardPVP/" + path + ".htm");
			lnr = new LineNumberReader(new FileReader(data));
			String line;
			while ((line = lnr.readLine()) != null)
			{
				int index = line.indexOf("_bbsbuff");
				if (line.startsWith("_bbsbuff;buff;" + path + ";" + skill_id + ";" + skill_lvl + ";", index))
					conf = true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception e1)
			{ /* ignore problems */}
		}
		return conf;
	}

	public void parsecmd(String command, L2Player player)
	{
		if (!command.equals("_bbsbuff;"))
		{
			if (command.startsWith("_bbsbuff;buff;"))
			{
				StringTokenizer stBuff = new StringTokenizer(command, ";");
				stBuff.nextToken();
				stBuff.nextToken();
				int path = Integer.parseInt(stBuff.nextToken());
				int skill_id = Integer.parseInt(stBuff.nextToken());
				int skill_lvl = Integer.parseInt(stBuff.nextToken());
				String BuffTarget = stBuff.nextToken();
				if(confirmBuff(path, skill_id, skill_lvl))
					doBuff(skill_id, skill_lvl, BuffTarget, player);
				else
					Log.IllegalPlayerAction(player, "This player: " + player.getName() + " is cheater, please baned.", 0);
			}
			else if (command.startsWith("_bbsbuff;grp;"))
			{
				StringTokenizer stBuffGrp = new StringTokenizer(command, ";");
				stBuffGrp.nextToken();
				stBuffGrp.nextToken();
				int id_groups = Integer.parseInt(stBuffGrp.nextToken());
				String BuffTarget = stBuffGrp.nextToken();

				doBuffGroup(id_groups, BuffTarget, player);
			}
			else if (command.equals("_bbsbuff;cancel"))
				player.getEffectList().stopAllEffects();
			else if (command.equals("_bbsbuff;regmp"))
				player.setCurrentMp(player.getMaxMp());
			else if (command.equals("_bbsbuff;save"))
			{
				if (!ConfigSystem.getBoolean("restoreBuff"))
				{
					player.sendMessage(new CustomMessage("l2rt.gameserver.communitybbs.Manager.BuffBBSManager.SaveBuff", player));
					return;
				}

				SAVE(player);
			}
			else if (command.equals("_bbsbuff;restore"))
			{
				if (!ConfigSystem.getBoolean("restoreBuff"))
				{
					player.sendMessage(new CustomMessage("l2rt.gameserver.communitybbs.Manager.BuffBBSManager.RestorBuff", player));
					return;
				}

				RESTOR(player);
			}
			else
				ShowBoard.separateAndSend("<html><body><br><br><center>В bbsbuff функция: " + command + " пока не реализована</center><br><br></body></html>", player);
		}
	}

	public void doBuff(int skill_id, int skill_lvl, String BuffTarget, L2Player player)
	{
		L2Summon pet = player.getPet();
		
		if (!checkCondition(player))
			return;

		if (player.getAdena() < ConfigSystem.getInt("OneBuffPrice"))
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		try
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skill_id, skill_lvl);

			if (BuffTarget.startsWith(" Player"))
				for (EffectTemplate et : skill.getEffectTemplates())
				{
					Env env = new Env(player, player, skill);
					L2Effect effect = et.getEffect(env);
					effect.setPeriod(ConfigSystem.getInt("BuffTime"));
					player.getEffectList().addEffect(effect);
				}
			if (BuffTarget.startsWith(" Pet"))
			{
				if (pet == null)
					return;

				for (EffectTemplate et : skill.getEffectTemplates())
				{
					Env env = new Env(pet, pet, skill);
					L2Effect effect = et.getEffect(env);
					effect.setPeriod(ConfigSystem.getInt("BuffTime"));
					pet.getEffectList().addEffect(effect);
				}
			}
			player.reduceAdena(ConfigSystem.getInt("OneBuffPrice"), true);
		}
		catch (Exception e)
		{
			player.sendMessage("Invalid skill!");
		}
	}

	public void doBuffGroup(int id_groups, String BuffTarget, L2Player player)
	{
		L2Summon pet = player.getPet();

		if (!checkCondition(player))
			return;

		if (player.getAdena() < ConfigSystem.getInt("OneBuffPrice") * ConfigSystem.getInt("GroupBuffPriceModifier"))
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		player.reduceAdena((ConfigSystem.getInt("OneBuffPrice") * ConfigSystem.getInt("GroupBuffPriceModifier")), true);

		for (int[] buff : buffs)
		{
			if (buff[2] != id_groups)
				continue;
			if (BuffTarget.startsWith(" Player"))
			{
				L2Skill skill = SkillTable.getInstance().getInfo(buff[0], buff[1]);
				for (EffectTemplate et : skill.getEffectTemplates())
				{
					Env env = new Env(player, player, skill);
					L2Effect effect = et.getEffect(env);
					effect.setPeriod(ConfigSystem.getInt("BuffTime"));
					player.getEffectList().addEffect(effect);
				}
			}

			if (!BuffTarget.startsWith(" Pet"))
				continue;
			if (pet == null)
				return;

			L2Skill skill = SkillTable.getInstance().getInfo(buff[0], buff[1]);

			for (EffectTemplate et : skill.getEffectTemplates())
			{
				Env env = new Env(pet, pet, skill);
				L2Effect effect = et.getEffect(env);
				effect.setPeriod(ConfigSystem.getInt("BuffTime"));
				pet.getEffectList().addEffect(effect);
			}
		}
	}

	private void SAVE(L2Player player)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT COUNT(*) FROM community_skillsave WHERE charId=?;");
			statement.setInt(1, player.getObjectId());
			rs = statement.executeQuery();
			rs.next();
			String allbuff = "";

			L2Effect[] skill = player.getEffectList().getAllFirstEffects();
			for (int j = 0; j < skill.length; j++)
				allbuff = allbuff + new StringBuilder().append(skill[j].getSkill().getId()).append(";").toString();

			if (rs.getInt(1) == 0)
			{
				statement = con.prepareStatement("INSERT INTO community_skillsave (charId,skills) values (?,?)");
				statement.setInt(1, player.getObjectId());
				statement.setString(2, allbuff);
				statement.execute();
				statement.close();
			}
			else
			{
				statement = con.prepareStatement("UPDATE community_skillsave SET skills=? WHERE charId=?;");
				statement.setString(1, allbuff);
				statement.setInt(2, player.getObjectId());
				statement.execute();
				statement.close();
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		} 
		finally
		{
            DatabaseUtils.closeDatabaseCSR(con, statement, rs);
        }
	}

	private void RESTOR(L2Player player)
	{
		if (player.isInOlympiadMode())
		{
			player.sendMessage("Во время Олимпиады нельзя использовать данную функцию.");
			return;
		}

		if (!checkCondition(player))
			return;

		if (player.getAdena() < ConfigSystem.getInt("OneBuffPrice") * ConfigSystem.getInt("GroupBuffPriceModifier"))
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		player.reduceAdena((ConfigSystem.getInt("OneBuffPrice") * ConfigSystem.getInt("GroupBuffPriceModifier")), true);

		ThreadConnection con = null;
		FiltredStatement community_skillsave_statement = null;
		FiltredPreparedStatement communitybuff_statement = null;
		ResultSet community_skillsave_rs = null; ResultSet communitybuff_rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			community_skillsave_statement = con.createStatement();

			community_skillsave_rs = community_skillsave_statement.executeQuery("SELECT `charId`, `skills`, `pet` FROM `community_skillsave` WHERE `charId`='" + player.getObjectId() + "'");

			if (!community_skillsave_rs.next())
				return;
			String allskills = community_skillsave_rs.getString(2);
			StringTokenizer stBuff = new StringTokenizer(allskills, ";");
			while (stBuff.hasMoreTokens())
			{
				int skilltoresatore = Integer.parseInt(stBuff.nextToken());
				int skilllevel = SkillTable.getInstance().getBaseLevel(skilltoresatore);
				L2Skill skill = SkillTable.getInstance().getInfo(skilltoresatore, skilllevel);

				if (communitybuff_statement == null)
					communitybuff_statement = con.prepareStatement("SELECT COUNT(*) FROM `communitybuff` WHERE `skillID`=?");

				communitybuff_statement.setInt(1, skilltoresatore);
				communitybuff_rs = communitybuff_statement.executeQuery();

				if (communitybuff_rs.next())
				{
					if (communitybuff_rs.getInt(1) != 0)
					{
						for (EffectTemplate et : skill.getEffectTemplates())
						{
							Env env = new Env(player, player, skill);
							L2Effect effect = et.getEffect(env);
							effect.setPeriod(ConfigSystem.getInt("BuffTime"));
							player.getEffectList().addEffect(effect);
						}
					}
					else
					{
						player.sendMessage("Бафф: " + skill.getName() + " (" + skill.getId() + "), не может быть восстановлен!");
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, community_skillsave_statement, community_skillsave_rs);
			DatabaseUtils.closeDatabaseSR(communitybuff_statement, communitybuff_rs);
		}
	}

	public boolean checkCondition(L2Player player)
	{
		if (player == null)
			return false;

		if(player.getReflectionId() != 0 && !ConfigSystem.getBoolean("ALlowCBBufferInInstance"))
		{
			player.sendMessage("Бафф доступен только в обычном мире.");
			return false;
		}

		if (!ConfigSystem.getBoolean("pvpBoardBuffer"))
		{
			player.sendMessage("Функция баффа отключена.");
			return false;
		}

		if (player.getLevel() > ConfigSystem.getInt("CommBufferMaxLvl") || player.getLevel() < ConfigSystem.getInt("CommBufferMinLvl"))
		{
			player.sendMessage("Ваш уровень не отвечает требованиям!");
			return false;
		}
		
		if(!ConfigSystem.getBoolean("AllowCBBufferOnEvent"))
		{
			if(player.getVar("LastHero_backCoords") != null || player.getVar("TvT_backCoords") != null || player.getVar("CtF_backCoords") != null || player.getVar("Tournament_backCoords") != null)
			{
				player.sendMessage("Нельзя использовать бафф во время эвентов.");
				return false;			
			}
		}

		if (!ConfigSystem.getBoolean("AllowCBBufferOnSiege"))
		{
			Residence castle = TownManager.getInstance().getClosestTown(player).getCastle();
			Siege siege = castle.getSiege();
			if (siege != null && siege.isInProgress())
			{
				player.sendMessage("Нельзя использовать бафф во время осады.");
				return false;
			}
		}
		return true;
	}

	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player player)
	{
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
			_buffer.broadcastPacket(new MagicSkillUse(_buffer, _target, _skill.getDisplayId(), _skill.getLevel(), 0, 0));
			ThreadPoolManager.getInstance().scheduleGeneral(new BuffBBSManager.EndPetBuff(_buffer, _skill, _target), 0);
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
			if (_target.isInOlympiadMode())
				return;
			_buffer.broadcastPacket(new MagicSkillUse(_buffer, _target, _skill.getDisplayId(), _skill.getLevel(), 0, 0));
			ThreadPoolManager.getInstance().scheduleGeneral(new BuffBBSManager.EndBuff(_buffer, _skill, _target), 0);
		}
	}
}