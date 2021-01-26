package npDev.telegramBot.cnOstra.handler;

import npDev.telegramBot.cnOstra.Bot;
import npDev.telegramBot.cnOstra.data.*;
import npDev.telegramBot.cnOstra.shell.MessageOstra;
import npDev.telegramBot.shell.ChatMemberShell;
import npDev.telegramBot.shell.MessageShell;
import npDev.telegramBot.shell.UserShell;

public class ReportHandler {
    public static final String COMMAND = "report";
    private final Bot bot;
    private final MessageOstra command;
    private final MessageShell reportedMessage;
    private final UserShell reporter;
    private final Member reporterMember;

    //	public ReportMessage(Message message){
//		this(new MessageHandler(message));
//	}
    public ReportHandler(Bot bot, MessageOstra message) {
        this.bot = bot;
        command = message;
        reporter = command.getFrom();
        reporterMember = command.getMember();
        reportedMessage = command.getReplyToMessage();
    }

    public void doReport() {
        try {
            if (!command.getChat().isGroupChat()) {
                doReplyToReport("只在群组中有效。");
                return;
            }
            if (reportedMessage.isNull()) {// 举报消息为空
                doReplyToReport("请用 `/report` *回复*想要举报的消息。");
                return;
            }// 有举报对象
            if (reportedUser().isBot()) {//被举报者是机器人
                doReplyToReport("无法举报机器人。");
                return;
            }
            if (System.currentTimeMillis() / 1000 - reportedMessage.getLastDate() > bot.getCacheHours()
                    * 3600L) {// 被举报消息大于三分钟
                doReplyToReport(String.format("只能举报 %d 小时内的消息。", bot.getCacheHours()));
                return;
            }// 有效期内的举报
            // 一事不二审
            Vote v = VoteDAO.queryForReportedMessage(reportedMessage.getPrivateID());
            if (v != null) {// 已有举报
                StringBuilder sb = new StringBuilder("已有举报：").append('\n');
                sb.append(MessageShell.getPrivateLink(v.getId()));
                if (!v.getFinished() && reporterMember.isValid(command.getDate(), bot.getPreHours())) {// 未结束且有投票权
                    VoteCount voteCount = new VoteCount(v, reporter.getID(), (byte) VoteAction.Option.恶意行为.ordinal());
                    VoteCountDAO.createOrUpdate(voteCount);
                    sb.append('\n').append(String.format("已帮你投%s。", VoteAction.Option.恶意行为.name()));
                }
                doReplyToReport(sb.toString());
                return;
            }// 新的举报
            Bot.LOGGER.info(reporter.getID()+" 举报 "+reportedMessage.getLinkOrPrivateID());
            ChatMemberShell reportedChatMember = reportedMessage.doGetChatMember(bot);
            if (!reportedChatMember.canSendMessages()) {//被处罚状态
                doDelete();//直接删除
                return;
            }
//			if(!reportedMember().isValid()) {//被举报者没发过言
//				if(reportedUser().isBot()) {//被举报者是自己
//					doReplyToReport(bot,conf, "无法举报机器人。");
//					return;
//				}else {//被举报者是人
//					doDelete(bot);//直接删除
//					return;
//				}
//			}//被举报者发过言
            doVote();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDelete() {
        reportedMessage.doDelete(bot);
        command.doDelete(bot);// 删除举报
    }

    private void doReplyToReport(String text) {// 回复举报者
        MessageShell reply = command.doReply(bot, text, true, null);
        if (!reply.isNull()) {
            reply.doDeleteAfter(bot, bot.getTipSeconds());// 9秒后删除提示
        }
        command.doDelete(bot);// 删除举报
    }

    private void doVote() {
        String sb = String.format("该消息被举报，请在 %d  分钟内投票表决。", bot.getVoteMinutes()) + '\n' +
                String.format("⚪%s：将根据票数和比例禁言被举报者，最低 %d 小时。", VoteAction.Option.恶意行为.name(), bot.getMuteHoursValid()) + '\n' +
                String.format("⚪%s：若被连续驳回 %d 次，将禁言举报者 %d 小时。", VoteAction.Option.驳回举报.name(), bot.getRejectLimit(), bot.getRejectLimit()) + '\n' +
                String.format("仅 %d 小时内发过言的成员有投票权。", bot.getPreHours()) + '\n' +
                String.format("至少 %d 票且过半为有效。", bot.getVoteValid()) + '\n';
        int[]voteCounts=new int[VoteAction.Option.length];
        boolean reporterIsValid=reporterMember.isValid(command.getDate(), bot.getPreHours());
        if(reporterIsValid){
            voteCounts[VoteAction.Option.恶意行为.ordinal()]=1;
        }
        MessageShell resultMessage = reportedMessage.doReply(bot, sb, true, VoteAction.buttons(voteCounts));
        if (resultMessage.isNull()) {// 没有权限
            doReplyToReport("无法处理被举报消息。");
            return;
        }
        command.doDelete(bot);// 删除举报
        Bot.LOGGER.info(reportedUser().getID()+" 被告 "+resultMessage.getLinkOrPrivateID());
//		ResultHandler resultMessage = new ResultHandler(vmh);
        Vote vote = new Vote(resultMessage.getPrivateID(), reporter.getID(), reportedMessage.getPrivateID(), reportedUser().getID());
        VoteDAO.createOrUpdate(vote);
//		resultMessage.doResultLater(bot);
//		return resultMessage;
        if (reporterIsValid) {//举报者有投票权
            VoteCount voteCount = new VoteCount(vote, reporter.getID(), (byte) VoteAction.Option.恶意行为.ordinal());
            VoteCountDAO.createOrUpdate(voteCount);
            Bot.LOGGER.info(reporter.getID()+" "+VoteAction.Option.恶意行为+" "+resultMessage.getLinkOrPrivateID());
        }
//		else if(!reporterMember().isValid()) {//举报者没发过言
//			reporterMember().log(1, 0);//设置为发过言但无投票权的用户
//		}
        resultMessage.doPin(bot, true);
    }

    private UserShell reportedUser() {
        return reportedMessage.getFrom();
    }
}