import javolution.util.FastMap;
import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.InstancedZoneManager;
import l2rt.gameserver.instancemanager.ServerVariables;
import l2rt.gameserver.instancemanager.InstancedZoneManager.InstancedZone;
import l2rt.gameserver.model.L2CommandChannel;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.Reflection;
import l2rt.gameserver.model.entity.KamalokaNightmare;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.ReflectionTable;
import l2rt.util.Location;
import l2rt.util.Rnd;
import l2rt.util.Util;

public class Kamaloka extends Functions implements ScriptFile
{
	private static final long SOD_OPEN_TIME = 12 * 60 * 60 * 1000L;

	public void onLoad()
	{
		System.out.println("Kamaloka Gate Loaded");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public void Gatekeeper(String[] param)
	{
		if(param.length < 1)
			throw new IllegalArgumentException();

		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		int instancedZoneId = Integer.parseInt(param[0]);
		InstancedZoneManager izm = InstancedZoneManager.getInstance();
		FastMap<Integer, InstancedZone> izs = izm.getById(instancedZoneId);
		if(izs == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}

		InstancedZone iz = izs.get(0);
		assert iz != null;

		String name = iz.getName();
		int timelimit = iz.getTimelimit();
		boolean dispellBuffs = iz.isDispellBuffs();
		int min_level = iz.getMinLevel();
		int max_level = iz.getMaxLevel();
		int minParty = iz.getMinParty();
		int maxParty = iz.getMaxParty();

		if(!player.isInParty())
		{
			player.sendPacket(Msg.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
			return;
		}

		if(player.getParty().isInReflection())
		{
			if(player.getLevel() < min_level || player.getLevel() > max_level)
			{
				player.sendPacket(new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(player));
				return;
			}
			if(player.isCursedWeaponEquipped())
			{
				player.sendPacket(new SystemMessage(SystemMessage.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(player));
				return;
			}
			Reflection old_ref = player.getParty().getReflection();
			if(old_ref != null)
			{
				if(!iz.equals(old_ref.getInstancedZone()))
				{
					player.sendMessage("Your party is in instanced zone already.");
					return;
				}
				if(!Config.ALT_KAMALOKA_LIMITS.equalsIgnoreCase("Leader") && izm.getTimeToNextEnterInstance(name, player) > 0)
				{
					player.sendPacket(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(player));
					return;
				}
				if(player.getLevel() < min_level || player.getLevel() > max_level)
				{
					player.sendPacket(new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(player));
					return;
				}
				player.teleToLocation(old_ref.getTeleportLoc(), old_ref.getId());
				if(dispellBuffs)
				{
					for(L2Effect e : player.getEffectList().getAllEffects())
						if(!e.getSkill().isOffensive() && !e.getSkill().getName().startsWith("Adventurer's "))
							e.exit();
					if(player.getPet() != null)
						for(L2Effect e : player.getPet().getEffectList().getAllEffects())
							if(!e.getSkill().isOffensive() && !e.getSkill().getName().startsWith("Adventurer's "))
								e.exit();
				}
				return;
			}
		}

		if(!player.getParty().isLeader(player))
		{
			player.sendPacket(Msg.ONLY_A_PARTY_LEADER_CAN_TRY_TO_ENTER);
			return;
		}

		if(player.getParty().getMemberCount() > maxParty)
		{
			player.sendPacket(Msg.YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT);
			return;
		}

		for(L2Player member : player.getParty().getPartyMembers())
		{
			if(member.getLevel() < min_level || member.getLevel() > max_level)
			{
				SystemMessage sm = new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member);
				member.sendPacket(sm);
				player.sendPacket(sm);
				return;
			}
			if(!player.isInRange(member, 500))
			{
				member.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
				player.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
				return;
			}
		}

		if(Config.ALT_KAMALOKA_LIMITS.equalsIgnoreCase("Leader"))
		{
			if(izm.getTimeToNextEnterInstance(name, player) > 0)
			{
				player.sendPacket(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(player));
				return;
			}
		}
		else
			for(L2Player member : player.getParty().getPartyMembers())
				if(izm.getTimeToNextEnterInstance(name, member) > 0)
				{
					player.sendPacket(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(member));
					return;
				}

		Reflection r = new Reflection(iz);
		r.setInstancedZoneId(instancedZoneId);

		for(InstancedZone i : izs.values())
		{
			if(r.getReturnLoc() == null)
				r.setReturnLoc(i.getReturnCoords());
			if(r.getTeleportLoc() == null)
				r.setTeleportLoc(i.getTeleportCoords());
			r.FillSpawns(i.getSpawnsInfo());
			r.FillDoors(i.getDoors());
		}

		if(minParty <= 1) // для соло инстансов без босса флаг ставится при входе
			player.setVar(name, String.valueOf(System.currentTimeMillis()));
		for(L2Player member : player.getParty().getPartyMembers())
		{
			if(dispellBuffs)
			{
				for(L2Effect e : member.getEffectList().getAllEffects())
					if(!e.getSkill().isOffensive() && !e.getSkill().getName().startsWith("Adventurer's "))
						e.exit();
				if(member.getPet() != null)
					for(L2Effect e : member.getPet().getEffectList().getAllEffects())
						if(!e.getSkill().isOffensive() && !e.getSkill().getName().startsWith("Adventurer's "))
							e.exit();
			}

			member.setVar("backCoords", r.getReturnLoc().toXYZString());
			member.teleToLocation(iz.getTeleportCoords(), r.getId());
		}

		player.getParty().setReflection(r);
		r.setParty(player.getParty());
		if(timelimit > 0)
		{
			r.startCollapseTimer(timelimit * 60 * 1000L);
			player.getParty().broadcastToPartyMembers(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(timelimit));
		}
	}

	public void SoloGatekeeper(String[] param)
	{
		if(param.length < 1)
			throw new IllegalArgumentException();

		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(player.isInParty())
		{
			player.sendPacket(Msg.YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT);
			return;
		}

		KamalokaNightmare r = ReflectionTable.getInstance().findSoloKamaloka(player.getObjectId());
		if(r != null)
		{
			player.setVar("backCoords", r.getReturnLoc().toXYZString());
			player.teleToLocation(r.getTeleportLoc(), r.getId());
			return;
		}

		if(param[0].equals("-1"))
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}

		if(Config.ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY && player.getBonus().RATE_XP <= 1)
		{
			player.sendMessage(new CustomMessage("common.PremiumOnly", player));
			return;
		}

		InstancedZoneManager izm = InstancedZoneManager.getInstance();
		FastMap<Integer, InstancedZone> izs = InstancedZoneManager.getInstance().getById(Integer.parseInt(param[0]));
		if(izs == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}

		InstancedZone iz = izs.get(0);
		assert iz != null;

		String name = iz.getName();
		int timelimit = iz.getTimelimit();
		int min_level = iz.getMinLevel();
		int max_level = iz.getMaxLevel();

		if(player.getLevel() < min_level || player.getLevel() > max_level)
		{
			player.sendPacket(new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(player));
			return;
		}

		if(player.isCursedWeaponEquipped())
		{
			player.sendPacket(new SystemMessage(SystemMessage.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(player));
			return;
		}

		if(izm.getTimeToNextEnterInstance(name, player) > 0)
		{
			player.sendPacket(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(player));
			return;
		}

		r = new KamalokaNightmare(player);

		for(InstancedZone i : izs.values())
		{
			if(r.getTeleportLoc() == null)
				r.setTeleportLoc(i.getTeleportCoords());
			r.FillSpawns(i.getSpawnsInfo());
			r.FillDoors(i.getDoors());
		}

		player.setVar(name, String.valueOf(System.currentTimeMillis()));
		r.setReturnLoc(player.getLoc());
		player.setVar("backCoords", r.getReturnLoc().toXYZString());
		player.teleToLocation(r.getTeleportLoc(), r.getId());
		ReflectionTable.getInstance().addSoloKamaloka(player.getObjectId(), r);
		if(timelimit > 0)
		{
			r.startCollapseTimer(timelimit * 60 * 1000L);
			player.sendPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(timelimit));
		}
	}

	public void StaticSoloInstance(String[] param)
	{
		if(param.length < 1)
			throw new IllegalArgumentException();

		L2Player player = (L2Player) getSelf();
		if(player == null || player.isDead())
			return;

		int instancedZoneId = Integer.parseInt(param[0]);

		// SoD
		if(instancedZoneId == 400 && ServerVariables.getLong("SoD_opened", 0) * 1000L + SOD_OPEN_TIME < System.currentTimeMillis())
		{
			TiatEnter();
			return;
		}

		FastMap<Integer, InstancedZone> izs = InstancedZoneManager.getInstance().getById(instancedZoneId);
		if(izs == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}

		InstancedZone iz = izs.get(0);
		assert iz != null;

		if(player.getLevel() < iz.getMinLevel() || player.getLevel() > 90 || player.isInFlyingTransform())
		{
			player.sendPacket(new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(player));
			return;
		}

		if(player.isCursedWeaponEquipped())
		{
			player.sendPacket(new SystemMessage(SystemMessage.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(player));
			return;
		}

		Reflection r = ReflectionTable.SOD_REFLECTION_ID == 0 ? null : ReflectionTable.getInstance().get(ReflectionTable.SOD_REFLECTION_ID);
		if(ReflectionTable.SOD_REFLECTION_ID > 0 && r != null)
		{
			player.setVar("backCoords", r.getReturnLoc().toXYZString());
			player.teleToLocation(r.getTeleportLoc(), r.getId());
			return;
		}
		else
		{
			r = new Reflection(iz.getName());
			r.setInstancedZoneId(instancedZoneId);
			ReflectionTable.SOD_REFLECTION_ID = r.getId();
		}

		long timelimit = 0;
		if(instancedZoneId == 400)
			timelimit = ServerVariables.getLong("SoD_opened", 0) * 1000L + SOD_OPEN_TIME - System.currentTimeMillis();

		for(InstancedZone i : izs.values())
		{
			if(r.getTeleportLoc() == null)
				r.setTeleportLoc(i.getTeleportCoords());
			r.FillSpawns(i.getSpawnsInfo());
			r.FillDoors(i.getDoors());
		}

		r.setCoreLoc(r.getReturnLoc());
		r.setReturnLoc(player.getLoc());
		player.setVar("backCoords", r.getReturnLoc().toXYZString());
		player.teleToLocation(r.getTeleportLoc(), r.getId());
		if(timelimit > 0)
			r.startCollapseTimer(timelimit);
	}

	public void TiatEnter()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;
		int instancedZoneId = 401;

		if(player.getParty() == null || !player.getParty().isInCommandChannel())
		{
			player.sendPacket(Msg.YOU_CANNOT_ENTER_BECAUSE_YOU_ARE_NOT_IN_A_CURRENT_COMMAND_CHANNEL);
			return;
		}
		L2CommandChannel cc = player.getParty().getCommandChannel();
		if(cc.getChannelLeader() != player)
		{
			player.sendMessage("You must be leader of the command channel.");
			return;
		}

		InstancedZoneManager izm = InstancedZoneManager.getInstance();
		FastMap<Integer, InstancedZone> izs = InstancedZoneManager.getInstance().getById(instancedZoneId);
		if(izs == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}

		InstancedZone iz = izs.get(0);
		assert iz != null;

		String name = iz.getName();
		int timelimit = iz.getTimelimit();
		int minMembers = iz.getMinParty();
		int maxMembers = iz.getMaxParty();

		if(cc.getMemberCount() < minMembers)
		{
			player.sendMessage("The command channel must contains at least " + minMembers + " members.");
			return;
		}
		if(cc.getMemberCount() > maxMembers)
		{
			player.sendMessage("The command channel must contains not more than " + maxMembers + " members.");
			return;
		}

		for(L2Player member : cc.getMembers())
		{
			if(member.getLevel() < iz.getMinLevel() || member.getLevel() > iz.getMaxLevel())
			{
				cc.broadcastToChannelMembers(new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member));
				return;
			}
			if(member.isCursedWeaponEquipped() || member.isInFlyingTransform() || member.isDead())
			{
				player.sendPacket(new SystemMessage(SystemMessage.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member));
				return;
			}
			if(!player.isInRange(member, 500))
			{
				member.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
				player.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
				return;
			}
			if(izm.getTimeToNextEnterInstance(name, member) > 0)
			{
				cc.broadcastToChannelMembers(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(member));
				return;
			}
		}

		Reflection r = new Reflection(name);
		r.setInstancedZoneId(instancedZoneId);

		for(InstancedZone i : izs.values())
		{
			if(r.getTeleportLoc() == null)
				r.setTeleportLoc(i.getTeleportCoords());
			r.FillSpawns(i.getSpawnsInfo());
			r.FillDoors(i.getDoors());
		}

		r.setCoreLoc(r.getReturnLoc());
		r.setReturnLoc(player.getLoc());

		for(L2Player member : cc.getMembers())
		{
			member.setVar(name, String.valueOf(System.currentTimeMillis()));
			member.setVar("backCoords", r.getReturnLoc().toXYZString());
			member.teleToLocation(iz.getTeleportCoords(), r.getId());
		}

		cc.setReflection(r);
		r.setCommandChannel(cc);

		if(timelimit > 0)
			r.startCollapseTimer(timelimit * 60 * 1000L);
	}

