package l2r.gameserver.templates;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.Skill;
import l2r.gameserver.stats.StatTemplate;

public class OptionDataTemplate extends StatTemplate
{
	private final List<Skill> _skills = new ArrayList<Skill>(0);
	private final int _id;

	public OptionDataTemplate(int id)
	{
		_id = id;
	}

	public void addSkill(Skill skill)
	{
		_skills.add(skill);
	}

	public List<Skill> getSkills()
	{
		return _skills;
	}

	public int getId()
	{
		return _id;
	}
}
