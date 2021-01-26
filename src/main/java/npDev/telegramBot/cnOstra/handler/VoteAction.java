package npDev.telegramBot.cnOstra.handler;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import npDev.telegramBot.cnOstra.Bot;
import npDev.telegramBot.cnOstra.data.*;
import npDev.telegramBot.cnOstra.shell.CallbackQueryOstra;
import npDev.telegramBot.cnOstra.shell.ChatMemberOstra;
import npDev.telegramBot.shell.ChatMemberShell;
import npDev.telegramBot.shell.ChatShell;
import npDev.telegramBot.shell.MessageShell;
import npDev.telegramBot.shell.UserShell;

public class VoteAction {
    private final CallbackQueryOstra ballot;
    private final MessageShell result;
    private final MessageShell reportedMessage;
    private final UserShell voter;
    private final Vote vote;
    private final Bot bot;
    private final ChatShell chat;

    public VoteAction(Bot bot, CallbackQueryOstra callbackQuery) {
        this.bot = bot;
        ballot = callbackQuery;
        result = ballot.getMessage();
        reportedMessage = result.getReplyToMessage();
        voter = ballot.getFrom();
        vote = VoteDAO.queryForId(result.getPrivateID());
        chat=result.getChat();
    }

    static InlineKeyboardMarkup buttons(int[] counts) {
        InlineKeyboardButton[] buttons = new InlineKeyboardButton[Option.length];
        for (int o = 0; o < Option.length; o++) {
            String name = Option.values[o].name();
            if (counts[o] > 0) {
                name = name + "*" + counts[o];
            }
            buttons[Option.length - o - 1] = new InlineKeyboardButton(name)
                    .callbackData(String.valueOf(o));
        }
        return new InlineKeyboardMarkup(buttons);
    }

    public void doVote() {
        if (vote == null) {// 数据库出错
            doCancelForError();// 撤销
            return;
        }
        if (vote.getFinished()) {// 已经结束
            ballot.doAnswer(bot, "投票已结束。", true);
            return;
        }
        String data = ballot.getData();
        // Option option=Option.values()[Integer.valueOf(data)];
        int voteUserId = voter.getID();
        byte option = Byte.parseByte(data);
        if (VoteCountDAO.countOfVoteUserAndOption(vote, voteUserId, option) > 0) {// 和上次点击一样
            ballot.doAnswer(bot, "不要重复点击。", true);// 反馈
            doResult();
            return;
        }
        Member voteMember = ballot.getMember();
//		switch (voteMember.getStatus(bot)) {
//		case restricted:
//		case left:
//		case kicked:
//			handler.doAnswer(bot, "你没有投票权");
//			doResult(bot);
//			return;
//		case creator:
//		case administrator:
//		default:
//			break;
//		}
        if (!voteMember.isValid(result.getDate(), bot.getPreHours())) {
            ballot.doAnswer(bot, "你没有投票权。", true);
            Bot.LOGGER.info(voteUserId+" 无权"+ Option.values[option]+" "+result.getLinkOrPrivateID());
            doResult();
            return;
        }
        VoteCount voteCount = new VoteCount(vote, voter.getID(), option);
        VoteCountDAO.createOrUpdate(voteCount);
        // handler.doEditMessageReplyMarkup(bot,new
        // InlineKeyboardMarkup(buttons(Option.counts(vote))));
        result.doEditReplyMarkup(bot, buttons(Option.counts(vote)));
        ballot.doAnswer(bot, String.format("你选择了%s。", Option.values[option]), true);// 反馈
        Bot.LOGGER.info(voteUserId+" "+Option.values[option]+" "+result.getLinkOrPrivateID());
        doResult();
    }

