package l2r.gameserver.model.instances;

import java.util.Collections;
import java.util.List;

import l2r.commons.lang.reference.HardReference;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.reference.L2Reference;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.MyTargetSelected;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.ShowTownMap;
import l2r.gameserver.network.serverpackets.StaticObject;
import l2r.gameserver.scripts.Events;
import l2r.gameserver.templates.StaticObjectTemplate;
import l2r.gameserver.utils.Location;

public class StaticObjectInstance extends GameObject
{
	private final HardReference<StaticObjectInstance> reference;
	private final StaticObjectTemplate _template;
	private int _meshIndex;

	public StaticObjectInstance(int objectId, StaticObjectTemplate template)
	{
		super(objectId);

		_template = template;
		reference = new L2Reference<StaticObjectInstance>(this);
	}

	@Override
	public HardReference<StaticObjectInstance> getRef()
	{
		return reference;
	}

	public int getUId()
	{
		return _template.getUId();
	}

	public int getType()
	{
		return _template.getType();
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(Events.onAction(player, this, shift))
			return;

		if(player.getTarget() != this)
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			return;
		}

		MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
		player.sendPacket(my);

		if(!isInRange(player, 150))
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			return;
		}

		if(_template.getType() == 0) // Arena Board
			player.sendPacket(new NpcHtmlMessage(player, getUId(), "newspaper/arena.htm", 0));
		else if(_template.getType() == 2) // Village map
		{
			player.sendPacket(new ShowTownMap(_template.getFilePath(), _template.getMapX(), _template.getMapY()));
			player.sendActionFailed();
		}
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		return Collections.<L2GameServerPacket>singletonList(new StaticObject(this));
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return false;
	}

	public void broadcastInfo(boolean force)
	{
		StaticObject p = new StaticObject(this);
		for(Player player : World.getAroundPlayers(this))
			player.sendPacket(p);
	}

	@Override
	public int getGeoZ(Location loc)   //FIXME [VISTALL] ?????????? ?????
	{
		return loc.z;
	}

	public int getMeshIndex()
	{
		return _meshIndex;
	}

	public void setMeshIndex(int meshIndex)
	{
		_meshIndex = meshIndex;
	}
}