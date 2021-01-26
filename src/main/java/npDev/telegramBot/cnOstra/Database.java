package npDev.telegramBot.cnOstra;


import com.j256.ormlite.jdbc.JdbcConnectionSource;

import java.sql.SQLException;

public class Database {
    private static final String DATABASE = "jdbc:sqlite:telegram-cn-ostra-bot.db";
    private static JdbcConnectionSource database;

    /**
     * 创建全局唯一连接
     *
     * @return 连接
     * @throws SQLException 异常
     */
    public static JdbcConnectionSource getConnection() throws SQLException {
        if (database == null) {
            database = new JdbcConnectionSource(DATABASE);
        }
        return database;
    }
}