package l2rt.gameserver.network.clientpackets;

import l2rt.database.mysql;
import l2rt.extensions.listeners.PropertyCollection;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.base.Transaction.TransactionType;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.MailParcelController;
import l2rt.gameserver.model.items.MailParcelController.Letter;
import l2rt.gameserver.network.serverpackets.ExNoticePostArrived;
import l2rt.gameserver.network.serverpackets.ExReplyWritePost;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.util.HWID;
import l2rt.util.Util;

/**
 * Запрос на отсылку нового письма. В ответ шлется {@link ExReplyWritePost}.
 * @see RequestExPostItemList
 * @see RequestExRequestReceivedPostList
 */
public class RequestExSendPost extends L2GameClientPacket
{
	private int _messageType;
	private String _targetName, _topic, _body;
	private int[] _attItems;
	private long[] _attItemsQ;
	private long _price;

	/**
	 * format: SdSS dx[dQ] Q
	 */
	@Override
	public void readImpl()
	{
		_targetName = readS(35); // имя адресата
		_messageType = readD(); // тип письма, 0 простое 1 с запросом оплаты
		_topic = readS(30); // topic
		_body = readS(30000); // body

		_attItems = new int[readD()]; // число прикрепленных вещей
		_attItemsQ = new long[_attItems.length];
		for(int i = 0; i < _attItems.length; i++)
		{
			_attItems[i] = readD(); // objectId
			_attItemsQ[i] = readQ(); // количество
		}

		_price = readQ(); // цена для писем с запросом оплаты
	}

	@Override
	public void runImpl()
	{
		L2Player cha = getClient().getActiveChar();
		if(cha == null)
			return;

		if(cha.isBlocked())
		{
			cha.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.RequestRestart.OutOfControl", cha));
			return;
		}

		if(cha.isInStoreMode())
		{
			cha.sendPacket(Msg.YOU_CANNOT_FORWARD_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS);
			return;
		}

		if(cha.isInTransaction() && cha.getTransaction().isTypeOf(TransactionType.TRADE))
		{
			cha.sendPacket(Msg.YOU_CANNOT_FORWARD_DURING_AN_EXCHANGE);
			return;
		}

		if(!cha.isInPeaceZone())
		{
			cha.sendPacket(Msg.YOU_CANNOT_FORWARD_IN_A_NON_PEACE_ZONE_LOCATION);
			return;
		}

		if(cha.getEnchantScroll() != null)
		{
			cha.sendPacket(Msg.YOU_CANNOT_FORWARD_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT);
			return;
		}

		if(cha.getName().equalsIgnoreCase(_targetName))
		{
			cha.sendPacket(Msg.YOU_CANNOT_SEND_A_MAIL_TO_YOURSELF);
			return;
		}

		long curTime = System.currentTimeMillis();
		Long lastMailTime = (Long) cha.getProperty(PropertyCollection.MailSent);
		if(lastMailTime != null && lastMailTime + (cha.getLevel() >= 20 ? 60000L : 300000L) > curTime)
		{
			cha.sendMessage("Mail is allowed once per " + (cha.getLevel() >= 20 ? "minute." : "5 minutes."));
			return;
		}
		cha.addProperty(PropertyCollection.MailSent, curTime);

		if(_price > 0)
		{
			String tradeBan = cha.getVar("tradeBan");
			if(tradeBan != null && (tradeBan.equals("-1") || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
			{
				cha.sendMessage("Your trade is banned! Expires: " + (tradeBan.equals("-1") ? "never" : Util.formatTime((Long.parseLong(tradeBan) - System.currentTimeMillis()) / 1000)) + ".");
				return;
			}

			if(cha.hasHWID())
			{
				int ban;
				if((ban = HWID.getBonus(cha.getHWID(), "tradeBan")) != 0)
					if(ban > System.currentTimeMillis() / 1000)
						cha.sendMessage("Your trade is totally banned! Expires: " + (ban < 0 ? "never" : Util.formatTime(ban - System.currentTimeMillis() / 1000)) + ".");
					else if(ban > 0)
						HWID.unsetBonus(cha.getHWID(), "tradeBan");
			}
		}

		// ищем цель и проверяем блоклисты
		if(cha.isInBlockList(_targetName)) // тем кто в блоклисте не шлем
		{
			cha.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_BLOCKED_C1).addString(_targetName));
			return;
		}
		int targetId;
		L2Player target = L2World.getPlayer(_targetName);
		if(target != null)
		{
			targetId = target.getObjectId();
			if(target.isInBlockList(cha)) // цель заблокировала отправителя
			{
				cha.sendPacket(new SystemMessage(SystemMessage.S1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_S1_).addString(_targetName));
				return;
			}
		}
		else
		{
			targetId = Util.GetCharIDbyName(_targetName);
			if(targetId > 0 && mysql.simple_get_int("target_Id", "character_blocklist", "obj_Id=" + targetId + " AND target_Id=" + cha.getObjectId()) > 0) // цель заблокировала отправителя
			{
				cha.sendPacket(new SystemMessage(SystemMessage.S1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_S1_).addString(_targetName));
				return;
			}
		}

		if(targetId == 0) // не нашли цель?
		{
			cha.sendPacket(Msg.WHEN_THE_RECIPIENT_DOESN_T_EXIST_OR_THE_CHARACTER_HAS_BEEN_DELETED_SENDING_MAIL_IS_NOT_POSSIBLE);
			return;
		}

		int expiretime = (_messageType == 1 ? 12 : 360) * 3600 + (int) (System.currentTimeMillis() / 1000);

		long serviceCost = 100 + _attItems.length * 1000;
		if(cha.getAdena() < serviceCost)
		{
			cha.sendPacket(Msg.YOU_CANNOT_FORWARD_BECAUSE_YOU_DON_T_HAVE_ENOUGH_ADENA);
			return;
		}

		for(int i = 0; i < _attItems.length; i++)
		{
			L2ItemInstance item = cha.getInventory().getItemByObjectId(_attItems[i]);
			if(item == null || item.getCount() < _attItemsQ[i] || item.getItemId() == 57 && item.getCount() < _attItemsQ[i] + serviceCost)
			{
				cha.sendPacket(Msg.THE_ITEM_THAT_YOU_RE_TRYING_TO_SEND_CANNOT_BE_FORWARDED_BECAUSE_IT_ISN_T_PROPER);
				return;
			}
		}

		cha.reduceAdena(serviceCost, true);

		Letter letter = new Letter();
		letter.receiverId = targetId;
		letter.receiverName = _targetName;
		letter.senderId = cha.getObjectId();
		letter.senderName = cha.getName();
		letter.topic = _topic;
		letter.body = _body;
		letter.price = _messageType > 0 ? _price : 0;
		letter.unread = 1;
		letter.validtime = expiretime;

		// цель существует и не против принять почту
		MailParcelController.getInstance().sendLetter(letter, _attItems, _attItemsQ, cha);

		cha.sendPacket(new ExReplyWritePost(1));
		if(target != null)
			target.sendPacket(new ExNoticePostArrived(1));
	}
}