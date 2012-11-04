package services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Summon;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.skills.effects.EffectTemplate;
import l2r.gameserver.stats.Env;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Util;

//издевался 4ipolino 
//TODO список бафов не помешает вынести в бд

public class Buffer extends Functions implements ScriptFile
{

private static int grpCount1, grpCount2, grpCount3, grpCount4, grpCount5;
private static final Logger _log = LoggerFactory.getLogger(Buffer.class);

	private static int buffs[][] = { // id, lvl, group
		{ 1251, 2, 5 }, // Chant of Fury
		{ 1252, 3, 5 }, // Chant of Evasion
		{ 1253, 3, 5 }, // Chant of Rage
		{ 1308, 3, 5 }, // Chant of Predator
		{ 1309, 3, 5 }, // Chant of Eagle
		{ 1310, 4, 5 }, // Chant of Vampire
		{ 1362, 1, 5 }, // Chant of Spirit
		{ 1363, 1, 5 }, // Chant of Victory
		{ 1390, 3, 5 }, // War Chant
		{ 1391, 3, 5 }, // Earth Chant
		{ 1500, 1, 4 }, // Improved magic
		{ 1503, 1, 4 }, // Improved shield defense
		{ 1303, 2, 4 }, // Wild Magic
		{ 1353, 1, 4 }, // Divine Protection
		{ 4350, 4, 4 }, // Resist shok
		{ 1504, 1, 4 }, // Improved movement
		{ 1397, 3, 4 }, // Clarity
		{ 4352, 2, 4 }, // Berserker spirit
		{ 4346, 4, 4 }, // Mental shield
		{ 4355, 3, 4 }, // Acumen
		{ 1501, 1, 4 }, // Improved condition
		{ 1352, 1, 4 }, // Elemental Protection
		{ 4351, 6, 4 }, // Concentration
		{ 1362, 1, 4 }, // Chant of spirit
		{ 1461, 1, 4 }, // Chant of protection
		{ 1413, 1, 4 }, // Magnus chant
		{ 264, 1, 4 }, // Song of earth
		{ 267, 1, 4 }, // Song of Warding
		{ 268, 1, 4 }, // Song of wind
		{ 304, 1, 4 }, // Song of vitality
		{ 363, 1, 4 }, // Song of meditation
		{ 349, 1, 4 }, // Song of renewal
		{ 273, 1, 4 }, // Dance of mystic
		{ 276, 1, 4 }, // Dance of Concentration
		{ 365, 1, 4 }, // Dance of siren
		{ 915, 1, 4 }, // dance of Berserker
		{ 1397, 3, 3 }, // Clarity
		{ 4350, 4, 3 }, // Resist shok
		{ 1500, 1, 3 }, // Improved magic
		{ 1503, 1, 3 }, // Improved shield defense
		{ 4346, 4, 3 }, // Mental shield
		{ 4352, 2, 3 }, // Berserker spirit
		{ 1352, 1, 3 }, // Elemental Protection
		{ 349, 1, 3 }, // Song of renewal
		{ 268, 1, 3 }, // Song of wind
		{ 304, 1, 3 }, // Song of vitality
		{ 269, 1, 3 }, // Song of hunter
		{ 267, 1, 3 }, // Song of Warding
		{ 264, 1, 3 }, // Song of Earth
		{ 310, 1, 3 }, // Dance of the vampire
		{ 271, 1, 3 }, // Dance of the warrior
		{ 274, 1, 3 }, // Dance of the fire
		{ 275, 1, 3 }, // Dance of the fury
		{ 1461, 1, 3 }, // Chant of protection
		{ 1519, 1, 3 }, // Chant of blood awakening
		{ 1363, 1, 3 }, // Chant of victory
		{ 1518, 1, 3 }, // Chant of critical attack
		{ 1517, 1, 3 }, // Chant of combat
		{ 1535, 1, 3 }, // Chatn of movement
		{ 1390, 3, 3 }, // War chant
		{ 1310, 3, 3 }, // Chant of vampire
		{ 306, 1, 2 },
		{ 308, 1, 2 },
		{ 307, 1, 2 },
		{ 309, 1, 2 },
		{ 266, 1, 2 },
		{ 529, 1, 2 },
		{ 530, 1, 2 },
		{ 1303, 2, 2 },
		{ 1085, 3, 2 },
		{ 1040, 3, 2 },
		{ 1062, 2, 2 },
		{ 4703, 3, 2 },
		{ 1389, 3, 2 },
		{ 1461, 1, 2 },
		{ 1413, 1, 2 },
		{ 1191, 3, 2 },
		{ 1182, 3, 2 },
		{ 1189, 3, 2 },
		{ 1392, 3, 2 },
		{ 1035, 4, 2 },
		{ 1259, 4, 2 },
		{ 1460, 1, 2 },
		{ 1044, 3, 2 },
		{ 1078, 6, 2 },
		{ 1259, 4, 2 },
		{ 264, 1, 2 },
		{ 265, 1, 2 },
		{ 267, 1, 2 },
		{ 268, 1, 2 },
		{ 304, 1, 2 },
		{ 349, 1, 2 },
		{ 363, 1, 2 },
		{ 273, 1, 2 },
		{ 276, 1, 2 },
		{ 365, 1, 2 },
		{ 270, 1, 2 },
		{ 830, 1, 2 },
		{ 1500, 1, 2 },
		{ 1503, 1, 2 },
		{ 1501, 1, 2 },
		{ 1504, 1, 2 },
		{ 1323, 1, 1 },
		{ 1240, 3, 1 },
		{ 1501, 1, 1 },
		{ 1036, 2, 1 },
		{ 1062, 2, 1 },
		{ 4699, 3, 1 },
		{ 1388, 3, 1 },
		{ 1461, 1, 1 },
		{ 1191, 3, 1 },
		{ 1182, 3, 1 },
		{ 1189, 3, 1 },
		{ 1392, 3, 1 },
		{ 1352, 1, 1 },
		{ 1035, 4, 1 },
		{ 264, 1, 1 },
		{ 265, 1, 1 },
		{ 267, 1, 1 },
		{ 268, 1, 1 },
		{ 269, 1, 1 },
		{ 304, 1, 1 },
		{ 349, 1, 1 },
		{ 363, 1, 1 },
		{ 364, 1, 1 },
		{ 271, 1, 1 },
		{ 274, 1, 1 },
		{ 275, 1, 1 },
		{ 270, 1, 1 },
		{ 269, 1, 1 },
		{ 306, 1, 1 },
		{ 308, 1, 1 },
		{ 272, 1, 1 },
		{ 307, 1, 1 },
		{ 309, 1, 1 },
		{ 310, 1, 1 },
		{ 266, 1, 1 },
		{ 529, 1, 1 },
		{ 530, 1, 1 },
		{ 1502, 1, 1 },
		{ 1503, 1, 1 },
		{ 1517, 1, 1 },
		{ 1259, 4, 1 },
		{ 1519, 1, 1 },
		{ 1323, 1, 1 },
		{ 1363, 1, 1 }, // Chant of Victory
		{ 1504, 1, 1 }
		};

