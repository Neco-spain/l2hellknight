package net.sf.l2j.gameserver.model;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExCloseMPCC;
import net.sf.l2j.gameserver.network.serverpackets.ExOpenMPCC;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class L2CommandChannel
{
	private List<L2Party> _partys = null;
	private L2PcInstance _commandLeader = null;
	private int _channelLvl;

	public L2CommandChannel(L2PcInstance leader)
	{
		_commandLeader = leader;
		_partys = new FastList<L2Party>();
		_partys.add(leader.getParty());
		_channelLvl = leader.getParty().getLevel();
		leader.getParty().setCommandChannel(this);
		leader.getParty().broadcastToPartyMembers(new ExOpenMPCC());
	}

	public void addParty(L2Party party)
	{
		_partys.add(party);
		if (party.getLevel() > _channelLvl)
			_channelLvl = party.getLevel();
		party.setCommandChannel(this);
		party.broadcastToPartyMembers(new ExOpenMPCC());
	}

	public void removeParty(L2Party party)
	{
		_partys.remove(party);
		_channelLvl = 0;
		for (L2Party pty : _partys)
		{
			if (pty.getLevel() > _channelLvl)
				_channelLvl = pty.getLevel();
		}
		party.setCommandChannel(null);
		party.broadcastToPartyMembers(new ExCloseMPCC());
		if(_partys.size() < 2)
		{
			SystemMessage sm = SystemMessage.sendString("The Command Channel was disbanded.");
    		broadcastToChannelMembers(sm);
			disbandChannel();
		}
	}

	public void disbandChannel()
	{
		for (L2Party party : _partys)
		{
			if(party != null)
				removeParty(party);
		}
		_partys = null;
	}

	public int getMemberCount()
	{
		int count = 0;
		for (L2Party party : _partys)
		{
			if(party != null)
				count += party.getMemberCount();
		}
		return count;
	}

	public void broadcastToChannelMembers(L2GameServerPacket gsp)
	{
		if (!_partys.isEmpty())
		{
			for (L2Party party : _partys)
			{
				if(party != null)
					party.broadcastToPartyMembers(gsp);
			}
		}
	}

	public List<L2Party> getPartys()
	{
		return _partys;
	}

	public List<L2PcInstance> getMembers()
	{
		List<L2PcInstance> members = new FastList<L2PcInstance>();
		for (L2Party party : getPartys())
		{
			members.addAll(party.getPartyMembers());
		}
		return members;
	}

	public int getLevel() { return _channelLvl; }

	public void setChannelLeader(L2PcInstance leader)
	{
		_commandLeader = leader;
	}

	public L2PcInstance getChannelLeader()
	{
		return _commandLeader;
	}

	public boolean meetRaidWarCondition(L2Object obj)
	{
		if (!(obj instanceof L2RaidBossInstance) || !(obj instanceof L2GrandBossInstance))
			return false;
		int npcId = ((L2Attackable)obj).getNpcId();
		switch(npcId)
		{
	    	case 29001: // Queen Ant
	    	case 29006: // Core
	    	case 29014: // Orfen
	    	case 29022: // Zaken
	    		return (getMemberCount() > 36);
	    	case 29020: // Baium
	    		return (getMemberCount() > 56);
	    	case 29019: // Antharas
	    		return (getMemberCount() > 225);
	    	case 29028: // Valakas
	    		return (getMemberCount() > 99);
	    	default: // normal Raidboss
	    		return (getMemberCount() > 18);
		}
	}
}
