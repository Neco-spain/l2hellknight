package intelligence.NPCs;

import java.util.concurrent.ScheduledFuture;

import l2.brick.Config;
import l2.brick.gameserver.ThreadPoolManager;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.zone.L2ZoneType;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.ExSetCompassZoneCode;
import l2.brick.gameserver.network.serverpackets.StatusUpdate;
import l2.brick.gameserver.network.serverpackets.SystemMessage;

public class LostNestTrees extends Quest
{
	private static final String qn = "LostNestTrees";
	private static final double mpBonus = 36;
	private static final int[] ZONES = { 12203, 12204 };
	protected ScheduledFuture<?> _mpTask = null;
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2PcInstance)
		{
			character.sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.ALTEREDZONE));
			if (!checkIfPc(zone) && _mpTask == null)
				_mpTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new giveMp(zone), 3000, 3000);
		}
		return super.onEnterZone(character, zone);
	}
	
	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2PcInstance)
		{
			character.sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE));
			if (howManyPc(zone) == 1 && _mpTask != null)
			{
				_mpTask.cancel(true);
				_mpTask = null;
			}
		}
		return super.onExitZone(character, zone);
	}
	
	private boolean checkIfPc(L2ZoneType zone)
	{
		for (L2Character c : zone.getCharactersInsideArray())
		{
			if (c instanceof L2PcInstance)
				return true;
		}
		return false;
	}
	
	private int howManyPc(L2ZoneType zone)
	{
		int count = 0;
		for (L2Character c : zone.getCharactersInsideArray())
		{
			if (c instanceof L2PcInstance)
				count++;
		}
		return count;
	}
	
	private void updateMp(L2Character player)
	{
		double currentMp = player.getCurrentMp();
		double maxMp = player.getMaxMp();
		if (currentMp != maxMp)
		{
			double newMp = 0;
			if ((currentMp + mpBonus) >= maxMp)
				newMp = maxMp;
			else
				newMp = currentMp + mpBonus;
			player.setCurrentMp(newMp);
			StatusUpdate sump = new StatusUpdate(player.getObjectId());
			sump.addAttribute(StatusUpdate.CUR_MP, (int) newMp);
			player.sendPacket(sump);
			SystemMessage smp = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
			smp.addNumber((int) mpBonus);
			player.sendPacket(smp);
		}
	}
	
	private class giveMp implements Runnable
	{
		private L2ZoneType _zone;
		
		public giveMp(L2ZoneType zone)
		{
			_zone = zone;
		}
		
		public void run()
		{
			if (howManyPc(_zone) > 0)
			{
				for (L2Character c : _zone.getCharactersInsideArray())
				{
					if (c instanceof L2PcInstance)
						updateMp(c);
				}
			}
			else if (_mpTask != null)
			{
				_mpTask.cancel(true);
				_mpTask = null;
			}
		}
	}
	
	public LostNestTrees(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int zones : ZONES)
		{
			addEnterZoneId(zones);
			addExitZoneId(zones);
		}
	}
	
	public static void main(String[] args)
	{
		new LostNestTrees(-1, qn, "custom");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded NPC: LostNest Trees");
	}
}