    private void doResult() {
        if (System.currentTimeMillis() / 1000 - result.getDate() < bot.getVoteMinutes() * 60L &&!reportedMessage.isNull()) {// 表决不够3分钟且消息未删除
            return;
        } // 已超时
        try {
            if (vote == null) {// 数据库损坏，或者是被撤销的表决
                doCancelForError();
                return;
            }
            if (vote.getFinished()) {//已经结束
                return;
            }
//			if (reportedMessage.isNull()) {//被举报消息已删除
//				doCancelForDelete();
//				return;
//			}
            Bot.LOGGER.info("判决 "+result.getLinkOrPrivateID());
            int[] counts = Option.counts(vote);
            int totalCounts = 0;
            byte maxIndex = 0;
            StringBuilder sb = new StringBuilder("所发消息表决情况：").append('\n');
            for (int i = Option.length - 1; i >= 0; i--) {
                sb.append(String.format("⚪%s %d 票", Option.values[i], counts[i])).append('\n');
                totalCounts += counts[i];
                if (counts[i] > counts[maxIndex]) {//如果当前值比最大的值大
                    maxIndex = (byte) i;
                }
            }
//			float ratio=counts[maxIndex]>=conf.getVoteValid()?(float)counts[maxIndex]/(float)totalCounts:0;
            float ratio = totalCounts >= bot.getVoteValid() ? (float) counts[maxIndex] / (float) totalCounts : 0;
            int reporterID = vote.getReporter();
            Member reporterMember = MemberDAO.queryForChatAndUser(chat.getID(), reporterID);
            if (reporterMember == null) {
                reporterMember = new Member(chat.getID(), reporterID);
            }
            if (ratio <= 0.5f) {
                sb.append("表决无效。不作处理。");
                reporterMember.resetFlag();
            } else {
                Option option = Option.values[maxIndex];
                sb.append(String.format("投票结果为%s。", option.name()));
                switch (option) {
                    case 恶意行为:
                        reporterMember.resetFlag();
                        int reportedUserID = vote.getReportedUser();
//					Member reportedMember =MemberDao.queryForChatAndUser(chatId, reportedUserID);
                        ChatMemberOstra reportedChatMember = ChatMemberOstra.cast(result.getChat().doGetChatMember(bot, reportedUserID));
//					if(reportedMember.isValid()){//没被处罚
                        float hours = bot.getMute(counts[maxIndex], ratio);
                        if (!reportedChatMember.doMuteOrMore(bot, (int) (hours * 60 * 60))) {//禁言失败
                            sb.append(String.format("%s 已被另案处理。", UserShell.toIDMarkdown(reportedUserID)));
                        } else {//没被处罚或刑期比本次短
                            sb.append(String.format("%s 被禁言 %d 小时。", UserShell.toIDMarkdown(reportedUserID), (int) hours));
                        }
                        Member reportedMember = reportedChatMember.getMember();
                        reportedMember.setInvalid();
                        MemberDAO.createOrUpdate(reportedMember);
                        if (!reportedMessage.isNull()) {//尚未删除
                            reportedMessage.doDelete(bot);// 删除被举报消息
                        }
                        break;
                    case 驳回举报:
                    default:
                        reporterMember.addFlag();
                        reporterMember.setInvalid();
//					if(reporterMember.isValid()){//没被封禁
                        ChatMemberShell reporterChatMember = result.getChat().doGetChatMember(bot, reporterID);
                        int flag = reporterMember.getFlag();
                        if(flag<bot.getRejectLimit()){//警告
                            sb.append(String.format("举报者被连续警告 %d 次。", flag));
                        }else if(reporterChatMember.doMuteOrMore(bot, flag * 60 * 60)){//加刑
                            sb.append(String.format("举报者被连续警告 %d 次，禁言 %d 小时", flag, flag));
                        }else{//超限且处于禁言中
                            sb.append("举报者已被另案处理。");
                        }
                        break;
                }
            }
            MemberDAO.createOrUpdate(reporterMember);
            vote.setFinished(true);
            VoteDAO.createOrUpdate(vote);
            result.doEditText(bot, sb.toString());
            result.doUnpin(bot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //	void doResultLater(final TelegramBot bot) {
//	Application.TIMER.schedule(new TimerTask() {
//		@Override
//		public void run() {
//			try {
//				doResult(bot);
//			} catch (Exception e) {
////				e.printStackTrace();
//			}
//		}
//	}, Configration.getVoteMinutes()*60000);
//}
    private void doCancel(@SuppressWarnings("SameParameterValue") String reason) {// 投票撤销
        result.doEditText(bot, String.format("%s。本次表决撤销。", reason));
        result.doDeleteAfter(bot, bot.getTipSeconds());
    }

    //	private void doCancelForDelete() {// 投票撤销
//		doCancel("所发消息已被删除！");
//		if(vote!=null){//删除投票数据
//			VoteDao.delete(vote);
//		}
//	}
    private void doCancelForError() {// 投票撤销
        doCancel("系统错误，请重新举报。");
    }

    @SuppressWarnings("NonAsciiCharacters")
    enum Option {
        驳回举报, 恶意行为;
        static Option[] values = values();
        static byte length = (byte) values.length;

        static int[] counts(Vote vote) {
            int[] counts = new int[Option.length];
            for (byte o = 0; o < Option.length; o++) {
                counts[o] = VoteCountDAO.countOfVoteAndOption(vote, o);
            }
            return counts;
        }
    }
}
