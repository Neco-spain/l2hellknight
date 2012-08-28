package net.sf.l2j.gameserver.network;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.clientpackets.Action;
import net.sf.l2j.gameserver.network.clientpackets.AddTradeItem;
import net.sf.l2j.gameserver.network.clientpackets.AllyDismiss;
import net.sf.l2j.gameserver.network.clientpackets.AllyLeave;
import net.sf.l2j.gameserver.network.clientpackets.AnswerJoinPartyRoom;
import net.sf.l2j.gameserver.network.clientpackets.AnswerTradeRequest;
import net.sf.l2j.gameserver.network.clientpackets.Appearing;
import net.sf.l2j.gameserver.network.clientpackets.AttackRequest;
import net.sf.l2j.gameserver.network.clientpackets.AuthLogin;
import net.sf.l2j.gameserver.network.clientpackets.CannotMoveAnymore;
import net.sf.l2j.gameserver.network.clientpackets.CannotMoveAnymoreInVehicle;
import net.sf.l2j.gameserver.network.clientpackets.ChangeMoveType2;
import net.sf.l2j.gameserver.network.clientpackets.ChangeWaitType2;
import net.sf.l2j.gameserver.network.clientpackets.CharacterCreate;
import net.sf.l2j.gameserver.network.clientpackets.CharacterDelete;
import net.sf.l2j.gameserver.network.clientpackets.CharacterRestore;
import net.sf.l2j.gameserver.network.clientpackets.CharacterSelected;
import net.sf.l2j.gameserver.network.clientpackets.DlgAnswer;
import net.sf.l2j.gameserver.network.clientpackets.EnterWorld;
import net.sf.l2j.gameserver.network.clientpackets.FinishRotating;
import net.sf.l2j.gameserver.network.clientpackets.GameGuardReply;
import net.sf.l2j.gameserver.network.clientpackets.L2GameClientPacket;
import net.sf.l2j.gameserver.network.clientpackets.Logout;
import net.sf.l2j.gameserver.network.clientpackets.MoveBackwardToLocation;
import net.sf.l2j.gameserver.network.clientpackets.MultiSellChoose;
import net.sf.l2j.gameserver.network.clientpackets.NewCharacter;
import net.sf.l2j.gameserver.network.clientpackets.ObserverReturn;
import net.sf.l2j.gameserver.network.clientpackets.ProtocolVersion;
import net.sf.l2j.gameserver.network.clientpackets.RequestActionUse;
import net.sf.l2j.gameserver.network.clientpackets.RequestAllyCrest;
import net.sf.l2j.gameserver.network.clientpackets.RequestAllyInfo;
import net.sf.l2j.gameserver.network.clientpackets.RequestAnswerFriendInvite;
import net.sf.l2j.gameserver.network.clientpackets.RequestAnswerJoinAlly;
import net.sf.l2j.gameserver.network.clientpackets.RequestAnswerJoinParty;
import net.sf.l2j.gameserver.network.clientpackets.RequestAnswerJoinPledge;
import net.sf.l2j.gameserver.network.clientpackets.RequestAquireSkill;
import net.sf.l2j.gameserver.network.clientpackets.RequestAquireSkillInfo;
import net.sf.l2j.gameserver.network.clientpackets.RequestAskJoinPartyRoom;
import net.sf.l2j.gameserver.network.clientpackets.RequestAutoSoulShot;
import net.sf.l2j.gameserver.network.clientpackets.RequestBBSwrite;
import net.sf.l2j.gameserver.network.clientpackets.RequestBlock;
import net.sf.l2j.gameserver.network.clientpackets.RequestBuyItem;
import net.sf.l2j.gameserver.network.clientpackets.RequestBuyProcure;
import net.sf.l2j.gameserver.network.clientpackets.RequestBuySeed;
import net.sf.l2j.gameserver.network.clientpackets.RequestBypassToServer;
import net.sf.l2j.gameserver.network.clientpackets.RequestChangePartyLeader;
import net.sf.l2j.gameserver.network.clientpackets.RequestChangePetName;
import net.sf.l2j.gameserver.network.clientpackets.RequestConfirmCancelItem;
import net.sf.l2j.gameserver.network.clientpackets.RequestConfirmGemStone;
import net.sf.l2j.gameserver.network.clientpackets.RequestConfirmRefinerItem;
import net.sf.l2j.gameserver.network.clientpackets.RequestConfirmSiegeWaitingList;
import net.sf.l2j.gameserver.network.clientpackets.RequestConfirmTargetItem;
import net.sf.l2j.gameserver.network.clientpackets.RequestCrystallizeItem;
import net.sf.l2j.gameserver.network.clientpackets.RequestCursedWeaponList;
import net.sf.l2j.gameserver.network.clientpackets.RequestCursedWeaponLocation;
import net.sf.l2j.gameserver.network.clientpackets.RequestDeleteMacro;
import net.sf.l2j.gameserver.network.clientpackets.RequestDestroyItem;
import net.sf.l2j.gameserver.network.clientpackets.RequestDismissAlly;
import net.sf.l2j.gameserver.network.clientpackets.RequestDismissPartyRoom;
import net.sf.l2j.gameserver.network.clientpackets.RequestDuelAnswerStart;
import net.sf.l2j.gameserver.network.clientpackets.RequestDuelStart;
import net.sf.l2j.gameserver.network.clientpackets.RequestDuelSurrender;
import net.sf.l2j.gameserver.network.clientpackets.RequestEnchantItemAlt;
import net.sf.l2j.gameserver.network.clientpackets.RequestEvaluate;
import net.sf.l2j.gameserver.network.clientpackets.RequestExAcceptJoinMPCC;
import net.sf.l2j.gameserver.network.clientpackets.RequestExAskJoinMPCC;
import net.sf.l2j.gameserver.network.clientpackets.RequestExEnchantSkill;
import net.sf.l2j.gameserver.network.clientpackets.RequestExEnchantSkillInfo;
import net.sf.l2j.gameserver.network.clientpackets.RequestExFishRanking;
import net.sf.l2j.gameserver.network.clientpackets.RequestExMPCCShowPartyMembersInfo;
import net.sf.l2j.gameserver.network.clientpackets.RequestExMagicSkillUseGround;
import net.sf.l2j.gameserver.network.clientpackets.RequestExOustFromMPCC;
import net.sf.l2j.gameserver.network.clientpackets.RequestExPledgeCrestLarge;
import net.sf.l2j.gameserver.network.clientpackets.RequestExSetPledgeCrestLarge;
import net.sf.l2j.gameserver.network.clientpackets.RequestExitPartyMatchingWaitingRoom;
import net.sf.l2j.gameserver.network.clientpackets.RequestFriendDel;
import net.sf.l2j.gameserver.network.clientpackets.RequestFriendInvite;
import net.sf.l2j.gameserver.network.clientpackets.RequestFriendList;
import net.sf.l2j.gameserver.network.clientpackets.RequestGMCommand;
import net.sf.l2j.gameserver.network.clientpackets.RequestGetItemFromPet;
import net.sf.l2j.gameserver.network.clientpackets.RequestGetOffVehicle;
import net.sf.l2j.gameserver.network.clientpackets.RequestGetOnVehicle;
import net.sf.l2j.gameserver.network.clientpackets.RequestGiveItemToPet;
import net.sf.l2j.gameserver.network.clientpackets.RequestGiveNickName;
import net.sf.l2j.gameserver.network.clientpackets.RequestGmList;
import net.sf.l2j.gameserver.network.clientpackets.RequestHennaEquip;
import net.sf.l2j.gameserver.network.clientpackets.RequestHennaItemInfo;
import net.sf.l2j.gameserver.network.clientpackets.RequestHennaList;
import net.sf.l2j.gameserver.network.clientpackets.RequestItemList;
import net.sf.l2j.gameserver.network.clientpackets.RequestJoinAlly;
import net.sf.l2j.gameserver.network.clientpackets.RequestJoinParty;
import net.sf.l2j.gameserver.network.clientpackets.RequestJoinPledge;
import net.sf.l2j.gameserver.network.clientpackets.RequestJoinSiege;
import net.sf.l2j.gameserver.network.clientpackets.RequestLinkHtml;
import net.sf.l2j.gameserver.network.clientpackets.RequestListPartyMatchingWaitingRoom;
import net.sf.l2j.gameserver.network.clientpackets.RequestMagicSkillList;
import net.sf.l2j.gameserver.network.clientpackets.RequestMagicSkillUse;
import net.sf.l2j.gameserver.network.clientpackets.RequestMakeMacro;
import net.sf.l2j.gameserver.network.clientpackets.RequestManorList;
import net.sf.l2j.gameserver.network.clientpackets.RequestMoveToLocationInVehicle;
import net.sf.l2j.gameserver.network.clientpackets.RequestOlympiadMatchList;
import net.sf.l2j.gameserver.network.clientpackets.RequestOlympiadObserverEnd;
import net.sf.l2j.gameserver.network.clientpackets.RequestOustFromPartyRoom;
import net.sf.l2j.gameserver.network.clientpackets.RequestOustPartyMember;
import net.sf.l2j.gameserver.network.clientpackets.RequestOustPledgeMember;
import net.sf.l2j.gameserver.network.clientpackets.RequestPCCafeCouponUse;
import net.sf.l2j.gameserver.network.clientpackets.RequestPackageSend;
import net.sf.l2j.gameserver.network.clientpackets.RequestPackageSendableItemList;
import net.sf.l2j.gameserver.network.clientpackets.RequestPartyMatchConfig;
import net.sf.l2j.gameserver.network.clientpackets.RequestPartyMatchDetail;
import net.sf.l2j.gameserver.network.clientpackets.RequestPartyMatchList;
import net.sf.l2j.gameserver.network.clientpackets.RequestPetGetItem;
import net.sf.l2j.gameserver.network.clientpackets.RequestPetUseItem;
import net.sf.l2j.gameserver.network.clientpackets.RequestPetition;
import net.sf.l2j.gameserver.network.clientpackets.RequestPetitionCancel;
import net.sf.l2j.gameserver.network.clientpackets.RequestPledgeCrest;
import net.sf.l2j.gameserver.network.clientpackets.RequestPledgeInfo;
import net.sf.l2j.gameserver.network.clientpackets.RequestPledgeMemberInfo;
import net.sf.l2j.gameserver.network.clientpackets.RequestPledgeMemberList;
import net.sf.l2j.gameserver.network.clientpackets.RequestPledgeMemberPowerInfo;
import net.sf.l2j.gameserver.network.clientpackets.RequestPledgePower;
import net.sf.l2j.gameserver.network.clientpackets.RequestPledgePowerGradeList;
import net.sf.l2j.gameserver.network.clientpackets.RequestPledgeReorganizeMember;
import net.sf.l2j.gameserver.network.clientpackets.RequestPledgeSetAcademyMaster;
import net.sf.l2j.gameserver.network.clientpackets.RequestPledgeSetMemberPowerGrade;
import net.sf.l2j.gameserver.network.clientpackets.RequestPledgeWarList;
import net.sf.l2j.gameserver.network.clientpackets.RequestPrivateStoreBuy;
import net.sf.l2j.gameserver.network.clientpackets.RequestPrivateStoreManageBuy;
import net.sf.l2j.gameserver.network.clientpackets.RequestPrivateStoreManageSell;
import net.sf.l2j.gameserver.network.clientpackets.RequestPrivateStoreQuitBuy;
import net.sf.l2j.gameserver.network.clientpackets.RequestPrivateStoreQuitSell;
import net.sf.l2j.gameserver.network.clientpackets.RequestPrivateStoreSell;
import net.sf.l2j.gameserver.network.clientpackets.RequestProcureCropList;
import net.sf.l2j.gameserver.network.clientpackets.RequestQuestAbort;
import net.sf.l2j.gameserver.network.clientpackets.RequestQuestList;
import net.sf.l2j.gameserver.network.clientpackets.RequestRecipeBookDestroy;
import net.sf.l2j.gameserver.network.clientpackets.RequestRecipeBookOpen;
import net.sf.l2j.gameserver.network.clientpackets.RequestRecipeItemMakeInfo;
import net.sf.l2j.gameserver.network.clientpackets.RequestRecipeItemMakeSelf;
import net.sf.l2j.gameserver.network.clientpackets.RequestRecipeShopListSet;
import net.sf.l2j.gameserver.network.clientpackets.RequestRecipeShopMakeInfo;
import net.sf.l2j.gameserver.network.clientpackets.RequestRecipeShopMakeItem;
import net.sf.l2j.gameserver.network.clientpackets.RequestRecipeShopManagePrev;
import net.sf.l2j.gameserver.network.clientpackets.RequestRecipeShopManageQuit;
import net.sf.l2j.gameserver.network.clientpackets.RequestRecipeShopMessageSet;
import net.sf.l2j.gameserver.network.clientpackets.RequestRecordInfo;
import net.sf.l2j.gameserver.network.clientpackets.RequestRefine;
import net.sf.l2j.gameserver.network.clientpackets.RequestRefineCancel;
import net.sf.l2j.gameserver.network.clientpackets.RequestReplyStartPledgeWar;
import net.sf.l2j.gameserver.network.clientpackets.RequestReplyStopPledgeWar;
import net.sf.l2j.gameserver.network.clientpackets.RequestReplySurrenderPledgeWar;
import net.sf.l2j.gameserver.network.clientpackets.RequestRestart;
import net.sf.l2j.gameserver.network.clientpackets.RequestRestartPoint;
import net.sf.l2j.gameserver.network.clientpackets.RequestSSQStatus;
import net.sf.l2j.gameserver.network.clientpackets.RequestSellItem;
import net.sf.l2j.gameserver.network.clientpackets.RequestSendFriendMsg;
import net.sf.l2j.gameserver.network.clientpackets.RequestSetAllyCrest;
import net.sf.l2j.gameserver.network.clientpackets.RequestSetCrop;
import net.sf.l2j.gameserver.network.clientpackets.RequestSetPledgeCrest;
import net.sf.l2j.gameserver.network.clientpackets.RequestSetSeed;
import net.sf.l2j.gameserver.network.clientpackets.RequestShortCutDel;
import net.sf.l2j.gameserver.network.clientpackets.RequestShortCutReg;
import net.sf.l2j.gameserver.network.clientpackets.RequestShowBoard;
import net.sf.l2j.gameserver.network.clientpackets.RequestShowMiniMap;
import net.sf.l2j.gameserver.network.clientpackets.RequestSiegeAttackerList;
import net.sf.l2j.gameserver.network.clientpackets.RequestSiegeDefenderList;
import net.sf.l2j.gameserver.network.clientpackets.RequestSkillCoolTime;
import net.sf.l2j.gameserver.network.clientpackets.RequestSkillList;
import net.sf.l2j.gameserver.network.clientpackets.RequestSocialAction;
import net.sf.l2j.gameserver.network.clientpackets.RequestStartPledgeWar;
import net.sf.l2j.gameserver.network.clientpackets.RequestStopPledgeWar;
import net.sf.l2j.gameserver.network.clientpackets.RequestSurrenderPersonally;
import net.sf.l2j.gameserver.network.clientpackets.RequestSurrenderPledgeWar;
import net.sf.l2j.gameserver.network.clientpackets.RequestTargetCanceld;
import net.sf.l2j.gameserver.network.clientpackets.RequestTutorialLinkHtml;
import net.sf.l2j.gameserver.network.clientpackets.RequestUnEquipItem;
import net.sf.l2j.gameserver.network.clientpackets.RequestUserCommand;
import net.sf.l2j.gameserver.network.clientpackets.RequestWearItem;
import net.sf.l2j.gameserver.network.clientpackets.RequestWithDrawalParty;
import net.sf.l2j.gameserver.network.clientpackets.RequestWithdrawPartyRoom;
import net.sf.l2j.gameserver.network.clientpackets.RequestWithdrawalPledge;
import net.sf.l2j.gameserver.network.clientpackets.RequestWriteHeroWords;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.clientpackets.SendBypassBuildCmd;
import net.sf.l2j.gameserver.network.clientpackets.SendWareHouseDepositList;
import net.sf.l2j.gameserver.network.clientpackets.SendWareHouseWithDrawList;
import net.sf.l2j.gameserver.network.clientpackets.SetPrivateStoreListBuy;
import net.sf.l2j.gameserver.network.clientpackets.SetPrivateStoreListSell;
import net.sf.l2j.gameserver.network.clientpackets.SetPrivateStoreMsgBuy;
import net.sf.l2j.gameserver.network.clientpackets.SetPrivateStoreMsgSell;
import net.sf.l2j.gameserver.network.clientpackets.SnoopQuit;
import net.sf.l2j.gameserver.network.clientpackets.StartRotating;
import net.sf.l2j.gameserver.network.clientpackets.TradeDone;
import net.sf.l2j.gameserver.network.clientpackets.TradeRequest;
import net.sf.l2j.gameserver.network.clientpackets.UseItem;
import net.sf.l2j.gameserver.network.clientpackets.ValidatePosition;
import net.sf.l2j.util.TimeLogger;
import net.sf.l2j.util.Util;
import org.mmocore.network.HeaderInfo;
import org.mmocore.network.IClientFactory;
import org.mmocore.network.IMMOExecutor;
import org.mmocore.network.IPacketHandler;
import org.mmocore.network.MMOConnection;
import org.mmocore.network.ReceivablePacket;
import org.mmocore.network.TCPHeaderHandler;

