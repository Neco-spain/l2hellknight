package l2rt.gameserver.skills;

/**
 * Simple class containing all neccessary information to maintain
 * valid timestamps and reuse for skills upon relog. Filter this
 * carefully as it becomes redundant to store reuse for small delays.
 * @author Yesod
 */
public class SkillTimeStamp
{
	private int skill;
	private long reuse;
	private long endTime;

	public SkillTimeStamp(int _skill, long _endTime, long _reuse)
	{
		skill = _skill;
		reuse = _reuse;
		endTime = _endTime;
	}

	public int getSkill()
	{
		return skill;
	}

	public long getReuseBasic()
	{
		if(reuse == 0)
			return getReuseCurrent();
		return reuse;
	}

	/**
	 * Возвращает оставшееся время реюза в миллисекундах.
	 */
	public long getReuseCurrent()
	{
		return Math.max(endTime - System.currentTimeMillis(), 0);
	}

	public long getEndTime()
	{
		return endTime;
	}

	/* Check if the reuse delay has passed and
	 * if it has not then update the stored reuse time
	 * according to what is currently remaining on
	 * the delay. */
	public boolean hasNotPassed()
	{
		return System.currentTimeMillis() < endTime;
	}
}