	public void onLoad()
	{
		if(Config.BUFFER_ON)
			_log.info("Loaded Service: Buffer [state: activated]");
		else
			_log.info("Loaded Service: Buffer [state: deactivated]");

		for(int buff[] : buffs)
			switch(buff[2])
			{
				case 1:
					grpCount1++;
				break;
				case 2:
					grpCount2++;
				break;
				case 3:
					grpCount3++;
				break;
				case 4:
					grpCount4++;
				break;
				case 5:
					grpCount5++;
				break;
			}
	}

	public void onReload()
	{}

	public void onShutdown()
	{}


	public void doBuffGroup(String[] args)
	{
		Player player = (Player) getSelf();
		Summon pet = player.getPet();

		if(!checkCondition(player))
			return;
		if(player.isCursedWeaponEquipped() || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.isFlying() || player.isTerritoryFlagEquipped())
            return;
		if(player.getAdena() < Config.BUFFER_PRICE * (Integer.valueOf(args[1]) + 2))
		{
			player.sendMessage("Недостаточно денег.");
			return;
		}
		player.reduceAdena(Config.BUFFER_PRICE * (Integer.valueOf(args[1]) + 2));

		int id_groups = Integer.valueOf(args[0]);
		int select_id = Integer.valueOf(args[1]);
		Skill skill;
		for(int buff[] : buffs)
			if(buff[2] == id_groups)
			{
				if(select_id == 0)
				{
					skill = SkillTable.getInstance().getInfo(buff[0], buff[1]);
					for(EffectTemplate et : skill.getEffectTemplates())
					{
						Env env = new Env(player, player, skill);
						Effect effect = et.getEffect(env);
						effect.setPeriod(Config.BBS_PVP_BUFFER_ALT_TIME);
						player.getEffectList().addEffect(effect);
					}
				}
				if(select_id == 1)
				{
					if(pet == null)
						return;

					skill = SkillTable.getInstance().getInfo(buff[0], buff[1]);
					for(EffectTemplate et : skill.getEffectTemplates())
					{
						Env envPet = new Env(pet, pet, skill);
						Effect effectPet = et.getEffect(envPet);
						effectPet.setPeriod(Config.BBS_PVP_BUFFER_ALT_TIME);
						pet.getEffectList().addEffect(effectPet);
					}
				}
			}
	}

