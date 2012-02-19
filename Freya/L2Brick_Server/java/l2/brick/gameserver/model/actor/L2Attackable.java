/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2.brick.gameserver.model.actor;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import gov.nasa.worldwind.formats.dds.DDSConverter;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javolution.util.FastList;
import javolution.util.FastMap;

import l2.brick.Config;
import l2.brick.gameserver.ItemsAutoDestroy;
import l2.brick.gameserver.ThreadPoolManager;
import l2.brick.gameserver.ai.CtrlEvent;
import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.ai.L2AttackableAI;
import l2.brick.gameserver.ai.L2CharacterAI;
import l2.brick.gameserver.ai.L2FortSiegeGuardAI;
import l2.brick.gameserver.ai.L2SiegeGuardAI;
import l2.brick.gameserver.datatables.EventDroplist;
import l2.brick.gameserver.datatables.EventDroplist.DateDrop;
import l2.brick.gameserver.datatables.HerbDropTable;
import l2.brick.gameserver.datatables.ItemTable;
import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.idfactory.IdFactory;
import l2.brick.gameserver.instancemanager.CursedWeaponsManager;
import l2.brick.gameserver.instancemanager.PcCafePointsManager;
import l2.brick.gameserver.model.L2CharPosition;
import l2.brick.gameserver.model.L2CommandChannel;
import l2.brick.gameserver.model.L2DropCategory;
import l2.brick.gameserver.model.L2DropData;
import l2.brick.gameserver.model.L2ItemInstance;
import l2.brick.gameserver.model.L2Manor;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.L2Party;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.actor.instance.L2DoorInstance;
import l2.brick.gameserver.model.actor.instance.L2GrandBossInstance;
import l2.brick.gameserver.model.actor.instance.L2MonsterInstance;
import l2.brick.gameserver.model.actor.instance.L2NpcInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.actor.instance.L2PetInstance;
import l2.brick.gameserver.model.actor.instance.L2SummonInstance;
import l2.brick.gameserver.model.actor.knownlist.AttackableKnownList;
import l2.brick.gameserver.model.actor.status.AttackableStatus;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.clientpackets.Say2;
import l2.brick.gameserver.network.serverpackets.CreatureSay;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.brick.gameserver.network.serverpackets.PledgeCrest;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.skills.AbnormalEffect;
import l2.brick.gameserver.skills.Stats;
import l2.brick.gameserver.templates.L2NpcTemplate;
import l2.brick.gameserver.templates.L2EtcItemType;
import l2.brick.gameserver.util.Util;
import l2.brick.util.Rnd;

public class L2Attackable extends L2Npc
{
	private boolean _isRaid = false;
	private boolean _isRaidMinion = false;
	private boolean _champion = false;
	
	/**
	 * This class contains all AggroInfo of the L2Attackable against the attacker L2Character.
	 *
	 * Data:
	 * attacker : The attacker L2Character concerned by this AggroInfo of this L2Attackable
	 * hate : Hate level of this L2Attackable against the attacker L2Character (hate = damage)
	 * damage : Number of damages that the attacker L2Character gave to this L2Attackable
	 *
	 */
	public static final class AggroInfo
	{
		private final L2Character _attacker;
		private int _hate = 0;
		private int _damage = 0;
		
		AggroInfo(L2Character pAttacker)
		{
			_attacker = pAttacker;
		}
		
		public final L2Character getAttacker()
		{
			return _attacker;
		}
		
		public final int getHate()
		{
			return _hate;
		}
		
		public final int checkHate(L2Character owner)
		{
			if (_attacker.isAlikeDead()
					|| !_attacker.isVisible()
					|| !owner.getKnownList().knowsObject(_attacker))
				_hate = 0;
			
			return _hate;
		}
		
		public final void addHate(int value)
		{
			_hate = (int)Math.min(_hate + (long)value, 999999999);
		}
		
		public final void stopHate()
		{
			_hate = 0;
		}
		
		public final int getDamage()
		{
			return _damage;
		}
		
		public final void addDamage(int value)
		{
			_damage = (int)Math.min(_damage + (long)value, 999999999);
		}
		
		@Override
		public final boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			
			if (obj instanceof AggroInfo)
				return (((AggroInfo)obj).getAttacker() == _attacker);
			
			return false;
		}
		
