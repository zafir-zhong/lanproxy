package org.fengfei.lanproxy.server.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.fengfei.lanproxy.server.config.MysqlConfig;
import org.fengfei.lanproxy.server.config.ProxyConfig;
import org.fengfei.lanproxy.server.entity.Client;
import org.fengfei.lanproxy.server.entity.ClientProxyMapping;
import org.fengfei.lanproxy.server.entity.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zafir.zhong
 * @description
 * @date Created in 5:02 下午 2022/4/2.
 */
public class MysqlUtils {
    private static Logger logger = LoggerFactory.getLogger(MysqlUtils.class);

    private static DruidDataSource dataSource;

    static {
        //数据源配置
        MysqlConfig mysqlConfig = ProxyConfig.getInstance().getMysqlConfig();
        dataSource = new DruidDataSource();
        dataSource.setUrl(mysqlConfig.getUrl());
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver"); //这个可以缺省的，会根据url自动识别
        dataSource.setUsername(mysqlConfig.getUsername());
        dataSource.setPassword(mysqlConfig.getPassword());
    }



    public static UserInfo getUserByName(String name) {
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("dataSource connect error", e);
            throw new RuntimeException(e);
        }
        //获取连接
        try {
            logger.info("get data source connect success");
            name = "\"" + name + "\"";

            //Statement接口
            Statement statement = connection.createStatement();

            String sql = "select id,username,password,permission,role,phone from user where username = " + name + " limit 1";
            final ResultSet resultSet = statement.executeQuery(sql);
//            //PreparedStatement接口
//            String sql2 = "insert into tb_student (name,age) values ('chy',21)";
//            PreparedStatement preparedStatement = connection.prepareStatement(sql2);
//            preparedStatement.execute();
            if (!resultSet.next()) {
                return null;
            }
            return new UserInfo(resultSet.getLong(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4),
                    resultSet.getString(5),
                    resultSet.getString(6));
            //关闭连接
        } catch (SQLException e) {
            logger.error("dataSource sql error", e);
            throw new RuntimeException(e);
        } finally {
            try {
                logger.info("dataSource connect close");
                connection.close();
            } catch (SQLException e) {
                logger.error("dataSource connect close error", e);
            }
        }
    }


    public static List<Client> getClients() {
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("dataSource connect error", e);
            throw new RuntimeException(e);
        }
        //获取连接
        List<Client> clients = new ArrayList<>();
        int num = 0;
        Long lastId = -1L;
        try {
            logger.info("get data source connect success");
            //Statement接口
            Statement statement = connection.createStatement();

            String sql = "select c.id,c.name,c.client_key,c.status," +
                    "m.id,m.client_id,m.inet_port,m.lan,m.name" +
                    " from lanproxy_client c left join lanproxy_mapping m on c.id = m.client_id order by c.id,m.id";
            final ResultSet resultSet = statement.executeQuery(sql);

            Client client = null;
            while (resultSet.next()) {
                if (lastId == null || !lastId.equals(resultSet.getLong(1))) {
                    // 一个新的客户端
                    lastId = resultSet.getLong(1);
                    client = new Client(resultSet.getLong(1), resultSet.getString(2), resultSet.getString(3), resultSet.getInt(4));
                    client.setProxyMappings(new ArrayList<ClientProxyMapping>());
                    final ClientProxyMapping clientProxyMapping = new ClientProxyMapping(resultSet.getLong(5), resultSet.getLong(6), resultSet.getInt(7), resultSet.getString(8), resultSet.getString(9));
                    if(clientProxyMapping.checkMapping()){
                        client.getProxyMappings().add(clientProxyMapping);
                    }
                    clients.add(client);
                    continue;
                }
                final ClientProxyMapping clientProxyMapping = new ClientProxyMapping(resultSet.getLong(5), resultSet.getLong(6), resultSet.getInt(7), resultSet.getString(8), resultSet.getString(9));
                if(clientProxyMapping.checkMapping()){
                    client.getProxyMappings().add(clientProxyMapping);
                }
            }
            logger.info("clients num:"+ clients.size());
            return clients;
            //关闭连接
        } catch (SQLException e) {
            logger.error("dataSource sql error", e);
            throw new RuntimeException(e);
        } finally {
            try {
                logger.info("dataSource connect close");
                connection.close();
            } catch (SQLException e) {
                logger.error("dataSource connect close error", e);
            }
        }
    }


//    public static void main(String[] args) {
//
//        final List<Client> clients = getClients();
//        for (Client client : clients) {
//            client.setName(client.getName()+"test");
//            for (ClientProxyMapping proxyMapping : client.getProxyMappings()) {
//                proxyMapping.setInetPort(proxyMapping.getInetPort()+1);
//            }
//        }
//        update(clients);
//    }

    public static void updateClients(List<Client> clientsUpdated){
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("dataSource connect error", e);
            throw new RuntimeException(e);
        }
        try {
            logger.info("get data source connect success");
            connection.setAutoCommit(false);
            String sql = "delete from lanproxy_client; delete from lanproxy_mapping;";
            //Statement接口
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            if(clientsUpdated == null || clientsUpdated.isEmpty()){
                logger.error("dataSource sql commit");
                connection.commit();
                return;
            }
            StringBuilder sb = new StringBuilder();
            int key = 1;
            for (int i = 0; i < clientsUpdated.size(); i++) {
                Client client = clientsUpdated.get(i);
                sb.append(" insert into lanproxy_client(id,name,client_key,status) value(")
                        .append(i+1).append(",\"").append(client.getName()).append("\",\"")
                        .append(client.getClientKey()).append("\",").append(client.getStatus()).append(");");
                if(client.getProxyMappings() == null || client.getProxyMappings().isEmpty()){
                    continue;
                }
                for (int j = 0; j < client.getProxyMappings().size(); j++) {
                    ClientProxyMapping mapping = client.getProxyMappings().get(j);
                    sb.append(" insert into lanproxy_mapping(id,client_id,inet_port,lan,name) value(")
                            .append(key++).append(",").append(i+1).append(",")
                            .append(mapping.getInetPort()).append(",\"").append(mapping.getLan()).append("\",\"")
                            .append(mapping.getName())
                            .append("\");");
                }
            }
            statement.executeUpdate(sb.toString());
            connection.commit();
            logger.error("dataSource sql commit");
        }catch (SQLException e) {
            try {
                logger.error("dataSource sql rollback");
                connection.rollback();
            } catch (SQLException ex) {
                logger.error("dataSource rollback close error", e);
            }
            logger.error("dataSource sql error", e);
            throw new RuntimeException(e);
        } finally {
            try {
                logger.info("dataSource connect close");
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
                logger.error("dataSource connect close error", e);
            }
        }
    }

}
