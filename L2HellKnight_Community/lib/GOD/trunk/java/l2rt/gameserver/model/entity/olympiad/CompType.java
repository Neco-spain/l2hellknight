package l2rt.gameserver.model.entity.olympiad;

import l2rt.Config;

public enum CompType
{
	CLASSED(2, Config.ALT_OLY_CLASSED_RITEM_C, 3, true),
	NON_CLASSED(2, Config.ALT_OLY_NONCLASSED_RITEM_C, 5, true),
	TEAM_RANDOM(6, Config.ALT_OLY_RANDOM_TEAM_RITEM_C, 5, true),
	TEAM(2, Config.ALT_OLY_TEAM_RITEM_C, 5, false);

	private int _minSize;
	private int _reward;
	private int _looseMult;
	private boolean _hasBuffer;

	private CompType(int minSize, int reward, int looseMult, boolean hasBuffer)
	{
		_minSize = minSize;
		_reward = reward;
		_looseMult = looseMult;
		_hasBuffer = hasBuffer;
	}

	public int getMinSize()
	{
		return _minSize;
	}

	public int getReward()
	{
		return _reward;
	}

	public int getLooseMult()
	{
		return _looseMult;
	}

	public boolean hasBuffer()
	{
		return _hasBuffer;
	}
}