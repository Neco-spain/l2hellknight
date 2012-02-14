package l2rt.gameserver.instancemanager;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.*;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.util.FastMap;

import l2rt.Config;
import l2rt.config.ConfigSystem;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2SkillLearn;
import l2rt.gameserver.network.serverpackets.ExCallToChangeClass;
import l2rt.gameserver.network.serverpackets.ExChangeToAwakenedClass;
import l2rt.gameserver.network.serverpackets.ExShowUsmVideo;
import l2rt.gameserver.network.serverpackets.SocialAction;
import l2rt.gameserver.tables.SkillTreeTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.GArray;
import l2rt.util.Location;

public class AwakingManager
{
	protected static Logger _log = Logger.getLogger(AwakingManager.class.getName());

	private static AwakingManager _instance;
	private static final TIntIntHashMap	_CA = new TIntIntHashMap(36);
	private static final TIntObjectHashMap<TIntArrayList> _RelationSkills = new TIntObjectHashMap<TIntArrayList>();

	/*********************************************************************************************
	* 139 Рыцарь Савуло: Рыцарь Феникса, Рыцарь Ада, Храмовник Евы, Храмовник Шилен.
	* 140 Воин Тейваза: Полководец, Дуэлист, Титан, Аватар, Мастер, Каратель.
	* 141 Разбойник Отилы: Авантюрист, Странник Ветра, Призрачный Охотник, Кладоискатель.
	* 142 Лучник Эйваза: Снайпер, Страж Лунного Света, Страж Теней, Диверсант.
	* 143 Волшебник Фео: Archmahe, Soultaker, Mystic muse, StormScreamer, SoulHound
	* 144 Ис Заклинатель: Hierophant, Doomcryer, Dominator, Sword Muse, Spectral Dancer, Judicator
	* 145 Призыватель Веньо: Arcana Lord, Elemental master, Spectral Master
	* 146 Целитель Альгиза: Cardinal, Eva’s Saint, Shilien saint
	**********************************************************************************************/

	public void load()
	{
		_CA.clear();
		
		_CA.put(90, 139);	_CA.put(91, 139);	_CA.put(99, 139);	_CA.put(106, 139);
		_CA.put(89, 140);	_CA.put(88, 140);	_CA.put(113, 140);	_CA.put(114, 140);	_CA.put(118, 140);	_CA.put(131, 140);	
		_CA.put(93, 141);	_CA.put(101, 141);	_CA.put(108, 141);	_CA.put(117, 141);			
		_CA.put(92, 142);	_CA.put(102, 142);	_CA.put(109, 142);	_CA.put(134, 142);		
		_CA.put(94, 143);	_CA.put(95, 143);	_CA.put(103, 143);	_CA.put(110, 143);	_CA.put(132, 143);	_CA.put(133, 143);			
		_CA.put(98, 144);	_CA.put(116, 144);	_CA.put(115, 144);	_CA.put(100, 144);	_CA.put(107, 144);	_CA.put(136, 144);	
		_CA.put(96, 145);	_CA.put(104, 145);	_CA.put(111, 145);	
		_CA.put(97, 146);	_CA.put(105, 146);	_CA.put(112, 146);
		
		_log.info("AwakingManager: Loaded 8 Awaking class for " + _CA.size() + " normal class.");
		
		
		LineNumberReader lnr = null;
		try
		{
			File rsData = new File(Config.DATAPACK_ROOT, "data/RelationSkills.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(rsData)));

			String line = null;
			while((line = lnr.readLine()) != null)
			{
				if(line.trim().length() == 0 || line.startsWith("#"))
					continue;
					
				String[] Args = line.split(";");
				
				String[] rSkills = Args[1].split(",");
				
				TIntArrayList _delSk = new TIntArrayList();
				
				for (String delS : rSkills)
				{
					_delSk.add(Integer.parseInt(delS));
				}
				
				_RelationSkills.put(Integer.parseInt(Args[0]), _delSk);
			}

			_log.info("AwakingManager: Loaded " + _RelationSkills.size() + " relation skills.");
		}
		catch(FileNotFoundException e)
		{
			_log.info("RelationSkills.csv is missing in data folder");
		}
		catch(Exception e)
		{
			_log.info("error while creating RelationSkills table " + e);
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception e1)
			{}
		}
	}
	
	public static AwakingManager getInstance()
	{
		if(_instance == null)
		{
			_log.info("Initializing: AwakingManager");
			_instance = new AwakingManager();
			_instance.load();
		}
		return _instance;
	}
	
	public void SendReqToStartQuest(L2Player player)
	{
		if (player.getClassId().level() < 3)
			return;
		int newClass = _CA.get(player.getClassId().getId());
		player.sendPacket(new ExCallToChangeClass(newClass));
		return;
	}	
	
	public void SendReqToAwaking(L2Player player)
	{
		if (player.getClassId().level() < 3)
			return;
		int newClass = _CA.get(player.getClassId().getId());
		player.sendPacket(new ExChangeToAwakenedClass(newClass));
		return;
	}

	public void onStartQuestAccept(L2Player player)
	{
		// Телепортируем в музей
		Location pos = GeoEngine.findPointToStay(-114708, 243918, -7968, 50, 100, player.getReflection().getGeoIndex());
		player.teleToLocation(pos);
		// Показываем видео
		player.sendPacket(new ExShowUsmVideo(ExShowUsmVideo.Q010));
		return;
	}
	
	// Удаляет скилы - зависимости
	public void onAddSkill(L2Player player, int SkillId)
	{
		int[] delSkill = getRelationSkillById(SkillId);
		for (int sk : delSkill)
		{
			player.removeSkill(sk, true);
		}
		return;
	}

	public void ChekRelationSkill(L2Player player)
	{
		for(L2Skill sk : player.getAllSkills())
		{
			int[] delSkill = getRelationSkillById(sk.getId());
			for (int skl : delSkill)
			{
				player.removeSkill(skl, true);
			}
		}
		return;
	}

	public int[] getRelationSkillById(int SkillId)
	{
		if (_RelationSkills.get(SkillId) != null)
			return _RelationSkills.get(SkillId).toArray();		
		return new int[0];
	}
	
	public TIntArrayList getRelationSkillListById(int SkillId)
	{
		if (_RelationSkills.get(SkillId) != null)
			return _RelationSkills.get(SkillId);
		return new TIntArrayList(0);
	}
	
	public void SetAwakingId(L2Player player)
	{	
		if (player.getLevel() < 85)
			return;
		if (player.getClassId().level() < 3)
			return;
		if (player.isAwaking())
			return;
			
		int _oldId = player.getClassId().getId();
			
		player.setAwakingId(_CA.get(_oldId));
		
		player.broadcastUserInfo(false);
		player.broadcastPacket(new SocialAction(player.getObjectId(), (20+(_CA.get(_oldId)-139))));
		
		L2Skill skill = null;
		if (player.getRace().ordinal() == 4) 
			skill = SkillTable.getInstance().getInfo(1320, 10); //другой скилл должен быть
		else 
			skill = SkillTable.getInstance().getInfo(1320, 10); 
		player.addSkill(skill);
		
		return;
	}

}