	public void doBuff(String[] args)
	{
		Player player = (Player) getSelf();
		Summon pet = player.getPet();

		if(!checkCondition(player))
			return;
		
			if(player.isCursedWeaponEquipped() || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.isFlying() || player.isTerritoryFlagEquipped())
            return;

		if(player.getAdena() < Config.BUFFER_PRICE)
		{
			player.sendMessage("Недостаточно денег.");
			return;
		}

		try
		{
			int skill_id = Integer.valueOf(args[0]);
			int skill_lvl = Integer.valueOf(args[1]);
			int select_id = Integer.valueOf(args[2]);
			Skill skill = SkillTable.getInstance().getInfo(skill_id, skill_lvl);
			if(select_id == 0)
				for(EffectTemplate et : skill.getEffectTemplates())
				{
					Env env = new Env(player, player, skill);
					Effect effect = et.getEffect(env);
					effect.setPeriod(Config.BBS_PVP_BUFFER_ALT_TIME);
					player.getEffectList().addEffect(effect);
				}
			
			if(select_id == 1)
			{
				if(pet == null)
					return;
				for(EffectTemplate et : skill.getEffectTemplates())
				{
				Env envPet = new Env(pet, pet, skill);
				Effect effectPet = et.getEffect(envPet);
				effectPet.setPeriod(Config.BBS_PVP_BUFFER_ALT_TIME);
				pet.getEffectList().addEffect(effectPet);
				}
			}
			player.reduceAdena(Config.BUFFER_PRICE);
			
		}
		
		catch(Exception e)
		{
			player.sendMessage("Invalid skill!");
		}
		show(HtmCache.getInstance().getNotNull("default/13100.htm", player), player);
	}

