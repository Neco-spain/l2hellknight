package zone_scripts.IsleOfPrayer.CrystalCaverns;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.L2ItemInstance;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;

import java.util.List;

public class Baylor extends L2AttackableAIScript
{
	private static final int BAYLOR = 29186;
	//s13 / s14 / s cursed 14
	private static final int RED13[] = { 5908, 9570, 10160 };
	private static final int GREEN13[] = { 5911, 9572, 10162 };
	private static final int BLUE13[] = { 5914, 9571, 10161 };
	
	public Baylor(int questId, String name, String descr)
	{
		super(questId, name, descr);
		int[] mobs = new int[] { BAYLOR };
		this.registerMobs(mobs, QuestEventType.ON_KILL, QuestEventType.ON_SPAWN, QuestEventType.ON_ATTACK);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc != null && npc.getNpcId() == BAYLOR)
		{
			if (killer.getParty() != null)
			{
				List<L2PcInstance> party = killer.getParty().getPartyMembers();
				for (L2PcInstance member : party)
				{
					if (Util.checkIfInRange(1500, npc, member, true))
						crystalCheck(member);
				}
			}
			else
				crystalCheck(killer);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	private void crystalCheck(L2PcInstance member)
	{
		QuestState st = member.getQuestState(getName());
		if (st == null)
			st = this.newQuestState(member);
		boolean lvled = false;
		L2ItemInstance[] inventory = member.getInventory().getItems();
		for (L2ItemInstance item : inventory)
		{
			int itemId = item.getItemId();
			if (!lvled && (itemId == RED13[0] || itemId == GREEN13[0] || itemId == BLUE13[0]))
			{
				int rnd = Rnd.get(1000);
				if (!lvled && itemId == RED13[0])
				{
					if (!lvled && (rnd > 850))
					{ // 15% chance to get Soul Crystal Stage 14
						st.takeItems(RED13[0], 1);
						st.giveItems(RED13[1], 1);
						lvled = true;
					}
					else if (!lvled && (rnd > 700))
					{ // 30% chance to get Cursed Soul Crystal Stage 14
						st.takeItems(RED13[0], 1);
						st.giveItems(RED13[2], 1);
						member.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
						lvled = true;
					}
					else
					{
						member.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
						lvled = true;
					}
				}
				else if (!lvled && itemId == GREEN13[0])
				{
					if (!lvled && (rnd > 850))
					{ // 15% chance to get Soul Crystal Stage 14
						st.takeItems(GREEN13[0], 1);
						st.giveItems(GREEN13[1], 1);
						lvled = true;
					}
					else if (!lvled && (rnd > 700))
					{ // 30% chance to get Cursed Soul Crystal Stage 14
						st.takeItems(GREEN13[0], 1);
						st.giveItems(GREEN13[2], 1);
						member.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
						lvled = true;
					}
					else
					{
						member.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
						lvled = true;
					}
				}
				else if (!lvled && itemId == BLUE13[0])
				{
					if (!lvled && (rnd > 850))
					{ // 15% chance to get Soul Crystal Stage 14
						st.takeItems(BLUE13[0], 1);
						st.giveItems(BLUE13[1], 1);
						lvled = true;
					}
					else if (!lvled && (rnd > 700))
					{ // 30% chance to get Cursed Soul Crystal Stage 14
						st.takeItems(BLUE13[0], 1);
						st.giveItems(BLUE13[2], 1);
						member.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
						lvled = true;
					}
					else
					{
						member.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
						lvled = true;
					}
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		new Baylor(-1, "Baylor", "zone_scripts");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Isle of Prayer: Crystal Caverns - Crystal Absorbing");
	}
}