package l2rt.gameserver.network;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.network.*;
import l2rt.gameserver.network.clientpackets.*;
import l2rt.gameserver.model.L2Player;
import l2rt.util.Log;
import l2rt.util.Util;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;

import static l2rt.gameserver.network.L2GameClient.GameClientState.IN_GAME;

/**
 * Stateful Packet Handler<BR>
 * The Stateful approach prevents the server from handling inconsistent packets, examples:<BR>
 * <li>Clients sends a MoveToLocation packet without having a character attached. (Potential errors handling the packet).</li>
 * <li>Clients sends a RequestAuthLogin being already authed. (Potential exploit).</li>
 * <BR><BR>
 * Note: If for a given exception a packet needs to be handled on more then one state, then it should be added to all these states.
 */
public final class L2GamePacketHandler extends TCPHeaderHandler<L2GameClient> implements IPacketHandler<L2GameClient>, IClientFactory<L2GameClient>, IMMOExecutor<L2GameClient>
{
	private static final Logger _log = Logger.getLogger(L2GamePacketHandler.class.getName());

	public L2GamePacketHandler()
	{
		super(null);
	}

	// implementation
	public ReceivablePacket<L2GameClient> handlePacket(ByteBuffer data, L2GameClient client)
	{
		if(client == null || client.isPacketsFailed())
			return null;
		if(!data.hasRemaining())
		{
			handleIncompletePacket(client);
			return null;
		}

		L2Player activeChar;
		int id = data.get() & 0xFF;
		ReceivablePacket<L2GameClient> msg = null;
		int id2 = 0;
		switch(client.getState())
		{
			case CONNECTED:
				switch(id)
				{
					case 0x00:
						msg = new RequestStatus();
						break;
					case 0x0e:
						msg = new ProtocolVersion();
						break;
					case 0x2b:
						msg = new AuthLogin();
						break;
					case 0xcb:
						msg = new ReplyGameGuardQuery();
						break;
					default:
						_log.severe("Unknown packet on state: CONNECTED, id: " + Integer.toHexString(id) + " from " + client.getConnection().getSocket().getInetAddress().getHostAddress());
						break;
				}
				break;
			case AUTHED:
				switch(id)
				{
					case 0x00:
						msg = new Logout();
						break;
					case 0x0c:
						msg = new CharacterCreate(); //RequestCharacterCreate();
						break;
					case 0x0d:
						msg = new CharacterDelete(); //RequestCharacterDelete();
						break;
					case 0x12:
						msg = new CharacterSelected(); //CharacterSelect();
						break;
					case 0x13:
						msg = new NewCharacter(); //RequestNewCharacter();
						break;
					case 0x7b:
						msg = new CharacterRestore(); //RequestCharacterRestore();
						break;
					case 0xcb:
						msg = new ReplyGameGuardQuery();
						break;
					case 0xd0:
						if(data.remaining() < 2)
						{
							handleIncompletePacket(client);
							break;
						}
						else
						{
							int id3 = data.getShort() & 0xffff;
							switch(id3)
							{
							case 0x36:
								msg = new GotoLobby();
								break;								
							case 0xAD:
								msg = new RequestEx2ndPasswordCheck();
								break;
							case 0xAE:
								msg = new RequestEx2ndPasswordVerify();
								break;
							case 0xAF:
								msg = new RequestEx2ndPasswordReq();
								break;
							case 0xb0:
								msg = new RequestCharacterNameCreatable();
								break;
							}
						}
						break;
					default:
						//_log.severe("Unknown packet on state: AUTHED, id: " + Integer.toHexString(id));
						break;
				}
				break;
			case IN_GAME:
				switch(id)
				{
					case 0x00:
						msg = new Logout();
						break;
					case 0x01:
						msg = new AttackRequest();
						break;
					case 0x02:
						//	msg = new ?();
						break;
					case 0x03:
						msg = new RequestStartPledgeWar();
						break;
					case 0x04:
						//	msg = new ?();
						break;
					case 0x05:
						msg = new RequestStopPledgeWar();
						break;
					case 0x06:
						//	msg = RequestSCCheck(); // ? Format: cdx
						break;
					case 0x07:
						msg = new ReplyGameGuardQuery();
						//здесь совсем другой пакет ResponseAuthGameGuard[cddddd] (c) Drin
						break;
					case 0x08:
						//	msg = new ?();
						break;
					case 0x09:
						msg = new RequestSetPledgeCrest();
						break;
					case 0x0a:
						//	msg = new ?();
						break;
					case 0x0b:
						msg = new RequestGiveNickName();
						break;
					case 0x0c:
						//	wtf???
						break;
					case 0x0d:
						//	wtf???
						break;
					case 0x0f:
						msg = new MoveBackwardToLocation();
						break;
					case 0x10:
						//	msg = new Say(); Format: cS // старый ?
						break;
					case 0x11:
						msg = new EnterWorld();
						break;
					case 0x12:
						//	wtf???
						break;
					case 0x14:
						msg = new RequestItemList();
						break;
					case 0x15:
						//	msg = new RequestEquipItem(); // старый?
						//	Format: cdd server id = %d Slot = %d
						break;
					case 0x16:
						msg = new RequestUnEquipItem();
						break;
					case 0x17:
						msg = new RequestDropItem();
						break;
					case 0x18:
						//	msg = new ?();
						break;
					case 0x19:
						msg = new UseItem();
						break;
					case 0x1a:
						msg = new TradeRequest();
						break;
					case 0x1b:
						msg = new AddTradeItem();
						break;
					case 0x1c:
						msg = new TradeDone();
						break;
					case 0x1d:
						//	msg = new ?();
						break;
					case 0x1e:
						//	msg = new ?();
						break;
					case 0x1f:
						msg = new Action();
						break;
					case 0x20:
						//	msg = new ?();
						break;
					case 0x21:
						//	msg = new ?();
						break;
					case 0x22:
						//msg = new RequestLinkHtml();
						_log.warning("RequestLinkHtml from ip: " + client.getIpAddr() + ", Login: " + client.getLoginName());
						break;
					case 0x23:
						msg = new RequestBypassToServer();
						break;
					case 0x24:
						msg = new RequestBBSwrite(); //RequestBBSWrite();
						break;
					case 0x25:
						msg = new RequestCreatePledge();
						break;
					case 0x26:
						msg = new RequestJoinPledge();
						break;
					case 0x27:
						msg = new RequestAnswerJoinPledge();
						break;
					case 0x28:
						msg = new RequestWithdrawalPledge();
						break;
					case 0x29:
						msg = new RequestOustPledgeMember();
						break;
					case 0x2a:
						//	msg = new ?();
						break;
					case 0x2c:
						msg = new RequestGetItemFromPet();
						break;
					case 0x2d:
						//	RequestDismissParty
						break;
					case 0x2e:
						msg = new RequestAllyInfo();
						break;
					case 0x2f:
						msg = new RequestCrystallizeItem();
						break;
					case 0x30:
						// RequestPrivateStoreManage, устарел
						break;
					case 0x31:
						msg = new SetPrivateStoreList();
						break;
					case 0x32:
						// RequestPrivateStoreManageCancel, устарел
						break;
					case 0x33:
						msg = new RequestTeleport();
						break;
					case 0x34:
						msg = new RequestSocialAction(); //SocialAction();
						break;
					case 0x35:
						// ChangeMoveType, устарел
						break;
					case 0x36:
						// ChangeWaitType, устарел
						break;
					case 0x37:
						msg = new RequestSellItem();
						break;
					case 0x38:
						msg = new RequestMagicSkillList();
						break;
					case 0x39:
						msg = new RequestMagicSkillUse();
						break;
					case 0x3a:
						msg = new Appearing(); //Appering();
						break;
					case 0x3b:
						if(Config.ALLOW_WAREHOUSE)
							msg = new SendWareHouseDepositList();
						break;
					case 0x3c:
						msg = new SendWareHouseWithDrawList();
						break;
					case 0x3d:
						msg = new RequestShortCutReg();
						break;
					case 0x3e:
						//	msg = new RequestShortCutUse(); // Format: cddc  ?
						break;
					case 0x3f:
						msg = new RequestShortCutDel();
						break;
					case 0x40:
						msg = new RequestBuyItem();
						break;
					case 0x41:
						//	msg = new RequestDismissPledge(); //Format: c ?
						break;
					case 0x42:
						msg = new RequestJoinParty();
						break;
					case 0x43:
						msg = new RequestAnswerJoinParty();
						break;
					case 0x44:
						msg = new RequestWithDrawalParty();
						break;
					case 0x45:
						msg = new RequestOustPartyMember();
						break;
					case 0x46:
						msg = new RequestDismissParty();
						break;
					case 0x47:
						msg = new CannotMoveAnymore();
						break;
					case 0x48:
						msg = new RequestTargetCanceld();
						break;
					case 0x49:
						msg = new Say2C();
						break;
					// -- maybe GM packet's
					case 0x4a:
						if(!data.hasRemaining())
						{
							handleIncompletePacket(client);
							break;
						}
						id2 = data.get() & 0xff;
						switch(id2)
						{
							case 0x00:
								//	msg = new SendCharacterInfo(); // Format: S
								break;
							case 0x01:
								//	msg = new SendSummonCmd(); // Format: S
								break;
							case 0x02:
								//	msg = new SendServerStatus(); // Format: (noargs)
								break;
							case 0x03:
								//	msg = new SendL2ParamSetting(); // Format: dd
								break;
							default:
								activeChar = client.getActiveChar();
								int size = data.remaining();
								_log.warning("Unknown Packet: 0x4A:" + Integer.toHexString(id2) + ", from ip: " + client.getIpAddr() + ", Char: " + activeChar != null ? activeChar.toString() : "null" + ", Login: " + client.getLoginName());
								Log.add("Unknown Packet: 0x4A:" + Integer.toHexString(id2) + ", from ip: " + client.getIpAddr() + ", Char: " + activeChar != null ? activeChar.toString() : "null" + ", Login: " + client.getLoginName(), "unknown_packets");
								byte[] array = new byte[size];
								data.get(array);
								_log.warning(Util.printData(array, size));
								client.onClientPacketFail();
								break;
						}
						break;
					case 0x4b:
						//	msg = new ?();
						break;
					case 0x4c:
						//	msg = new ?();
						break;
					case 0x4d:
						msg = new RequestPledgeMemberList();
						break;
					case 0x4e:
						//	msg = new ?();
						break;
					case 0x4f:
						System.out.println("Unhandled client packet 0x4f");
						//	msg = new RequestMagicItem(); // Format: c ?
						break;
					case 0x50:
						msg = new RequestSkillList(); // trigger
						break;
					case 0x51:
						//	msg = new ?();
						break;
					case 0x52:
						msg = new MoveWithDelta();
						break;
					case 0x53:
						msg = new RequestGetOnVehicle();
						break;
					case 0x54:
						msg = new RequestGetOffVehicle();
						break;
					case 0x55:
						msg = new AnswerTradeRequest();
						break;
					case 0x56:
						msg = new RequestActionUse();
						break;
					case 0x57:
						msg = new RequestRestart();
						break;
					case 0x58:
						msg = new RequestSiegeInfo();
						break;
					case 0x59:
						msg = new ValidatePosition();
						break;
					case 0x5a:
						msg = new RequestSEKCustom();
						break;
					case 0x5b:
						msg = new StartRotatingC();
						break;
					case 0x5c:
						msg = new FinishRotatingC();
						break;
					case 0x5d:
						//	msg = new ?();
						break;
					case 0x5e:
						msg = new RequestShowBoard();
						break;
					case 0x5f:
						msg = new RequestEnchantItem();
						break;
					case 0x60:
						msg = new RequestDestroyItem();
						break;
					case 0x61:
						//	msg = new ?();
						break;
					case 0x62:
						msg = new RequestQuestList();
						break;
					case 0x63:
						msg = new RequestQuestAbort(); //RequestDestroyQuest();
						break;
					case 0x64:
						//	msg = new ?();
						break;
					case 0x65:
						msg = new RequestPledgeInfo();
						break;
					case 0x66:
						msg = new RequestPledgeExtendedInfo();
						break;
					case 0x67:
						msg = new RequestPledgeCrest();
						break;
					case 0x68:
						//	msg = new ?();
						break;
					case 0x69:
						//	msg = new ?();
						break;
					case 0x6a:
						//	msg = new ?();
						break;
					case 0x6b:
						msg = new RequestSendL2FriendSay();
						break;
					case 0x6c:
						msg = new RequestShowMiniMap(); //RequestOpenMinimap();
						break;
					case 0x6d:
						msg = new RequestSendMsnChatLog();
						break;
					case 0x6e:
						msg = new RequestReload(); // record video
						break;
					case 0x6f:
						msg = new RequestHennaEquip();
						break;
					case 0x70:
						msg = new RequestHennaUnequipList();
						break;
					case 0x71:
						msg = new RequestHennaUnequipInfo();
						break;
					case 0x72:
						msg = new RequestHennaUnequip();
						break;
					case 0x73:
						msg = new RequestAquireSkillInfo(); //RequestAcquireSkillInfo();
						break;
					case 0x74:
						msg = new SendBypassBuildCmd();
						break;
					case 0x75:
						msg = new RequestMoveToLocationInVehicle();
						break;
					case 0x76:
						msg = new CannotMoveAnymoreInVehicle();
						break;
					case 0x77:
						msg = new RequestFriendInvite();
						break;
					case 0x78:
						msg = new RequestFriendAddReply();
						break;
					case 0x79:
						msg = new RequestFriendList();
						break;
					case 0x7a:
						msg = new RequestFriendDel();
						break;
					case 0x7c:
						msg = new RequestAquireSkill();
						break;
					case 0x7d:
						msg = new RequestRestartPoint();
						break;
					case 0x7e:
						msg = new RequestGMCommand();
						break;
					case 0x7f:
						msg = new RequestPartyMatchConfig();
						break;
					case 0x80:
						msg = new RequestPartyMatchList();
						break;
					case 0x81:
						msg = new RequestPartyMatchDetail();
						break;
					case 0x82:
						msg = new RequestPrivateStoreList();
						break;
					case 0x83:
						msg = new RequestPrivateStoreBuy();
						break;
					case 0x84:
						//	msg = new ReviveReply(); // format: cd ?
						break;
					case 0x85:
						msg = new RequestTutorialLinkHtml();
						break;
					case 0x86:
						msg = new RequestTutorialPassCmdToServer();
						break;
					case 0x87:
						msg = new RequestTutorialQuestionMark(); //RequestTutorialQuestionMarkPressed();
						break;
					case 0x88:
						msg = new RequestTutorialClientEvent();
						break;
					case 0x89:
						msg = new RequestPetition();
						break;
					case 0x8a:
						msg = new RequestPetitionCancel();
						break;
					case 0x8b:
						msg = new RequestGmList();
						break;
					case 0x8c:
						msg = new RequestJoinAlly();
						break;
					case 0x8d:
						msg = new RequestAnswerJoinAlly();
						break;
					case 0x8e:
						// Команда /allyleave - выйти из альянса
						msg = new RequestWithdrawAlly();
						break;
					case 0x8f:
						// Команда /allydismiss - выгнать клан из альянса
						msg = new RequestOustAlly();
						break;
					case 0x90:
						// Команда /allydissolve - распустить альянс
						msg = new RequestDismissAlly();
						break;
					case 0x91:
						msg = new RequestSetAllyCrest();
						break;
					case 0x92:
						msg = new RequestAllyCrest();
						break;
					case 0x93:
						msg = new RequestChangePetName();
						break;
					case 0x94:
						msg = new RequestPetUseItem();
						break;
					case 0x95:
						msg = new RequestGiveItemToPet();
						break;
					case 0x96:
						msg = new RequestPrivateStoreQuitSell();
						break;
					case 0x97:
						msg = new SetPrivateStoreMsgSell();
						break;
					case 0x98:
						msg = new RequestPetGetItem();
						break;
					case 0x99:
						msg = new RequestPrivateStoreBuyManage();
						break;
					case 0x9a:
						msg = new SetPrivateStoreBuyList();
						break;
					case 0x9b:
						msg = new RequestPrivateStoreBuyManageCancel();
						break;
					case 0x9c:
						msg = new RequestPrivateStoreQuitBuy();
						break;
					case 0x9d:
						msg = new SetPrivateStoreMsgBuy();
						break;
					case 0x9e:
						msg = new RequestPrivateStoreBuyList(); // TODO не используется
						break;
					case 0x9f:
						msg = new RequestPrivateStoreBuySellList();
						break;
					case 0xa0:
						msg = new RequestTimeCheck();
						break;
					case 0xa1:
						//	msg = new ?();
						break;
					case 0xa2:
						//	msg = new ?();
						break;
					case 0xa3:
						//	msg = new ?();
						break;
					case 0xa4:
						//	msg = new ?();
						break;
					case 0xa5:
						//	msg = new ?();
						break;
					case 0xa6:
						//msg = new RequestSkillCoolTime();
						break;
					case 0xa7:
						msg = new RequestPackageSendableItemList();
						break;
					case 0xa8:
						msg = new RequestPackageSend();
						break;
					case 0xa9:
						msg = new RequestBlock();
						break;
					case 0xaa:
						//	msg = new RequestCastleSiegeInfo(); // format: cd ?
						break;
					case 0xab:
						msg = new RequestSiegeAttackerList(); //RequestCastleSiegeAttackerList();
						break;
					case 0xac:
						msg = new RequestSiegeDefenderList(); //RequestCastleSiegeDefenderList();
						break;
					case 0xad:
						msg = new RequestJoinSiege(); //RequestJoinCastleSiege();
						break;
					case 0xae:
						msg = new RequestConfirmSiegeWaitingList(); //RequestConfirmCastleSiegeWaitingList();
						break;
					case 0xaf:
						msg = new RequestSetCastleSiegeTime();
						break;
					case 0xb0:
						msg = new RequestMultiSellChoose();
						break;
					case 0xb1:
						msg = new NetPing();
						break;
					case 0xb2:
						msg = new RequestRemainTime();
						break;
					case 0xb3:
						msg = new BypassUserCmd();
						break;
					case 0xb4:
						msg = new SnoopQuit();
						break;
					case 0xb5:
						msg = new RequestRecipeBookOpen();
						break;
					case 0xb6:
						msg = new RequestRecipeItemDelete();
						break;
					case 0xb7:
						msg = new RequestRecipeItemMakeInfo();
						break;
					case 0xb8:
						msg = new RequestRecipeItemMakeSelf();
						break;
					case 0xb9:
						// msg = new RequestRecipeShopManageList(); deprecated // format: c
						break;
					case 0xba:
						msg = new RequestRecipeShopMessageSet();
						break;
					case 0xbb:
						msg = new RequestRecipeShopListSet();
						break;
					case 0xbc:
						msg = new RequestRecipeShopManageQuit();
						break;
					case 0xbd:
						msg = new RequestRecipeShopManageCancel();
						break;
					case 0xbe:
						msg = new RequestRecipeShopMakeInfo();
						break;
					case 0xbf:
						msg = new RequestRecipeShopMakeDo();
						break;
					case 0xc0:
						msg = new RequestRecipeShopSellList();
						break;
					case 0xc1:
						msg = new RequestObserverEnd();
						break;
					case 0xc2:
						msg = new VoteSociality(); // Recommend
						break;
					case 0xc3:
						msg = new RequestHennaList(); //RequestHennaItemList();
						break;
					case 0xc4:
						msg = new RequestHennaItemInfo();
						break;
					case 0xc5:
						msg = new RequestBuySeed();
						break;
					case 0xc6:
						msg = new ConfirmDlg();
						break;
					case 0xc7:
						msg = new RequestPreviewItem();
						break;
					case 0xc8:
						msg = new RequestSSQStatus();
						break;
					case 0xc9:
						msg = new PetitionVote();
						break;
					case 0xca:
						//	msg = new ?();
						break;
					case 0xcb:
						msg = new ReplyGameGuardQuery();
						break;
					case 0xcc:
						msg = new RequestPledgePower();
						break;
					case 0xcd:
						msg = new RequestMakeMacro();
						break;
					case 0xce:
						msg = new RequestDeleteMacro();
						break;
					case 0xcf:
						msg = new RequestProcureCrop(); // ?
						break;
					case 0xd0:
						if(data.remaining() < 2)
						{
							handleIncompletePacket(client);
							break;
						}
						int id3 = data.getShort() & 0xffff;
						switch(id3)
						{
							case 0x01:
								msg = new RequestManorList();
								break;
							case 0x02:
								msg = new RequestProcureCropList();
								break;
							case 0x03:
								msg = new RequestSetSeed();
								break;
							case 0x04:
								msg = new RequestSetCrop();
								break;
							case 0x05:
								msg = new RequestWriteHeroWords();
								break;
							case 0x06:
								msg = new RequestExMPCCAskJoin(); //RequestExAskJoinMPCC();
								break;
							case 0x07:
								msg = new RequestExMPCCAcceptJoin(); //RequestExAcceptJoinMPCC();
								break;
							case 0x08:
								msg = new RequestExOustFromMPCC();
								break;
							case 0x09:
								msg = new RequestOustFromPartyRoom();
								break;
							case 0x0a:
								msg = new RequestDismissPartyRoom();
								break;
							case 0x0b:
								msg = new RequestWithdrawPartyRoom();
								break;
							case 0x0c:
								msg = new RequestHandOverPartyMaster();
								break;
							case 0x0d:
								msg = new RequestAutoSoulShot();
								break;
							case 0x0e:
								msg = new RequestExEnchantSkillInfo();
								break;
							case 0x0f:
								msg = new RequestExEnchantSkill();
								break;
							case 0x10:
								msg = new RequestPledgeCrestLarge();
								break;
							case 0x11:
								msg = new RequestSetPledgeCrestLarge();
								break;
							case 0x12:
								msg = new RequestPledgeSetAcademyMaster();
								break;
							case 0x13:
								msg = new RequestPledgePowerGradeList();
								break;
							case 0x14:
								msg = new RequestPledgeMemberPowerInfo();
								break;
							case 0x15:
								msg = new RequestPledgeSetMemberPowerGrade();
								break;
							case 0x16:
								msg = new RequestPledgeMemberInfo();
								break;
							case 0x17:
								msg = new RequestPledgeWarList();
								break;
							case 0x18:
								msg = new RequestExFishRanking();
								break;
							case 0x19:
								msg = new RequestPCCafeCouponUse();
								break;
							case 0x1a:
								//	msg = new ?();
								// format: (ch)b, b - array размером в 64 байта
								break;
							case 0x1b:
								msg = new RequestDuelStart();
								break;
							case 0x1c:
								msg = new RequestDuelAnswerStart();
								break;
							case 0x1d:
								msg = new RequestTutorialClientEvent(); //RequestExSetTutorial();
								// Format: d / требует отладки, ИМХО, это совсем другой пакет (с) Drin
								break;
							case 0x1e:
								msg = new RequestExRqItemLink(); // chat item links
								break;
							case 0x1f:
								// CanNotMoveAnymore(AirShip)
								// format: (ch)ddddd
								break;
							case 0x20:
								msg = new RequestMoveToLocationInAirShip();
								break;
							case 0x21:
								msg = new RequestKeyMapping();
								break;
							case 0x22:
								msg = new RequestSaveKeyMapping();
								break;
							case 0x23:
								msg = new RequestExRemoveItemAttribute();
								break;
							case 0x24:
								msg = new RequestSaveInventoryOrder(); // сохранение порядка инвентаря
								break;
							case 0x25:
								msg = new RequestExitPartyMatchingWaitingRoom();
								break;
							case 0x26:
								msg = new RequestConfirmTargetItem();
								break;
							case 0x27:
								msg = new RequestConfirmRefinerItem();
								break;
							case 0x28:
								msg = new RequestConfirmGemStone();
								break;
							case 0x29:
								msg = new RequestOlympiadObserverEnd();
								break;
							case 0x2a:
								msg = new RequestCursedWeaponList();
								break;
							case 0x2b:
								msg = new RequestCursedWeaponLocation();
								break;
							case 0x2c:
								msg = new RequestPledgeReorganizeMember();
								break;
							case 0x2d:
								msg = new RequestExMPCCShowPartyMembersInfo();
								break;
							case 0x2e:
								msg = new RequestExOlympiadObserverEnd(); // не уверен (в клиенте называется RequestOlympiadMatchList)
								break;
							case 0x2f:
								msg = new RequestAskJoinPartyRoom();
								break;
							case 0x30:
								msg = new AnswerJoinPartyRoom();
								break;
							case 0x31:
								msg = new RequestListPartyMatchingWaitingRoom();
								break;
							case 0x32:
								msg = new RequestExEnchantSkillSafe();
								break;
							case 0x33:
								msg = new RequestExEnchantSkillUntrain();
								break;
							case 0x34:
								msg = new RequestExEnchantSkillRouteChange();
								break;
							case 0x35:
								msg = new RequestEnchantItemAttribute();
								break;
							case 0x36:
								//RequestGotoLobby - случается при многократном нажатии кнопки "вход"
								break;
							case 0x38:
								msg = new RequestExMoveToLocationAirShip();
								break;
							case 0x39:
								msg = new RequestBidItemAuction();
								break;
							case 0x3a:
								msg = new RequestInfoItemAuction();
								break;
							case 0x3b:
								msg = new RequestExChangeName();
								break;
							case 0x3c:
								msg = new RequestAllCastleInfo();
								break;
							case 0x3d:
								msg = new RequestAllFortressInfo();
								break;
							case 0x3e:
								msg = new RequestAllAgitInfo();
								break;
							case 0x3f:
								msg = new RequestFortressSiegeInfo();
								break;
							case 0x40:
								msg = new RequestGetBossRecord();
								break;
							case 0x41:
								msg = new RequestRefine();
								break;
							case 0x42:
								msg = new RequestConfirmCancelItem();
								break;
							case 0x43:
								msg = new RequestRefineCancel();
								break;
							case 0x44:
								msg = new RequestExMagicSkillUseGround();
								break;
							case 0x45:
								msg = new RequestDuelSurrender();
								break;
							case 0x46:
								msg = new RequestExEnchantSkillInfoDetail();
								break;
							/*case 0x47: ?*/
							case 0x48:
								msg = new RequestFortressMapInfo();
								break;
							case 0x49:
								msg = new RequestPVPMatchRecord();
								break;
							case 0x4a:
								msg = new SetPrivateStoreWholeMsg();
								break;
							case 0x4b:
								msg = new RequestDispel();
								break;
							case 0x4c:
								msg = new RequestExTryToPutEnchantTargetItem();
								break;
							case 0x4d:
								msg = new RequestExTryToPutEnchantSupportItem();
								break;
							case 0x4e:
								msg = new RequestExCancelEnchantItem();
								break;
							case 0x4f:
								msg = new RequestChangeNicknameColor();
								break;
							case 0x50:
								msg = new RequestResetNickname();
								break;
							case 0xb8:
								msg = new RequestChangeAttributeItem();
								break;
							case 0xb9:
								msg = new RequestChangeAttributeCancel();
								break;
							case 0x51:
								if(data.remaining() < 4)
								{
									handleIncompletePacket(client);
									break;
								}
								int id4 = data.getInt();
								switch(id4)
								{
									case 0x00:
										msg = new RequestBookMarkSlotInfo();
										break;
									case 0x01:
										msg = new RequestSaveBookMarkSlot();
										break;
									case 0x02:
										msg = new RequestModifyBookMarkSlot();
										break;
									case 0x03:
										msg = new RequestDeleteBookMarkSlot();
										break;
									case 0x04:
										msg = new RequestTeleportBookMark();
										break;
									case 0x05:
										msg = new RequestChangeBookMarkSlot();
										break;
									default:
										_log.warning("Unknown BookMark packet: " + id4);
										break;
								}
								break;
							case 0x52:
								//msg = new RequestWithDrawPremiumItem(); QQ
								break;
							case 0x53:
								msg = new RequestExJump();
								break;
							case 0x54:
								msg = new RequestExStartShowCrataeCubeRank();
								break;
							case 0x55:
								msg = new RequestExStopShowCrataeCubeRank();
								break;
							case 0x56:
								msg = new NotifyStartMiniGame();
								break;
							case 0x57:
								msg = new RequestExJoinDominionWar();
								break;
							case 0x58:
								msg = new RequestExDominionInfo();
								break;
							case 0x59:
								msg = new RequestExCleftEnter();
								break;
							case 0x5A:
								msg = new RequestExBlockGameEnter();
								break;
							case 0x5B:
								msg = new RequestExEndScenePlayer();
								break;
							case 0x5C:
								msg = new RequestExBlockGameVote();
								break;
							case 0x5D:
								msg = new RequestExListMpccWaiting();
								break;
							case 0x5E:
								msg = new RequestExManageMpccRoom();
								break;
							case 0x5F:
								msg = new RequestExJoinMpccRoom();
								break;
							case 0x60:
								msg = new RequestExOustFromMpccRoom();
								break;
							case 0x61:
								msg = new RequestExDismissMpccRoom();
								break;
							case 0x62:
								msg = new RequestExWithdrawMpccRoom();
								break;
							case 0x63:
								msg = new RequestExSeedPhase();
								break;
							case 0x64:
								msg = new RequestExMpccPartymasterList();
								break;
							case 0x65:
								msg = new RequestExPostItemList();
								break;
							case 0x66:
								msg = new RequestExSendPost();
								break;
							case 0x67:
								msg = new RequestExRequestReceivedPostList();
								break;
							case 0x68:
								msg = new RequestExDeleteReceivedPost();
								break;
							case 0x69:
								msg = new RequestExRequestReceivedPost();
								break;
							case 0x6A:
								msg = new RequestExReceivePost();
								break;
							case 0x6B:
								msg = new RequestExRejectPost();
								break;
							case 0x6C:
								msg = new RequestExRequestSentPostList();
								break;
							case 0x6D:
								msg = new RequestExDeleteSentPost();
								break;
							case 0x6E:
								msg = new RequestExRequestSentPost();
								break;
							case 0x6F:
								msg = new RequestExCancelSentPost(); //100
								break;
							case 0x70:
								msg = new RequestExShowNewUserPetition();
								break;
							case 0x71:
								msg = new RequestExShowStepTwo();
								break;
							case 0x72:
								msg = new RequestExShowStepThree();
								break;
							case 0x75:
								msg = new RequestExRefundItem();
								break;
							case 0x76:
								msg = new RequestExBuySellUIClose(); // закрытие окна торговли с npc, trigger
								break;
							case 0x77:
								msg = new RequestExEventMatchObserverEnd();
								break;
							case 0x78:
								msg = new RequestExBR_GamePoint();
								break;
							case 0x79:
								msg = new RequestExBR_ProductList();
								break;
							case 0x7A:
								msg = new RequestExBR_ProductInfo();
								break;
							case 0x7B:
								msg = new RequestExBR_BuyProduct();
								break;
							case 0x7C:
								msg = new RequestExBR_RecentProductList();
								break;
							case 0x7D:
								msg = new RequestExBR_EventRankerList();
								break;
							case 0x7E:
								msg = new RequestBR_MinigameLoadScores();
								break;
							case 0x7F:
								msg = new RequestBR_MinigameInsertScore();
								break;
							case 0x80:
								msg = new RequstBR_LectureMark();
								break;
							case 0x81:
								msg = new RequestExBR_ProductInfo();
								break;
							case 0x82:
								msg = new RequestExBR_BuyProduct();
								break;
							case 0x83:
								msg = new RequestExBR_RecentProductList();
								break;
							case 0x84:
								msg = new RequestBR_MinigameLoadScores();
								break;
							case 0x85:
								msg = new RequestBR_MinigameInsertScore();
								break;
							case 0x86:
								msg = new RequstBR_LectureMark();
								break;
							case 0x91:
								msg = new RequestCrystallizeEstimate();
								break;
							case 0x92:
								// msg = new RequestCrystallizeItemCancel(); 
								break;
							case 0x93:
								// msg = RequestExEscapeScene(); 
								break;
							case 0x94:
								msg = new RequestFlyMove();
								break;	
							case 0x95:
								//msg = new RequestSurrenderPledgeWarEX();
								break;
							case 0x96:
								//msg = new RequestDynamicQuestHTML();
								break;
							case 0x97:
								//msg = new RequestFriendDetailInfo();
								break;		
							case 0x98:
								//msg = new RequestUpdateFriendMemo();
								break;			
							case 0x99:
								//msg = new RequestUpdateBlockMemo();
								break;				
							case 0x9A:
								//msg = new RequestInzonePartyInfoHistory();
								break;					
							case 0x9B:
								//msg = new RequestCommissionRegistrableItemList();
								break;						
							case 0x9C:
								//msg = new RequestCommissionInfo();
								break;							
							case 0x9D:
								//msg = new RequestCommissionRegister();
								break;		
							case 0x9E: 
								//msg= new RequestCommissionCancel();
								break;
							case 0x9F: 
								//msg= new RequestCommissionDelete();
								break;
							case 0xA0: 
								//msg= new RequestCommissionList();
								break;
							case 0xA1: 
								//msg= new RequestCommissionBuyInfo();
								break;
							case 0xA2: 
								//msg= new RequestCommissionBuyItem();
								break;
							case 0xA3: 
								//msg= new RequestCommissionRegisteredItem();
								break;
							case 0xA4: 
								msg= new RequestCallToChangeClass();
								break;
							case 0xA5: 
								msg= new RequestChangeToAwakenedClass();
								break;
							case 0xA6: 
								msg= new RequestWorldStatistics();
								break;
							case 0xA7: 
								//msg= new RequestUserStatistics();
								break;
							case 0xA8: 
								//msg= new RequestRegistPartySubstitute();
								break;
							case 0xA9: 
								//msg= new RequestDeletePartySubstitute();
								break;
							case 0xAA: 
								msg= new RequestRegistWaitingSubstitute();
								break;
							case 0xAB: 
								//msg= new RequestAcceptWaitingSubstitute();
								break;
							case 0xAC: 
								//msg = new Request24HzSessionID();
								break;
							case 0xB1: 
								//msg= new RequestGoodsInventoryInfo();
								break;
							case 0xB2: 
								//msg= new RequestUseGoodsInventoryItem();
								break;
							case 0xB3: 
								//msg= new RequestFirstPlayStart();
								break;
							case 0xB4:
								msg = new RequestFlyMoveStart();	
								break;
							case 0xB7:
								msg = new SendChangeAttributeTargetItem();
								break;
							default:
								int size = data.remaining();
								_log.warning("Unknown Packet: 0x"+Integer.toHexString(id)+":0x" + Integer.toHexString(id2)+" Client: "+client.toString());
								byte[] array = new byte[size];
								data.get(array);
								_log.warning(Util.printData(array, size));
								client.onClientPacketFail();
								break;
						}
						break;
					default:
					{
						try
						{
							int sz = data.remaining();
							activeChar = client.getActiveChar();
							byte[] arr = new byte[sz];
							data.get(arr);
							_log.warning(Util.printData(arr, sz));
							client.onClientPacketFail();
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						break;
					}
				}
				break;
		}
		return msg;
	}

	// impl
	public L2GameClient create(MMOConnection<L2GameClient> con)
	{
		return new L2GameClient(con);
	}

	public void execute(ReceivablePacket<L2GameClient> rp)
	{
		try
		{
			if(rp.getClient().getState() == IN_GAME)
				ThreadPoolManager.getInstance().executePacket(rp);
			else
				ThreadPoolManager.getInstance().executeIOPacket(rp);
		}
		catch(RejectedExecutionException e)
		{
			// if the server is shutdown we ignore
			if(!ThreadPoolManager.getInstance().isShutdown())
				_log.severe("Failed executing: " + rp.getClass().getSimpleName() + " for Client: " + rp.getClient().toString());
		}
	}

	public void handleIncompletePacket(L2GameClient client)
	{
		L2Player activeChar = client.getActiveChar();

		if(activeChar == null)
			_log.warning("Packet not completed. Maybe cheater. IP:" + client.getIpAddr() + ", account:" + client.getLoginName());
		else
			_log.warning("Packet not completed. Maybe cheater. IP:" + client.getIpAddr() + ", account:" + client.getLoginName() + ", character:" + activeChar.getName());

		client.onClientPacketFail();
	}

	@SuppressWarnings("unchecked")
	@Override
	public HeaderInfo handleHeader(SelectionKey key, ByteBuffer buf)
	{
		if(buf.remaining() >= 2)
		{
			int dataPending = (buf.getShort() & 0xffff) - 2;
			L2GameClient client = ((MMOConnection<L2GameClient>) key.attachment()).getClient();
			return getHeaderInfoReturn().set(0, dataPending, client);
		}
		L2GameClient client = ((MMOConnection<L2GameClient>) key.attachment()).getClient();
		return getHeaderInfoReturn().set(2 - buf.remaining(), 0, client);
	}
}

/***
	S->C
	FE:DF - ExShowChannelingEffectPacket
    FE:E0 - ExGetCrystalizingEstimation
    FE:E1 - ExGetCrystalizingFail
    FE:E2 - ExNavitAdventPointInfoPacket
    FE:E3 - ExNavitAdventEffectPacket
    FE:E4 - ExNavitAdventTimeChangePacket
    FE:E5 - ExAbnormalStatusUpdateFromTargetPacket dhddd <=== Бафы Цели
    FE:E6 - ExStopScenePlayerPacket
    FE:E7 - ExFlyMove ddddd ddddd <=== прижки
    FE:E8 - ExDynamicQuestPacket
    FE:E9 - ExSubjobInfo <=== Сабы
    FE:EA - ExChangeMPCost
    FE:EB - ExFriendDetailInfo dSddhhddSddSccdS
    FE:EC - ExBlockAddResult
    FE:ED - ExBlockRemoveResult
    FE:EE - ExBlockDefailInfo
    FE:EF - ExLoadInzonePartyHistory dddShh
    FE:F0 - ExFriendNotifyNameChange
    FE:F1 - ExShowCommission
    FE:F2 - ExResponseCommissionItemList
    FE:F3 - ExResponseCommissionInfo ddQQd
    FE:F4 - ExResponseCommissionRegister
    FE:F5 - ExResponseCommissionDelete
    FE:F6 - ExResponseCommissionList ddd QQdddS
    FE:F7 - ExResponseCommissionBuyInfo QQd
    FE:F8 - ExResponseCommissionBuyItem ddQ
    FE:F9 - ExAcquirableSkillListByClass dddhd dQ dd <==== 3-я панелька в скил листе
    FE:FA - ExMagicAttackInfo ddd
    FE:FB - ExAcquireSkillInfo <==== Изучение скила через панельку 
    FE:FC - ExNewSkillToLearnByLevelUp <== Если есть доступные скилы для изучения - отправляем это тпакет
    FE:FD - ExCallToChangeClass dd <=== Запрос на начало квеста для перерождения
    FE:FE - ExChangeToAwakenedClass <=== Запрос на перерождение
    FE:FF - ExTacticalSign dd
    FE:100 - ExLoadStatWorldRank dd
    FE:101 - ExLoadStatUser ddQQ
    FE:102 - ExLoadStatHotLink dd hdSQhdd hdSQhdd
    FE:103 - ExWaitWaitingSubStituteInfo <--- Поиск групы
    FE:104 - ExRegistWaitingSubstituteOk ddddddd
    FE:105 - ExRegistPartySubstitute dd
    FE:106 - ExDeletePartySubstitute dd
    FE:107 - ExTimeOverPartySubstitute dd
    FE:108 - ExGet24HzSessionID dd
    FE:109 - Ex2NDPasswordCheckPacket dd
    FE:10A - Ex2NDPasswordVerifyPacket dd
    FE:10B - Ex2NDPasswordAckPacket cdd
    FE:10C - ExFlyMoveBroadcast dddddddddd
    FE:10D - ExShowUsmPacket
    FE:10E - ExShowStatPage
    FE:10F - ExIsCharNameCreatable dd
    FE:110 - ExGoodsInventoryChangedNotiPacket
    FE:111 - ExGoodsInventoryInfoPacket QcSdQ QdSSQccSSh dd
    FE:112 - ExGoodsInventoryResultPacket
    FE:113 - ExAlterSkillRequest ddd
    FE:114 - ExNotifyFlyMoveStart
    FE:115 - ExDummyPacket
    FE:116 - ExCloseCommission
***/