	public boolean checkCondition(Player player)
	{
		if(!Config.BUFFER_ON || player == null)
			return false;

		String html;

		if(player.getLevel() > Config.BUFFER_MAX_LVL || player.getLevel() < Config.BUFFER_MIN_LVL)
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/no-lvl.htm", player);
			html = html.replace("%min_lvl%", Integer.toString(Config.BUFFER_MIN_LVL));
			html = html.replace("%max_lvl%", Integer.toString(Config.BUFFER_MAX_LVL));
			show(html, player);
			return false;
		}
        return true;
	}

	public void SelectMenu(String[] args)
	{
		int select_menu = Integer.valueOf(args[0]);
		Player player = (Player) getSelf();

		String html = null;

		if(select_menu == 0)
			html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/buffschar.htm", player);

		if(select_menu == 1)
		{
			if(Config.BUFFER_PET_ENABLED != true)
				return;
			if(player.getPet() == null)
				return;
			html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/buffspet.htm", player);
		}

		assert html != null;
		html = html.replace("%grp_price1%", Util.formatAdena(Config.BUFFER_PRICE * (grpCount1 + 2)));
		html = html.replace("%grp_price2%", Util.formatAdena(Config.BUFFER_PRICE * (grpCount2 + 2)));
		html = html.replace("%grp_price3%", Util.formatAdena(Config.BUFFER_PRICE * (grpCount3 + 2)));
		html = html.replace("%grp_price4%", Util.formatAdena(Config.BUFFER_PRICE * (grpCount4 + 2)));
		html = html.replace("%grp_price5%", Util.formatAdena(Config.BUFFER_PRICE * (grpCount5 + 2)));
		html = html.replace("%buffs_in_grp1%", Integer.toString(grpCount1));
		html = html.replace("%buffs_in_grp2%", Integer.toString(grpCount2));
		html = html.replace("%buffs_in_grp3%", Integer.toString(grpCount3));
		html = html.replace("%buffs_in_grp4%", Integer.toString(grpCount4));
		html = html.replace("%buffs_in_grp5%", Integer.toString(grpCount5));
		html = html.replace("%price%", Util.formatAdena(Config.BUFFER_PRICE));
		show(html, player);
	}

	public void SelectBuffs()
	{
	Player player = (Player) getSelf();
	
	if(!checkCondition(player))
		return;
			if(player.isCursedWeaponEquipped() || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.isFlying() || player.isTerritoryFlagEquipped())
            return;	
	
	show(HtmCache.getInstance().getNotNull("default/13100.htm", player), player);
	}

	public String OutDia()
	{
	if(!Config.BUFFER_ON)
		return "";
	String append = "<br><a action=\"bypass -h scripts_services.Buffer:SelectBuffs\">";
	append += new CustomMessage("scripts.services.Buffer.selectBuffs", getSelf());
	append += "</a>";
		return append;
	}


	public String DialogAppend_12741(Integer val)
	{
	if(val != 0)
		return "";
	return OutDia();
	}

	public class BeginBuff implements Runnable
	{
		Creature _buffer;
		Skill _skill;
		Player _target;

		public BeginBuff(Creature buffer, Skill skill, Player target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		public void run()
		{
			if(_target.isInOlympiadMode())
				return;
			_buffer.broadcastPacket(new MagicSkillUse(_buffer, _target, _skill.getDisplayId(), _skill.getLevel(), 1, 0));
			ThreadPoolManager.getInstance().schedule(new EndBuff(_buffer, _skill, _target), 1);
		}
	}

	public class EndBuff implements Runnable
	{
		Creature _buffer;
		Skill _skill;
		Player _target;

		public EndBuff(Creature buffer, Skill skill, Player target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		public void run()
		{
			_skill.getEffects(_buffer, _target, false, false);
			_buffer.broadcastPacket(new MagicSkillLaunched(_buffer.getObjectId(), _skill.getId(), _skill.getLevel(), _target));
		}
	}

	public class BeginPetBuff implements Runnable
	{
		Creature _buffer;
		Skill _skill;
		Summon _target;

		public BeginPetBuff(Creature buffer, Skill skill, Summon target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		public void run()
		{
			_buffer.broadcastPacket(new MagicSkillUse(_buffer, _target, _skill.getDisplayId(), _skill.getLevel(), 1, 0));
			ThreadPoolManager.getInstance().schedule(new EndPetBuff(_buffer, _skill, _target), 1);
		}
	}

	public class EndPetBuff implements Runnable
	{
		Creature _buffer;
		Skill _skill;
		Summon _target;

		public EndPetBuff(Creature buffer, Skill skill, Summon target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		public void run()
		{
			_skill.getEffects(_buffer, _target, false, false);
			_buffer.broadcastPacket(new MagicSkillLaunched(_buffer.getObjectId(), _skill.getId(), _skill.getLevel(), _target));
		}

}
}