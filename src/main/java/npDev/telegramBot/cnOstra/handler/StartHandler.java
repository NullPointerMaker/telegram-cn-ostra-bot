package npDev.telegramBot.cnOstra.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import npDev.telegramBot.cnOstra.Bot;
import npDev.telegramBot.cnOstra.data.Vote;
import npDev.telegramBot.cnOstra.data.VoteDAO;
import npDev.telegramBot.cnOstra.shell.MessageOstra;
import npDev.telegramBot.shell.ChatMemberShell;
import npDev.telegramBot.shell.MessageShell;
import npDev.telegramBot.shell.UserShell;

public class StartHandler {
    public static final String COMMAND = "start";
    private final MessageOstra command;
    private final Bot bot;
    private final UserShell user;

    public StartHandler(Bot bot, MessageOstra message) {
        this.bot = bot;
        this.command = message;
        this.user = command.getFrom();
    }

    public void doStart() {
        try {
            if (!command.getChat().isPrivate()) {//不是私聊
                return;
            }
            String text = command.getText();
            if (text.length() < 8) {//没有参数
                return;
            }
            text = text.substring(7);
            String paras = new String(Bot.BASE64_DECODER.decode(text));
            JsonObject json;
            try {
                json = JsonParser.parseString(paras).getAsJsonObject();
            } catch (JsonSyntaxException jse) {
//				jse.printStackTrace();
                return;
            }
            if (!json.has(QueryHandler.COMMAND)) {//没有对应参数
                return;
            }
            String voteID = json.get(QueryHandler.COMMAND).getAsString();
            Vote vote = VoteDAO.queryForId(voteID);
            if (vote == null) {//无此表决
                return;
            }
            ChatMemberShell chatMember = user.doGetChatMember(bot, vote.getChatID());
            if (chatMember.isModerator()) {
                Bot.LOGGER.info(user.getID()+" "+QueryHandler.COMMAND + MessageShell.getPrivateLink(voteID));
                String result = QueryHandler.getQueryResult(voteID);
                user.doSendMessage(bot, result, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}