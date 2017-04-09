/*
 * Copyright 2017 lorislab.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lorislab.mechanic.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lorislab.mechanic.app.Parameters;

/**
 *
 * @author Andrej Petras
 */
public class Database implements AutoCloseable {

    private static final String CREATE_TABLE = "CREATE TABLE {0} (ID VARCHAR(255) PRIMARY KEY,AUTHOR VARCHAR(255),FILENAME VARCHAR(255),DATEEXECUTED DATETIME,ORDEREXECUTED INT,"
            + "EXECTYPE VARCHAR(255),MD5SUM VARCHAR(255),DESCRIPTION VARCHAR(255),COMMENTS VARCHAR(255),PROFILE VARCHAR(255),"
            + "MECHANIC VARCHAR(20))";

    private static final String MAX_VER_SELECT = "SELECT NVL(MAX(ID),0) FROM {0} WHERE AUTHOR='{1}'";

    private static final String INSERT_CHANGE_LOG = "INSERT INTO {0} (ID,AUTHOR,FILENAME,DATEEXECUTED,ORDEREXECUTED,EXECTYPE,MD5SUM,DESCRIPTION,COMMENTS,PROFILE,MECHANIC) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

    private static final String SELECT_CHANGE_LOG = "SELECT ID,AUTHOR,FILENAME,DATEEXECUTED,ORDEREXECUTED,EXECTYPE,MD5SUM,DESCRIPTION,COMMENTS,PROFILE,MECHANIC FROM {0} {1} {2}";

    private static final String SELECT_CHECK_SUM = "SELECT ID,MD5SUM FROM {0}";

    private static final String DELETE_ALL = "DELETE FROM {0}";

    private final Connection connection;

    private final String logTable;

    public Database(Parameters parameters) {
        this.connection = createConnection(parameters.getDbUrl(), parameters.getDbUser(), parameters.getDbPassword());
        this.logTable = parameters.getDbLogTable();
        initLogTable();
    }

    public void commit() {
        try {
            connection.commit();
        } catch (Exception e) {
            throw new RuntimeException("Could not commit the transaction", e);
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (Exception e) {
            throw new RuntimeException("Could not rollback the transaction", e);
        }
    }

    public Map<String, String> selectChangeLogHistoryCheckSum() {
        String sql = MessageFormat.format(SELECT_CHECK_SUM, logTable);

        Map<String, String> result = new HashMap<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getString(2));
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not execute the select: " + sql, e);
        }
        return result;
    }

    public List<ChangeLogHistory> selectChangeLogHistory() {
        String sql = MessageFormat.format(SELECT_CHANGE_LOG, logTable, "", "");
        return selectChangeLogHistorySql(sql);
    }

    public List<ChangeLogHistory> selectChangeLogHistory(String sqlWhere) {
        String tmp = MessageFormat.format(SELECT_CHANGE_LOG, logTable, "WHERE", sqlWhere);
        return selectChangeLogHistorySql(tmp);
    }

    private List<ChangeLogHistory> selectChangeLogHistorySql(String sql) {
        List<ChangeLogHistory> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChangeLogHistory item = new ChangeLogHistory();
                item.setId(rs.getString(1));
                item.setAuthor(rs.getString(2));
                item.setFileName(rs.getString(3));
                item.setExecuted(new Date(rs.getTimestamp(4).getTime()));
                item.setOrder(rs.getInt(5));
                item.setType(rs.getString(6));
                item.setSum(rs.getString(7));
                item.setDesc(rs.getString(8));
                item.setComments(rs.getString(9));
                item.setProfile(rs.getString(10));
                item.setVersion(rs.getString(11));
                result.add(item);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not execute the select: " + sql, e);
        }
        return result;
    }

    public void insertChangeLogHistory(ChangeLogHistory chlh) {
        String sql = MessageFormat.format(INSERT_CHANGE_LOG, logTable);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, chlh.getId());
            ps.setString(2, chlh.getAuthor());
            ps.setString(3, chlh.getFileName());
            ps.setTimestamp(4, new Timestamp(chlh.getExecuted().getTime()));
            ps.setInt(5, chlh.getOrder());
            ps.setString(6, chlh.getType());
            ps.setString(7, chlh.getSum());
            ps.setString(8, chlh.getDesc());
            ps.setString(9, chlh.getComments());
            ps.setString(10, chlh.getProfile());
            ps.setString(11, chlh.getVersion());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Could not execute the insert: " + sql, e);
        }
    }

    public int getMaxAuthorVersion(String author) {
        int result = 0;
        String sql = MessageFormat.format(MAX_VER_SELECT, logTable, author);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            result = rs.getInt(1);
        } catch (Exception e) {
            throw new RuntimeException("Could not execute the sql: " + sql, e);
        }
        return result;
    }

    private void initLogTable() {
        try {
            ResultSet rs = connection.getMetaData().getTables(null, null, logTable, new String[]{"TABLE"});
            if (!rs.isBeforeFirst()) {
                String sql = MessageFormat.format(CREATE_TABLE, logTable);
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.executeUpdate();
                } catch (Exception e) {
                    throw new RuntimeException("Could not execute the update: " + sql, e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not read the information about log table : " + logTable, e);
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                throw new IOException("Error closing the database connection!", e);
            }
        }
    }

    public void cleanTable() {
        String sql = MessageFormat.format(DELETE_ALL, logTable);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Could not execute the delete sql: " + sql, e);
        }
    }

    private static Connection createConnection(String url, String user, String password) {
        try {
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Error loading the H2 jdbc driver!", e);
            }
            Connection result = DriverManager.getConnection(url, user, password);
            result.setAutoCommit(false);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("The database " + url + " could not be created!", e);
        }
    }
}
