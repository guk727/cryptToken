package io.vertx.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.serviceproxy.ProxyHelper;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class WikiDatabase extends AbstractVerticle {

  public static final String SERVER_ADDRESS = "server-address";

  private AsyncSQLClient client;

  public HashMap<SqlQuery, String> loadSqlQuerys() {
    //获取数据库文件信息
    InputStream queryInputStream = getClass().getResourceAsStream("/SQLProperties.properties");
    Properties properties = new Properties();
    try {
      //把获取的文件信息放入新建的文件对象
      properties.load(queryInputStream);
      queryInputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    //放入map集合（数据名，执行代码）
    //获取的时候用sqlQueries.get(SqlQuery.USER_INSERT_ONE)
    HashMap<SqlQuery, String> sqlQueries = new HashMap<>();
    sqlQueries.put(SqlQuery.SQL_GET_PAGE, properties.getProperty("SQL_GET_PAGE"));
    sqlQueries.put(SqlQuery.INSERT_TOKEN,properties.getProperty("INSERT_TOKEN"));
    sqlQueries.put(SqlQuery.QUERY_TOKEN,properties.getProperty("QUERY_TOKEN"));
    sqlQueries.put(SqlQuery.UPDATE_TOKEN,properties.getProperty("UPDATE_TOKEN"));
    return sqlQueries;
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    HashMap<SqlQuery, String> sqlQueries = loadSqlQuerys();
    JsonObject mySQLClientConfig = new JsonObject()
      .put("host", "localhost")
      .put("port", 3306)
      .put("maxPoolSize", 10)
      .put("username", "root")
      .put("password", "gukuo123")
      .put("database", "tokentest");
    client = MySQLClient.createShared(vertx, mySQLClientConfig);

    WikiDatabaseService.create(client, sqlQueries, ready -> {
      if (ready.succeeded()) {
        ProxyHelper.registerService(WikiDatabaseService.class, vertx, ready.result(), SERVER_ADDRESS); // <1>
        startFuture.complete();
      } else {
        startFuture.fail(ready.cause());
      }
    });
  }
}

