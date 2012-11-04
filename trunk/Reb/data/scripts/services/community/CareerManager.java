package services.community;

import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SubClass;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.ClassType;
import l2r.gameserver.model.base.PlayerClass;
import l2r.gameserver.model.base.Race;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.HtmlUtils;
import l2r.gameserver.utils.Language;
import l2r.gameserver.utils.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CareerManager implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CareerManager.class);
	
	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED/* && Config.BBS_PVP_CB_ENABLED*/)
		{
			_log.info("CommunityBoard: Manage Career service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED/* && Config.BBS_PVP_CB_ENABLED*/)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown()
	{}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_bbscareer;", "_bbscareer;sub;", "_bbscareer;classmaster;change_class;" };
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onBypassCommand(Player activeChar, String command)
	{
		if(!CheckCondition(activeChar))
			return;

		if(command.startsWith("_bbscareer;"))
		{
			ClassId classId = activeChar.getClassId();
			int jobLevel = classId.getLevel();
			int level = activeChar.getLevel();
			StringBuilder html = new StringBuilder();
			html.append("<br>");
			html.append("<table width=600>");
			html.append("<tr><td>");
			if(Config.ALLOW_CLASS_MASTERS_LIST.isEmpty() || !Config.ALLOW_CLASS_MASTERS_LIST.contains(jobLevel))
				jobLevel = 4;

			if((level >= 20 && jobLevel == 1 || level >= 40 && jobLevel == 2 || level >= 76 && jobLevel == 3) && Config.ALLOW_CLASS_MASTERS_LIST.contains(jobLevel))
			{
				ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.CLASS_MASTERS_PRICE_ITEM);
				if(activeChar.getLanguage() == Language.ENGLISH)
				{
					html.append("You have to pay: <font color=\"LEVEL\">");
					html.append(Util.formatAdena(Config.CLASS_MASTERS_PRICE_LIST[jobLevel])).append("</font> <font color=\"LEVEL\">").append(item.getName()).append("</font> to change profession<br>");						
				}
				else
				{
					html.append("Вы должны заплатить: <font color=\"LEVEL\">");
					html.append(Util.formatAdena(Config.CLASS_MASTERS_PRICE_LIST[jobLevel])).append("</font> <font color=\"LEVEL\">").append(item.getName()).append("</font> для смены профессии<br>");
				}
				html.append("<center><table width=600><tr>");
				for(ClassId cid : ClassId.values())
				{
					if(cid == ClassId.inspector)
						continue;
					if(cid.childOf(classId) && cid.level() == classId.level() + 1)
						html.append("<td><center><button value=\"").append(cid.name()).append("\" action=\"bypass _bbscareer;classmaster;change_class;").append(cid.getId()).append(";").append(Config.CLASS_MASTERS_PRICE_LIST[jobLevel]).append("\" width=150 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");

				}
				html.append("</tr></table></center>");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table>");
			}
			else
			{
				switch(jobLevel)
				{
					case 1:
						if(activeChar.getLanguage() == Language.ENGLISH)
						{
							html.append("Greetings <font color=F2C202>" + activeChar.getName() + "</font> your current profession <font color=F2C202>" + activeChar.getClassId().name() + "</font><br>");
							html.append("To change your profession you have to reach: <font color=F2C202>level 20</font><br>");
							html.append("To activate the subclass you have to reach <font color=F2C202>level 75</font><br>");
							html.append("To become a nobleman, you have to bleed to subclass <font color=F2C202>level 76</font><br>");					
						}
						else
						{
							html.append("Приветствую <font color=F2C202>" + activeChar.getName() + "</font> ваша текущая профессия <font color=F2C202>" + activeChar.getClassId().name() + "</font><br>");
							html.append("Для того чтобы сменить вашу профессию вы должны достичь: <font color=F2C202>20-го уровня</font><br>");
							html.append("Для активации сабклассов вы должны достичь <font color=F2C202>75-го уровня</font><br>");
							html.append("Чтобы стать дворянином вы должны прокачать сабкласс до <font color=F2C202>76-го уровня</font><br>");
						}
						html.append(getSubClassesHtml(activeChar, true));
						break;
					case 2:
						if(activeChar.getLanguage() == Language.ENGLISH)
						{
							html.append("Greetings <font color=F2C202>" + activeChar.getName() + "</font> your current profession <font color=F2C202>" + activeChar.getClassId().name() + "</font><br>");
							html.append("To change your profession you have to reach: <font color=F2C202>level 40</font><br>");
							html.append("To activate the subclass you have to reach <font color=F2C202>level 75</font><br>");
							html.append("To become a nobleman, you have to bleed to subclass <font color=F2C202>7level 76</font><br>");						
						}
						else
						{
							html.append("Приветствую <font color=F2C202>" + activeChar.getName() + "</font> ваша текущая профессия <font color=F2C202>" + activeChar.getClassId().name() + "</font><br>");
							html.append("Для того чтобы сменить вашу профессию вы должны достичь: <font color=F2C202>40-го уровня</font><br>");
							html.append("Для активации сабклассов вы должны достичь <font color=F2C202>75-го уровня</font><br>");
							html.append("Чтобы стать дворянином вы должны прокачать сабкласс до <font color=F2C202>76-го уровня</font><br>");
						}
						html.append(getSubClassesHtml(activeChar, true));
						break;
					case 3:
						if(activeChar.getLanguage() == Language.ENGLISH)
						{
							html.append("Greetings <font color=F2C202>" + activeChar.getName() + "</font> your current profession <font color=F2C202>" + activeChar.getClassId().name() + "</font><br>");
							html.append("To change your profession you have to reach: <font color=F2C202>level 76</font><br>");
							html.append("To activate the subclass you have to reach <font color=F2C202>level 75</font><br>");
							html.append("To become a nobleman, you have to bleed to subclass <font color=F2C202>level 76</font><br>");						
						}
						else
						{
							html.append("Приветствую <font color=F2C202>" + activeChar.getName() + "</font> ваша текущая профессия <font color=F2C202>" + activeChar.getClassId().name() + "</font><br>");
							html.append("Для того чтобы сменить вашу профессию вы должны достичь: <font color=F2C202>76-го уровня</font><br>");
							html.append("Для активации сабклассов вы должны достичь <font color=F2C202>75-го уровня</font><br>");
							html.append("Чтобы стать дворянином вы должны прокачать сабкласс до <font color=F2C202>76-го уровня</font><br>");
						}
						html.append(getSubClassesHtml(activeChar, true));
						break;
					case 4:
						if(activeChar.getLanguage() == Language.ENGLISH)
						{
							html.append("Greetings <font color=F2C202>" + activeChar.getName() + "</font> your current profession <font color=F2C202>" + activeChar.getClassId().name() + "</font><br>");
							html.append("For you are no more jobs available, or the master class is not currently available.<br>");
							if(level >= 76)
							{
								html.append("You have reached the <font color=F2C202>level 75</font> activation of the subclass is now available<br>");
								if(!activeChar.isNoble() && activeChar.getSubLevel() < 75)
								{
									html.append("You can get the nobility only after your sub-class reaches the 76 level.<br>");
								}
								else if(!activeChar.isNoble() && activeChar.getSubLevel() > 75)
								{
									html.append("You can get the nobility. Your sub-class has reached the 76th level.<br>");
								}
								else if(activeChar.isNoble())
								{
									html.append("You have a gentleman. Getting the nobility no longer available.<br>");
								}							
							}					
						}
						else
						{
							html.append("Приветствую <font color=F2C202>" + activeChar.getName() + "</font> ваша текущая профессия <font color=F2C202>" + activeChar.getClassId().name() + "</font><br>");
							html.append("Для вас больше нет доступных профессий, либо Класс мастер в данный момент недоступен.<br>");
							if(level >= 76)
							{
								html.append("Вы достигли <font color=F2C202>75-го уровня</font> активация сабклассов теперь доступна<br>");
								if(!activeChar.isNoble() && activeChar.getSubLevel() < 75)
								{
									html.append("Вы можете получить дворянство только после того как ваш саб-класс достигнет 76-го уровня.<br>");
								}
								else if(!activeChar.isNoble() && activeChar.getSubLevel() > 75)
								{
									html.append("Вы можете получить дворянство. Ваш саб-класс достиг 76-го уровня.<br>");
								}
								else if(activeChar.isNoble())
								{
									html.append("Вы уже дворянин. Получение дворянства более не доступно.<br>");
								}							
							}
						}
						html.append(getSubClassesHtml(activeChar, true));
						break;
				}
			}
			String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/career.htm", activeChar);
			content = content.replace("%career%", html.toString());
			ShowBoard.separateAndSend(content, activeChar);
		}
		if(command.startsWith("_bbscareer;sub;"))
		{
			if(activeChar.getPet() != null)
			{
				activeChar.sendPacket(SystemMsg.A_SUBCLASS_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SERVITOR_OR_PET_IS_SUMMONED);
				return;
			}

			// Саб класс нельзя получить или поменять, пока используется скилл или персонаж находится в режиме трансформации
			if(activeChar.isActionsDisabled() || activeChar.getTransformation() != 0)
			{
				activeChar.sendPacket(SystemMsg.SUBCLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE);
				return;
			}

			if(activeChar.getWeightPenalty() >= 3)
			{
				activeChar.sendPacket(SystemMsg.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_WHILE_YOU_ARE_OVER_YOUR_WEIGHT_LIMIT);
				return;
			}

			if(activeChar.getInventoryLimit() * 0.8 < activeChar.getInventory().getSize())
			{
				activeChar.sendPacket(SystemMsg.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_BECAUSE_YOU_HAVE_EXCEEDED_YOUR_INVENTORY_LIMIT);
				return;
			}
			
			StringBuilder html = new StringBuilder();

			Map<Integer, SubClass> playerClassList = activeChar.getSubClasses();
			Set<PlayerClass> subsAvailable;

			if(activeChar.getLevel() < 40)
			{
				html.append("You must be level 40 or more to operate with your sub-classes.");
				String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/career.htm", activeChar);
				content = content.replace("%career%", html.toString());
				ShowBoard.separateAndSend(content, activeChar);
				return;
			}

			int classId = 0;
			int newClassId = 0;
			int intVal = 0;

			try
			{
				for(String id : command.substring(15, command.length()).split(" "))
				{
					if(intVal == 0)
					{
						intVal = Integer.parseInt(id);
						continue;
					}
					if(classId > 0)
					{
						newClassId = Short.parseShort(id);
						continue;
					}
					classId = Short.parseShort(id);
				}
			}
			catch(Exception NumberFormatException)
			{}

			switch(intVal)
			{
				case 1: // Возвращает список сабов, которые можно взять (см case 4)
					subsAvailable = getAvailableSubClasses(activeChar, true);

					if(subsAvailable != null && !subsAvailable.isEmpty())
					{
						html.append("<br>Вам доступны следующие саб-классы:<br>");

						for(PlayerClass subClass : subsAvailable)
							html.append("<a action=\"bypass _bbscareer;sub;4 " + subClass.ordinal() + "\">" + formatClassForDisplay(subClass) + "</a><br>");
					}
					else
					{
						html.append("<br>Вам доступны следующие саб-классы:<br>");
					}
					break;
				case 2: // Установка уже взятого саба (см case 5)
					html.append("<br>Переключить саб-класс:<br>");

					final int baseClassId = activeChar.getBaseClassId();

					if(playerClassList.size() < 2)
						html.append("У вас нет саб-классов для переключения, но вы можете добавить его прямо сейчас<br><a action=\"bypass _bbscareer;sub;1\">Добавить саб.</a>");
					else
					{
						html.append("Какой саб-класс вы желаете использовать?<br>");

						if(baseClassId == activeChar.getActiveClassId())
							html.append(HtmlUtils.htmlClassName(activeChar.getActiveClassId()) + " <font color=\"LEVEL\">(Базовый)</font><br><br>");
						else
							html.append("<a action=\"bypass _bbscareer;sub;5 " + baseClassId + "\">" + HtmlUtils.htmlClassName(activeChar.getActiveClassId()) + "</a> " + "<font color=\"LEVEL\">(Базовый)</font><br><br>");

						for(SubClass subClass : playerClassList.values())
						{
							if(subClass.isBase())
								continue;
							int subClassId = subClass.getClassId();

							if(subClassId == activeChar.getActiveClassId())
								html.append(HtmlUtils.htmlClassName(activeChar.getActiveClassId()) + "<br>");
							else
								html.append("<a action=\"bypass _bbscareer;sub;5 " + subClassId + "\">" + HtmlUtils.htmlClassName(activeChar.getActiveClassId()) + "</a><br>");
						}
					}
					break;
				case 3: // Отмена сабкласса - список имеющихся (см case 6)
					html.append("<br>Отмена саб-класса:<br>Какой из имеющихся сабов вы хотете заменить?<br>");

					for(SubClass sub : playerClassList.values())
					{
						html.append("<br>");
						if(!sub.isBase())
							html.append("<a action=\"bypass _bbscareer;sub;6 " + sub.getClassId() + "\">" + HtmlUtils.htmlClassName(activeChar.getActiveClassId()) + "</a><br>");
					}

					html.append("<br>");
					break;
				case 4: // Добавление сабкласса - обработка выбора из case 1
					boolean allowAddition = true;

					// Проверка хватает ли уровня
					if(activeChar.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.model.instances.L2VillageMasterInstance.NoSubBeforeLevel", activeChar).addNumber(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS));
						allowAddition = false;
					}

					if(!playerClassList.isEmpty())
					{
						for(SubClass subClass : playerClassList.values())
						{
							if(subClass.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)
							{
								activeChar.sendMessage(new CustomMessage("l2r.gameserver.model.instances.L2VillageMasterInstance.NoSubBeforeLevel", activeChar).addNumber(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS));
								allowAddition = false;
								break;
							}
						}
					}
					else
					{
						html.append("Error! Your Class List is Empty. Call to GM!");
					}

					if(Config.ENABLE_OLYMPIAD && Olympiad.isRegisteredInComp(activeChar))
					{
						activeChar.sendPacket(SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASS_CHARACTER_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
						return;
					}

					if(allowAddition)
					{
						String className = HtmlUtils.htmlClassName(activeChar.getActiveClassId());

						if(!activeChar.addSubClass(classId, true, 0))
						{
							html.append("Саб-класс не добавлен!");
							return;
						}

						html.append("<br><br>Саб-класс <font color=\"LEVEL\">" + className + "</font> успешно добавлен!");
						activeChar.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_A_CLASS_TRANSFER); // Transfer to new class.
					}
					else
						html.append("<br><br>Вы не можете добавить подкласс в данный момент.<br>Для активации сабклассов вы должны достичь <font color=F2C202>75-го уровня</font><br>");
					break;
				case 5: // Смена саба на другой из уже взятых - обработка выбора из case 2
					if(Config.ENABLE_OLYMPIAD && Olympiad.isRegisteredInComp(activeChar))
					{
						activeChar.sendPacket(SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASS_CHARACTER_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
						return;
					}

					activeChar.setActiveSubClass(classId, true);

					html.append("<br>Ваш активный саб-класс теперь: <font color=\"LEVEL\">" + HtmlUtils.htmlClassName(activeChar.getActiveClassId()) + "</font>.");

					activeChar.sendPacket(SystemMsg.YOU_HAVE_SUCCESSFULLY_SWITCHED_TO_YOUR_SUBCLASS); // Transfer
					// completed.
					break;
				case 6: // Отмена сабкласса - обработка выбора из case 3
					html.append("<br><br>Выберите саб-класс для смены.<br>" + //
					"<font color=\"LEVEL\">Внимание!</font> Все профессии и скилы для этого саба будут удалены.<br><br>");

					subsAvailable = getAvailableSubClasses(activeChar, false);

					if(!subsAvailable.isEmpty())
						for(PlayerClass subClass : subsAvailable)
							html.append("<a action=\"bypass _bbscareer;sub;7 " + classId + " " + subClass.ordinal() + "\">" + formatClassForDisplay(subClass) + "</a><br>");
					else
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.model.instances.L2VillageMasterInstance.NoSubAtThisTime", activeChar));
						return;
					}
					break;
				case 7: // Отмена сабкласса - обработка выбора из case 6
					// activeChar.sendPacket(Msg.YOUR_PREVIOUS_SUB_CLASS_WILL_BE_DELETED_AND_YOUR_NEW_SUB_CLASS_WILL_START_AT_LEVEL_40__DO_YOU_WISH_TO_PROCEED); // Change confirmation.

					if(Config.ENABLE_OLYMPIAD && Olympiad.isRegisteredInComp(activeChar))
					{
						activeChar.sendPacket(SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASS_CHARACTER_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
						return;
					}

					// Удаляем скиллы трансфера
					int item_id = 0;
					switch(ClassId.values()[classId])
					{
						case cardinal:
							item_id = 15307;
							break;
						case evaSaint:
							item_id = 15308;
							break;
						case shillienSaint:
							item_id = 15309;
							break;
					}
					if(item_id > 0)
						activeChar.unsetVar("TransferSkills" + item_id);

					if(activeChar.modifySubClass(classId, newClassId))
					{

						html.append("<br>Ваш саб-класс изменен на: <font color=\"LEVEL\">" + HtmlUtils.htmlClassName(activeChar.getActiveClassId()) + "</font>.");
						activeChar.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED); // Subclass added.
					}
					else
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded", activeChar));
						return;
					}
					break;
			}

			String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/career.htm", activeChar);
			content = content.replace("%career%", html.toString());
			ShowBoard.separateAndSend(content, activeChar);
		}
		if(command.startsWith("_bbscareer;nobles;"))
		{
			
		}
		if(command.startsWith("_bbscareer;sps;"))
		{
			
		}
		if(command.startsWith("_bbscareer;spa;"))
		{
			
		}
		if(command.startsWith("_bbscareer;classmaster;change_class;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			st.nextToken();
			short val = Short.parseShort(st.nextToken());
			int price = Integer.parseInt(st.nextToken());
			ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.CLASS_MASTERS_PRICE_ITEM);
			ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
			if(pay != null && pay.getCount() >= price)
			{
				activeChar.getInventory().destroyItem(pay, (long) price);
				changeClass(activeChar, val);
				onBypassCommand(activeChar, "_bbscareer;");
			}
			else if(Config.CLASS_MASTERS_PRICE_ITEM == 57)
			{
				activeChar.sendPacket(new SystemMessage2(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
			}
			else
			{
				activeChar.sendPacket(new SystemMessage2(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
			}
		}
	}

	private StringBuilder getSubClassesHtml(Player activeChar, boolean condition)
	{
		StringBuilder html = new StringBuilder();
		
		if(!Config.BBS_PVP_SUB_MANAGER_ALLOW)
		{
			if (activeChar.isLangRus())
				activeChar.sendMessage("Сервис отключен.");
			else
				activeChar.sendMessage("Service is disabled.");
			return html;
		}
		
		Set<PlayerClass> subsAvailable = getAvailableSubClasses(activeChar, true);

		if(subsAvailable != null && !subsAvailable.isEmpty() && condition)
		{
			if(!activeChar.isInZone(Zone.ZoneType.peace_zone) && Config.BBS_PVP_SUB_MANAGER_PIACE)
			{
				html.append("<br><font color=F2C202>" + activeChar.getName() + "</font> вам доступны следующие операции над саб-классами:<br><br>Вернитесь в город. Операции над сабом доступны только в городе");				
			}
			else
			{
				html.append("<br><font color=F2C202>" + activeChar.getName() + "</font> вам доступны следующие операции над саб-классами:<br>");
				html.append("<center><table width=600><tr>");
				html.append("<td><center><button value=\"Добавить\" action=\"bypass _bbscareer;sub;1\" width=150 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
				html.append("<td><center><button value=\"Изменить\" action=\"bypass _bbscareer;sub;2\" width=150 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
				html.append("<td><center><button value=\"Отменить\" action=\"bypass _bbscareer;sub;3\" width=150 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
				html.append("</tr></table></center>");
			}
		}
		else
		{
			html.append("<br>");
		}
		return html;
	}
	
	private Set<PlayerClass> getAvailableSubClasses(Player player, boolean isNew)
	{
		final int charClassId = player.getBaseClassId();
		final Race pRace = player.getRace();
		final ClassType pTeachType = getTeachType(player);

		PlayerClass currClass = PlayerClass.values()[charClassId];// .valueOf(charClassName);

		Set<PlayerClass> availSubs = currClass.getAvailableSubclasses();
		if(availSubs == null)
			return null;

		// Из списка сабов удаляем мейн класс игрока
		availSubs.remove(currClass);

		for(PlayerClass availSub : availSubs)
		{
			// Удаляем из списка возможных сабов, уже взятые сабы и их предков
			for(SubClass subClass : player.getSubClasses().values())
			{
				if(availSub.ordinal() == subClass.getClassId())
				{
					availSubs.remove(availSub);
					continue;
				}

				// Удаляем из возможных сабов их родителей, если таковые есть у чара
				ClassId parent = ClassId.values()[availSub.ordinal()].getParent(player.getSex());
				if(parent != null && parent.getId() == subClass.getClassId())
				{
					availSubs.remove(availSub);
					continue;
				}

				// Удаляем из возможных сабов родителей текущих сабклассов, иначе если взять саб berserker
				// и довести до 3ей профы - doombringer, игроку будет предложен berserker вновь (дежавю)
				ClassId subParent = ClassId.values()[subClass.getClassId()].getParent(player.getSex());
				if(subParent != null && subParent.getId() == availSub.ordinal())
					availSubs.remove(availSub);
			}

			if(!availSub.isOfRace(Race.human) && !availSub.isOfRace(Race.elf))
			{
				if(!availSub.isOfRace(pRace))
					availSubs.remove(availSub);
			}
			else if(!availSub.isOfType(pTeachType))
				availSubs.remove(availSub);

			// Особенности саб классов камаэль
			if(availSub.isOfRace(Race.kamael))
			{
				// Для Soulbreaker-а и SoulHound не предлагаем Soulbreaker-а другого пола
				if((currClass == PlayerClass.MaleSoulHound || currClass == PlayerClass.FemaleSoulHound || currClass == PlayerClass.FemaleSoulbreaker || currClass == PlayerClass.MaleSoulbreaker) && (availSub == PlayerClass.FemaleSoulbreaker || availSub == PlayerClass.MaleSoulbreaker))
					availSubs.remove(availSub);

				// Для Berserker(doombringer) и Arbalester(trickster) предлагаем Soulbreaker-а только своего пола
				if(currClass == PlayerClass.Berserker || currClass == PlayerClass.Doombringer || currClass == PlayerClass.Arbalester || currClass == PlayerClass.Trickster)
					if(player.getSex() == 1 && availSub == PlayerClass.MaleSoulbreaker || player.getSex() == 0 && availSub == PlayerClass.FemaleSoulbreaker)
						availSubs.remove(availSub);

				// Inspector доступен, только когда вкачаны 2 возможных первых саба камаэль(+ мейн класс)
				if(availSub == PlayerClass.Inspector && player.getSubClasses().size() < (isNew ? 3 : 4))
					availSubs.remove(availSub);
			}
		}
		return availSubs;
	}

	private String formatClassForDisplay(PlayerClass className)
	{
		String classNameStr = className.toString();
		char[] charArray = classNameStr.toCharArray();

		for(int i = 1; i < charArray.length; i++)
			if(Character.isUpperCase(charArray[i]))
				classNameStr = classNameStr.substring(0, i) + " " + classNameStr.substring(i);

		return classNameStr;
	}
	
	private ClassType getTeachType(Player player)
	{
		if(!PlayerClass.values()[player.getBaseClassId()].isOfType(ClassType.Priest))
			return ClassType.Priest;

		if(!PlayerClass.values()[player.getBaseClassId()].isOfType(ClassType.Mystic))
			return ClassType.Mystic;

		return ClassType.Fighter;
	}

	private void changeClass(Player player, int val)
	{
		if(player.getClassId().getLevel() == 3)
			player.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_YOUR_THIRDCLASS_TRANSFER_QUEST); // для 3 профы
		else
			player.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_A_CLASS_TRANSFER); // для 1 и 2 профы

		player.setClassId(val, false, false);
		player.broadcastUserInfo(true);
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{}

	private static boolean CheckCondition(Player player)
	{
		if(player == null)
            return false;

		if(!Config.USE_BBS_PROF_IS_COMBAT && (player.getPvpFlag() != 0 || player.isInDuel() || player.isInCombat() || player.isAttackingNow()))
		{
			if (player.isLangRus())
				player.sendMessage("Во время боя нельзя использовать данную функцию.");
			else
				player.sendMessage("During combat, you can not use this feature.");
			return false;
		}

		return true;
	}
}