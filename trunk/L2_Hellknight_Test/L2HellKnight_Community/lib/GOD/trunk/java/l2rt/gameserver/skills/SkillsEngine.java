package l2rt.gameserver.skills;

import l2rt.gameserver.model.L2Skill;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class SkillsEngine
{

	protected static Logger _log = Logger.getLogger(SkillsEngine.class.getName());

	private static final SkillsEngine _instance = new SkillsEngine();

	private List<File> _skillFiles;

	public static SkillsEngine getInstance()
	{
		return _instance;
	}

	private SkillsEngine()
	{
		_skillFiles = new LinkedList<File>();
		hashFiles("data/stats/skills", _skillFiles);
	}

	private void hashFiles(String dirname, List<File> hash)
	{
		File dir = new File(dirname);
		if(!dir.exists())
		{
			_log.config("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		File[] files = dir.listFiles();
		for(File f : files)
			if(f.getName().endsWith(".xml"))
				hash.add(f);
	}

	public List<L2Skill> loadSkills(File file)
	{
		if(file == null)
		{
			_log.config("SkillsEngine: File not found!");
			return null;
		}
		DocumentSkill doc = new DocumentSkill(file);
		doc.parse();
		return doc.getSkills();
	}

	public L2Skill[][] loadAllSkills(int _maxid, int _maxLevel)
	{
		int skillIdIdx, skillLvlIdx, total = 0, maxLevel = 0;
		L2Skill[][] result = new L2Skill[_maxid][];

		// загружаем скилы
		for(File file : _skillFiles)
		{
			List<L2Skill> s = loadSkills(file);
			if(s == null)
				continue;
			for(L2Skill skill : s)
			{
				skillIdIdx = skill.getId() - 1;
				skillLvlIdx = skill.getLevel() - 1;

				if(result[skillIdIdx] == null)
					result[skillIdIdx] = new L2Skill[_maxLevel];
				if(result[skillIdIdx][skillLvlIdx] != null)
					new KeyAlreadyExistsException("Unable to store skill " + skill).printStackTrace();
				else
					result[skillIdIdx][skillLvlIdx] = skill;
				maxLevel = Math.max(maxLevel, skill.getLevel());
				total++;
			}
		}

		int topindex = result.length - 1;
		// если надо ресайзим саму таблицу result
		if(result[topindex] == null)
		{
			do
				topindex--;
			while(topindex > 0 && result[topindex] == null);

			L2Skill[][] tmp = result;
			result = new L2Skill[topindex + 1][];
			System.arraycopy(tmp, 0, result, 0, result.length);
			tmp = null;
		}

		// если надо ресайзим отдельные субтаблицы result[]
		for(int i = 0; i < result.length; i++)
		{
			if(result[i] == null)
				continue;
			topindex = result[i].length - 1;
			if(result[i][topindex] == null)
			{
				do
					topindex--;
				while(topindex > 0 && result[i][topindex] == null);
				L2Skill[] tmp = result[i];
				result[i] = new L2Skill[topindex + 1];
				System.arraycopy(tmp, 0, result[i], 0, result[i].length);
				tmp = null;
			}
		}

		_log.info("SkillsEngine: Loaded " + total + " skill templates from XML files. Max id: " + result.length + ", max level: " + maxLevel);
		return result;
	}
}