		@Override
		public final int hashCode()
		{
			return _attacker.getObjectId();
		}
	}
	
	/**
	 * This class contains all RewardInfo of the L2Attackable against the any attacker L2Character, based on amount of damage done.
	 *
	 * Data:
	 * attacker : The attacker L2Character concerned by this RewardInfo of this L2Attackable
	 * dmg : Total amount of damage done by the attacker to this L2Attackable (summon + own)
	 *
	 */
	protected static final class RewardInfo
	{
		protected L2Character _attacker;
		
		protected int _dmg = 0;
		
		public RewardInfo(L2Character pAttacker, int pDmg)
		{
			_attacker = pAttacker;
			_dmg = pDmg;
		}
		
		public void addDamage(int pDmg)
		{
			_dmg += pDmg;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			
			if (obj instanceof RewardInfo)
				return (((RewardInfo)obj)._attacker == _attacker);
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}
	}
	
	/**
	 * This class contains all AbsorberInfo of the L2Attackable against the absorber L2Character.
	 *
	 * Data:
	 * absorber : The attacker L2Character concerned by this AbsorberInfo of this L2Attackable
	 */
	public static final class AbsorberInfo
	{
		public int _objId;
		public double _absorbedHP;
		
		AbsorberInfo(int objId, double pAbsorbedHP)
		{
			_objId = objId;
			_absorbedHP = pAbsorbedHP;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			
			if (obj instanceof AbsorberInfo)
				return (((AbsorberInfo)obj)._objId == _objId);
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return _objId;
		}
	}
	
	public static final class RewardItem
	{
		protected int _itemId;
		
		protected int _count;
		
		public RewardItem(int itemId, int count)
		{
			_itemId = itemId;
			_count = count;
		}
		
		public int getItemId() { return _itemId;}
		
		public int getCount() { return _count;}
	}
	
	private FastMap<L2Character, AggroInfo> _aggroList = new FastMap<L2Character, AggroInfo>().shared();
	
	public final FastMap<L2Character, AggroInfo> getAggroList()
	{
		return _aggroList;
	}
	
	private boolean _isReturningToSpawnPoint = false;
	
	public final boolean isReturningToSpawnPoint()
	{
		return _isReturningToSpawnPoint;
	}
	
	public final void setisReturningToSpawnPoint(boolean value)
	{
		_isReturningToSpawnPoint = value;
	}
	
	private boolean _canReturnToSpawnPoint = true;
	
	public final boolean canReturnToSpawnPoint()
	{
		return _canReturnToSpawnPoint;
	}
	
	public final void setCanReturnToSpawnPoint(boolean value)
	{
		_canReturnToSpawnPoint = value;
	}
	
	private boolean _seeThroughSilentMove = false;
	public boolean canSeeThroughSilentMove() { return _seeThroughSilentMove; }
	public void setSeeThroughSilentMove(boolean val) { _seeThroughSilentMove = val; }
	
	private RewardItem[] _sweepItems;
	
	private RewardItem[] _harvestItems;
	private boolean _seeded;
	private int _seedType = 0;
	private int _seederObjId = 0;
	
	private boolean _overhit;
	
	private double _overhitDamage;
	
	private L2Character _overhitAttacker;
	
	private L2CommandChannel _firstCommandChannelAttacked = null;
	private CommandChannelTimer _commandChannelTimer = null;
	private long _commandChannelLastAttack = 0;
	
	private boolean _absorbed;
	
	private FastMap<Integer, AbsorberInfo> _absorbersList = new FastMap<Integer, AbsorberInfo>().shared();
	
	private boolean _mustGiveExpSp;
	
	/** True if a Dwarf has used Spoil on this L2NpcInstance */
	private boolean _isSpoil = false;
	
	private int _isSpoiledBy = 0;
	
	protected int _onKillDelay = 5000;
	
	/**
	 * Constructor of L2Attackable (use L2Character and L2NpcInstance constructor).
	 *
	 * Actions:
	 * Call the L2Character constructor to set the _template of the L2Attackable (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)
	 * Set the name of the L2Attackable
	 * Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param L2NpcTemplate Template to apply to the NPC
	 */
	public L2Attackable(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2Attackable);
		setIsInvul(false);
		_mustGiveExpSp = true;
	}
	
	@Override
	public AttackableKnownList getKnownList()
	{
		return (AttackableKnownList)super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new AttackableKnownList(this));
	}
	
	@Override
	public AttackableStatus getStatus()
	{
		return (AttackableStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new AttackableStatus(this));
	}
	
	/**
	 * Return the L2Character AI of the L2Attackable and if its null create a new one.
	 */
	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai;
		
		if (ai == null)
		{
			synchronized(this)
			{
				if (_ai == null) _ai = new L2AttackableAI(new AIAccessor());
				return _ai;
			}
		}
		return ai;
	}
	
	/**
	 * Not used.
	 * get condition to hate, actually isAggressive() is checked by monster and karma by guards in motheds that overwrite this one.
	 *
	 * @deprecated
	 */
	@Deprecated
	public boolean getCondition2(L2Character target)
	{
		if (target instanceof L2NpcInstance || target instanceof L2DoorInstance)
			return false;
		
		if
		(
				target.isAlikeDead()
				|| !isInsideRadius(target, getAggroRange(), false, false)
				|| Math.abs(getZ()-target.getZ()) > 100
		)
			return false;
		
		return !target.isInvul();
	}

	public void useMagic(L2Skill skill)
	{
		if (skill == null || isAlikeDead())
			return;

		if (skill.isPassive())
			return;

		if (isCastingNow())
			return;

		if (isSkillDisabled(skill))
			return;

		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
			return;

		if (getCurrentHp() <= skill.getHpConsume())
			return;

		if (skill.isMagic())
		{
			if (isMuted())
				return;
		}
		else
		{
			if (isPhysicalMuted())
				return;
		}

		L2Object target = skill.getFirstOfTargetList(this);
		if (target == null)
			return;

		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}

	/**
	 * Reduce the current HP of the L2Attackable.
	 *
	 * @param damage The HP decrease value
	 * @param attacker The L2Character who attacks
	 */
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(damage, attacker, true, false, skill);
	}
	
	/**
	 * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.
	 *
	 * @param i The HP decrease value
	 * @param attacker The L2Character who attacks
	 * @param awake The awake state (If True : stop sleeping)
	 */
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (isRaid() && !isMinion() && attacker != null && attacker.getParty() != null
				&& attacker.getParty().isInCommandChannel() && attacker.getParty().getCommandChannel().meetRaidWarCondition(this))
		{
			if (_firstCommandChannelAttacked == null) //looting right isn't set
			{
				synchronized (this)
				{
					if (_firstCommandChannelAttacked == null)
					{
						_firstCommandChannelAttacked = attacker.getParty().getCommandChannel();
						if (_firstCommandChannelAttacked != null)
						{
							_commandChannelTimer = new CommandChannelTimer(this);
							_commandChannelLastAttack = System.currentTimeMillis();
							ThreadPoolManager.getInstance().scheduleGeneral(_commandChannelTimer, 10000); // check for last attack
							_firstCommandChannelAttacked.broadcastToChannelMembers(new CreatureSay(0, Say2.PARTYROOM_ALL, "", "You have looting rights!")); //TODO: retail msg
						}
					}
				}
			}
			else if (attacker.getParty().getCommandChannel().equals(_firstCommandChannelAttacked)) //is in same channel
			{
				_commandChannelLastAttack = System.currentTimeMillis(); // update last attack time
			}
		}
		
		if (isEventMob) return;
		
		// Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList
		if (attacker != null)
			addDamage(attacker, (int)damage, skill);
		
		// If this L2Attackable is a L2MonsterInstance and it has spawned minions, call its minions to battle
		if (this instanceof L2MonsterInstance)
		{
			L2MonsterInstance master = (L2MonsterInstance) this;

			if (master.hasMinions())
				master.getMinionList().onAssist(this, attacker);
			
			master = master.getLeader();				
			if (master != null && master.hasMinions())
				master.getMinionList().onAssist(this, attacker);
		}
		// Reduce the current HP of the L2Attackable and launch the doDie Task if necessary
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}
	
	public synchronized void setMustRewardExpSp(boolean value)
	{
		_mustGiveExpSp = value;
	}
	
	public synchronized boolean getMustRewardExpSP()
	{
		return _mustGiveExpSp;
	}
	
	public static StringBuilder finalString = new StringBuilder();
	NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
	private static BufferedImage generateCaptcha()
	{    
		   Color textColor = new Color(98, 213, 43);
		   Color circleColor = new Color(98, 213, 43);
		   Font textFont = new Font("comic sans ms", Font.BOLD, 24);
		   int charsToPrint = 5;
		   int width = 256;
		   int height = 64;
		   int circlesToDraw = 8;
		   float horizMargin = 20.0f;
		   double rotationRange = 0.7; // this is radians
		   BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		   Graphics2D g = (Graphics2D) bufferedImage.getGraphics();

		   //Draw an oval
		   g.setColor(new Color(30,31,31));
		   g.fillRect(0, 0, width, height);

		   // lets make some noisey circles
		   g.setColor(circleColor);
		   for ( int i = 0; i < circlesToDraw; i++ ) {
		     int circleRadius = (int) (Math.random() * height / 2.0);
		     int circleX = (int) (Math.random() * width - circleRadius);
		     int circleY = (int) (Math.random() * height - circleRadius);
		     g.drawOval(circleX, circleY, circleRadius * 2, circleRadius * 2);
		   }

		   g.setColor(textColor);
		   g.setFont(textFont);

		   FontMetrics fontMetrics = g.getFontMetrics();
		   int maxAdvance = fontMetrics.getMaxAdvance();
		   int fontHeight = fontMetrics.getHeight();
		   
		   // Suggestions ----------------------------------------------------------------------
		   // i removed 1 and l and i because there are confusing to users...
		   // Z, z, and N also get confusing when rotated
		   // 0, O, and o are also confusing...
		   // lowercase G looks a lot like a 9 so i killed it
		   // this should ideally be done for every language...
		   // i like controlling the characters though because it helps prevent confusion
		   // So recommended chars are:
		   // String elegibleChars = "ABCDEFGHJKLMPQRSTUVWXYabcdefhjkmnpqrstuvwxy23456789";
		   // Suggestions ----------------------------------------------------------------------
		   String elegibleChars = "ABCDEFGHJKLMPQRSTUVWXYZabcdefghjklmpqrstuvwxy23456789";
		   char[] chars = elegibleChars.toCharArray();

		   float spaceForLetters = -horizMargin * 2 + width;
		   float spacePerChar = spaceForLetters / (charsToPrint - 1.0f);

		   for ( int i = 0; i < charsToPrint; i++ ) {
		     double randomValue = Math.random();
		     int randomIndex = (int) Math.round(randomValue * (chars.length - 1));
		     char characterToShow = chars[randomIndex];
		     finalString.append(characterToShow);

		     // this is a separate canvas used for the character so that
		     // we can rotate it independently
		     int charWidth = fontMetrics.charWidth(characterToShow);
		     int charDim = Math.max(maxAdvance, fontHeight);
		     int halfCharDim = (charDim / 2);

		     BufferedImage charImage = new BufferedImage(charDim, charDim, BufferedImage.TYPE_INT_ARGB);
		     Graphics2D charGraphics = charImage.createGraphics();
		     charGraphics.translate(halfCharDim, halfCharDim);
		     double angle = (Math.random() - 0.5) * rotationRange;
		     charGraphics.transform(AffineTransform.getRotateInstance(angle));
		     charGraphics.translate(-halfCharDim,-halfCharDim);
		     charGraphics.setColor(textColor);
		     charGraphics.setFont(textFont);

		     int charX = (int) (0.5 * charDim - 0.5 * charWidth);
		     charGraphics.drawString("" + characterToShow, charX, 
		                            ((charDim - fontMetrics.getAscent()) 
		                                   / 2 + fontMetrics.getAscent()));

		     float x = horizMargin + spacePerChar * (i) - charDim / 2.0f;
		     int y = ((height - charDim) / 2);
		     g.drawImage(charImage, (int) x, y, charDim, charDim, null, null);

		     charGraphics.dispose();
		   }
		   
			g.dispose();     
			
			return bufferedImage;
			}
	/**
	 * Kill the L2Attackable (the corpse disappeared after 7 seconds), distribute rewards (EXP, SP, Drops...) and notify Quest Engine.
	 *
	 * Actions:
	 * Distribute Exp and SP rewards to L2PcInstance (including Summon owner) that hit the L2Attackable and to their Party members
	 * Notify the Quest Engine of the L2Attackable death if necessary
	 * Kill the L2NpcInstance (the corpse disappeared after 7 seconds)
	 *
	 * Caution: This method DOESN'T GIVE rewards to L2PetInstance
	 *
	 * @param killer The L2Character that has killed the L2Attackable
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		// Kill the L2NpcInstance (the corpse disappeared after 7 seconds)
		if ( killer == null || !super.doDie(killer))
			return false;
		
		if( killer instanceof L2PcInstance )
		{
			((L2PcInstance) killer).setKills(((L2PcInstance) killer).getKills() + Config.CAPTCHA_SYSTEM);
			if(((L2PcInstance) killer).getKills() >= Config.MIN_KILLS_FOR_CAPTCHA || ((L2PcInstance) killer).getKills() == 0)
			{
				//Random image file name
				int imgId = IdFactory.getInstance().getNextId();
				//Convertion from .png to .dds, and crest packed send
				try
				{
					File captcha = new File("data/captcha/captcha.png");    
					ImageIO.write(generateCaptcha(), "png", captcha);
					PledgeCrest packet = new PledgeCrest(imgId, DDSConverter.convertToDDS(captcha).array()); //Convertion to DDS where is antibot
					killer.sendPacket(packet);
				}
				catch (Exception e)
				{    
					_log.warning(e.getMessage());
				}
				//Paralyze, abnormal effect, invul, html with captcha output and start of the 1 min counter
			    killer.startAbnormalEffect(AbnormalEffect.REAL_TARGET);
				killer.setIsParalyzed(true);
				killer.setIsInvul(true);
				adminReply.setHtml("<html><title>Captcha Antibot System</title><body><center>Enter the 5-digits,code below and click Confirm.<br1>You will be invulnerable until you enter<br1>the code so no one will kill you!<br><img src=\"Crest.crest_" + Config.SERVER_ID + "_" + imgId + "\" width=256 height=64><br><font color=\"888888\">(There are only english uppercase letters.)</font><br><font color=\"FF0000\">Correct Big and Small</font><br><edit var=\"antibot\" width=110><br><button value=\"Confirm\" action=\"bypass -h voice .antibot $antibot\" width=80 height=26 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"><br>You have 60 seconds to answer or you<br1>will get jailed for 1 min.<br1>You will also get jailed for 1 min.<br1>if you will answer worng.</center></body></html>");
				killer.sendPacket(adminReply);
				((L2PcInstance) killer).setCode(finalString);
				ThreadPoolManager.getInstance().scheduleGeneral(new CaptchaTimer((L2PcInstance)killer), 60000);//60sec
				((L2PcInstance) killer).setCodeRight(false);
				     finalString.replace(0, 5, "");
			}
		}
		
		// Notify the Quest Engine of the L2Attackable death if necessary
		try
		{
			L2PcInstance player = killer.getActingPlayer();
			if (player != null)
			{
				if (getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL) != null)
					for (Quest quest: getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
						ThreadPoolManager.getInstance().scheduleEffect(new OnKillNotifyTask(this, quest, player, killer instanceof L2Summon), _onKillDelay);
			}
		}
		catch (Exception e) { _log.log(Level.SEVERE, "", e); }
		return true;
	}
	
	protected static class OnKillNotifyTask implements Runnable
	{
		private L2Attackable _attackable;
		private Quest _quest;
		private L2PcInstance _killer;
		private boolean _isPet;
		
		public OnKillNotifyTask(L2Attackable attackable, Quest quest, L2PcInstance killer, boolean isPet)
		{
			_attackable = attackable;
			_quest = quest;
			_killer = killer;
			_isPet = isPet;
		}
		public void run()
		{
			_quest.notifyKill(_attackable, _killer, _isPet);
		}
	}
	
	/**
	 * Distribute Exp and SP rewards to L2PcInstance (including Summon owner) that hit the L2Attackable and to their Party members.
	 *
	 * Actions:
	 * Get the L2PcInstance owner of the L2SummonInstance (if necessary) and L2Party in progress
	 * Calculate the Experience and SP rewards in function of the level difference
	 * Add Exp and SP rewards to L2PcInstance (including Summon penalty) and to Party members in the known area of the last attacker
	 *
	 * Caution : This method DOESN'T GIVE rewards to L2PetInstance
	 *
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 */
	@Override
	protected void calculateRewards(L2Character lastAttacker)
	{
		// Creates an empty list of rewards
		FastMap<L2Character, RewardInfo> rewards = new FastMap<L2Character, RewardInfo>().shared();
		try
		{
			if (getAggroList().isEmpty())
				return;
			
			// Manage Base, Quests and Sweep drops of the L2Attackable
			doItemDrop(lastAttacker);
			
			// Manage drop of Special Events created by GM for a defined period
			doEventDrop(lastAttacker);
			
			if (!getMustRewardExpSP())
				return;
			
			int damage;
			L2Character attacker, ddealer;
			RewardInfo reward;
			
			// While Interating over This Map Removing Object is Not Allowed
			//synchronized (getAggroList())
			{
				// Go through the _aggroList of the L2Attackable
				for (AggroInfo info : getAggroList().values())
				{
					if (info == null)
						continue;
					
					// Get the L2Character corresponding to this attacker
					attacker = info.getAttacker();
					
					// Get damages done by this attacker
					damage = info.getDamage();
					
					// Prevent unwanted behavior
					if (damage > 1)
					{
						if ((attacker instanceof L2SummonInstance) || ((attacker instanceof L2PetInstance) && ((L2PetInstance)attacker).getPetLevelData().getOwnerExpTaken() > 0))
							ddealer = ((L2Summon)attacker).getOwner();
						else
							ddealer = info.getAttacker();
						
						// Check if ddealer isn't too far from this (killed monster)
						if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, ddealer, true))
							continue;
						
						// Calculate real damages (Summoners should get own damage plus summon's damage)
						reward = rewards.get(ddealer);
						
						if (reward == null)
							reward = new RewardInfo(ddealer, damage);
						else
							reward.addDamage(damage);
						
						rewards.put(ddealer, reward);
					}
				}
			}
			if (!rewards.isEmpty())
			{
				L2Party attackerParty;
				long exp,exp_premium;
				int levelDiff, partyDmg, partyLvl, sp, sp_premium;
				float partyMul, penalty;
				RewardInfo reward2;
				int[] tmp;
				
				for (FastMap.Entry<L2Character, RewardInfo> entry = rewards.head(), end = rewards.tail(); (entry = entry.getNext()) != end;)
				{
					if (entry == null)
						continue;
					
					reward = entry.getValue();
					
					if (reward == null)
						continue;
					
					// Penalty applied to the attacker's XP
					penalty = 0;
					
					// Attacker to be rewarded
					attacker = reward._attacker;
					
					// Total amount of damage done
					damage = reward._dmg;
					
					// If the attacker is a Pet, get the party of the owner
					if (attacker instanceof L2PetInstance)
						attackerParty = ((L2PetInstance)attacker).getParty();
					else if (attacker instanceof L2PcInstance)
						attackerParty = ((L2PcInstance)attacker).getParty();
					else
						return;
					
					// If this attacker is a L2PcInstance with a summoned L2SummonInstance, get Exp Penalty applied for the current summoned L2SummonInstance
					if (attacker instanceof L2PcInstance && ((L2PcInstance)attacker).getPet() instanceof L2SummonInstance)
						penalty = ((L2SummonInstance)((L2PcInstance)attacker).getPet()).getExpPenalty();
					
					// We must avoid "over damage", if any
					if (damage > getMaxHp())
						damage = getMaxHp();
					
					// If there's NO party in progress
					if (attackerParty == null)
					{
						// Calculate Exp and SP rewards
						if (attacker.getKnownList().knowsObject(this))
						{
							// Calculate the difference of level between this attacker (L2PcInstance or L2SummonInstance owner) and the L2Attackable
							// mob = 24, atk = 10, diff = -14 (full xp)
							// mob = 24, atk = 28, diff = 4 (some xp)
							// mob = 24, atk = 50, diff = 26 (no xp)
							levelDiff = attacker.getLevel() - getLevel();
							tmp = calculateExpAndSp(levelDiff, damage, attacker.getPremiumService());
							exp = tmp[0];
							exp *= 1 - penalty;
							sp = tmp[1];
							
							if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
							{
								exp *= Config.L2JMOD_CHAMPION_REWARDS;
								sp *= Config.L2JMOD_CHAMPION_REWARDS;
							}
							
							// Check for an over-hit enabled strike
							if (attacker instanceof L2PcInstance)
							{
								L2PcInstance player = (L2PcInstance)attacker;
								if (isOverhit() && attacker == getOverhitAttacker())
								{
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OVER_HIT));
									exp += calculateOverhitExp(exp);
								}
							}
							
							// Distribute the Exp and SP between the L2PcInstance and its L2Summon
							if (!attacker.isDead())
							{
								long addexp = Math.round(attacker.calcStat(Stats.EXPSP_RATE, exp, null, null));
								int addsp = (int)attacker.calcStat(Stats.EXPSP_RATE, sp, null, null);
								
								if (attacker instanceof L2PcInstance)
								{
									if (((L2PcInstance)attacker).getSkillLevel(467) > 0)
									{
										L2Skill skill = SkillTable.getInstance().getInfo(467,((L2PcInstance)attacker).getSkillLevel(467));
										
										if (skill.getExpNeeded() <= addexp)
											((L2PcInstance)attacker).absorbSoul(skill,this);
									}
									((L2PcInstance)attacker).addExpAndSp(addexp,addsp, useVitalityRate());
									if (addexp > 0)
									{
 										((L2PcInstance)attacker).updateVitalityPoints(getVitalityPoints(damage), true, false);
										PcCafePointsManager.getInstance().givePcCafePoint(((L2PcInstance) attacker), addexp);
									}
								}
								else
									attacker.addExpAndSp(addexp,addsp);
							}
						}
					}
					else
					{
						//share with party members
						partyDmg = 0;
						partyMul = 1.f;
						partyLvl = 0;
						
						// Get all L2Character that can be rewarded in the party
						List<L2Playable> rewardedMembers = new FastList<L2Playable>();
						// Go through all L2PcInstance in the party
						List<L2PcInstance> groupMembers;
						
						if (attackerParty.isInCommandChannel())
							groupMembers = attackerParty.getCommandChannel().getMembers();
						else
							groupMembers = attackerParty.getPartyMembers();
						
						for (L2PcInstance pl : groupMembers)
						{
							if (pl == null || pl.isDead())
								continue;
							
							// Get the RewardInfo of this L2PcInstance from L2Attackable rewards
							reward2 = rewards.get(pl);
							
							// If the L2PcInstance is in the L2Attackable rewards add its damages to party damages
							if (reward2 != null)
							{
								if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true))
								{
									partyDmg += reward2._dmg; // Add L2PcInstance damages to party damages
									rewardedMembers.add(pl);
									
									if (pl.getLevel() > partyLvl)
									{
										if (attackerParty.isInCommandChannel())
											partyLvl = attackerParty.getCommandChannel().getLevel();
										else
											partyLvl = pl.getLevel();
									}
								}
								rewards.remove(pl); // Remove the L2PcInstance from the L2Attackable rewards
							}
							else
							{
								// Add L2PcInstance of the party (that have attacked or not) to members that can be rewarded
								// and in range of the monster.
								if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true))
								{
									rewardedMembers.add(pl);
									if (pl.getLevel() > partyLvl)
									{
										if (attackerParty.isInCommandChannel())
											partyLvl = attackerParty.getCommandChannel().getLevel();
										else
											partyLvl = pl.getLevel();
									}
								}
							}
							L2Playable summon = pl.getPet();
							
							if (summon != null && summon instanceof L2PetInstance)
							{
								reward2 = rewards.get(summon);
								
								if (reward2 != null) // Pets are only added if they have done damage
								{
									if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, summon, true))
									{
										partyDmg += reward2._dmg; // Add summon damages to party damages
										rewardedMembers.add(summon);
										
										if (summon.getLevel() > partyLvl)
											partyLvl = summon.getLevel();
									}
									rewards.remove(summon); // Remove the summon from the L2Attackable rewards
								}
							}
						}
						
						// If the party didn't killed this L2Attackable alone
						if (partyDmg < getMaxHp())
							partyMul = ((float)partyDmg / (float)getMaxHp());
						
						// Avoid "over damage"
						if (partyDmg > getMaxHp())
							partyDmg = getMaxHp();
						
						// Calculate the level difference between Party and L2Attackable
						levelDiff = partyLvl - getLevel();
						
						// Calculate Exp and SP rewards
						tmp = calculateExpAndSp(levelDiff, partyDmg, 1);
						exp_premium = tmp[0];
						sp_premium = tmp[1];
						tmp = calculateExpAndSp(levelDiff, partyDmg, 0);
						exp = tmp[0];
						sp = tmp[1];
						
						if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
						{
							exp *= Config.L2JMOD_CHAMPION_REWARDS;
							sp *= Config.L2JMOD_CHAMPION_REWARDS;
						}
						
						exp *= partyMul;
						sp *= partyMul;
						exp_premium *= partyMul;
						sp_premium *= partyMul;
						// Check for an over-hit enabled strike
						// (When in party, the over-hit exp bonus is given to the whole party and splitted proportionally through the party members)
						if (attacker instanceof L2PcInstance)
						{
							L2PcInstance player = (L2PcInstance)attacker;
							
							if (isOverhit() && attacker == getOverhitAttacker())
							{
								player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OVER_HIT));
								exp += calculateOverhitExp(exp);
								exp_premium += calculateOverhitExp(exp_premium);
							}
						}
						// Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker
						if (partyDmg > 0)
							attackerParty.distributeXpAndSp(exp_premium, sp_premium, exp, sp, rewardedMembers, partyLvl, partyDmg, this);
					}
				}
			}
			rewards = null;
		}
		catch (Exception e) { _log.log(Level.SEVERE, "", e); }
	}
	
	
	/**
	 * 
	 * @see l2.brick.gameserver.model.actor.L2Character#addAttackerToAttackByList(l2.brick.gameserver.model.actor.L2Character)
	 */
	@Override
	public void addAttackerToAttackByList (L2Character player)
	{
		if (player == null || player == this || getAttackByList().contains(player))
			return;
		getAttackByList().add(player);
	}
	
	/**
	 * Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList.
	 *
	 * @param attacker The L2Character that gave damages to this L2Attackable
	 * @param damage The number of damages given by the attacker L2Character
	 *
	 */
	public void addDamage(L2Character attacker, int damage, L2Skill skill)
	{
		if (attacker == null)
			return;
		
		// Notify the L2Attackable AI with EVT_ATTACKED
		if (!isDead())
		{
			try
			{
				L2PcInstance player = attacker.getActingPlayer();
				if (player != null)
				{
					if (getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK) !=null)
						for (Quest quest: getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
							quest.notifyAttack(this, player, damage, attacker instanceof L2Summon, skill);
				}
				// for now hard code damage hate caused by an L2Attackable
				else
				{
					getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
					addDamageHate(attacker, damage, (damage*100)/(getLevel()+7));
				}
			}
			catch (Exception e) { _log.log(Level.SEVERE, "", e); }
		}
	}
	
	/**
	 * Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList.
	 *
	 * @param attacker The L2Character that gave damages to this L2Attackable
	 * @param damage The number of damages given by the attacker L2Character
	 * @param aggro The hate (=damage) given by the attacker L2Character
	 *
	 */
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
			return;
		
		L2PcInstance targetPlayer = attacker.getActingPlayer();
		// Get the AggroInfo of the attacker L2Character from the _aggroList of the L2Attackable
		AggroInfo ai = getAggroList().get(attacker);
		
		if (ai == null)
		{
			ai = new AggroInfo(attacker);
			getAggroList().put(attacker, ai);
		}
		ai.addDamage(damage);
		// traps does not cause aggro
		// making this hack because not possible to determine if damage made by trap
		// so just check for triggered trap here
		if (targetPlayer == null
				|| targetPlayer.getTrap() == null
				|| !targetPlayer.getTrap().isTriggered())
			ai.addHate(aggro);
		
		if (targetPlayer != null && aggro == 0)
		{
			if (getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER) !=null)
				for (Quest quest: getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER))
					quest.notifyAggroRangeEnter(this, targetPlayer, (attacker instanceof L2Summon));
		}
		else if (targetPlayer == null && aggro == 0)
		{
			aggro = 1;
			ai.addHate(1);
		}
		
		// Set the intention to the L2Attackable to AI_INTENTION_ACTIVE
		if (aggro > 0 && getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}
	
	public void reduceHate(L2Character target, int amount)
	{
		if (getAI() instanceof L2SiegeGuardAI || getAI() instanceof L2FortSiegeGuardAI)
		{
			// TODO: this just prevents error until siege guards are handled properly
			stopHating(target);
			setTarget(null);
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			return;
		}
		
		if (target == null) // whole aggrolist
		{
			L2Character mostHated = getMostHated();
			
			if (mostHated == null) // makes target passive for a moment more
			{
				((L2AttackableAI)getAI()).setGlobalAggro(-25);
				return;
			}
			else
			{
				for(L2Character aggroed : getAggroList().keySet())
				{
					AggroInfo ai = getAggroList().get(aggroed);
					
					if (ai == null)
						return;
					ai.addHate(-amount);
				}
			}
			
			amount = getHating(mostHated);
			
			if (amount <= 0)
			{
				((L2AttackableAI)getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
			return;
		}
		AggroInfo ai = getAggroList().get(target);
		
		if (ai == null)
			return;
		ai.addHate(-amount);
		
		if (ai.getHate() <= 0)
		{
			if (getMostHated() == null)
			{
				((L2AttackableAI)getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
		}
	}
	
	/**
	 * Clears _aggroList hate of the L2Character without removing from the list.
	 */
	public void stopHating(L2Character target)
	{
		if (target == null)
			return;
		AggroInfo ai = getAggroList().get(target);
		if (ai != null)
			ai.stopHate();
	}
	
	/**
	 * Return the most hated L2Character of the L2Attackable _aggroList.
	 */
	public L2Character getMostHated()
	{
		if (getAggroList().isEmpty() || isAlikeDead()) return null;
		
		L2Character mostHated = null;
		int maxHate = 0;
		
		// While Interating over This Map Removing Object is Not Allowed
		//synchronized (getAggroList())
		{
			// Go through the aggroList of the L2Attackable
			for (AggroInfo ai : getAggroList().values())
			{
				if (ai == null)
					continue;
				
				if (ai.checkHate(this) > maxHate)
				{
					mostHated = ai.getAttacker();
					maxHate = ai.getHate();
				}
			}
		}
		return mostHated;
	}
	
	/**
	 * Return the 2 most hated L2Character of the L2Attackable _aggroList.
	 */
	public List<L2Character> get2MostHated()
	{
		if (getAggroList().isEmpty() || isAlikeDead())
			return null;
		
		L2Character mostHated = null;
		L2Character secondMostHated = null;
		int maxHate = 0;
		List<L2Character> result = new FastList<L2Character>();
		
		// While iterating over this map removing objects is not allowed
		//synchronized (getAggroList())
		{
			// Go through the aggroList of the L2Attackable
			for (AggroInfo ai : getAggroList().values())
			{
				if (ai == null)
					continue;
				
				if (ai.checkHate(this) > maxHate)
				{
					secondMostHated = mostHated;
					mostHated = ai.getAttacker();
					maxHate = ai.getHate();
				}
			}
		}
		result.add(mostHated);
		
		if (getAttackByList().contains(secondMostHated))
			result.add(secondMostHated);
		else
			result.add(null);
		return result;
	}
	
	public List<L2Character> getHateList()
	{
		if (getAggroList().isEmpty() || isAlikeDead()) return null;
		List<L2Character> result = new FastList<L2Character>();
		
		//synchronized (getAggroList())
		{
			for (AggroInfo ai : getAggroList().values())
			{
				if (ai == null)
					continue;
				ai.checkHate(this);
				
				result.add(ai.getAttacker());
			}
		}
		
		return result;
	}
	/**
	 * Return the hate level of the L2Attackable against this L2Character contained in _aggroList.
	 *
	 * @param target The L2Character whose hate level must be returned
	 */
	public int getHating(final L2Character target)
	{
		if (getAggroList().isEmpty() || target == null)
			return 0;
		
		AggroInfo ai = getAggroList().get(target);
		
		if (ai == null)
			return 0;
		
		if (ai.getAttacker() instanceof L2PcInstance)
		{
			L2PcInstance act = (L2PcInstance)ai.getAttacker();
			if(act.getAppearance().getInvisible() || ai.getAttacker().isInvul()
					|| act.isSpawnProtected())
			{
				//Remove Object Should Use This Method and Can be Blocked While Interating
				getAggroList().remove(target);
				return 0;
			}
		}
		
		if (!ai.getAttacker().isVisible())
		{
			getAggroList().remove(target);
			return 0;
		}
		
		if (ai.getAttacker().isAlikeDead())
		{
			ai.stopHate();
			return 0;
		}
		return ai.getHate();
	}
	
	/**
	 * Calculates quantity of items for specific drop acording to current situation
	 *
	 * @param drop The L2DropData count is being calculated for
	 * @param lastAttacker The L2PcInstance that has killed the L2Attackable
	 * @param deepBlueDrop Factor to divide the drop chance
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 */
	private RewardItem calculateRewardItem(L2PcInstance lastAttacker, L2DropData drop, int levelModifier, boolean isSweep)
	{
		// Get default drop chance
		double dropChance = drop.getChance();
		
		int deepBlueDrop = 1;
		
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES)
				|| (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
		{
			if (levelModifier > 0)
			{
				// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
				// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
				deepBlueDrop = 3;
				if (drop.getItemId() == 57)
					deepBlueDrop *= isRaid() && !isRaidMinion()? (int)Config.RATE_DROP_ITEMS_BY_RAID : (int)Config.RATE_DROP_ITEMS;
			}
		}
		
		// Avoid dividing by 0
		if (deepBlueDrop == 0)
			deepBlueDrop = 1;
		
		// Check if we should apply our maths so deep blue mobs will not drop that easy
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES)
				|| (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
			dropChance = ((drop.getChance() - ((drop.getChance() * levelModifier)/100)) / deepBlueDrop);
		
		// Applies Drop rates
		if (Config.RATE_DROP_ITEMS_ID.get(drop.getItemId()) != 0)
		{
			if ((lastAttacker.getPremiumService()==1) && (Config.PREMIUM_RATE_DROP_ITEMS_ID.get(drop.getItemId()) != 0))
				dropChance *= Config.PREMIUM_RATE_DROP_ITEMS_ID.get(drop.getItemId());
			else
				dropChance *= Config.RATE_DROP_ITEMS_ID.get(drop.getItemId());
		}
		
		else if (isSweep)
		{
			if (lastAttacker.getPremiumService()==1)
				dropChance *= Config.PREMIUM_RATE_DROP_SPOIL;
			else
				dropChance *= Config.RATE_DROP_SPOIL;
		}
		else
		{
			if (lastAttacker.getPremiumService()==1)
				dropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.PREMIUM_RATE_DROP_ITEMS;
			else
				dropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
		}
		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
			dropChance *= Config.L2JMOD_CHAMPION_REWARDS;
				
		// Set our limits for chance of drop
		if (dropChance < 1)
			dropChance = 1;
		
		// Get min and max Item quantity that can be dropped in one time
		int minCount = drop.getMinDrop();
		int maxCount = drop.getMaxDrop();
		int itemCount = 0;
		
		// Count and chance adjustment for high rate servers
		if (dropChance > L2DropData.MAX_CHANCE && !Config.PRECISE_DROP_CALCULATION)
		{
			int multiplier = (int)dropChance / L2DropData.MAX_CHANCE;
			
			if (minCount < maxCount)
				itemCount += Rnd.get(minCount * multiplier, maxCount * multiplier);
			else if (minCount == maxCount)
				itemCount += minCount * multiplier;
			else
				itemCount += multiplier;
			
			dropChance = dropChance % L2DropData.MAX_CHANCE;
		}
		// Check if the Item must be dropped
		int random = Rnd.get(L2DropData.MAX_CHANCE);
		while (random < dropChance)
		{
			// Get the item quantity dropped
			if (minCount < maxCount)
				itemCount += Rnd.get(minCount, maxCount);
			else if (minCount == maxCount)
				itemCount += minCount;
			else itemCount++;
			
			// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
			dropChance -= L2DropData.MAX_CHANCE;
		}
		if (Config.L2JMOD_CHAMPION_ENABLE)
			// TODO (April 11, 2009): Find a way not to hardcode these values.
			if ((drop.getItemId() == 57 || (drop.getItemId() >= 6360 && drop.getItemId() <= 6362)) && isChampion())
				itemCount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS;
		
		if (itemCount > 0)
			return new RewardItem(drop.getItemId(), itemCount);
		else if (itemCount == 0 && Config.DEBUG)
			_log.fine("Roll produced no drops.");
		
		return null;
	}
	
	/**
	 * Calculates quantity of items for specific drop CATEGORY according to current situation
	 * Only a max of ONE item from a category is allowed to be dropped.
	 *
	 * @param drop The L2DropData count is being calculated for
	 * @param lastAttacker The L2PcInstance that has killed the L2Attackable
	 * @param deepBlueDrop Factor to divide the drop chance
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 */
	private RewardItem calculateCategorizedRewardItem(L2PcInstance lastAttacker, L2DropCategory categoryDrops, int levelModifier)
	{
		if (categoryDrops == null)
			return null;
		
		// Get default drop chance for the category (that's the sum of chances for all items in the category)
		// keep track of the base category chance as it'll be used later, if an item is drop from the category.
		// for everything else, use the total "categoryDropChance"
		int basecategoryDropChance = categoryDrops.getCategoryChance() ;
		int categoryDropChance = basecategoryDropChance;
		
		int deepBlueDrop = 1;
		
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES)
				|| (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
		{
			// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
			// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
			if (levelModifier > 0)
				deepBlueDrop = 3;
		}
		
		// Avoid dividing by 0
		if (deepBlueDrop == 0)
			deepBlueDrop = 1;
		
		// Check if we should apply our maths so deep blue mobs will not drop that easy
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES)
				|| (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
			categoryDropChance = ((categoryDropChance - ((categoryDropChance * levelModifier)/100)) / deepBlueDrop);
		
		// Applies Drop rates
		if (lastAttacker.getPremiumService()==1)
			categoryDropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.PREMIUM_RATE_DROP_ITEMS;
		else
		categoryDropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
		
		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
			categoryDropChance *= Config.L2JMOD_CHAMPION_REWARDS;
				
		// Set our limits for chance of drop
		if (categoryDropChance < 1)
			categoryDropChance = 1;
		
		// Check if an Item from this category must be dropped
		if (Rnd.get(L2DropData.MAX_CHANCE) < categoryDropChance)
		{
			L2DropData drop = categoryDrops.dropOne(isRaid() && !isRaidMinion());
			
			if (drop == null)
				return null;
			
			// Now decide the quantity to drop based on the rates and penalties.	To get this value
			// simply divide the modified categoryDropChance by the base category chance.	This
			// results in a chance that will dictate the drops amounts: for each amount over 100
			// that it is, it will give another chance to add to the min/max quantities.
			
			// For example, If the final chance is 120%, then the item should drop between
			// its min and max one time, and then have 20% chance to drop again.	If the final
			// chance is 330%, it will similarly give 3 times the min and max, and have a 30%
			// chance to give a 4th time.
			// At least 1 item will be dropped for sure.	So the chance will be adjusted to 100%
			// if smaller.
			
			double dropChance = drop.getChance();
			
			if (Config.RATE_DROP_ITEMS_ID.get(drop.getItemId()) != 0)
			{
				if ((lastAttacker.getPremiumService()==1) && (Config.PREMIUM_RATE_DROP_ITEMS_ID.get(drop.getItemId()) != 0))
					dropChance *= Config.PREMIUM_RATE_DROP_ITEMS_ID.get(drop.getItemId());
				else
					dropChance *= Config.RATE_DROP_ITEMS_ID.get(drop.getItemId());
			}
			
			else
			{
				if (lastAttacker.getPremiumService()==1)
					dropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.PREMIUM_RATE_DROP_ITEMS;
				else
					dropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
			}
			
			if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
				dropChance *= Config.L2JMOD_CHAMPION_REWARDS;
			
			dropChance = Math.round(dropChance);
			
			if (dropChance < L2DropData.MAX_CHANCE)
				dropChance = L2DropData.MAX_CHANCE;
			
			// Get min and max Item quantity that can be dropped in one time
			int min = drop.getMinDrop();
			int max = drop.getMaxDrop();
			
			// Get the item quantity dropped
			int itemCount = 0;
			
			// Count and chance adjustment for high rate servers
			if (dropChance > L2DropData.MAX_CHANCE && !Config.PRECISE_DROP_CALCULATION)
			{
				long multiplier = Math.round(dropChance / L2DropData.MAX_CHANCE);
				
				if (min < max)
					itemCount += Rnd.get(min * multiplier, max * multiplier);
				else if (min == max)
					itemCount += min * multiplier;
				else
					itemCount += multiplier;
				
				dropChance = dropChance % L2DropData.MAX_CHANCE;
			}
			
			// Check if the Item must be dropped
			int random = Rnd.get(L2DropData.MAX_CHANCE);
			while (random < dropChance)
			{
				// Get the item quantity dropped
				if (min < max)
					itemCount += Rnd.get(min, max);
				else if (min == max)
					itemCount += min;
				else
					itemCount++;
				
				// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
				dropChance -= L2DropData.MAX_CHANCE;
			}
			if (Config.L2JMOD_CHAMPION_ENABLE)
				// TODO (April 11, 2009): Find a way not to hardcode these values.
				if ((drop.getItemId() == 57 || (drop.getItemId() >= 6360 && drop.getItemId() <= 6362)) && isChampion())
					itemCount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS;
			
			if (!Config.MULTIPLE_ITEM_DROP && !ItemTable.getInstance().getTemplate(drop.getItemId()).isStackable() && itemCount > 1)
				itemCount = 1;
			
			if (itemCount > 0)
				return new RewardItem(drop.getItemId(), itemCount);
			else if (itemCount == 0 && Config.DEBUG)
				_log.fine("Roll produced no drops.");
		}
		return null;
	}
	
	/**
	 * Calculates the level modifier for drop
	 *
	 * @param lastAttacker The L2PcInstance that has killed the L2Attackable
	 */
	private int calculateLevelModifierForDrop(L2PcInstance lastAttacker)
	{
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES)
				|| (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
		{
			int highestLevel = lastAttacker.getLevel();
			
			// Check to prevent very high level player to nearly kill mob and let low level player do the last hit.
			if (!getAttackByList().isEmpty())
			{
				for (L2Character atkChar: getAttackByList())
					if (atkChar != null && atkChar.getLevel() > highestLevel)
						highestLevel = atkChar.getLevel();
			}
			
			// According to official data (Prima), deep blue mobs are 9 or more levels below players
			if (highestLevel - 9 >= getLevel())
				return ((highestLevel - (getLevel() + 8)) * 9);
		}
		return 0;
	}
	
	private RewardItem calculateCategorizedHerbItem(L2PcInstance lastAttacker, L2DropCategory categoryDrops)
	{
		if (categoryDrops == null)
			return null;
		
		// Get default drop chance for the category (that's the sum of chances for all items in the category)
		// keep track of the base category chance as it'll be used later, if an item is drop from the category.
		// for everything else, use the total "categoryDropChance"
		int basecategoryDropChance = categoryDrops.getCategoryChance() ;
		int categoryDropChance = basecategoryDropChance;
		
		// Applies Drop rates
		switch(categoryDrops.getCategoryType())
		{
			case 0:
				if (Config.ENABLE_DROP_VITALITY_HERBS)
					categoryDropChance *= Config.RATE_DROP_VITALITY_HERBS;
				else
					return null;
				break;
			case 1:
				categoryDropChance *= Config.RATE_DROP_HP_HERBS;
				break;
			case 2:
				categoryDropChance *= Config.RATE_DROP_MP_HERBS;
				break;
			case 3:
				categoryDropChance *= Config.RATE_DROP_SPECIAL_HERBS;
				break;
			default:
				categoryDropChance *= Config.RATE_DROP_COMMON_HERBS;
		}
				
		// Set our limits for chance of drop
		if (categoryDropChance < 1)
			categoryDropChance = 1;
		
		// Check if an Item from this category must be dropped
		if (Rnd.get(L2DropData.MAX_CHANCE) < categoryDropChance)
		{
			L2DropData drop = categoryDrops.dropOne(false);
			
			if (drop == null)
				return null;
			
			// Now decide the quantity to drop based on the rates and penalties.	To get this value
			// simply divide the modified categoryDropChance by the base category chance.	This
			// results in a chance that will dictate the drops amounts: for each amount over 100
			// that it is, it will give another chance to add to the min/max quantities.
			
			// For example, If the final chance is 120%, then the item should drop between
			// its min and max one time, and then have 20% chance to drop again.	If the final
			// chance is 330%, it will similarly give 3 times the min and max, and have a 30%
			// chance to give a 4th time.
			// At least 1 item will be dropped for sure.	So the chance will be adjusted to 100%
			// if smaller.
			
			double dropChance = drop.getChance();
			
			switch(categoryDrops.getCategoryType())
			{
				case 0:
					dropChance *= Config.RATE_DROP_VITALITY_HERBS;
					break;
				case 1:
					dropChance *= Config.RATE_DROP_HP_HERBS;
					break;
				case 2:
					dropChance *= Config.RATE_DROP_MP_HERBS;
					break;
				case 3:
					dropChance *= Config.RATE_DROP_SPECIAL_HERBS;
					break;
				default:
					dropChance *= Config.RATE_DROP_COMMON_HERBS;
			}
			
						
			if (dropChance < L2DropData.MAX_CHANCE)
				dropChance = L2DropData.MAX_CHANCE;
			
			// Get min and max Item quantity that can be dropped in one time
			int min = drop.getMinDrop();
			int max = drop.getMaxDrop();
			
			// Get the item quantity dropped
			int itemCount = 0;
			
			// Count and chance adjustment for high rate servers
			if (dropChance > L2DropData.MAX_CHANCE && !Config.PRECISE_DROP_CALCULATION)
			{
				long multiplier = Math.round(dropChance / L2DropData.MAX_CHANCE);
				
				if (min < max)
					itemCount += Rnd.get(min * multiplier, max * multiplier);
				else if (min == max)
					itemCount += min * multiplier;
				else
					itemCount += multiplier;
				
				dropChance = dropChance % L2DropData.MAX_CHANCE;
			}
			
			// Check if the Item must be dropped
			int random = Rnd.get(L2DropData.MAX_CHANCE);
			while (random < dropChance)
			{
				// Get the item quantity dropped
				if (min < max)
					itemCount += Rnd.get(min, max);
				else if (min == max)
					itemCount += min;
				else
					itemCount++;
				
				// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
				dropChance -= L2DropData.MAX_CHANCE;
			}
			
			if (itemCount > 0)
				return new RewardItem(drop.getItemId(), itemCount);
			else if (itemCount == 0 && Config.DEBUG)
				_log.fine("Roll produced no drops.");
		}
		return null;
	}
	
	public void doItemDrop(L2Character lastAttacker)
	{
		doItemDrop(getTemplate(),lastAttacker);
	}
	
	/**
	 * Manage Base, Quests and Special Events drops of L2Attackable (called by calculateRewards).
	 *
	 * Concept:
	 * During a Special Event all L2Attackable can drop extra Items.
	 * Those extra Items are defined in the table allNpcDateDrops of the EventDroplist.
	 * Each Special Event has a start and end date to stop to drop extra Items automaticaly.
	 *
	 * Actions:
	 * Manage drop of Special Events created by GM for a defined period
	 * Get all possible drops of this L2Attackable from L2NpcTemplate and add it Quest drops
	 * For each possible drops (base + quests), calculate which one must be dropped (random)
	 * Get each Item quantity dropped (random)
	 * Create this or these L2ItemInstance corresponding to each Item Identifier dropped
	 * If the autoLoot mode is actif and if the L2Character that has killed the L2Attackable is a L2PcInstance, Give the item(s) to the L2PcInstance that has killed the L2Attackable
	 * If the autoLoot mode isn't actif or if the L2Character that has killed the L2Attackable is not a L2PcInstance, add this or these item(s) in the world as a visible object at the position where mob was last
	 *
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 */
	public void doItemDrop(L2NpcTemplate npcTemplate, L2Character lastAttacker)
	{
		if (lastAttacker == null)
			return;
		
		L2PcInstance player = lastAttacker.getActingPlayer();
		
		// Don't drop anything if the last attacker or owner isn't L2PcInstance
		if (player == null)
			return;
		
		// level modifier in %'s (will be subtracted from drop chance)
		int levelModifier = calculateLevelModifierForDrop(player);
		
		CursedWeaponsManager.getInstance().checkDrop(this, player);
		
		// now throw all categorized drops and handle spoil.
		if (npcTemplate.getDropData()!=null)
		{
			for(L2DropCategory cat:npcTemplate.getDropData())
			{
				RewardItem item = null;
				if (cat.isSweep())
				{
					// according to sh1ny, seeded mobs CAN be spoiled and swept.
					if ( isSpoil()/* && !isSeeded() */)
					{
						FastList<RewardItem> sweepList = new FastList<RewardItem>();
						
						for(L2DropData drop: cat.getAllDrops() )
						{
							item = calculateRewardItem(player, drop, levelModifier, true);
							if (item == null)
								continue;
							
							if (Config.DEBUG)
								_log.fine("Item id to spoil: " + item.getItemId() + " amount: " + item.getCount());
							sweepList.add(item);
						}
						// Set the table _sweepItems of this L2Attackable
						if (!sweepList.isEmpty())
							_sweepItems = sweepList.toArray(new RewardItem[sweepList.size()]);
					}
				}
				else
				{
					if (isSeeded())
					{
						L2DropData drop = cat.dropSeedAllowedDropsOnly();
						
						if (drop == null)
							continue;
						
						item = calculateRewardItem(player, drop, levelModifier, false);
					}
					else
					{
						item = calculateCategorizedRewardItem(player, cat, levelModifier);
						if ((cat.getCategoryType() == 1) || (cat.getCategoryType() == 2))
						{
							if (Math.round(Rnd.get(3)) == 1) // 33% chance for add drop another item
							{
								RewardItem item2 = null;
								item2 = calculateCategorizedRewardItem(player, cat, levelModifier);
								if ((item != null) && (item2 != null)) //Recalculate, if duplicate
								{
									if (item2.getItemId() == item.getItemId())
										item2 = calculateCategorizedRewardItem(player, cat, levelModifier);
									if (item2 != null)
										if (item2.getItemId() == item.getItemId())
											item2 = null; //if 2-th duplicate, not drop this item
								}
								// Check if the autoLoot mode is active
								if (item2 != null)
								{
									if (player._useAutoLoot)//Config.AUTO_LOOT
										player.doAutoLoot(this, item2); // Give this or these Item(s) to the L2PcInstance that has killed the L2Attackable
									else
										dropItem(player, item2); // drop the item on the ground
								}
							}
						}
					}
					
					if (item != null)
					{
						if (Config.DEBUG)
							_log.fine("Item id to drop: " + item.getItemId() + " amount: " + item.getCount());
		                 // Individual Looting process
						if (Config.L2JMOD_AUTO_LOOT_INDIVIDUAL)
		                  {
		                         switch (item.getItemId())
		                         {
		                         // Adena / Seal stones
		                         case 57:
		                         case 5575:
		                         case 6360:
		                         case 6361:
		                         case 6362:
		                                if (Config.AUTO_LOOT_RAIDS && isRaid() && player._useAutoLoot)
		                                       player.doAutoLoot(this, item);
		                                else if (player._useAutoLoot && !isRaid())
		                                       player.doAutoLoot(this, item);
		                                else
		                                       dropItem(player, item); // drop the item on the
		                         break;
		                        
		                         // Herbs
		                         case 8600:
		                         case 8608:
		                         case 8601:
		                         case 8609:
		                         case 8602:
		                         case 8610:
		                         case 8603:
		                         case 8611:
		                         case 8604:
		                         case 8612:
		                         case 8605:
		                         case 8613:
		                         case 8606:
		                         case 8614:
		                         case 8607:
		                                if (player._useAutoLootHerbs)
		                                       player.doAutoLoot(this, item); // Give this or these Item(s) to the L2PcInstance that has killed the L2Attackable
		                                else
		                                       dropItem(player, item); // drop the item on the
		                         break;
		                        
		                         default:
		                                if (Config.AUTO_LOOT_RAIDS && isRaid() && player._useAutoLoot)
		                                       player.doAutoLoot(this, item); // Give this or these Item(s) to the L2PcInstance that has killed the L2Attackable
		                                else if (player._useAutoLoot && !isRaid())
		                                       player.doAutoLoot(this, item);
		                                else
		                                       dropItem(player, item); // drop the item on the
		                                break;
		                         }
		                  }
                  else if (isFlying() || (!isRaid() && Config.AUTO_LOOT) || (isRaid() && Config.AUTO_LOOT_RAIDS))
							player.doAutoLoot(this, item); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
						else
							dropItem(player, item); // drop the item on the ground
						
						// Broadcast message if RaidBoss was defeated
						if (isRaid() && !isRaidMinion())
						{
							SystemMessage sm;
							sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DIED_DROPPED_S3_S2);
							sm.addCharName(this);
							sm.addItemName(item.getItemId());
							sm.addItemNumber(item.getCount());
							broadcastPacket(sm);
						}
					}
				}
			}
		}
		// Apply Special Item drop with random(rnd) quantity(qty) for champions.
		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion() && (Config.L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE > 0 || Config.L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE > 0))
		{
			int champqty = Rnd.get(Config.L2JMOD_CHAMPION_REWARD_QTY);
			RewardItem item = new RewardItem(Config.L2JMOD_CHAMPION_REWARD_ID,++champqty);
			
			if (player.getLevel() <= getLevel() && (Rnd.get(100) < Config.L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE))
			{
	               if (Config.AUTO_LOOT || isFlying() || player._useAutoLoot)
                       player.addItem("ChampionLoot", item.getItemId(), item.getCount(), this, true); // Give
                // the
                // item(s)
                // to
                // the
                // L2PcInstance that has
                // killed the
                // L2Attackable
                else
                       dropItem(player, item);
			}
		}
		
		//Instant Item Drop :>
		if (getTemplate().dropherbgroup > 0)
		{
			for(L2DropCategory cat : HerbDropTable.getInstance().getHerbDroplist(getTemplate().dropherbgroup))
			{
				RewardItem item = calculateCategorizedHerbItem(player, cat);
				if (item != null)
				{
					// more than one herb cant be auto looted!
					int count = item.getCount();
					if (count > 1)
					{
						item._count = 1;
						for(int i = 0; i < count; i++)
							dropItem(player, item);
					}
					if (isFlying() || Config.AUTO_LOOT_HERBS || (player._useAutoLootHerbs && Config.L2JMOD_AUTO_LOOT_INDIVIDUAL))
						player.addItem("Loot", item.getItemId(), count, this, true);
					else
						dropItem(player, item);
				}
			}
		}
	}
	
	/**
	 * Manage Special Events drops created by GM for a defined period.
	 *
	 * Concept:
	 * During a Special Event all L2Attackable can drop extra Items.
	 * Those extra Items are defined in the table allNpcDateDrops of the EventDroplist.
	 * Each Special Event has a start and end date to stop to drop extra Items automaticaly.
	 *
	 * Actions: <I>If an extra drop must be generated</I>
	 * Get an Item Identifier (random) from the DateDrop Item table of this Event
	 * Get the Item quantity dropped (random)
	 * Create this or these L2ItemInstance corresponding to this Item Identifier
	 * If the autoLoot mode is actif and if the L2Character that has killed the L2Attackable is a L2PcInstance, Give the item(s) to the L2PcInstance that has killed the L2Attackable
	 * If the autoLoot mode isn't actif or if the L2Character that has killed the L2Attackable is not a L2PcInstance, add this or these item(s) in the world as a visible object at the position where mob was last
	 *
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 */
	public void doEventDrop(L2Character lastAttacker)
	{
		if (lastAttacker == null)
			return;
		
		L2PcInstance player = lastAttacker.getActingPlayer();
		
		// Don't drop anything if the last attacker or owner isn't L2PcInstance
		if (player == null)
			return;
		
		if (player.getLevel() - getLevel() > 9)
			return;
		
		// Go through DateDrop of EventDroplist allNpcDateDrops within the date range
		for (DateDrop drop : EventDroplist.getInstance().getAllDrops())
		{
			if (Rnd.get(L2DropData.MAX_CHANCE) < drop.chance)
			{
				RewardItem item = new RewardItem(drop.items[Rnd.get(drop.items.length)], Rnd.get(drop.min, drop.max));
				
				if (Config.AUTO_LOOT || isFlying())
					player.doAutoLoot(this, item); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
				else
					dropItem(player, item); // drop the item on the ground
			}
		}
	}
	
	/**
	 * Drop reward item.
	 */
	public L2ItemInstance dropItem(L2PcInstance lastAttacker, RewardItem item)
	{
		int randDropLim = 70;
		
		L2ItemInstance ditem = null;
		for (int i = 0; i < item.getCount(); i++)
		{
			// Randomize drop position
			int newX = getX() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
			int newY = getY() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
			int newZ = Math.max(getZ(), lastAttacker.getZ()) + 20; // TODO: temp hack, do somethign nicer when we have geodatas
			
			if (ItemTable.getInstance().getTemplate(item.getItemId()) != null)
			{
				// Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
				ditem = ItemTable.getInstance().createItem("Loot", item.getItemId(), item.getCount(), lastAttacker, this);
				ditem.dropMe(this, newX, newY, newZ);
				
				// Add drop to auto destroy item task
				if (!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
				{
					if ((Config.AUTODESTROY_ITEM_AFTER > 0 && ditem.getItemType() != L2EtcItemType.HERB) || (Config.HERB_AUTO_DESTROY_TIME > 0 && ditem.getItemType() == L2EtcItemType.HERB))
						ItemsAutoDestroy.getInstance().addItem(ditem);
				}
				ditem.setProtected(false);
				
				// If stackable, end loop as entire count is included in 1 instance of item
				if (ditem.isStackable() || !Config.MULTIPLE_ITEM_DROP)
					break;
			}
			else
				_log.log(Level.SEVERE, "Item doesn't exist so cannot be dropped. Item ID: " + item.getItemId());
		}
		return ditem;
	}
	
	public L2ItemInstance dropItem(L2PcInstance lastAttacker, int itemId, int itemCount)
	{
		return dropItem(lastAttacker, new RewardItem(itemId, itemCount));
	}
	
	/**
	 * Return the active weapon of this L2Attackable (= null).
	 */
	public L2ItemInstance getActiveWeapon()
	{
		return null;
	}
	
	/**
	 * Return True if the _aggroList of this L2Attackable is Empty.
	 */
	public boolean noTarget()
	{
		return getAggroList().isEmpty();
	}
	
	/**
	 * Return True if the _aggroList of this L2Attackable contains the L2Character.
	 *
	 * @param player The L2Character searched in the _aggroList of the L2Attackable
	 */
	public boolean containsTarget(L2Character player)
	{
		return getAggroList().containsKey(player);
	}
	
	/**
	 * Clear the _aggroList of the L2Attackable.
	 */
	public void clearAggroList()
	{
		getAggroList().clear();
		
		// clear overhit values
		_overhit = false;
		_overhitDamage = 0;
		_overhitAttacker = null;
	}
	
	/**
	 * Return True if a Dwarf use Sweep on the L2Attackable and if item can be spoiled.
	 */
	public boolean isSweepActive()
	{
		return _sweepItems != null;
	}
	
	/**
	 * Return table containing all L2ItemInstance that can be spoiled.
	 */
	public synchronized RewardItem[] takeSweep()
	{
		RewardItem[] sweep = _sweepItems;
		_sweepItems = null;
		return sweep;
	}
	
	/**
	 * Return table containing all L2ItemInstance that can be harvested.
	 */
	public synchronized RewardItem[] takeHarvest()
	{
		RewardItem[] harvest = _harvestItems;
		_harvestItems = null;
		return harvest;
	}
	
	/**
	 * Set the over-hit flag on the L2Attackable.
	 *
	 * @param status The status of the over-hit flag
	 *
	 */
	public void overhitEnabled(boolean status)
	{
		_overhit = status;
	}
	
	/**
	 * Set the over-hit values like the attacker who did the strike and the amount of damage done by the skill.
	 *
	 * @param attacker The L2Character who hit on the L2Attackable using the over-hit enabled skill
	 * @param damage The ammount of damage done by the over-hit enabled skill on the L2Attackable
	 *
	 */
	public void setOverhitValues(L2Character attacker, double damage)
	{
		// Calculate the over-hit damage
		// Ex: mob had 10 HP left, over-hit skill did 50 damage total, over-hit damage is 40
		double overhitDmg = ((getCurrentHp() - damage) * (-1));
		if (overhitDmg < 0)
		{
			// we didn't killed the mob with the over-hit strike. (it wasn't really an over-hit strike)
			// let's just clear all the over-hit related values
			overhitEnabled(false);
			_overhitDamage = 0;
			_overhitAttacker = null;
			return;
		}
		overhitEnabled(true);
		_overhitDamage = overhitDmg;
		_overhitAttacker = attacker;
	}
	
	/**
	 * Return the L2Character who hit on the L2Attackable using an over-hit enabled skill.
	 *
	 * @return L2Character attacker
	 */
	public L2Character getOverhitAttacker()
	{
		return _overhitAttacker;
	}
	
	/**
	 * Return the ammount of damage done on the L2Attackable using an over-hit enabled skill.
	 *
	 * @return double damage
	 */
	public double getOverhitDamage()
	{
		return _overhitDamage;
	}
	
	/**
	 * Return True if the L2Attackable was hit by an over-hit enabled skill.
	 */
	public boolean isOverhit()
	{
		return _overhit;
	}
	
	/**
	 * Activate the absorbed soul condition on the L2Attackable.
	 */
	public void absorbSoul()
	{
		_absorbed = true;
	}
	
	/**
	 * Return True if the L2Attackable had his soul absorbed.
	 */
	public boolean isAbsorbed()
	{
		return _absorbed;
	}
	
	/**
	 * Adds an attacker that successfully absorbed the soul of this L2Attackable into the _absorbersList.
	 *
	 * Params:
	 * attacker - a valid L2PcInstance
	 * condition - an integer indicating the event when mob dies. This should be:
	 * = 0 - "the crystal scatters";
	 * = 1 - "the crystal failed to absorb. nothing happens";
	 * = 2 - "the crystal resonates because you got more than 1 crystal on you";
	 * = 3 - "the crystal cannot absorb the soul because the mob level is too low";
	 * = 4 - "the crystal successfuly absorbed the soul";
	 */
	public void addAbsorber(L2PcInstance attacker)
	{
		// If we have no _absorbersList initiated, do it
		AbsorberInfo ai = _absorbersList.get(attacker.getObjectId());
		
		// If the L2Character attacker isn't already in the _absorbersList of this L2Attackable, add it
		if (ai == null)
		{
			ai = new AbsorberInfo(attacker.getObjectId(), getCurrentHp());
			_absorbersList.put(attacker.getObjectId(), ai);
		}
		else
		{
			ai._objId = attacker.getObjectId();
			ai._absorbedHP = getCurrentHp();
		}
		
		// Set this L2Attackable as absorbed
		absorbSoul();
	}
	
	public void resetAbsorbList()
	{
		_absorbed = false;
		_absorbersList.clear();
	}
	
	public FastMap<Integer, AbsorberInfo> getAbsorbersList()
	{
		return _absorbersList;
	}
	
	/**
	 * Calculate the Experience and SP to distribute to attacker (L2PcInstance, L2SummonInstance or L2Party) of the L2Attackable.
	 *
	 * @param diff The difference of level between attacker (L2PcInstance, L2SummonInstance or L2Party) and the L2Attackable
	 * @param damage The damages given by the attacker (L2PcInstance, L2SummonInstance or L2Party)
	 *
	 */
	private int[] calculateExpAndSp(int diff, int damage, int IsPremium)
	{
		double xp;
		double sp;
		
		if (diff < -5)
			diff = -5; // makes possible to use ALT_GAME_EXPONENT configuration
		
		xp = (double) getExpReward(IsPremium) * damage / getMaxHp();
		if (Config.ALT_GAME_EXPONENT_XP != 0)
			xp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_XP);
		
		sp = (double) getSpReward(IsPremium) * damage / getMaxHp();
		if (Config.ALT_GAME_EXPONENT_SP != 0)
			sp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_SP);
		
		if (Config.ALT_GAME_EXPONENT_XP == 0 && Config.ALT_GAME_EXPONENT_SP == 0)
		{
			if (diff > 5) // formula revised May 07
			{
				double pow = Math.pow((double)5/6, diff-5);
				xp = xp*pow;
				sp = sp*pow;
			}
			
			if (xp <= 0)
			{
				xp = 0;
				sp = 0;
			}
			else if (sp <= 0)
				sp = 0;
		}
		int[] tmp = { (int)xp, (int)sp };
		return tmp;
	}
	
	public long calculateOverhitExp(long normalExp)
	{
		// Get the percentage based on the total of extra (over-hit) damage done relative to the total (maximum) ammount of HP on the L2Attackable
		double overhitPercentage = ((getOverhitDamage() * 100) / getMaxHp());
		
		// Over-hit damage percentages are limited to 25% max
		if (overhitPercentage > 25)
			overhitPercentage = 25;
		
		// Get the overhit exp bonus according to the above over-hit damage percentage
		// (1/1 basis - 13% of over-hit damage, 13% of extra exp is given, and so on...)
		double overhitExp = ((overhitPercentage / 100) * normalExp);
		
		// Return the rounded ammount of exp points to be added to the player's normal exp reward
		long bonusOverhit = Math.round(overhitExp);
		return bonusOverhit;
	}
	
	/**
	 * Return True.
	 */
	@Override
	public boolean isAttackable()
	{
		return true;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		// Clear mob spoil, seed
		setSpoil(false);
		// Clear all aggro char from list
		clearAggroList();
		// Clear Harvester Rewrard List
		_harvestItems = null;
		// Clear mod Seeded stat
		_seeded = false;
		_seedType = 0;
		_seederObjId = 0;
		// Clear overhit value
		overhitEnabled(false);
		
		_sweepItems = null;
		resetAbsorbList();
		
		setWalking();
		
		// check the region where this mob is, do not activate the AI if region is inactive.
		if (!isInActiveRegion())
		{
			if (hasAI())
				getAI().stopAITask();
		}
	}
	
	/**
	 * Return True if this L2NpcInstance has drops that can be sweeped.<BR><BR>
	 */
	public boolean isSpoil()
	{
		return _isSpoil;
	}
	
	/**
	 * Set the spoil state of this L2NpcInstance.<BR><BR>
	 */
	public void setSpoil(boolean isSpoil)
	{
		_isSpoil = isSpoil;
	}
	
	public final int getIsSpoiledBy()
	{
		return _isSpoiledBy;
	}
	
	public final void setIsSpoiledBy(int value)
	{
		_isSpoiledBy = value;
	}
	
	/**
	 * Sets state of the mob to seeded. Paramets needed to be set before.
	 */
	public void setSeeded(L2PcInstance seeder)
	{
		if (_seedType != 0 && _seederObjId == seeder.getObjectId())
			setSeeded(_seedType, seeder.getLevel());
	}
	
	/**
	 * Sets the seed parameters, but not the seed state
	 * @param id	- id of the seed
	 * @param seeder - player who is sowind the seed
	 */
	public void setSeeded(int id, L2PcInstance seeder)
	{
		if (!_seeded)
		{
			_seedType = id;
			_seederObjId = seeder.getObjectId();
		}
	}
	
	private void setSeeded(int id, int seederLvl)
	{
		_seeded = true;
		_seedType = id;
		int count = 1;
		
		Map<Integer, L2Skill> skills = getTemplate().getSkills();
		
		if (skills != null)
		{
			for (int skillId : skills.keySet())
			{
				switch (skillId)
				{
					case 4303: //Strong type x2
						count *= 2;
						break;
					case 4304: //Strong type x3
						count *= 3;
						break;
					case 4305: //Strong type x4
						count *= 4;
						break;
					case 4306: //Strong type x5
						count *= 5;
						break;
					case 4307: //Strong type x6
						count *= 6;
						break;
					case 4308: //Strong type x7
						count *= 7;
						break;
					case 4309: //Strong type x8
						count *= 8;
						break;
					case 4310: //Strong type x9
						count *= 9;
						break;
				}
			}
		}
		
		int diff = (getLevel() - (L2Manor.getInstance().getSeedLevel(_seedType) - 5));
		
		// hi-lvl mobs bonus
		if (diff > 0)
			count += diff;
		
		FastList<RewardItem> harvested = new FastList<RewardItem>();
		
		harvested.add(new RewardItem(L2Manor.getInstance().getCropType(_seedType), count* Config.RATE_DROP_MANOR));
		
		_harvestItems = harvested.toArray(new RewardItem[harvested.size()]);
	}
	
	public int getSeederId()
	{
		return _seederObjId;
	}
	
	public int getSeedType()
	{
		return _seedType;
	}
	
	public boolean isSeeded()
	{
		return _seeded;
	}
	
	/**
	 * Set delay for onKill() call, in ms
	 * Default: 5000 ms
	 * @param delay
	 */
	public final void setOnKillDelay(int delay)
	{
		_onKillDelay = delay;
	}
	
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc instanceof L2MonsterInstance)
		{
			final L2MonsterInstance mob = (L2MonsterInstance)npc;
			if (mob.getLeader() != null)
			{
				final int respawnTime = Config.MINIONS_RESPAWN_TIME.get(mob.getNpcId()) > 0 ? Config.MINIONS_RESPAWN_TIME.get(mob.getNpcId()) * 1000 : -1;
				mob.getLeader().getMinionList().onMinionDie(mob, respawnTime);
			}

			if (mob.hasMinions())
				mob.getMinionList().onMasterDie(false);
		}
		return null;
	}
	
	/**
	 * Check if the server allows Random Animation.
	 */
	// This is located here because L2Monster and L2FriendlyMob both extend this class. The other non-pc instances extend either L2NpcInstance or L2MonsterInstance.
	@Override
	public boolean hasRandomAnimation()
	{
		return ((Config.MAX_MONSTER_ANIMATION > 0) && isRandomAnimationEnabled() && !(this instanceof L2GrandBossInstance));
	}
	
	@Override
	public boolean isMob()
	{
		return true; // This means we use MAX_MONSTER_ANIMATION instead of MAX_NPC_ANIMATION
	}
	
	protected void setCommandChannelTimer(CommandChannelTimer commandChannelTimer)
	{
		_commandChannelTimer = commandChannelTimer;
	}
	
	public CommandChannelTimer getCommandChannelTimer()
	{
		return _commandChannelTimer;
	}
	
	public L2CommandChannel getFirstCommandChannelAttacked()
	{
		return _firstCommandChannelAttacked;
	}
	
	public void setFirstCommandChannelAttacked(L2CommandChannel firstCommandChannelAttacked)
	{
		_firstCommandChannelAttacked = firstCommandChannelAttacked;
	}
	
	/**
	 * @return the _commandChannelLastAttack
	 */
	public long getCommandChannelLastAttack()
	{
		return _commandChannelLastAttack;
	}
	
	/**
	 * @param channelLastAttack the _commandChannelLastAttack to set
	 */
	public void setCommandChannelLastAttack(long channelLastAttack)
	{
		_commandChannelLastAttack = channelLastAttack;
	}
	
	private static class CommandChannelTimer implements Runnable
	{
		private L2Attackable _monster;
		
		public CommandChannelTimer(L2Attackable monster)
		{
			_monster = monster;
		}
		
		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			if ((System.currentTimeMillis() - _monster.getCommandChannelLastAttack()) > Config.LOOT_RAIDS_PRIVILEGE_INTERVAL)
			{
				_monster.setCommandChannelTimer(null);
				_monster.setFirstCommandChannelAttacked(null);
				_monster.setCommandChannelLastAttack(0);
			}
			else
				ThreadPoolManager.getInstance().scheduleGeneral(this, 10000); // 10sec
		}
	}
	
	public void returnHome()
	{
		clearAggroList();
		
		if (hasAI() && getSpawn() != null)
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz(), 0));
	}
	
	/*
	 * Return vitality points decrease (if positive)
	 * or increase (if negative) based on damage.
	 * Maximum for damage = maxHp.
	 */
	public float getVitalityPoints(int damage)
	{
		// sanity check
		if (damage <= 0)
			return 0;
		
		final float divider = getTemplate().baseVitalityDivider;
		if (divider == 0)
			return 0;
		
		// negative value - vitality will be consumed
		return - Math.min(damage, getMaxHp()) / divider;
	}
	
	/*
	 * True if vitality rate for exp and sp should be applied
	 */
	public boolean useVitalityRate()
	{
		if (isChampion() && !Config.L2JMOD_CHAMPION_ENABLE_VITALITY)
			return false;
		
		return true;
	}
	
	/** Return True if the L2Character is RaidBoss or his minion. */
	@Override
	public boolean isRaid()
	{
		return _isRaid;
	}
	
	/**
	 * Set this Npc as a Raid instance.<BR><BR>
	 * @param isRaid
	 */
	public void setIsRaid(boolean isRaid)
	{
		_isRaid = isRaid;
	}
	
	/**
	 * Set this Npc as a Minion instance.<BR><BR>
	 * @param val
	 */
	public void setIsRaidMinion(boolean val)
	{
		_isRaid = val;
		_isRaidMinion = val;
	}
	
	@Override
	public boolean isRaidMinion()
	{
		return _isRaidMinion;
	}
	
	@Override
	public boolean isMinion()
	{
		return getLeader() != null;
	}

	/**
	 * Return leader of this minion or null.
	 */
	public L2Attackable getLeader()
	{
		return null;
	}

	public void setChampion(boolean champ)
	{
		_champion = champ;
	}
	
	@Override
	public boolean isChampion()
	{
		return _champion;
	}
	//1 Min Counter and the event that happens
	class CaptchaTimer implements Runnable{
		L2PcInstance activeChar;
		public CaptchaTimer(L2PcInstance player){
			activeChar = player;
		}
		public void run(){
			//here will be code that will run after 1 min
			if(!activeChar.isCodeRight()){
				//here will run method with jailing player after 1 min
				activeChar.stopAbnormalEffect(AbnormalEffect.REAL_TARGET);
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
				npcHtmlMessage.setHtml("<html><title>Captcha Antibot System</title><body><center><font color=\"FF0000\">60 Seconds Passed.<br><br></font><font color=\"66FF00\"><center></font><font color=\"FF0000\">You will be jailed for 1 min.</font><br><button value=\"Exit\" action=\"bypass -h npc_%objectId%_Quest\" width=45 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
				if (activeChar.isFlyingMounted())
					activeChar.untransform();
				activeChar.setPunishLevel(L2PcInstance.PunishLevel.JAIL, 1);
				activeChar.setIsInvul(false);
				activeChar.setIsParalyzed(false);
				activeChar.sendPacket(npcHtmlMessage);
			}
		}
	}
}