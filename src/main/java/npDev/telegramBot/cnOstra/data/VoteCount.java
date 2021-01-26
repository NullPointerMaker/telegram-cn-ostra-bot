package npDev.telegramBot.cnOstra.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Objects;

@DatabaseTable(tableName = "vote_count")
public class VoteCount {
    static final String VOTE = "vc_vote", OPTION = "vc_option", USER = "vc_user";
    @DatabaseField(id = true)
    private String id;
    @DatabaseField(columnName = VOTE, canBeNull = false, foreign = true)
    private Vote vote;
    @DatabaseField(columnName = USER, canBeNull = false)
    private Integer user;
    @DatabaseField(columnName = OPTION, canBeNull = false)
    private Byte option;

    public VoteCount(Vote vote, Integer user, Byte option) {
        setVote(vote);
        setUser(user);
        setId(vote.getId() + "@" + user);
        setOption(option);
    }

    @SuppressWarnings("unused")
    VoteCount() {
    }

    @SuppressWarnings("unused")
    String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public Vote getVote() {
        return vote;
    }

    public void setVote(Vote vote) {
        this.vote = vote;
    }

    public int getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }

    @SuppressWarnings("unused")
    byte getOption() {
        return option;
    }

    public void setOption(Byte option) {
        this.option = option;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + user;
        result = prime * result + ((vote == null) ? 0 : vote.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VoteCount other = (VoteCount) obj;
        if (!Objects.equals(user, other.user))
            return false;
        if (vote == null) {
            return other.vote == null;
        } else return vote.equals(other.vote);
    }
}
