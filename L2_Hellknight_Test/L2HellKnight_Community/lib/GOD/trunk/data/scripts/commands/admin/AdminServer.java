package commands.admin;

import java.io.File;
import java.io.FileInputStream;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.ai.L2CharacterAI;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.IAdminCommandHandler;
import l2rt.gameserver.instancemanager.ServerVariables;
import l2rt.gameserver.loginservercon.LSConnection;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.L2WorldRegion;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.instances.L2RaidBossInstance;
import l2rt.gameserver.model.instances.L2NpcInstance.AggroInfo;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.GArray;

/**
 * This class handles following admin commands: - help path = shows
 * /data/html/admin/path file to char, should not be used by GM's directly
 */
public class AdminServer extends Functions implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_server,
		admin_gc,
		admin_test,
		admin_pstat,
		admin_check_actor,
		admin_find_broken_ai,
		admin_setvar,
		admin_set_ai_interval,
		admin_spawn2
	}

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().Menu)
			return false;

		switch(command)
		{
			case admin_server:
				try
				{
					String val = fullString.substring(13);
					showHelpPage(activeChar, val);
				}
				catch(StringIndexOutOfBoundsException e)
				{
					// case of empty filename
				}
				break;
			case admin_gc:
				try
				{
					System.gc();
				}
				catch(Exception e)
				{}
				activeChar.sendMessage("OK! - garbage collector called.");
				break;
			case admin_test:
				StringTokenizer st = new StringTokenizer(fullString);
				st.nextToken(); //skip command

				//String val1 = null;
				//if(st.hasMoreTokens())
				//	val1 = st.nextToken();
				//String val2 = null;
				//if(st.hasMoreTokens())
				//	val2 = st.nextToken();

				// Сюда пихать тестовый код
				try
				{
					//activeChar.sendMessage(activeChar.getTarget().getClass().getName());
					/*
					Location target = activeChar.getLoc();
					target.x += 100;
					activeChar.broadcastPacket(new ExJumpToLocation(activeChar.getObjectId(), activeChar.getLoc(), target));
					*/

					//((DefaultAI) activeChar.getTarget().getAI()).DebugTasks();

					/*
					// Пример вызова методов через рефлекшн из соседнего скрипта
					Field field = Scripts.getInstance().getClasses().get("bosses.FrintezzaManager").getRawClass().getDeclaredField("_state");
					field.setAccessible(true);
					Object state = field.get(null);

					Class<?> state2 = Scripts.getInstance().getClasses().get("bosses.EpicBossState").getRawClass().getClasses()[0];
					Method setState = state.getClass().getMethod("setState", state2);
					setState.invoke(state, state2.getEnumConstants()[0]);

					Method update = state.getClass().getMethod("update");
					update.invoke(state);
					*/

					/**
					 * Для UserInfo:
					 * 128-191: Щит на синем фоне
					 * 192-255: Корона на синем фоне
					 * 384-447: Меч на синем фоне
					 * 448-511: Флаг на синем фоне
					 * 640-703: Щит на синем фоне
					 * 704-767: Корона на синем фоне
					 * 896-959: Меч на синем фоне
					 * 960-1023: Флаг на синем фоне
					 * 1152-1215: Щит на синем фоне
					 * 1216-1279: Корона на синем фоне
					 * ...
					 * Для RelationChanged:
					 * 32640-32767: Флаг на синем фоне
					 * 32768-xxx: Меч на фиолетовом фоне в шестиграннике (односторонний кланвар)
					 * 0x80000(524288): Территория Глудио
					 */

					/*
					if(wordList.length > 1)
						Config.RELATION = Integer.parseInt(wordList[1]);
					if(activeChar.getTarget() != null && activeChar.getTarget().isPlayable())
						activeChar.sendPacket(new RelationChanged(activeChar.getTarget().getPlayer(), true, Config.RELATION));
					activeChar.sendUserInfo(true);
					String html = "<center>Current relation: 0x" + Integer.toHexString(Config.RELATION) + " int:(" + Config.RELATION + ")<br>";
					html += "<button width=150 height=30 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h admin_test " + (Config.RELATION + 1) + "\" value=\"NEXT RELATION >>>\">";
					html += "<button width=150 height=30 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h admin_test " + (Config.RELATION - 1) + "\" value=\"<<< PREV RELATION\">";
					html += "</center>";
					show(html, activeChar);
					*/

					/*
					MMOConnection.getPool().shutdownNow(); // убиваем старый пул

					// получаем через рефлект старый пул в виде поля
					java.lang.reflect.Field interestPool = Class.forName("l2rt.extensions.network.MMOConnection").getDeclaredField("interestPool");
					interestPool.setAccessible(true);

					// получаем адрес поля модификаторов в старом пуле
					java.lang.reflect.Field modifiers = Class.forName("java.lang.reflect.Field").getDeclaredField("modifiers");
					modifiers.setAccessible(true);

					// снимаем с него final и ставим паблик
					modifiers.setInt(interestPool, interestPool.getModifiers() ^ Modifier.FINAL);
					modifiers.setInt(interestPool, interestPool.getModifiers() ^ Modifier.PRIVATE);
					modifiers.setInt(interestPool, interestPool.getModifiers() | Modifier.PUBLIC);

					// заминяем accessor на свой, которому будет пофиг на финал
					java.lang.reflect.Field accessorField = Class.forName("java.lang.reflect.Field").getDeclaredField("overrideFieldAccessor");
					accessorField.setAccessible(true);
					java.lang.reflect.Constructor<?> con1 = Class.forName("sun.reflect.UnsafeQualifiedStaticObjectFieldAccessorImpl").getDeclaredConstructor(java.lang.reflect.Field.class, boolean.class);
					con1.setAccessible(true);
					accessorField.set(interestPool, con1.newInstance(interestPool, false));

					// заменяем таки старый пул на новый
					interestPool.set(null, new ScheduledThreadPoolExecutor(Config.INTEREST_MAX_THREAD, new PriorityThreadFactory("InterestManagerPool", 4)));

					// запускаем в новом пуле таски для игроков
					java.lang.reflect.Constructor<?> con = Class.forName("l2rt.extensions.network.MMOConnection$ScheduleInterest").getDeclaredConstructor(Class.forName("l2rt.extensions.network.MMOConnection"));
					con.setAccessible(true);
					for(L2Player p : L2ObjectsStorage.getAllPlayersForIterate())
						if(p.getNetConnection() != null)
							MMOConnection.getPool().scheduleWithFixedDelay((Runnable) con.newInstance(p.getNetConnection().getConnection()), 10, 50, TimeUnit.MILLISECONDS);
					*/
					/*
					for(L2ItemInstance item : ((L2Player) activeChar.getTarget()).getInventory().getItemsList())
					{
						int id = item.getItemId();

						if(id == 13560 || id == 13561 || id == 13562 || id == 13563 || id == 13564 || id == 13565 || id == 13566 || id == 13567 || id == 13568)
						{
							L2TerritoryFlagInstance flagNpc = TerritorySiege.getNpcFlagByItemId(id);
							flagNpc.drop((L2Player) activeChar.getTarget());

							((L2Player) activeChar.getTarget()).sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_DROPPED_S1).addItemName(id));
							String terrName = CastleManager.getInstance().getCastleByIndex(flagNpc.getBaseTerritoryId()).getName();
							TerritorySiege.announceToPlayer(new SystemMessage(SystemMessage.THE_CHARACTER_THAT_ACQUIRED_S1_WARD_HAS_BEEN_KILLED).addString(terrName), true);
						}
					}
					*/

					/*
					L2WorldRegion curRegion = activeChar.getCurrentRegion();
					System.out.println("*** Regions dump around region: " + curRegion.getName());
					for(L2WorldRegion wr : curRegion.getNeighbors())
						System.out.println("Region " + wr.getName() + " is " + (!wr.isActive() ? "NOT " : "") + "active");
					*/

					/*
					setSocial(22691, "ragna_orc", 200);
					setSocial(22692, "ragna_orc", 200);
					setSocial(22693, "ragna_orc", 200);
					setSocial(22694, "ragna_orc", 200);
					setSocial(22695, "ragna_orc", 200);
					setSocial(22696, "ragna_orc", 200);
					setSocial(22697, "ragna_orc", 200);
					setSocial(22698, "ragna_orc", 200);
					setSocial(22699, "ragna_orc", 200);
					
					setSocial(22700, "den_of_evil", 300);
					setSocial(22701, "den_of_evil", 300);
					setSocial(22702, "den_of_evil", 300);
					*/

					//activeChar.sendPacket(new ExShowScreenMessage("Test!!!", 3000, ScreenMessageAlign.TOP_CENTER, true, 0, SystemMessage.C1_HAS_RESISTED_YOUR_S2, false));

					/*
					org.rrd4j.core.RrdDb rdb = new org.rrd4j.core.RrdDb("./config/extended.rrd");
					boolean already = rdb.getArchive(org.rrd4j.ConsolFun.AVERAGE, 1) != null;
					rdb.close();

					if(already)
					{
						activeChar.sendMessage("Already converted.");
						throw new Exception("Already converted.");
					}

					org.rrd4j.core.RrdToolkit.resizeArchive("./config/extended.rrd", org.rrd4j.ConsolFun.AVERAGE, 10, 4032, false);
					org.rrd4j.core.RrdToolkit.resizeArchive("./config/extended.rrd", org.rrd4j.ConsolFun.MAX, 10, 4032, false);
					org.rrd4j.core.RrdToolkit.resizeArchive("./config/extended.rrd", org.rrd4j.ConsolFun.AVERAGE, 60, 8064, false);
					org.rrd4j.core.RrdToolkit.resizeArchive("./config/extended.rrd", org.rrd4j.ConsolFun.MAX, 60, 8064, false);
					org.rrd4j.core.RrdToolkit.resizeArchive("./config/extended.rrd", org.rrd4j.ConsolFun.AVERAGE, 1440, 1008, false);
					org.rrd4j.core.RrdToolkit.resizeArchive("./config/extended.rrd", org.rrd4j.ConsolFun.MAX, 1440, 1008, false);

					java.io.FileWriter save = null;
					try
					{
						new java.io.File("./config/extended.xml").delete();
						save = new java.io.FileWriter("./config/extended.xml", true);
						rdb = new org.rrd4j.core.RrdDb("./config/extended.rrd");
						save.write(rdb.getXml().replaceFirst("<step>600</step>", "<step>60</step>").replaceAll("<pdp_per_row>10</pdp_per_row>", "<pdp_per_row>100</pdp_per_row>").replaceAll("<pdp_per_row>60</pdp_per_row>", "<pdp_per_row>600</pdp_per_row>").replaceAll("<pdp_per_row>1440</pdp_per_row>", "<pdp_per_row>14400</pdp_per_row>"));
						rdb.close();
					}
					catch(java.io.IOException e)
					{
						e.printStackTrace();
					}
					finally
					{
						try
						{
							if(save != null)
								save.close();
						}
						catch(Exception e1)
						{}
					}
					new org.rrd4j.core.RrdDb("./config/extended.rrd", org.rrd4j.core.RrdDb.PREFIX_XML + "./config/extended.xml").close();
					new java.io.File("./config/extended.xml").delete();

					org.rrd4j.core.RrdToolkit.addArchive("./config/extended.rrd", new org.rrd4j.core.ArcDef(org.rrd4j.ConsolFun.AVERAGE, 0.5, 1, 1440), false);
					org.rrd4j.core.RrdToolkit.addArchive("./config/extended.rrd", new org.rrd4j.core.ArcDef(org.rrd4j.ConsolFun.MAX, 0.5, 1, 1440), false);
					org.rrd4j.core.RrdToolkit.setDsHeartbeat("./config/extended.rrd", "adena", 120);
					org.rrd4j.core.RrdToolkit.setDsHeartbeat("./config/extended.rrd", "avglevel", 120);
					org.rrd4j.core.RrdToolkit.removeArchive("./config/extended.rrd", org.rrd4j.ConsolFun.AVERAGE, 10080, false);
					org.rrd4j.core.RrdToolkit.removeArchive("./config/extended.rrd", org.rrd4j.ConsolFun.MAX, 10080, false);

					org.rrd4j.core.RrdToolkit.setDsHeartbeat("./config/main.rrd", "online", 120);
					org.rrd4j.core.RrdToolkit.resizeArchive("./config/main.rrd", org.rrd4j.ConsolFun.AVERAGE, 1440, 3360, false);
					org.rrd4j.core.RrdToolkit.resizeArchive("./config/main.rrd", org.rrd4j.ConsolFun.MAX, 1440, 3360, false);

					org.rrd4j.core.RrdToolkit.setDsHeartbeat("./config/memory.rrd", "memory", 120);
					org.rrd4j.core.RrdToolkit.resizeArchive("./config/memory.rrd", org.rrd4j.ConsolFun.AVERAGE, 1440, 3360, false);
					org.rrd4j.core.RrdToolkit.resizeArchive("./config/memory.rrd", org.rrd4j.ConsolFun.MAX, 1440, 3360, false);
					activeChar.sendMessage("Successfully converted.");
					*/
					LSConnection.getInstance().restart();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				// тут тестовый код кончается

				activeChar.sendMessage("Test.");
				break;
			case admin_pstat:
				if(wordList.length == 2 && wordList[1].equals("on"))
					activeChar.packetsCount = true;
				else if(wordList.length == 2 && wordList[1].equals("off"))
				{
					activeChar.packetsCount = false;
					activeChar.packetsStat = null;
				}
				else if(activeChar.packetsCount)
				{
					activeChar.packetsCount = false;
					for(Entry<String, Integer> entry : activeChar.packetsStat.entrySet())
						activeChar.sendMessage(entry.getValue() + " : " + entry.getKey());
					activeChar.packetsCount = true;
				}
				break;
			case admin_check_actor:
				L2Object target = activeChar.getTarget();
				if(target == null)
				{
					activeChar.sendMessage("target == null");
					return false;
				}

				if(!target.isCharacter())
				{
					activeChar.sendMessage("target is not a character");
					return false;
				}

				L2CharacterAI ai = target.getAI();
				if(ai == null)
				{
					activeChar.sendMessage("ai == null");
					return false;
				}

				L2Character actor = ai.getActor();
				if(actor == null)
				{
					activeChar.sendMessage("actor == null");
					return false;
				}

				activeChar.sendMessage("actor: " + actor);
				break;
			case admin_find_broken_ai:
				for(L2NpcInstance npc : L2ObjectsStorage.getAllNpcsForIterate())
					if(npc.getAI().getActor() != npc)
					{
						activeChar.sendMessage("type 1");
						activeChar.teleToLocation(npc.getLoc());
						return true;
					}
					else if(!npc.isVisible())
					{
						L2WorldRegion region = L2World.getRegion(npc);
						if(region != null && region.getNpcsList(new GArray<L2NpcInstance>(region.getObjectsSize()), 0, npc.getReflection().getId()).contains(npc))
						{
							activeChar.sendMessage("type 2");
							activeChar.teleToLocation(npc.getLoc());
							return true;
						}

						L2WorldRegion currentRegion = npc.getCurrentRegion();
						if(currentRegion != null)
						{
							activeChar.sendMessage("type 3");
							activeChar.teleToLocation(npc.getLoc());
							return true;
						}
					}
					else if(npc.isDead())
						for(AggroInfo aggro : npc.getAggroMap().values())
							if(aggro.damage > 0 && aggro.hate == 0 && aggro.attacker != null && !aggro.attacker.isDead())
							{
								activeChar.sendMessage("type 4");
								activeChar.teleToLocation(npc.getLoc());
								return true;
							}
				break;
			case admin_setvar:
				if(wordList.length != 3)
				{
					activeChar.sendMessage("Incorrect argument count!!!");
					return false;
				}
				ServerVariables.set(wordList[1], wordList[2]);
				activeChar.sendMessage("Value changed.");
				break;
			case admin_set_ai_interval:
				if(wordList.length != 2)
				{
					activeChar.sendMessage("Incorrect argument count!!!");
					return false;
				}
				int interval = Integer.parseInt(wordList[1]);
				int count = 0;
				int count2 = 0;
				for(final L2NpcInstance npc : L2ObjectsStorage.getAllNpcsForIterate())
				{
					if(npc == null || npc instanceof L2RaidBossInstance)
						continue;
					final L2CharacterAI char_ai = npc.getAI();
					if(char_ai instanceof DefaultAI)
						try
						{
							final java.lang.reflect.Field field = l2rt.gameserver.ai.DefaultAI.class.getDeclaredField("AI_TASK_DELAY");
							field.setAccessible(true);
							field.set(char_ai, interval);

							if(char_ai.isActive())
							{
								char_ai.stopAITask();
								char_ai.teleportHome(true);
								count++;
								L2WorldRegion region = npc.getCurrentRegion();
								if(region != null && !region.areNeighborsEmpty())
								{
									char_ai.startAITask();
									count2++;
								}
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
				}
				activeChar.sendMessage(count + " AI stopped, " + count2 + " AI started");
				break;
			case admin_spawn2: // Игнорирует запрет на спавн рейдбоссов
				st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					String id = st.nextToken();
					int respawnTime = 30;
					int mobCount = 1;
					if(st.hasMoreTokens())
						mobCount = Integer.parseInt(st.nextToken());
					if(st.hasMoreTokens())
						respawnTime = Integer.parseInt(st.nextToken());
					spawnMonster(activeChar, id, respawnTime, mobCount);
				}
				catch(Exception e)
				{}
				break;
		}

		return true;
	}

	public void setSocial(int npcId, String faction, int range)
	{
		try
		{
			L2NpcTemplate t = NpcTable.getTemplate(npcId);

			java.lang.reflect.Field factionId = t.getClass().getDeclaredField("factionId");
			factionId.setAccessible(true);
			factionId.set(t, faction);

			java.lang.reflect.Field factionRange = t.getClass().getDeclaredField("factionRange");
			factionRange.setAccessible(true);
			factionRange.set(t, (short) range);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	// PUBLIC & STATIC so other classes from package can include it directly
	public static void showHelpPage(L2Player targetChar, String filename)
	{
		File file = new File("./", "data/html/admin/" + filename);
		FileInputStream fis = null;

		try
		{
			fis = new FileInputStream(file);
			byte[] raw = new byte[fis.available()];
			fis.read(raw);

			String content = new String(raw, "UTF-8");

			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

			adminReply.setHtml(content);
			targetChar.sendPacket(adminReply);
		}
		catch(Exception e)
		{}
		finally
		{
			try
			{
				if(fis != null)
					fis.close();
			}
			catch(Exception e1)
			{}
		}
	}

	private void spawnMonster(L2Player activeChar, String monsterId, int respawnTime, int mobCount)
	{
		L2Object target = activeChar.getTarget();
		if(target == null)
			target = activeChar;

		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher regexp = pattern.matcher(monsterId);
		L2NpcTemplate template;
		if(regexp.matches())
		{
			// First parameter was an ID number
			int monsterTemplate = Integer.parseInt(monsterId);
			template = NpcTable.getTemplate(monsterTemplate);
		}
		else
		{
			// First parameter wasn't just numbers so go by name not ID
			monsterId = monsterId.replace('_', ' ');
			template = NpcTable.getTemplateByName(monsterId);
		}

		if(template == null)
		{
			activeChar.sendMessage("Incorrect monster template.");
			return;
		}

		try
		{
			L2Spawn spawn = new L2Spawn(template);
			spawn.setLoc(target.getLoc());
			spawn.setLocation(0);
			spawn.setAmount(mobCount);
			spawn.setHeading(activeChar.getHeading());
			spawn.setRespawnDelay(respawnTime);
			spawn.setReflection(activeChar.getReflection().getId());
			spawn.init();
			if(respawnTime == 0)
				spawn.stopRespawn();
			activeChar.sendMessage("Created " + template.name + " on " + target.getObjectId() + ".");
		}
		catch(Exception e)
		{
			activeChar.sendMessage("Target is not ingame.");
		}
	}

	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}