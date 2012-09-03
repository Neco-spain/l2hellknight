package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Summon;
import l2rt.util.GArray;

public class ExEventMatchTeamInfo extends L2GameServerPacket
{
	@SuppressWarnings("unused")
	private int leader_id, loot;
	private GArray<EventMatchTeamInfo> members = new GArray<EventMatchTeamInfo>();

	public ExEventMatchTeamInfo(GArray<L2Player> party, L2Player exclude)
	{
		leader_id = party.get(0).getObjectId();
		loot = party.get(0).getParty().getLootDistribution();

		for(L2Player member : party)
			if(!member.equals(exclude))
				members.add(new EventMatchTeamInfo(member));
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x1C);
		// TODO dcd[dSdddddddddd]
	}

	public static class EventMatchTeamInfo
	{
		public String _name, pet_Name;
		public int _id, curCp, maxCp, curHp, maxHp, curMp, maxMp, level, class_id, race_id;
		public int pet_id, pet_NpcId, pet_curHp, pet_maxHp, pet_curMp, pet_maxMp, pet_level;

		public EventMatchTeamInfo(L2Player member)
		{
			_name = member.getName();
			_id = member.getObjectId();
			curCp = (int) member.getCurrentCp();
			maxCp = member.getMaxCp();
			curHp = (int) member.getCurrentHp();
			maxHp = member.getMaxHp();
			curMp = (int) member.getCurrentMp();
			maxMp = member.getMaxMp();
			level = member.getLevel();
			class_id = member.getClassId().getId();
			race_id = member.getRace().ordinal();

			if (member.isAwaking())
				class_id = member.getAwakingId();
			
			L2Summon pet = member.getPet();
			if(pet != null)
			{
				pet_id = pet.getObjectId();
				pet_NpcId = pet.getNpcId() + 1000000;
				pet_Name = pet.getName();
				pet_curHp = (int) pet.getCurrentHp();
				pet_maxHp = pet.getMaxHp();
				pet_curMp = (int) pet.getCurrentMp();
				pet_maxMp = pet.getMaxMp();
				pet_level = pet.getLevel();
			}
			else
				pet_id = 0;
		}
	}
}