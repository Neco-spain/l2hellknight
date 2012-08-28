package net.sf.l2j.gameserver.ai.special.manager;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.special.Antharas;
import net.sf.l2j.gameserver.ai.special.Baium;
import net.sf.l2j.gameserver.ai.special.Benom;
import net.sf.l2j.gameserver.ai.special.Core;
import net.sf.l2j.gameserver.ai.special.EvaBox;
import net.sf.l2j.gameserver.ai.special.FairyTrees;
import net.sf.l2j.gameserver.ai.special.FleeNpc;
import net.sf.l2j.gameserver.ai.special.Frintezza;
import net.sf.l2j.gameserver.ai.special.Gordon;
import net.sf.l2j.gameserver.ai.special.HotSprings;
import net.sf.l2j.gameserver.ai.special.IceFairySirra;
import net.sf.l2j.gameserver.ai.special.Monastery;
import net.sf.l2j.gameserver.ai.special.Orfen;
import net.sf.l2j.gameserver.ai.special.QueenAnt;
import net.sf.l2j.gameserver.ai.special.Transform;
import net.sf.l2j.gameserver.ai.special.Valakas;
import net.sf.l2j.gameserver.ai.special.Zaken;
import net.sf.l2j.gameserver.ai.special.ZombieGatekeepers;


public class AILoader
{
	private static final Logger _log = Logger.getLogger(AILoader.class.getName());

	public static void init()
	{
		_log.info(" ---------------");
		_log.info("AI load:");
		_log.info(" ---------------");
		if (Config.GB_LOADER)
		{
			_log.info(" - Valakas");
			new Valakas(-1,"valakas","ai");
		}
		if (Config.GB_LOADER)
		{
			_log.info(" - Zaken");
			new Zaken(-1,"zaken","ai");
		}
		if (Config.GB_LOADER)
		{
			_log.info(" - Antharas");		
			new Antharas(-1, "Antharas", "ai");
		}
		if (Config.GB_LOADER)
		{
			_log.info(" - Baium");		
			new Baium(-1, "baium", "ai");
		}
		_log.info(" - Queen Ant");
		new QueenAnt(-1,"queen_ant","ai");
		if (Config.GB_LOADER)
		{
	    	_log.info(" - Frintezza");
	    	new Frintezza(-1,"frintezza","ai");
		}
		_log.info(" - Core");
		new Core(-1,"core","ai");
		_log.info(" - Orfen");
		new Orfen(-1,"orfen","ai");
		_log.info(" ---------------");
		_log.info(" - Ice Fairy Sirra");
		new IceFairySirra(-1, "IceFairySirra", "ai");
		_log.info(" - Ancient Egg");
		_log.info(" - Monastery");
		new Monastery(-1, "Monastery", "ai");
		_log.info(" - Transform");
		new Transform(-1, "transform", "ai");
		_log.info(" - Fairy Trees");
		new FairyTrees(-1, "FairyTrees", "ai");
		_log.info(" - Zombie Gatekeepers");
		new ZombieGatekeepers(-1, "ZombieGatekeepers", "ai");
		_log.info(" - Eva Box");
    	new EvaBox(-1, "EvaBox", "ai");
		_log.info(" - Flee Npcs");
		new FleeNpc(-1, "FleeNpc", "Ai for Flee Npcs");
		_log.info(" - Benom");	
		new Benom(-1, "Benom", "ai");
		_log.info(" - Gordon");
		new Gordon(-1,"gordon","ai");
		_log.info(" - Last Imperial Tomb");
		_log.info(" - Sailren");
		_log.info(" - VanHalter");
		new HotSprings(-1, "HotSprings", "ai");
		_log.info(" - Hot Springs Debuffs");
		_log.info(" ---------------");
	}
}