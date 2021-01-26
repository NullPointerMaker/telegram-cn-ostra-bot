package npDev.telegramBot.cnOstra;

import com.google.gson.Gson;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import npDev.telegramBot.Instance;
import npDev.telegramBot.cnOstra.handler.QueryHandler;
import npDev.telegramBot.cnOstra.handler.ReportHandler;
import npDev.telegramBot.cnOstra.handler.StartHandler;
import npDev.telegramBot.cnOstra.handler.VoteAction;
import npDev.telegramBot.cnOstra.shell.CallbackQueryOstra;
import npDev.telegramBot.cnOstra.shell.MessageOstra;

import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

public class Bot extends Instance {
    public static final Gson GSON = new Gson();
    public static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder();
    public static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
    public static final Logger LOGGER = Logger.getGlobal();

    public Bot(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        System.setProperty("java.net.useSystemProxies", "true");
        new Bot("telegram-cn-ostra-bot.conf");
    }

    @Override
    public int process(List<Update> updates) {
        try {
            for (Update update : updates) {
                MessageOstra message = new MessageOstra(update.message());
                if(!message.getPinnedMessage().isNull()&&getUser().equals(message.getFrom())){//置顶通知
                    message.doDelete(this);
                    continue;
                }
                String atUsername=getUser().getAtUsername();
                if (message.hasCommand(ReportHandler.COMMAND, atUsername)) {
                    new ReportHandler(this, message).doReport();
                    continue;
                }
                if (message.hasCommand(QueryHandler.COMMAND, atUsername)) {
                    new QueryHandler(this, message).doQuery();
                    continue;
                }
                if (message.hasCommand(StartHandler.COMMAND, atUsername)) {
                    new StartHandler(this, message).doStart();
                    continue;
                }
                if (message.hasText()) {
//                    } else if (message.hasText()&&message.getChat().isGroupChat()) {
//                        Member member = message.getMember();member.log(message.getDate(), getPreHours());
                    message.logMember(getPreHours());
                    continue;
                }
                CallbackQueryOstra callbackQuery = new CallbackQueryOstra(update.callbackQuery());
                if (callbackQuery.hasData()) {
                    new VoteAction(this, callbackQuery).doVote();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return UpdatesListener.CONFIRMED_UPDATES_NONE;
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public int getCacheHours() {
        return getInteger("cache_hours", 3);
    }

    public int getTipSeconds() {
        return getInteger("tip_seconds", 9);
    }

    public int getVoteMinutes() {
        return getInteger("vote_minutes", 3);
    }

    public int getMuteHoursValid() {
        return getInteger("mute_hours_valid", 3);
    }

    private int getMuteHours3() {
        return getInteger("mute_hours_3", 168);
    }

    public int getRejectLimit() {
        return getInteger("reject_limit", 3);
    }

    public int getVoteValid() {
        return getInteger("vote_valid", 3);
    }

    public int getPreHours() {//投票前多久时间内有发言的人有投票权
        return getInteger("pre_hours", 3);
    }

    /**
     * 全票刑期=恶意票数*bc系数+bc常数<br/>
     * 其中bc系数和bc常数按照以下方程计算：<br/>
     * bcValid*bc系数+bc常数=termValid<br/>
     * bc3*bc系数+bc常数=term3<br/>
     * 刑期=恶意票比*br系数+br常数<br/>
     * 其中br系数和br常数按照以下方程计算：<br/>
     * 0.5f*br系数+br常数=termValid<br/>
     * br系数+br常数=全票刑期<br/>
     */
    @SuppressWarnings("NonAsciiCharacters")
    public float getMute(int breakCount, float breakRatio) {
        int bcValid = getVoteValid() / 2 + 1, bc3 = 3 * getVoteValid();
        float termValid = getMuteHoursValid(), term3 = getMuteHours3();
        float bc系数 = (term3 - termValid) / (bc3 - bcValid);
        float bc常数 = termValid - bcValid * bc系数;
        float 全票刑期 = bc系数 * breakCount + bc常数;
        float br系数 = (全票刑期 - termValid) / 0.5f;
        float br常数 = termValid - 0.5f * br系数;
        return breakRatio * br系数 + br常数;
    }
}
