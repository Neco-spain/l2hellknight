package net.sf.l2j.gameserver.model.zone.type;

import java.util.Collection;
import java.util.concurrent.Future;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;

public class L2DamageZone extends L2ZoneType
{
	private int _damagePerSec;
	private Future<?> _task;

	public L2DamageZone(int id)
	{
		super(id);
		_damagePerSec = 100;
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("dmgSec"))
		{
			_damagePerSec = Integer.parseInt(value);
		}
		else super.setParameter(name, value);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(character instanceof L2PcInstance)
			((L2PcInstance)character).enterDangerArea();
		if (_task == null)
		{
			_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ApplyDamage(this), 10, 1000);
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(character instanceof L2PcInstance)
			((L2PcInstance)character).exitDangerArea();
		if (_characterList.isEmpty())
		{
			_task.cancel(true);
			_task = null;
		}
	}

	protected Collection<L2Character> getCharacterList()
	{
		return _characterList.values();
	}

	protected int getDamagePerSecond()
	{
		return _damagePerSec;
	}

	class ApplyDamage implements Runnable
	{
		private L2DamageZone _dmgZone;
		ApplyDamage(L2DamageZone zone)
		{
			_dmgZone = zone;
		}

		public void run()
		{
			for (L2Character temp : _dmgZone.getCharacterList())
			{
				if (temp != null && !temp.isDead())
				{
					temp.reduceCurrentHp(_dmgZone.getDamagePerSecond(), null);
				}
			}
		}
	}

    @Override
    protected void onDieInside(L2Character character)
    {
    }
    
    @Override
    protected void onReviveInside(L2Character character)
    {
    }

}
