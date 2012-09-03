package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.skillclasses.Transformation;
import l2rt.gameserver.network.serverpackets.NpcInfo;
import l2rt.util.Rnd;

public final class EffectTransformationAll extends L2Effect
{

	private int[][] transf = {
		{104,13093}, //pig
		{105,13096}, //rabbit	
		{111,13157} //frog					
	};
	public EffectTransformationAll(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		int rnd = Rnd.get(2);
		int id = (int) calc();
		if (id == 0)
		{

			// Для игроков и для монстров функции разные.
			// Игрокам надо удалять скилы.
			if (_effected instanceof L2Player)
			{
				L2Player player = (L2Player) _effected;
				player.setTransformation(transf[rnd][0]);
				player.setTransformationTemplate(transf[rnd][1]);
			}
			else if (_effected instanceof L2MonsterInstance)
			{				
				_effected.setTransformationId(transf[rnd][0]);
				_effected.setTransformationTemplate(transf[rnd][1]);
			}
		}
		else
		{
			
			if (_effected instanceof L2Player)
			{				
				L2Player player = (L2Player) _effected;
				player.setTransformationTemplate(getSkill().getNpcId());
				player.setTransformation(id);
			}
			else if (_effected instanceof L2MonsterInstance)
			{				
				_effected.setTransformationTemplate(getSkill().getNpcId());	
				_effected.setTransformationId(id);				
			}
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		
		if (_effected instanceof L2Player)
		{
			L2Player player = (L2Player) _effected;
			player.setTransformation(0);
		}
		_effected.setTransformationTemplate(0);
		_effected.setTransformationId(0);
		if (_effected instanceof L2MonsterInstance)
			_effected.broadcastPacket(new NpcInfo((L2MonsterInstance)_effected, null));
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}