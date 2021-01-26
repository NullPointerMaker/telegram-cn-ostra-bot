package npDev.telegramBot.cnOstra.handler;

import com.google.gson.JsonObject;
import npDev.telegramBot.cnOstra.Bot;
import npDev.telegramBot.cnOstra.data.Vote;
import npDev.telegramBot.cnOstra.data.VoteCount;
import npDev.telegramBot.cnOstra.data.VoteCountDAO;
import npDev.telegramBot.cnOstra.data.VoteDAO;
import npDev.telegramBot.cnOstra.shell.MessageOstra;
import npDev.telegramBot.shell.ChatMemberShell;
import npDev.telegramBot.shell.MessageShell;
import npDev.telegramBot.shell.UserShell;

import java.util.List;

public class QueryHandler {
    public static final String COMMAND = "query";
    private final Bot bot;
    private final MessageOstra command;
    private final MessageShell result;
    private final UserShell inquirer;

    public QueryHandler(Bot bot, MessageOstra message) {
        this.bot = bot;
        command = message;
        inquirer = command.getFrom();
        result = command.getReplyToMessage();
    }

    static String getQueryResult(String voteId) {
        StringBuilder sb = new StringBuilder(String.format("投票 %s 详情如下：", MessageOstra.getPrivateLink(voteId)));
        Vote vote = new Vote(voteId, 0, null, 0);
        for (VoteAction.Option option : VoteAction.Option.values()) {
            sb.append("\n").append(option.name()).append("：");
            List<VoteCount> list = VoteCountDAO.queryForMatchingVoteAndOption(vote, (byte) option.ordinal());
            for (VoteCount vc : list) {
                sb.append(UserShell.toIDMarkdown(vc.getUser())).append("；");
            }
        }
        return sb.toString();
    }

    public void doQuery() {
        try {
            if (result.isNull()) {// 查询消息为空
                return;
            }
            if (!bot.getUser().equals(result.getFrom())) {//被查询的消息不是来自于bot
                return;
            }
            Vote vote = VoteDAO.queryForId(result.getPrivateID());
            if (vote == null) {//无此表决
                return;
            }
            ChatMemberShell chatMember = command.doGetChatMember(bot);
            if (chatMember.isModerator()) {
                Bot.LOGGER.info(inquirer.getID()+" "+result.getLinkOrPrivateID());
                doReplyToQuery();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doReplyToQuery() {// 回复查询者
        String message = getQueryResult(result.getPrivateID());
        inquirer.doSendMessage(bot, message, true);
//		doQueryResult(bot, resultMessage.getPrivateID(), queryUser().id());
        JsonObject json = new JsonObject();
        json.addProperty(COMMAND, result.getPrivateID());
        String paras = Bot.BASE64_ENCODER.encodeToString(json.toString().getBytes());
        message = String.format("已私聊投票详情，若未收到，请点击[链接](%s)跳转。", bot.getStartLink(paras));
        MessageShell reply = result.doReply(bot, message, true, null);
        if (reply != null) {
            reply.doDeleteAfter(bot, bot.getTipSeconds());// 9秒后删除提示
        }
        command.doDelete(bot);
    }
}