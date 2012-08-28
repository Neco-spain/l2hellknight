package l2p.gameserver.network;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import l2p.commons.net.nio.impl.IClientFactory;
import l2p.commons.net.nio.impl.IMMOExecutor;
import l2p.commons.net.nio.impl.IPacketHandler;
import l2p.commons.net.nio.impl.MMOConnection;
import l2p.commons.net.nio.impl.ReceivablePacket;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.clientpackets.Action;
import l2p.gameserver.clientpackets.AddTradeItem;
import l2p.gameserver.clientpackets.AnswerCoupleAction;
import l2p.gameserver.clientpackets.AnswerJoinPartyRoom;
import l2p.gameserver.clientpackets.AnswerPartyLootModification;
import l2p.gameserver.clientpackets.AnswerTradeRequest;
import l2p.gameserver.clientpackets.Appearing;
import l2p.gameserver.clientpackets.AttackRequest;
import l2p.gameserver.clientpackets.AuthLogin;
import l2p.gameserver.clientpackets.BypassUserCmd;
import l2p.gameserver.clientpackets.CannotMoveAnymore;
import l2p.gameserver.clientpackets.CannotMoveAnymoreInVehicle;
import l2p.gameserver.clientpackets.CharacterCreate;
import l2p.gameserver.clientpackets.CharacterDelete;
import l2p.gameserver.clientpackets.CharacterRestore;
import l2p.gameserver.clientpackets.CharacterSelected;
import l2p.gameserver.clientpackets.ConfirmDlg;
import l2p.gameserver.clientpackets.EnterWorld;
import l2p.gameserver.clientpackets.FinishRotatingC;
import l2p.gameserver.clientpackets.GotoLobby;
import l2p.gameserver.clientpackets.Logout;
import l2p.gameserver.clientpackets.MoveBackwardToLocation;
import l2p.gameserver.clientpackets.MoveWithDelta;
import l2p.gameserver.clientpackets.NetPing;
import l2p.gameserver.clientpackets.NewCharacter;
import l2p.gameserver.clientpackets.NotifyStartMiniGame;
import l2p.gameserver.clientpackets.PetitionVote;
import l2p.gameserver.clientpackets.ProtocolVersion;
import l2p.gameserver.clientpackets.ReplyGameGuardQuery;
import l2p.gameserver.clientpackets.RequestActionUse;
import l2p.gameserver.clientpackets.RequestAddExpandQuestAlarm;
import l2p.gameserver.clientpackets.RequestAllAgitInfo;
import l2p.gameserver.clientpackets.RequestAllCastleInfo;
import l2p.gameserver.clientpackets.RequestAllFortressInfo;
import l2p.gameserver.clientpackets.RequestAllyCrest;
import l2p.gameserver.clientpackets.RequestAllyInfo;
import l2p.gameserver.clientpackets.RequestAnswerJoinAlly;
import l2p.gameserver.clientpackets.RequestAnswerJoinParty;
import l2p.gameserver.clientpackets.RequestAnswerJoinPledge;
import l2p.gameserver.clientpackets.RequestAquireSkill;
import l2p.gameserver.clientpackets.RequestAquireSkillInfo;
import l2p.gameserver.clientpackets.RequestAskJoinPartyRoom;
import l2p.gameserver.clientpackets.RequestAutoSoulShot;
import l2p.gameserver.clientpackets.RequestBBSwrite;
import l2p.gameserver.clientpackets.RequestBR_MiniGameInsertScore;
import l2p.gameserver.clientpackets.RequestBR_MiniGameLoadScores;
import l2p.gameserver.clientpackets.RequestBidItemAuction;
import l2p.gameserver.clientpackets.RequestBlock;
import l2p.gameserver.clientpackets.RequestBookMarkSlotInfo;
import l2p.gameserver.clientpackets.RequestBuyItem;
import l2p.gameserver.clientpackets.RequestBuySeed;
import l2p.gameserver.clientpackets.RequestBypassToServer;
import l2p.gameserver.clientpackets.RequestCastleSiegeAttackerList;
import l2p.gameserver.clientpackets.RequestCastleSiegeDefenderList;
import l2p.gameserver.clientpackets.RequestChangeBookMarkSlot;
import l2p.gameserver.clientpackets.RequestChangeNicknameColor;
import l2p.gameserver.clientpackets.RequestChangePetName;
import l2p.gameserver.clientpackets.RequestConfirmCancelItem;
import l2p.gameserver.clientpackets.RequestConfirmCastleSiegeWaitingList;
import l2p.gameserver.clientpackets.RequestConfirmGemStone;
import l2p.gameserver.clientpackets.RequestConfirmRefinerItem;
import l2p.gameserver.clientpackets.RequestConfirmTargetItem;
import l2p.gameserver.clientpackets.RequestCreatePledge;
import l2p.gameserver.clientpackets.RequestCrystallizeItem;
import l2p.gameserver.clientpackets.RequestCursedWeaponList;
import l2p.gameserver.clientpackets.RequestCursedWeaponLocation;
import l2p.gameserver.clientpackets.RequestDeleteBookMarkSlot;
import l2p.gameserver.clientpackets.RequestDeleteMacro;
import l2p.gameserver.clientpackets.RequestDestroyItem;
import l2p.gameserver.clientpackets.RequestDismissAlly;
import l2p.gameserver.clientpackets.RequestDismissParty;
import l2p.gameserver.clientpackets.RequestDismissPartyRoom;
import l2p.gameserver.clientpackets.RequestDispel;
import l2p.gameserver.clientpackets.RequestDropItem;
import l2p.gameserver.clientpackets.RequestDuelAnswerStart;
import l2p.gameserver.clientpackets.RequestDuelStart;
import l2p.gameserver.clientpackets.RequestDuelSurrender;
import l2p.gameserver.clientpackets.RequestEnchantItem;
import l2p.gameserver.clientpackets.RequestEnchantItemAttribute;
import l2p.gameserver.clientpackets.RequestEx2ndPasswordCheck;
import l2p.gameserver.clientpackets.RequestEx2ndPasswordReq;
import l2p.gameserver.clientpackets.RequestEx2ndPasswordVerify;
import l2p.gameserver.clientpackets.RequestExAddPostFriendForPostBox;
import l2p.gameserver.clientpackets.RequestExBR_BuyProduct;
import l2p.gameserver.clientpackets.RequestExBR_EventRankerList;
import l2p.gameserver.clientpackets.RequestExBR_GamePoint;
import l2p.gameserver.clientpackets.RequestExBR_LectureMark;
import l2p.gameserver.clientpackets.RequestExBR_ProductInfo;
import l2p.gameserver.clientpackets.RequestExBR_ProductList;
import l2p.gameserver.clientpackets.RequestExBR_RecentProductList;
import l2p.gameserver.clientpackets.RequestExBuySellUIClose;
import l2p.gameserver.clientpackets.RequestExCancelEnchantItem;
import l2p.gameserver.clientpackets.RequestExCancelSentPost;
import l2p.gameserver.clientpackets.RequestExChangeName;
import l2p.gameserver.clientpackets.RequestExCleftEnter;
import l2p.gameserver.clientpackets.RequestExCubeGameChangeTeam;
import l2p.gameserver.clientpackets.RequestExCubeGameReadyAnswer;
import l2p.gameserver.clientpackets.RequestExDeletePostFriendForPostBox;
import l2p.gameserver.clientpackets.RequestExDeleteReceivedPost;
import l2p.gameserver.clientpackets.RequestExDeleteSentPost;
import l2p.gameserver.clientpackets.RequestExDismissMpccRoom;
import l2p.gameserver.clientpackets.RequestExDominionInfo;
import l2p.gameserver.clientpackets.RequestExEnchantSkill;
import l2p.gameserver.clientpackets.RequestExEnchantSkillInfo;
import l2p.gameserver.clientpackets.RequestExEnchantSkillInfoDetail;
import l2p.gameserver.clientpackets.RequestExEnchantSkillRouteChange;
import l2p.gameserver.clientpackets.RequestExEnchantSkillSafe;
import l2p.gameserver.clientpackets.RequestExEnchantSkillUntrain;
import l2p.gameserver.clientpackets.RequestExEndScenePlayer;
import l2p.gameserver.clientpackets.RequestExEventMatchObserverEnd;
import l2p.gameserver.clientpackets.RequestExFishRanking;
import l2p.gameserver.clientpackets.RequestExFriendListForPostBox;
import l2p.gameserver.clientpackets.RequestExJoinDominionWar;
import l2p.gameserver.clientpackets.RequestExJoinMpccRoom;
import l2p.gameserver.clientpackets.RequestExJump;
import l2p.gameserver.clientpackets.RequestExListMpccWaiting;
import l2p.gameserver.clientpackets.RequestExMPCCAcceptJoin;
import l2p.gameserver.clientpackets.RequestExMPCCAskJoin;
import l2p.gameserver.clientpackets.RequestExMPCCShowPartyMembersInfo;
import l2p.gameserver.clientpackets.RequestExMagicSkillUseGround;
import l2p.gameserver.clientpackets.RequestExManageMpccRoom;
import l2p.gameserver.clientpackets.RequestExMoveToLocationAirShip;
import l2p.gameserver.clientpackets.RequestExMoveToLocationInAirShip;
import l2p.gameserver.clientpackets.RequestExMpccPartymasterList;
import l2p.gameserver.clientpackets.RequestExOlympiadObserverEnd;
import l2p.gameserver.clientpackets.RequestExOustFromMPCC;
import l2p.gameserver.clientpackets.RequestExOustFromMpccRoom;
import l2p.gameserver.clientpackets.RequestExPostItemList;
import l2p.gameserver.clientpackets.RequestExReceivePost;
import l2p.gameserver.clientpackets.RequestExRefundItem;
import l2p.gameserver.clientpackets.RequestExRejectPost;
import l2p.gameserver.clientpackets.RequestExRemoveItemAttribute;
import l2p.gameserver.clientpackets.RequestExRequestReceivedPost;
import l2p.gameserver.clientpackets.RequestExRequestReceivedPostList;
import l2p.gameserver.clientpackets.RequestExRequestSentPost;
import l2p.gameserver.clientpackets.RequestExRequestSentPostList;
import l2p.gameserver.clientpackets.RequestExRqItemLink;
import l2p.gameserver.clientpackets.RequestExSeedPhase;
import l2p.gameserver.clientpackets.RequestExSendPost;
import l2p.gameserver.clientpackets.RequestExShowNewUserPetition;
import l2p.gameserver.clientpackets.RequestExShowPostFriendListForPostBox;
import l2p.gameserver.clientpackets.RequestExShowStepThree;
import l2p.gameserver.clientpackets.RequestExShowStepTwo;
import l2p.gameserver.clientpackets.RequestExStartShowCrataeCubeRank;
import l2p.gameserver.clientpackets.RequestExStopShowCrataeCubeRank;
import l2p.gameserver.clientpackets.RequestExTryToPutEnchantSupportItem;
import l2p.gameserver.clientpackets.RequestExTryToPutEnchantTargetItem;
import l2p.gameserver.clientpackets.RequestExWithdrawMpccRoom;
import l2p.gameserver.clientpackets.RequestExitPartyMatchingWaitingRoom;
import l2p.gameserver.clientpackets.RequestFortressMapInfo;
import l2p.gameserver.clientpackets.RequestFortressSiegeInfo;
import l2p.gameserver.clientpackets.RequestFriendAddReply;
import l2p.gameserver.clientpackets.RequestFriendDel;
import l2p.gameserver.clientpackets.RequestFriendInvite;
import l2p.gameserver.clientpackets.RequestFriendList;
import l2p.gameserver.clientpackets.RequestGMCommand;
import l2p.gameserver.clientpackets.RequestGetBossRecord;
import l2p.gameserver.clientpackets.RequestGetItemFromPet;
import l2p.gameserver.clientpackets.RequestGetOffVehicle;
import l2p.gameserver.clientpackets.RequestGetOnVehicle;
import l2p.gameserver.clientpackets.RequestGiveItemToPet;
import l2p.gameserver.clientpackets.RequestGiveNickName;
import l2p.gameserver.clientpackets.RequestGmList;
import l2p.gameserver.clientpackets.RequestGoodsInventoryInfo;
import l2p.gameserver.clientpackets.RequestHandOverPartyMaster;
import l2p.gameserver.clientpackets.RequestHennaEquip;
import l2p.gameserver.clientpackets.RequestHennaItemInfo;
import l2p.gameserver.clientpackets.RequestHennaList;
import l2p.gameserver.clientpackets.RequestHennaUnequip;
import l2p.gameserver.clientpackets.RequestHennaUnequipInfo;
import l2p.gameserver.clientpackets.RequestHennaUnequipList;
import l2p.gameserver.clientpackets.RequestInfoItemAuction;
import l2p.gameserver.clientpackets.RequestItemList;
import l2p.gameserver.clientpackets.RequestJoinAlly;
import l2p.gameserver.clientpackets.RequestJoinCastleSiege;
import l2p.gameserver.clientpackets.RequestJoinParty;
import l2p.gameserver.clientpackets.RequestJoinPledge;
import l2p.gameserver.clientpackets.RequestKeyMapping;
import l2p.gameserver.clientpackets.RequestListPartyMatchingWaitingRoom;
import l2p.gameserver.clientpackets.RequestMagicSkillList;
import l2p.gameserver.clientpackets.RequestMagicSkillUse;
import l2p.gameserver.clientpackets.RequestMakeMacro;
import l2p.gameserver.clientpackets.RequestManorList;
import l2p.gameserver.clientpackets.RequestModifyBookMarkSlot;
import l2p.gameserver.clientpackets.RequestMoveToLocationInVehicle;
import l2p.gameserver.clientpackets.RequestMultiSellChoose;
import l2p.gameserver.clientpackets.RequestObserverEnd;
import l2p.gameserver.clientpackets.RequestOlympiadMatchList;
import l2p.gameserver.clientpackets.RequestOlympiadObserverEnd;
import l2p.gameserver.clientpackets.RequestOustAlly;
import l2p.gameserver.clientpackets.RequestOustFromPartyRoom;
import l2p.gameserver.clientpackets.RequestOustPartyMember;
import l2p.gameserver.clientpackets.RequestOustPledgeMember;
import l2p.gameserver.clientpackets.RequestPCCafeCouponUse;
import l2p.gameserver.clientpackets.RequestPVPMatchRecord;
import l2p.gameserver.clientpackets.RequestPackageSend;
import l2p.gameserver.clientpackets.RequestPackageSendableItemList;
import l2p.gameserver.clientpackets.RequestPartyLootModification;
import l2p.gameserver.clientpackets.RequestPartyMatchConfig;
import l2p.gameserver.clientpackets.RequestPartyMatchDetail;
import l2p.gameserver.clientpackets.RequestPartyMatchList;
import l2p.gameserver.clientpackets.RequestPetGetItem;
import l2p.gameserver.clientpackets.RequestPetUseItem;
import l2p.gameserver.clientpackets.RequestPetition;
import l2p.gameserver.clientpackets.RequestPetitionCancel;
import l2p.gameserver.clientpackets.RequestPledgeCrest;
import l2p.gameserver.clientpackets.RequestPledgeCrestLarge;
import l2p.gameserver.clientpackets.RequestPledgeExtendedInfo;
import l2p.gameserver.clientpackets.RequestPledgeInfo;
import l2p.gameserver.clientpackets.RequestPledgeMemberInfo;
import l2p.gameserver.clientpackets.RequestPledgeMemberList;
import l2p.gameserver.clientpackets.RequestPledgeMemberPowerInfo;
import l2p.gameserver.clientpackets.RequestPledgePower;
import l2p.gameserver.clientpackets.RequestPledgePowerGradeList;
import l2p.gameserver.clientpackets.RequestPledgeReorganizeMember;
import l2p.gameserver.clientpackets.RequestPledgeSetAcademyMaster;
import l2p.gameserver.clientpackets.RequestPledgeSetMemberPowerGrade;
import l2p.gameserver.clientpackets.RequestPledgeWarList;
import l2p.gameserver.clientpackets.RequestPreviewItem;
import l2p.gameserver.clientpackets.RequestPrivateStoreBuy;
import l2p.gameserver.clientpackets.RequestPrivateStoreBuyManage;
import l2p.gameserver.clientpackets.RequestPrivateStoreBuySellList;
import l2p.gameserver.clientpackets.RequestPrivateStoreList;
import l2p.gameserver.clientpackets.RequestPrivateStoreQuitBuy;
import l2p.gameserver.clientpackets.RequestPrivateStoreQuitSell;
import l2p.gameserver.clientpackets.RequestProcureCrop;
import l2p.gameserver.clientpackets.RequestProcureCropList;
import l2p.gameserver.clientpackets.RequestQuestAbort;
import l2p.gameserver.clientpackets.RequestQuestList;
import l2p.gameserver.clientpackets.RequestRecipeBookOpen;
import l2p.gameserver.clientpackets.RequestRecipeItemDelete;
import l2p.gameserver.clientpackets.RequestRecipeItemMakeInfo;
import l2p.gameserver.clientpackets.RequestRecipeItemMakeSelf;
import l2p.gameserver.clientpackets.RequestRecipeShopListSet;
import l2p.gameserver.clientpackets.RequestRecipeShopMakeDo;
import l2p.gameserver.clientpackets.RequestRecipeShopMakeInfo;
import l2p.gameserver.clientpackets.RequestRecipeShopManageCancel;
import l2p.gameserver.clientpackets.RequestRecipeShopManageQuit;
import l2p.gameserver.clientpackets.RequestRecipeShopMessageSet;
import l2p.gameserver.clientpackets.RequestRecipeShopSellList;
import l2p.gameserver.clientpackets.RequestRefine;
import l2p.gameserver.clientpackets.RequestRefineCancel;
import l2p.gameserver.clientpackets.RequestReload;
import l2p.gameserver.clientpackets.RequestRemainTime;
import l2p.gameserver.clientpackets.RequestResetNickname;
import l2p.gameserver.clientpackets.RequestRestart;
import l2p.gameserver.clientpackets.RequestRestartPoint;
import l2p.gameserver.clientpackets.RequestSEKCustom;
import l2p.gameserver.clientpackets.RequestSSQStatus;
import l2p.gameserver.clientpackets.RequestSaveBookMarkSlot;
import l2p.gameserver.clientpackets.RequestSaveInventoryOrder;
import l2p.gameserver.clientpackets.RequestSaveKeyMapping;
import l2p.gameserver.clientpackets.RequestSellItem;
import l2p.gameserver.clientpackets.RequestSendL2FriendSay;
import l2p.gameserver.clientpackets.RequestSendMsnChatLog;
import l2p.gameserver.clientpackets.RequestSetAllyCrest;
import l2p.gameserver.clientpackets.RequestSetCastleSiegeTime;
import l2p.gameserver.clientpackets.RequestSetCrop;
import l2p.gameserver.clientpackets.RequestSetPledgeCrest;
import l2p.gameserver.clientpackets.RequestSetPledgeCrestLarge;
import l2p.gameserver.clientpackets.RequestSetSeed;
import l2p.gameserver.clientpackets.RequestShortCutDel;
import l2p.gameserver.clientpackets.RequestShortCutReg;
import l2p.gameserver.clientpackets.RequestShowBoard;
import l2p.gameserver.clientpackets.RequestShowMiniMap;
import l2p.gameserver.clientpackets.RequestSiegeInfo;
import l2p.gameserver.clientpackets.RequestSkillList;
import l2p.gameserver.clientpackets.RequestStartPledgeWar;
import l2p.gameserver.clientpackets.RequestStatus;
import l2p.gameserver.clientpackets.RequestStopPledgeWar;
import l2p.gameserver.clientpackets.RequestTargetCanceld;
import l2p.gameserver.clientpackets.RequestTeleport;
import l2p.gameserver.clientpackets.RequestTeleportBookMark;
import l2p.gameserver.clientpackets.RequestTimeCheck;
import l2p.gameserver.clientpackets.RequestTutorialClientEvent;
import l2p.gameserver.clientpackets.RequestTutorialLinkHtml;
import l2p.gameserver.clientpackets.RequestTutorialPassCmdToServer;
import l2p.gameserver.clientpackets.RequestTutorialQuestionMark;
import l2p.gameserver.clientpackets.RequestVoteNew;
import l2p.gameserver.clientpackets.RequestWithDrawPremiumItem;
import l2p.gameserver.clientpackets.RequestWithDrawalParty;
import l2p.gameserver.clientpackets.RequestWithdrawAlly;
import l2p.gameserver.clientpackets.RequestWithdrawPartyRoom;
import l2p.gameserver.clientpackets.RequestWithdrawalPledge;
import l2p.gameserver.clientpackets.RequestWriteHeroWords;
import l2p.gameserver.clientpackets.Say2C;
import l2p.gameserver.clientpackets.SendBypassBuildCmd;
import l2p.gameserver.clientpackets.SendWareHouseDepositList;
import l2p.gameserver.clientpackets.SendWareHouseWithDrawList;
import l2p.gameserver.clientpackets.SetPrivateStoreBuyList;
import l2p.gameserver.clientpackets.SetPrivateStoreMsgBuy;
import l2p.gameserver.clientpackets.SetPrivateStoreMsgSell;
import l2p.gameserver.clientpackets.SetPrivateStoreSellList;
import l2p.gameserver.clientpackets.SetPrivateStoreWholeMsg;
import l2p.gameserver.clientpackets.SnoopQuit;
import l2p.gameserver.clientpackets.StartRotatingC;
import l2p.gameserver.clientpackets.TradeDone;
import l2p.gameserver.clientpackets.TradeRequest;
import l2p.gameserver.clientpackets.UseItem;
import l2p.gameserver.clientpackets.ValidatePosition;
import l2p.gameserver.model.security.SecondaryPasswordAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GamePacketHandler
  implements IPacketHandler<GameClient>, IClientFactory<GameClient>, IMMOExecutor<GameClient>
{
  private static final Logger _log = LoggerFactory.getLogger(GamePacketHandler.class);

  public ReceivablePacket<GameClient> handlePacket(ByteBuffer buf, GameClient client)
  {
    int id = buf.get() & 0xFF;

    ReceivablePacket msg = null;
    try
    {
      int id2 = 0;
      switch (1.$SwitchMap$l2p$gameserver$network$GameClient$GameClientState[client.getState().ordinal()])
      {
      case 1:
        switch (id)
        {
        case 0:
          msg = new RequestStatus();
          break;
        case 14:
          msg = new ProtocolVersion();
          break;
        case 43:
          msg = new AuthLogin();
          break;
        case 203:
          msg = new ReplyGameGuardQuery();
          break;
        default:
          client.onUnknownPacket();
        }break;
      case 2:
        switch (id)
        {
        case 0:
          msg = new Logout();
          break;
        case 12:
          msg = new CharacterCreate();
          break;
        case 13:
          if ((!client.getSecondaryAuth().isAuthed()) && (Config.EX_SECOND_PASSWORD))
            break;
          msg = new CharacterDelete();
          break;
        case 18:
          if ((!client.getSecondaryAuth().isAuthed()) && (Config.EX_SECOND_PASSWORD))
            break;
          msg = new CharacterSelected();
          break;
        case 19:
          msg = new NewCharacter();
          break;
        case 123:
          msg = new CharacterRestore();
          break;
        case 203:
          msg = new ReplyGameGuardQuery();
          break;
        case 208:
          int id3 = buf.getShort() & 0xFFFF;
          switch (id3)
          {
          case 54:
            msg = new GotoLobby();
            break;
          case 147:
            msg = new RequestEx2ndPasswordCheck();
            break;
          case 148:
            msg = new RequestEx2ndPasswordVerify();
            break;
          case 149:
            msg = new RequestEx2ndPasswordReq();
            break;
          default:
            client.onUnknownPacket();
          }break;
        default:
          client.onUnknownPacket();
        }

        break;
      case 3:
        switch (id)
        {
        case 0:
          msg = new Logout();
          break;
        case 1:
          msg = new AttackRequest();
          break;
        case 2:
          break;
        case 3:
          msg = new RequestStartPledgeWar();
          break;
        case 4:
          break;
        case 5:
          msg = new RequestStopPledgeWar();
          break;
        case 6:
          break;
        case 7:
          msg = new ReplyGameGuardQuery();

          break;
        case 8:
          break;
        case 9:
          msg = new RequestSetPledgeCrest();
          break;
        case 10:
          break;
        case 11:
          msg = new RequestGiveNickName();
          break;
        case 12:
          break;
        case 13:
          break;
        case 15:
          msg = new MoveBackwardToLocation();
          break;
        case 16:
          break;
        case 17:
          msg = new EnterWorld();
          break;
        case 18:
          break;
        case 20:
          msg = new RequestItemList();
          break;
        case 21:
          break;
        case 22:
          break;
        case 23:
          msg = new RequestDropItem();
          break;
        case 24:
          break;
        case 25:
          msg = new UseItem();
          break;
        case 26:
          msg = new TradeRequest();
          break;
        case 27:
          msg = new AddTradeItem();
          break;
        case 28:
          msg = new TradeDone();
          break;
        case 29:
          break;
        case 30:
          break;
        case 31:
          msg = new Action();
          break;
        case 32:
          break;
        case 33:
          break;
        case 34:
          break;
        case 35:
          msg = new RequestBypassToServer();
          break;
        case 36:
          msg = new RequestBBSwrite();
          break;
        case 37:
          msg = new RequestCreatePledge();
          break;
        case 38:
          msg = new RequestJoinPledge();
          break;
        case 39:
          msg = new RequestAnswerJoinPledge();
          break;
        case 40:
          msg = new RequestWithdrawalPledge();
          break;
        case 41:
          msg = new RequestOustPledgeMember();
          break;
        case 42:
          break;
        case 44:
          msg = new RequestGetItemFromPet();
          break;
        case 45:
          break;
        case 46:
          msg = new RequestAllyInfo();
          break;
        case 47:
          msg = new RequestCrystallizeItem();
          break;
        case 48:
          break;
        case 49:
          msg = new SetPrivateStoreSellList();
          break;
        case 50:
          break;
        case 51:
          msg = new RequestTeleport();
          break;
        case 52:
          break;
        case 53:
          break;
        case 54:
          break;
        case 55:
          msg = new RequestSellItem();
          break;
        case 56:
          msg = new RequestMagicSkillList();
          break;
        case 57:
          msg = new RequestMagicSkillUse();
          break;
        case 58:
          msg = new Appearing();
          break;
        case 59:
          if (!Config.ALLOW_WAREHOUSE) break;
          msg = new SendWareHouseDepositList(); break;
        case 60:
          msg = new SendWareHouseWithDrawList();
          break;
        case 61:
          msg = new RequestShortCutReg();
          break;
        case 62:
          break;
        case 63:
          msg = new RequestShortCutDel();
          break;
        case 64:
          msg = new RequestBuyItem();
          break;
        case 65:
          break;
        case 66:
          msg = new RequestJoinParty();
          break;
        case 67:
          msg = new RequestAnswerJoinParty();
          break;
        case 68:
          msg = new RequestWithDrawalParty();
          break;
        case 69:
          msg = new RequestOustPartyMember();
          break;
        case 70:
          msg = new RequestDismissParty();
          break;
        case 71:
          msg = new CannotMoveAnymore();
          break;
        case 72:
          msg = new RequestTargetCanceld();
          break;
        case 73:
          msg = new Say2C();
          break;
        case 74:
          id2 = buf.get() & 0xFF;
          switch (id2)
          {
          case 0:
            break;
          case 1:
            break;
          case 2:
            break;
          case 3:
            break;
          default:
            client.onUnknownPacket();
          }break;
        case 75:
          break;
        case 76:
          break;
        case 77:
          msg = new RequestPledgeMemberList();
          break;
        case 78:
          break;
        case 79:
          break;
        case 80:
          msg = new RequestSkillList();
          break;
        case 81:
          break;
        case 82:
          msg = new MoveWithDelta();
          break;
        case 83:
          msg = new RequestGetOnVehicle();
          break;
        case 84:
          msg = new RequestGetOffVehicle();
          break;
        case 85:
          msg = new AnswerTradeRequest();
          break;
        case 86:
          msg = new RequestActionUse();
          break;
        case 87:
          msg = new RequestRestart();
          break;
        case 88:
          msg = new RequestSiegeInfo();
          break;
        case 89:
          msg = new ValidatePosition();
          break;
        case 90:
          msg = new RequestSEKCustom();
          break;
        case 91:
          msg = new StartRotatingC();
          break;
        case 92:
          msg = new FinishRotatingC();
          break;
        case 93:
          break;
        case 94:
          msg = new RequestShowBoard();
          break;
        case 95:
          msg = new RequestEnchantItem();
          break;
        case 96:
          msg = new RequestDestroyItem();
          break;
        case 97:
          break;
        case 98:
          msg = new RequestQuestList();
          break;
        case 99:
          msg = new RequestQuestAbort();
          break;
        case 100:
          break;
        case 101:
          msg = new RequestPledgeInfo();
          break;
        case 102:
          msg = new RequestPledgeExtendedInfo();
          break;
        case 103:
          msg = new RequestPledgeCrest();
          break;
        case 104:
          break;
        case 105:
          break;
        case 106:
          break;
        case 107:
          msg = new RequestSendL2FriendSay();
          break;
        case 108:
          msg = new RequestShowMiniMap();
          break;
        case 109:
          msg = new RequestSendMsnChatLog();
          break;
        case 110:
          msg = new RequestReload();
          break;
        case 111:
          msg = new RequestHennaEquip();
          break;
        case 112:
          msg = new RequestHennaUnequipList();
          break;
        case 113:
          msg = new RequestHennaUnequipInfo();
          break;
        case 114:
          msg = new RequestHennaUnequip();
          break;
        case 115:
          msg = new RequestAquireSkillInfo();
          break;
        case 116:
          msg = new SendBypassBuildCmd();
          break;
        case 117:
          msg = new RequestMoveToLocationInVehicle();
          break;
        case 118:
          msg = new CannotMoveAnymoreInVehicle();
          break;
        case 119:
          msg = new RequestFriendInvite();
          break;
        case 120:
          msg = new RequestFriendAddReply();
          break;
        case 121:
          msg = new RequestFriendList();
          break;
        case 122:
          msg = new RequestFriendDel();
          break;
        case 124:
          msg = new RequestAquireSkill();
          break;
        case 125:
          msg = new RequestRestartPoint();
          break;
        case 126:
          msg = new RequestGMCommand();
          break;
        case 127:
          msg = new RequestPartyMatchConfig();
          break;
        case 128:
          msg = new RequestPartyMatchList();
          break;
        case 129:
          msg = new RequestPartyMatchDetail();
          break;
        case 130:
          msg = new RequestPrivateStoreList();
          break;
        case 131:
          msg = new RequestPrivateStoreBuy();
          break;
        case 132:
          break;
        case 133:
          msg = new RequestTutorialLinkHtml();
          break;
        case 134:
          msg = new RequestTutorialPassCmdToServer();
          break;
        case 135:
          msg = new RequestTutorialQuestionMark();
          break;
        case 136:
          msg = new RequestTutorialClientEvent();
          break;
        case 137:
          msg = new RequestPetition();
          break;
        case 138:
          msg = new RequestPetitionCancel();
          break;
        case 139:
          msg = new RequestGmList();
          break;
        case 140:
          msg = new RequestJoinAlly();
          break;
        case 141:
          msg = new RequestAnswerJoinAlly();
          break;
        case 142:
          msg = new RequestWithdrawAlly();
          break;
        case 143:
          msg = new RequestOustAlly();
          break;
        case 144:
          msg = new RequestDismissAlly();
          break;
        case 145:
          msg = new RequestSetAllyCrest();
          break;
        case 146:
          msg = new RequestAllyCrest();
          break;
        case 147:
          msg = new RequestChangePetName();
          break;
        case 148:
          msg = new RequestPetUseItem();
          break;
        case 149:
          msg = new RequestGiveItemToPet();
          break;
        case 150:
          msg = new RequestPrivateStoreQuitSell();
          break;
        case 151:
          msg = new SetPrivateStoreMsgSell();
          break;
        case 152:
          msg = new RequestPetGetItem();
          break;
        case 153:
          msg = new RequestPrivateStoreBuyManage();
          break;
        case 154:
          msg = new SetPrivateStoreBuyList();
          break;
        case 155:
          break;
        case 156:
          msg = new RequestPrivateStoreQuitBuy();
          break;
        case 157:
          msg = new SetPrivateStoreMsgBuy();
          break;
        case 158:
          break;
        case 159:
          msg = new RequestPrivateStoreBuySellList();
          break;
        case 160:
          msg = new RequestTimeCheck();
          break;
        case 161:
          break;
        case 162:
          break;
        case 163:
          break;
        case 164:
          break;
        case 165:
          break;
        case 166:
          break;
        case 167:
          msg = new RequestPackageSendableItemList();
          break;
        case 168:
          msg = new RequestPackageSend();
          break;
        case 169:
          msg = new RequestBlock();
          break;
        case 170:
          break;
        case 171:
          msg = new RequestCastleSiegeAttackerList();
          break;
        case 172:
          msg = new RequestCastleSiegeDefenderList();
          break;
        case 173:
          msg = new RequestJoinCastleSiege();
          break;
        case 174:
          msg = new RequestConfirmCastleSiegeWaitingList();
          break;
        case 175:
          msg = new RequestSetCastleSiegeTime();
          break;
        case 176:
          msg = new RequestMultiSellChoose();
          break;
        case 177:
          msg = new NetPing();
          break;
        case 178:
          msg = new RequestRemainTime();
          break;
        case 179:
          msg = new BypassUserCmd();
          break;
        case 180:
          msg = new SnoopQuit();
          break;
        case 181:
          msg = new RequestRecipeBookOpen();
          break;
        case 182:
          msg = new RequestRecipeItemDelete();
          break;
        case 183:
          msg = new RequestRecipeItemMakeInfo();
          break;
        case 184:
          msg = new RequestRecipeItemMakeSelf();
          break;
        case 185:
          break;
        case 186:
          msg = new RequestRecipeShopMessageSet();
          break;
        case 187:
          msg = new RequestRecipeShopListSet();
          break;
        case 188:
          msg = new RequestRecipeShopManageQuit();
          break;
        case 189:
          msg = new RequestRecipeShopManageCancel();
          break;
        case 190:
          msg = new RequestRecipeShopMakeInfo();
          break;
        case 191:
          msg = new RequestRecipeShopMakeDo();
          break;
        case 192:
          msg = new RequestRecipeShopSellList();
          break;
        case 193:
          msg = new RequestObserverEnd();
          break;
        case 194:
          break;
        case 195:
          msg = new RequestHennaList();
          break;
        case 196:
          msg = new RequestHennaItemInfo();
          break;
        case 197:
          msg = new RequestBuySeed();
          break;
        case 198:
          msg = new ConfirmDlg();
          break;
        case 199:
          msg = new RequestPreviewItem();
          break;
        case 200:
          msg = new RequestSSQStatus();
          break;
        case 201:
          msg = new PetitionVote();
          break;
        case 202:
          break;
        case 203:
          msg = new ReplyGameGuardQuery();
          break;
        case 204:
          msg = new RequestPledgePower();
          break;
        case 205:
          msg = new RequestMakeMacro();
          break;
        case 206:
          msg = new RequestDeleteMacro();
          break;
        case 207:
          msg = new RequestProcureCrop();
          break;
        case 208:
          int id3 = buf.getShort() & 0xFFFF;
          int id5;
          switch (id3)
          {
          case 0:
            break;
          case 1:
            msg = new RequestManorList();
            break;
          case 2:
            msg = new RequestProcureCropList();
            break;
          case 3:
            msg = new RequestSetSeed();
            break;
          case 4:
            msg = new RequestSetCrop();
            break;
          case 5:
            msg = new RequestWriteHeroWords();
            break;
          case 6:
            msg = new RequestExMPCCAskJoin();
            break;
          case 7:
            msg = new RequestExMPCCAcceptJoin();
            break;
          case 8:
            msg = new RequestExOustFromMPCC();
            break;
          case 9:
            msg = new RequestOustFromPartyRoom();
            break;
          case 10:
            msg = new RequestDismissPartyRoom();
            break;
          case 11:
            msg = new RequestWithdrawPartyRoom();
            break;
          case 12:
            msg = new RequestHandOverPartyMaster();
            break;
          case 13:
            msg = new RequestAutoSoulShot();
            break;
          case 14:
            msg = new RequestExEnchantSkillInfo();
            break;
          case 15:
            msg = new RequestExEnchantSkill();
            break;
          case 16:
            msg = new RequestPledgeCrestLarge();
            break;
          case 17:
            msg = new RequestSetPledgeCrestLarge();
            break;
          case 18:
            msg = new RequestPledgeSetAcademyMaster();
            break;
          case 19:
            msg = new RequestPledgePowerGradeList();
            break;
          case 20:
            msg = new RequestPledgeMemberPowerInfo();
            break;
          case 21:
            msg = new RequestPledgeSetMemberPowerGrade();
            break;
          case 22:
            msg = new RequestPledgeMemberInfo();
            break;
          case 23:
            msg = new RequestPledgeWarList();
            break;
          case 24:
            msg = new RequestExFishRanking();
            break;
          case 25:
            msg = new RequestPCCafeCouponUse();
            break;
          case 26:
            break;
          case 27:
            msg = new RequestDuelStart();
            break;
          case 28:
            msg = new RequestDuelAnswerStart();
            break;
          case 29:
            msg = new RequestTutorialClientEvent();

            break;
          case 30:
            msg = new RequestExRqItemLink();
            break;
          case 31:
            break;
          case 32:
            msg = new RequestExMoveToLocationInAirShip();
            break;
          case 33:
            msg = new RequestKeyMapping();
            break;
          case 34:
            msg = new RequestSaveKeyMapping();
            break;
          case 35:
            msg = new RequestExRemoveItemAttribute();
            break;
          case 36:
            msg = new RequestSaveInventoryOrder();
            break;
          case 37:
            msg = new RequestExitPartyMatchingWaitingRoom();
            break;
          case 38:
            msg = new RequestConfirmTargetItem();
            break;
          case 39:
            msg = new RequestConfirmRefinerItem();
            break;
          case 40:
            msg = new RequestConfirmGemStone();
            break;
          case 41:
            msg = new RequestOlympiadObserverEnd();
            break;
          case 42:
            msg = new RequestCursedWeaponList();
            break;
          case 43:
            msg = new RequestCursedWeaponLocation();
            break;
          case 44:
            msg = new RequestPledgeReorganizeMember();
            break;
          case 45:
            msg = new RequestExMPCCShowPartyMembersInfo();
            break;
          case 46:
            msg = new RequestExOlympiadObserverEnd();
            break;
          case 47:
            msg = new RequestAskJoinPartyRoom();
            break;
          case 48:
            msg = new AnswerJoinPartyRoom();
            break;
          case 49:
            msg = new RequestListPartyMatchingWaitingRoom();
            break;
          case 50:
            msg = new RequestExEnchantSkillSafe();
            break;
          case 51:
            msg = new RequestExEnchantSkillUntrain();
            break;
          case 52:
            msg = new RequestExEnchantSkillRouteChange();
            break;
          case 53:
            msg = new RequestEnchantItemAttribute();
            break;
          case 54:
            break;
          case 56:
            msg = new RequestExMoveToLocationAirShip();
            break;
          case 57:
            msg = new RequestBidItemAuction();
            break;
          case 58:
            msg = new RequestInfoItemAuction();
            break;
          case 59:
            msg = new RequestExChangeName();
            break;
          case 60:
            msg = new RequestAllCastleInfo();
            break;
          case 61:
            msg = new RequestAllFortressInfo();
            break;
          case 62:
            msg = new RequestAllAgitInfo();
            break;
          case 63:
            msg = new RequestFortressSiegeInfo();
            break;
          case 64:
            msg = new RequestGetBossRecord();
            break;
          case 65:
            msg = new RequestRefine();
            break;
          case 66:
            msg = new RequestConfirmCancelItem();
            break;
          case 67:
            msg = new RequestRefineCancel();
            break;
          case 68:
            msg = new RequestExMagicSkillUseGround();
            break;
          case 69:
            msg = new RequestDuelSurrender();
            break;
          case 70:
            msg = new RequestExEnchantSkillInfoDetail();
            break;
          case 72:
            msg = new RequestFortressMapInfo();
            break;
          case 73:
            msg = new RequestPVPMatchRecord();
            break;
          case 74:
            msg = new SetPrivateStoreWholeMsg();
            break;
          case 75:
            msg = new RequestDispel();
            break;
          case 76:
            msg = new RequestExTryToPutEnchantTargetItem();
            break;
          case 77:
            msg = new RequestExTryToPutEnchantSupportItem();
            break;
          case 78:
            msg = new RequestExCancelEnchantItem();
            break;
          case 79:
            msg = new RequestChangeNicknameColor();
            break;
          case 80:
            msg = new RequestResetNickname();
            break;
          case 81:
            int id4 = buf.getInt();
            switch (id4)
            {
            case 0:
              msg = new RequestBookMarkSlotInfo();
              break;
            case 1:
              msg = new RequestSaveBookMarkSlot();
              break;
            case 2:
              msg = new RequestModifyBookMarkSlot();
              break;
            case 3:
              msg = new RequestDeleteBookMarkSlot();
              break;
            case 4:
              msg = new RequestTeleportBookMark();
              break;
            case 5:
              msg = new RequestChangeBookMarkSlot();
              break;
            default:
              client.onUnknownPacket();
            }break;
          case 82:
            msg = new RequestWithDrawPremiumItem();
            break;
          case 83:
            msg = new RequestExJump();
            break;
          case 84:
            msg = new RequestExStartShowCrataeCubeRank();
            break;
          case 85:
            msg = new RequestExStopShowCrataeCubeRank();
            break;
          case 86:
            msg = new NotifyStartMiniGame();
            break;
          case 87:
            msg = new RequestExJoinDominionWar();
            break;
          case 88:
            msg = new RequestExDominionInfo();
            break;
          case 89:
            msg = new RequestExCleftEnter();
            break;
          case 90:
            msg = new RequestExCubeGameChangeTeam();
            break;
          case 91:
            msg = new RequestExEndScenePlayer();
            break;
          case 92:
            msg = new RequestExCubeGameReadyAnswer();
            break;
          case 93:
            msg = new RequestExListMpccWaiting();
            break;
          case 94:
            msg = new RequestExManageMpccRoom();
            break;
          case 95:
            msg = new RequestExJoinMpccRoom();
            break;
          case 96:
            msg = new RequestExOustFromMpccRoom();
            break;
          case 97:
            msg = new RequestExDismissMpccRoom();
            break;
          case 98:
            msg = new RequestExWithdrawMpccRoom();
            break;
          case 99:
            msg = new RequestExSeedPhase();
            break;
          case 100:
            msg = new RequestExMpccPartymasterList();
            break;
          case 101:
            msg = new RequestExPostItemList();
            break;
          case 102:
            msg = new RequestExSendPost();
            break;
          case 103:
            msg = new RequestExRequestReceivedPostList();
            break;
          case 104:
            msg = new RequestExDeleteReceivedPost();
            break;
          case 105:
            msg = new RequestExRequestReceivedPost();
            break;
          case 106:
            msg = new RequestExReceivePost();
            break;
          case 107:
            msg = new RequestExRejectPost();
            break;
          case 108:
            msg = new RequestExRequestSentPostList();
            break;
          case 109:
            msg = new RequestExDeleteSentPost();
            break;
          case 110:
            msg = new RequestExRequestSentPost();
            break;
          case 111:
            msg = new RequestExCancelSentPost();
            break;
          case 112:
            msg = new RequestExShowNewUserPetition();
            break;
          case 113:
            msg = new RequestExShowStepTwo();
            break;
          case 114:
            msg = new RequestExShowStepThree();
            break;
          case 115:
            break;
          case 117:
            msg = new RequestExRefundItem();
            break;
          case 118:
            msg = new RequestExBuySellUIClose();
            break;
          case 119:
            msg = new RequestExEventMatchObserverEnd();
            break;
          case 120:
            msg = new RequestPartyLootModification();
            break;
          case 121:
            msg = new AnswerPartyLootModification();
            break;
          case 122:
            msg = new AnswerCoupleAction();
            break;
          case 123:
            msg = new RequestExBR_EventRankerList();
            break;
          case 124:
            break;
          case 125:
            msg = new RequestAddExpandQuestAlarm();
            break;
          case 126:
            msg = new RequestVoteNew();
            break;
          case 127:
            _log.info("D0:7F");
            break;
          case 128:
            _log.info("D0:80");
            break;
          case 129:
            _log.info("D0:81");
            break;
          case 130:
            _log.info("D0:82");
            break;
          case 131:
            id5 = buf.getInt();

            break;
          case 132:
            msg = new RequestExAddPostFriendForPostBox();
            break;
          case 133:
            msg = new RequestExDeletePostFriendForPostBox();
            break;
          case 134:
            msg = new RequestExShowPostFriendListForPostBox();
            break;
          case 135:
            msg = new RequestExFriendListForPostBox();
            break;
          case 136:
            msg = new RequestOlympiadMatchList();
            break;
          case 137:
            msg = new RequestExBR_GamePoint();
            break;
          case 138:
            msg = new RequestExBR_ProductList();
            break;
          case 139:
            msg = new RequestExBR_ProductInfo();
            break;
          case 140:
            msg = new RequestExBR_BuyProduct();
            break;
          case 141:
            msg = new RequestExBR_RecentProductList();
            break;
          case 142:
            msg = new RequestBR_MiniGameLoadScores();
            break;
          case 143:
            msg = new RequestBR_MiniGameInsertScore();
            break;
          case 144:
            msg = new RequestExBR_LectureMark();
            break;
          case 145:
            msg = new RequestGoodsInventoryInfo();
            break;
          case 146:
            break;
          case 55:
          case 71:
          case 116:
          default:
            client.onUnknownPacket();
          }

          break;
        case 14:
        case 19:
        case 43:
        case 123:
        default:
          client.onUnknownPacket();
        }

      }

    }
    catch (BufferUnderflowException e)
    {
      client.onPacketReadFail();
    }
    return msg;
  }

  public GameClient create(MMOConnection<GameClient> con)
  {
    return new GameClient(con);
  }

  public void execute(Runnable r)
  {
    ThreadPoolManager.getInstance().execute(r);
  }
}