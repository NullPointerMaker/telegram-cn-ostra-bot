package npDev.telegramBot.cnOstra.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "vote")
public class Vote {
    private static final String REPORTER = "v_reporter", REPORTED_MESSAGE = "v_reported_message", REPORTED_USER = "v_reported_user", FINISHED = "v_finished";
    @DatabaseField(id = true)
    private String id;
    @DatabaseField(canBeNull = false, columnName = REPORTER)
    private int reporter;
    @DatabaseField(canBeNull = false, columnName = REPORTED_MESSAGE)
    private String reportedMessage;
    @DatabaseField(canBeNull = false, columnName = REPORTED_USER)
    private int reportedUser;
    @DatabaseField(columnName = FINISHED)
    private Boolean finished;

    public Vote(String id, int reporter, String reportedMessage, int reportedUser) {
        setId(id);
        setReporter(reporter);
        setReportedMessage(reportedMessage);
        setReportedUser(reportedUser);
    }

    Vote() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getChatID() {
        return Long.parseLong(id.split("/")[0]);
    }

    public int getReporter() {
        return reporter;
    }

    public void setReporter(int reporter) {
        this.reporter = reporter;
    }

    @SuppressWarnings("unused")
    String getReportedMessage() {
        return reportedMessage;
    }

    public void setReportedMessage(String reportedMessage) {
        this.reportedMessage = reportedMessage;
    }

    public int getReportedUser() {
        return reportedUser;
    }

    public void setReportedUser(int reportedUser) {
        this.reportedUser = reportedUser;
    }

    public boolean getFinished() {
        return finished != null && finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        Vote other = (Vote) obj;
        if (id == null) {
            return other.id == null;
        } else return id.equals(other.id);
    }
}