	public void LeaveKamaloka(String[] param)
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;
		if(player.getParty() == null || !player.getParty().isLeader(player))
		{
			show("You are not a party leader.", player, npc);
			return;
		}

		player.getReflection().collapse();
	}

	public String DialogAppend_32484(Integer val)
	{
		L2Player player = (L2Player) getSelf();
		String ret = "";
		if(player == null || player.getLevel() < 20)
			return ret;
		if(Config.ALT_KAMALOKA_NIGHTMARE_REENTER || Config.ALT_KAMALOKA_ABYSS_REENTER || Config.ALT_KAMALOKA_LAB_REENTER)
		{
			ret += "<br>Ticket price: " + Util.formatAdena(player.getLevel() * 5000) + " adena.";
			if(Config.ALT_KAMALOKA_NIGHTMARE_REENTER)
				ret += "<br>[scripts_Kamaloka:buyTicket 13011|Buy ticket to Hall of Nightmates]";
			if(Config.ALT_KAMALOKA_ABYSS_REENTER)
				ret += "<br>[scripts_Kamaloka:buyTicket 13010|Buy ticket to Hall of Abyss]";
			if(Config.ALT_KAMALOKA_LAB_REENTER)
				ret += "<br>[scripts_Kamaloka:buyTicket 13012|Buy ticket to Labirinth of Abyss]";
		}
		return ret;
	}

	public void buyTicket(String[] id)
	{
		L2Player player = (L2Player) getSelf();
		if(player == null || player.getLevel() < 20)
			return;
		int price = player.getLevel() * 5000;
		if(Functions.getItemCount(player, 57) < price)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		Functions.removeItem(player, 57, price);
		Functions.addItem(player, Integer.parseInt(id[0]), 1);
	}
}