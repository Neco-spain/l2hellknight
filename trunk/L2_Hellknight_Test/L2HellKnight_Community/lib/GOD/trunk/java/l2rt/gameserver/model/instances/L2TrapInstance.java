package l2rt.gameserver.model.instances;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.L2ObjectTasks.TrapDestroyTask;
import l2rt.gameserver.model.L2Skill.SkillTargetType;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.GArray;
import l2rt.util.Location;

import java.util.concurrent.ScheduledFuture;

public final class L2TrapInstance extends L2NpcInstance
{
	private final long _ownerStoreId;
	private final L2Skill _skill;
	private final L2RoundTerritoryWithSkill _territory;
	private ScheduledFuture<?> _destroyTask;
	private boolean _detected = false;

	public L2TrapInstance(int objectId, L2NpcTemplate template, L2Character owner, L2Skill trapSkill)
	{
		this(objectId, template, owner, trapSkill, owner.getLoc());
	}

	public L2TrapInstance(int objectId, L2NpcTemplate template, L2Character owner, L2Skill trapSkill, Location loc)
	{
		super(objectId, template);
		_ownerStoreId = owner.getStoredId();
		_skill = trapSkill;

		setReflection(owner.getReflection().getId());
		setLevel(owner.getLevel());
		setTitle(owner.getName());
		spawnMe(loc);

		_territory = new L2RoundTerritoryWithSkill(objectId, loc.x, loc.y, 150, loc.z - 100, loc.z + 100, this, trapSkill);
		L2World.addTerritory(_territory);

		for(L2Character cha : L2World.getAroundCharacters(this, 300, 200))
			cha.updateTerritories();

		_destroyTask = ThreadPoolManager.getInstance().scheduleGeneral(new TrapDestroyTask(this), 120000);
	}

	public void detonate(L2Character target)
	{
		L2Character owner = getOwner();
		if(owner == null || _skill == null)
		{
			destroy();
			return;
		}
		if(target == owner || target == this)
			return;
		if(!target.isMonster() && !target.isPlayable())
			return;

		if(_skill.checkTarget(owner, target, null, false, false) == null)
		{
			GArray<L2Character> targets = new GArray<L2Character>();

			if(_skill.getTargetType() != SkillTargetType.TARGET_AREA)
				targets.add(target);
			else
				for(L2Character t : getAroundCharacters(_skill.getSkillRadius(), 128))
					if(_skill.checkTarget(owner, t, null, false, false) == null)
						targets.add(target);

			_skill.useSkill(this, targets);
			target.sendMessage(new CustomMessage("common.Trap", target));
			destroy();
		}
	}

	public void destroy()
	{
		L2World.removeTerritory(_territory);
		L2Character owner = getOwner();
		if(owner != null)
			owner.removeTrap(this);
		deleteMe();
		if(_destroyTask != null)
			_destroyTask.cancel(false);
		_destroyTask = null;
	}

	@Override
	public int getPAtk(L2Character target)
	{
		L2Character owner = getOwner();
		return owner == null ? 0 : owner.getPAtk(target);
	}

	@Override
	public int getMAtk(L2Character target, L2Skill skill)
	{
		L2Character owner = getOwner();
		return owner == null ? 0 : owner.getMAtk(target, skill);
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public boolean isAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public void doDie(L2Character killer)
	{}

	@Override
	public boolean isInvul()
	{
		return true;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public void showChatWindow(L2Player player, int val)
	{}

	@Override
	public void showChatWindow(L2Player player, String filename)
	{}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{}

	@Override
	public void onAction(L2Player player, boolean shift)
	{
		player.sendActionFailed();
	}

	public L2Character getOwner()
	{
		return L2ObjectsStorage.getAsCharacter(_ownerStoreId);
	}

	public boolean isDetected()
	{
		return _detected;
	}

	public void setDetected(boolean detected)
	{
		_detected = detected;
	}
}