public final class L2GamePacketHandler extends TCPHeaderHandler<L2GameClient>
  implements IPacketHandler<L2GameClient>, IClientFactory<L2GameClient>, IMMOExecutor<L2GameClient>
{
  private static final Logger _log = Logger.getLogger(L2GamePacketHandler.class.getName());

  public L2GamePacketHandler()
  {
    super(null);
  }

  public ReceivablePacket<L2GameClient> handlePacket(ByteBuffer buf, L2GameClient client)
  {
    int opcode = buf.get() & 0xFF;

    ReceivablePacket msg = null;
    L2GameClient.GameClientState state = client.getState();

    switch (1.$SwitchMap$net$sf$l2j$gameserver$network$L2GameClient$GameClientState[state.ordinal()]) {
    case 1:
      switch (opcode) {
      case 0:
        msg = new ProtocolVersion();
        break;
      case 8:
        msg = new AuthLogin();
        break;
      default:
        client.forcedClose();
      }
      break;
    case 2:
      switch (opcode) {
      case 9:
        msg = new Logout();
        break;
      case 11:
        msg = new CharacterCreate();
        break;
      case 12:
        msg = new CharacterDelete();
        break;
      case 13:
        msg = new CharacterSelected();
        break;
      case 14:
        msg = new NewCharacter();
        break;
      case 98:
        msg = new CharacterRestore();
        break;
      case 104:
        msg = new RequestPledgeCrest();
        break;
      default:
        client.forcedClose();
      }
      break;
    case 3:
      switch (opcode) {
      case 0:
        msg = new ProtocolVersion();
        break;
      case 1:
        msg = new MoveBackwardToLocation();
        break;
      case 3:
        msg = new EnterWorld();
        break;
      case 4:
        msg = new Action();
        break;
      case 9:
        msg = new Logout();
        break;
      case 10:
        msg = new AttackRequest();
        break;
      case 15:
        msg = new RequestItemList();
        break;
      case 17:
        msg = new RequestUnEquipItem();
        break;
      case 18:
        break;
      case 20:
        msg = new UseItem();
        break;
      case 21:
        msg = new TradeRequest();
        break;
      case 22:
        msg = new AddTradeItem();
        break;
      case 23:
        msg = new TradeDone();
        break;
      case 26:
        break;
      case 27:
        msg = new RequestSocialAction();
        break;
      case 28:
        msg = new ChangeMoveType2();
        break;
      case 29:
        msg = new ChangeWaitType2();
        break;
      case 30:
        msg = new RequestSellItem();
        break;
      case 31:
        msg = new RequestBuyItem();
        break;
      case 32:
        msg = new RequestLinkHtml();
        break;
      case 33:
        msg = new RequestBypassToServer();
        break;
      case 34:
        msg = new RequestBBSwrite();
        break;
      case 35:
        break;
      case 36:
        msg = new RequestJoinPledge();
        break;
      case 37:
        msg = new RequestAnswerJoinPledge();
        break;
      case 38:
        msg = new RequestWithdrawalPledge();
        break;
      case 39:
        msg = new RequestOustPledgeMember();
        break;
      case 41:
        msg = new RequestJoinParty();
        break;
      case 42:
        msg = new RequestAnswerJoinParty();
        break;
      case 43:
        msg = new RequestWithDrawalParty();
        break;
      case 44:
        msg = new RequestOustPartyMember();
        break;
      case 45:
        break;
      case 46:
        msg = new RequestMagicSkillList();
        break;
      case 47:
        msg = new RequestMagicSkillUse();
        break;
      case 48:
        msg = new Appearing();
        break;
      case 49:
        msg = new SendWareHouseDepositList();
        break;
      case 50:
        msg = new SendWareHouseWithDrawList();
        break;
      case 51:
        msg = new RequestShortCutReg();
        break;
      case 52:
        break;
      case 53:
        msg = new RequestShortCutDel();
        break;
      case 54:
        msg = new CannotMoveAnymore();
        break;
      case 55:
        msg = new RequestTargetCanceld();
        break;
      case 56:
        msg = new Say2();
        break;
      case 60:
        msg = new RequestPledgeMemberList();
        break;
      case 62:
        break;
      case 63:
        msg = new RequestSkillList();
        break;
      case 66:
        msg = new RequestGetOnVehicle();
        break;
      case 67:
        msg = new RequestGetOffVehicle();
        break;
      case 68:
        msg = new AnswerTradeRequest();
        break;
      case 69:
        msg = new RequestActionUse();
        break;
      case 70:
        msg = new RequestRestart();
        break;
      case 72:
        msg = new ValidatePosition();
        break;
      case 74:
        msg = new StartRotating();
        break;
      case 75:
        msg = new FinishRotating();
        break;
      case 77:
        msg = new RequestStartPledgeWar();
        break;
      case 78:
        msg = new RequestReplyStartPledgeWar();
        break;
      case 79:
        msg = new RequestStopPledgeWar();
        break;
      case 80:
        msg = new RequestReplyStopPledgeWar();
        break;
      case 81:
        msg = new RequestSurrenderPledgeWar();
        break;
      case 82:
        msg = new RequestReplySurrenderPledgeWar();
        break;
      case 83:
        msg = new RequestSetPledgeCrest();
        break;
      case 85:
        msg = new RequestGiveNickName();
        break;
      case 87:
        msg = new RequestShowBoard();
        break;
      case 88:
        msg = new RequestEnchantItemAlt();
        break;
      case 89:
        msg = new RequestDestroyItem();
        break;
      case 91:
        msg = new SendBypassBuildCmd();
        break;
      case 92:
        msg = new RequestMoveToLocationInVehicle();
        break;
      case 93:
        msg = new CannotMoveAnymoreInVehicle();
        break;
      case 94:
        msg = new RequestFriendInvite();
        break;
      case 95:
        msg = new RequestAnswerFriendInvite();
        break;
      case 96:
        msg = new RequestFriendList();
        break;
      case 97:
        msg = new RequestFriendDel();
        break;
      case 99:
        msg = new RequestQuestList();
        break;
      case 100:
        msg = new RequestQuestAbort();
        break;
      case 102:
        msg = new RequestPledgeInfo();
        break;
      case 104:
        msg = new RequestPledgeCrest();
        break;
      case 105:
        msg = new RequestSurrenderPersonally();
        break;
      case 107:
        msg = new RequestAquireSkillInfo();
        break;
      case 108:
        msg = new RequestAquireSkill();
        break;
      case 109:
        msg = new RequestRestartPoint();
        break;
      case 110:
        msg = new RequestGMCommand();
        break;
      case 111:
        msg = new RequestPartyMatchList();
        break;
      case 112:
        msg = new RequestPartyMatchConfig();
        break;
      case 113:
        msg = new RequestPartyMatchDetail();
        break;
      case 114:
        msg = new RequestCrystallizeItem();
        break;
      case 115:
        msg = new RequestPrivateStoreManageSell();
        break;
      case 116:
        msg = new SetPrivateStoreListSell();
        break;
      case 118:
        msg = new RequestPrivateStoreQuitSell();
        break;
      case 119:
        msg = new SetPrivateStoreMsgSell();
        break;
      case 121:
        msg = new RequestPrivateStoreBuy();
        break;
      case 123:
        msg = new RequestTutorialLinkHtml();
        break;
      case 127:
        msg = new RequestPetition();
        break;
      case 128:
        msg = new RequestPetitionCancel();
        break;
      case 129:
        msg = new RequestGmList();
        break;
      case 130:
        msg = new RequestJoinAlly();
        break;
      case 131:
        msg = new RequestAnswerJoinAlly();
        break;
      case 132:
        msg = new AllyLeave();
        break;
      case 133:
        msg = new AllyDismiss();
        break;
      case 134:
        msg = new RequestDismissAlly();
        break;
      case 135:
        msg = new RequestSetAllyCrest();
        break;
      case 136:
        msg = new RequestAllyCrest();
        break;
      case 137:
        msg = new RequestChangePetName();
        break;
      case 138:
        msg = new RequestPetUseItem();
        break;
      case 139:
        msg = new RequestGiveItemToPet();
        break;
      case 140:
        msg = new RequestGetItemFromPet();
        break;
      case 142:
        msg = new RequestAllyInfo();
        break;
      case 143:
        msg = new RequestPetGetItem();
        break;
      case 144:
        msg = new RequestPrivateStoreManageBuy();
        break;
      case 145:
        msg = new SetPrivateStoreListBuy();
        break;
      case 147:
        msg = new RequestPrivateStoreQuitBuy();
        break;
      case 148:
        msg = new SetPrivateStoreMsgBuy();
        break;
      case 150:
        msg = new RequestPrivateStoreSell();
        break;
      case 157:
        msg = new RequestSkillCoolTime();
        break;
      case 158:
        msg = new RequestPackageSendableItemList();
        break;
      case 159:
        msg = new RequestPackageSend();
        break;
      case 160:
        msg = new RequestBlock();
        break;
      case 162:
        msg = new RequestSiegeAttackerList();
        break;
      case 163:
        msg = new RequestSiegeDefenderList();
        break;
      case 164:
        msg = new RequestJoinSiege();
        break;
      case 165:
        msg = new RequestConfirmSiegeWaitingList();
        break;
      case 167:
        msg = new MultiSellChoose();
        break;
      case 170:
        msg = new RequestUserCommand();
        break;
      case 171:
        msg = new SnoopQuit();
        break;
      case 172:
        msg = new RequestRecipeBookOpen();
        break;
      case 173:
        msg = new RequestRecipeBookDestroy();
        break;
      case 174:
        msg = new RequestRecipeItemMakeInfo();
        break;
      case 175:
        msg = new RequestRecipeItemMakeSelf();
        break;
      case 177:
        msg = new RequestRecipeShopMessageSet();
        break;
      case 178:
        msg = new RequestRecipeShopListSet();
        break;
      case 179:
        msg = new RequestRecipeShopManageQuit();
        break;
      case 181:
        msg = new RequestRecipeShopMakeInfo();
        break;
      case 182:
        msg = new RequestRecipeShopMakeItem();
        break;
      case 183:
        msg = new RequestRecipeShopManagePrev();
        break;
      case 184:
        msg = new ObserverReturn();
        break;
      case 185:
        msg = new RequestEvaluate();
        break;
      case 186:
        msg = new RequestHennaList();
        break;
      case 187:
        msg = new RequestHennaItemInfo();
        break;
      case 188:
        msg = new RequestHennaEquip();
        break;
      case 192:
        msg = new RequestPledgePower();
        break;
      case 193:
        msg = new RequestMakeMacro();
        break;
      case 194:
        msg = new RequestDeleteMacro();
        break;
      case 195:
        msg = new RequestBuyProcure();
        break;
      case 196:
        msg = new RequestBuySeed();
        break;
      case 197:
        msg = new DlgAnswer();
        break;
      case 198:
        msg = new RequestWearItem();
        break;
      case 199:
        msg = new RequestSSQStatus();
        break;
      case 202:
        msg = new GameGuardReply();
        break;
      case 204:
        msg = new RequestSendFriendMsg();
        break;
      case 205:
        msg = new RequestShowMiniMap();
        break;
      case 206:
        break;
      case 207:
        msg = new RequestRecordInfo();
        break;
      case 208:
        int id2 = -1;
        if (buf.remaining() >= 2) {
          id2 = buf.getShort() & 0xFFFF;
        } else {
          _log.warning(TimeLogger.getLogTime() + "Client: " + client.toString() + " sent a 0xd0 without the second opcode.");
          break;
        }

        switch (id2) {
        case 1:
          msg = new RequestOustFromPartyRoom();
          break;
        case 2:
          msg = new RequestDismissPartyRoom();
          break;
        case 3:
          msg = new RequestWithdrawPartyRoom();
          break;
        case 4:
          msg = new RequestChangePartyLeader();
          break;
        case 5:
          msg = new RequestAutoSoulShot();
          break;
        case 6:
          msg = new RequestExEnchantSkillInfo();
          break;
        case 7:
          msg = new RequestExEnchantSkill();
          break;
        case 8:
          msg = new RequestManorList();
          break;
        case 9:
          msg = new RequestProcureCropList();
          break;
        case 10:
          msg = new RequestSetSeed();
          break;
        case 11:
          msg = new RequestSetCrop();
          break;
        case 12:
          msg = new RequestWriteHeroWords();
          break;
        case 13:
          msg = new RequestExAskJoinMPCC();
          break;
        case 14:
          msg = new RequestExAcceptJoinMPCC();
          break;
        case 15:
          msg = new RequestExOustFromMPCC();
          break;
        case 16:
          msg = new RequestExPledgeCrestLarge();
          break;
        case 17:
          msg = new RequestExSetPledgeCrestLarge();
          break;
        case 18:
          msg = new RequestOlympiadObserverEnd();
          break;
        case 19:
          msg = new RequestOlympiadMatchList();
          break;
        case 20:
          msg = new RequestAskJoinPartyRoom();
          break;
        case 21:
          msg = new AnswerJoinPartyRoom();
          break;
        case 22:
          msg = new RequestListPartyMatchingWaitingRoom();
          break;
        case 23:
          msg = new RequestExitPartyMatchingWaitingRoom();
          break;
        case 24:
          break;
        case 25:
          msg = new RequestPledgeSetAcademyMaster();
          break;
        case 26:
          msg = new RequestPledgePowerGradeList();
          break;
        case 27:
          msg = new RequestPledgeMemberPowerInfo();
          break;
        case 28:
          msg = new RequestPledgeSetMemberPowerGrade();
          break;
        case 29:
          msg = new RequestPledgeMemberInfo();
          break;
        case 30:
          msg = new RequestPledgeWarList();
          break;
        case 31:
          msg = new RequestExFishRanking();
          break;
        case 32:
          msg = new RequestPCCafeCouponUse();
          break;
        case 34:
          msg = new RequestCursedWeaponList();
          break;
        case 35:
          msg = new RequestCursedWeaponLocation();
          break;
        case 36:
          msg = new RequestPledgeReorganizeMember();
          break;
        case 38:
          msg = new RequestExMPCCShowPartyMembersInfo();
          break;
        case 39:
          msg = new RequestDuelStart();
          break;
        case 40:
          msg = new RequestDuelAnswerStart();
          break;
        case 41:
          msg = new RequestConfirmTargetItem();
          break;
        case 42:
          msg = new RequestConfirmRefinerItem();
          break;
        case 43:
          msg = new RequestConfirmGemStone();
          break;
        case 44:
          msg = new RequestRefine();
          break;
        case 45:
          msg = new RequestConfirmCancelItem();
          break;
        case 46:
          msg = new RequestRefineCancel();
          break;
        case 47:
          msg = new RequestExMagicSkillUseGround();
          break;
        case 48:
          msg = new RequestDuelSurrender();
          break;
        case 33:
        case 37:
        default:
          printDebugDoubleOpcode(opcode, id2, buf, state, client);
        }break;
      case 14:
      case 16:
      case 19:
      case 57:
      case 84:
      case 242:
        client.forcedClose();
        break;
      case 13:
        break;
      case 2:
      case 5:
      case 6:
      case 7:
      case 8:
      case 11:
      case 12:
      case 24:
      case 25:
      case 40:
      case 58:
      case 59:
      case 61:
      case 64:
      case 65:
      case 71:
      case 73:
      case 76:
      case 86:
      case 90:
      case 98:
      case 101:
      case 103:
      case 106:
      case 117:
      case 120:
      case 122:
      case 124:
      case 125:
      case 126:
      case 141:
      case 146:
      case 149:
      case 151:
      case 152:
      case 153:
      case 154:
      case 155:
      case 156:
      case 161:
      case 166:
      case 168:
      case 169:
      case 176:
      case 180:
      case 189:
      case 190:
      case 191:
      case 200:
      case 201:
      case 203:
      case 209:
      case 210:
      case 211:
      case 212:
      case 213:
      case 214:
      case 215:
      case 216:
      case 217:
      case 218:
      case 219:
      case 220:
      case 221:
      case 222:
      case 223:
      case 224:
      case 225:
      case 226:
      case 227:
      case 228:
      case 229:
      case 230:
      case 231:
      case 232:
      case 233:
      case 234:
      case 235:
      case 236:
      case 237:
      case 238:
      case 239:
      case 240:
      case 241:
      default:
        printDebug(opcode, buf, state, client);
      }

    }

    return msg;
  }

  private void printDebug(int opcode, ByteBuffer buf, L2GameClient.GameClientState state, L2GameClient client) {
    _log.warning(TimeLogger.getLogTime() + "Unknown Packet: " + Integer.toHexString(opcode) + " on State: " + state.name() + " Client: " + client.toString());
    if (state.name().equals("AUTHED")) {
      client.forcedClose();
      return;
    }
    int size = buf.remaining();
    byte[] array = new byte[size];
    buf.get(array);
    _log.warning(Util.printData(array, size));
    unknownPacketProtection(client);
  }

  private void printDebugDoubleOpcode(int opcode, int id2, ByteBuffer buf, L2GameClient.GameClientState state, L2GameClient client) {
    int size = buf.remaining();
    _log.warning(TimeLogger.getLogTime() + "Unknown Packet: " + Integer.toHexString(opcode) + ":" + Integer.toHexString(id2) + " on State: " + state.name() + " Client: " + client.toString());
    byte[] array = new byte[size];
    buf.get(array);
    _log.warning(Util.printData(array, size));
    unknownPacketProtection(client);
  }

  public void unknownPacketProtection(L2GameClient client) {
    try {
      if (client.getUPTryes() > 4) {
        L2PcInstance player = client.getActiveChar();
        if (player == null) {
          _log.warning(TimeLogger.getLogTime() + "Too many unknown packets, connection closed. IP: " + client.getIpAddr() + ", account:" + client.getAccountName());
          client.forcedClose();
          return;
        }
        _log.warning(TimeLogger.getLogTime() + "Too many unknown packets, connection closed. IP: " + client.getIpAddr() + ", account:" + client.getAccountName() + ", character:" + player.getName());
        player.kick();
      } else {
        client.addUPTryes();
      }
    }
    catch (Exception e)
    {
    }
  }

  public L2GameClient create(MMOConnection<L2GameClient> con) {
    return new L2GameClient(con);
  }

  public void execute(ReceivablePacket<L2GameClient> rp) {
    try {
      if (((L2GameClient)rp.getClient()).getState() == L2GameClient.GameClientState.IN_GAME)
        ThreadPoolManager.getInstance().executePacket((L2GameClientPacket)rp);
      else {
        ThreadPoolManager.getInstance().executeIOPacket((L2GameClientPacket)rp);
      }

    }
    catch (RejectedExecutionException e)
    {
      if (!ThreadPoolManager.getInstance().isShutdown())
        _log.severe(TimeLogger.getLogTime() + "Failed executing: " + rp.getClass().getSimpleName() + " for Client: " + ((L2GameClient)rp.getClient()).toString());
    }
  }

  public HeaderInfo handleHeader(SelectionKey key, ByteBuffer buf)
  {
    if (buf.remaining() >= 2)
    {
      int dataPending = (buf.getShort() & 0xFFFF) - 2;

      L2GameClient client = (L2GameClient)((MMOConnection)key.attachment()).getClient();

      return getHeaderInfoReturn().set(0, dataPending, false, client);
    }
    L2GameClient client = (L2GameClient)((MMOConnection)key.attachment()).getClient();
    return getHeaderInfoReturn().set(2 - buf.remaining(), 0, false, client);
  }
}