package io.vertx.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.util.Secret.NewMd5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class WikiDatabaseServiceImpl implements WikiDatabaseService{

  private static final Logger LOGGER = LoggerFactory.getLogger(WikiDatabaseServiceImpl.class);
  private final HashMap<SqlQuery, String> sqlQueries;
  private AsyncSQLClient client;

  WikiDatabaseServiceImpl(AsyncSQLClient client, HashMap<SqlQuery, String> sqlQueries, Handler<AsyncResult<WikiDatabaseService>> readyHandler) {
    this.client = client;
    this.sqlQueries = sqlQueries;
    readyHandler.handle(Future.succeededFuture(this));
  }

  @Override
  public WikiDatabaseService INSERT_TOKEN(String guid, String token, String device, Handler<AsyncResult<JsonObject>> resultHandler) {
    JsonObject response = new JsonObject();
    client.getConnection(res->{
      if (res.succeeded()){
        SQLConnection connection = res.result();
        connection.updateWithParams(sqlQueries.get(SqlQuery.INSERT_TOKEN),new JsonArray().add(guid).add(token).add(device),result ->{
          connection.close();
          if (result.succeeded()){
            int updated = result.result().getUpdated();
            if(updated==1){
              System.out.println("token插入成功");
              response.put("isSuccess",true);
            }else {
              System.out.println("token插入失败");
              response.put("isSuccess",false);
            }
          }else {
            System.out.println("插入token时操作数据库失败");
          }
          resultHandler.handle(Future.succeededFuture(response));
        });
      }else {
        System.out.println("连接数据库失败");
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
    return this;
  }

  @Override
  public WikiDatabaseService QUERY_TOKEN(String guid, Handler<AsyncResult<JsonObject>> resultHandler) {
    JsonObject response = new JsonObject();
    client.getConnection(res->{
      if (res.succeeded()){
        SQLConnection connection = res.result();
        connection.queryWithParams(sqlQueries.get(SqlQuery.QUERY_TOKEN),new JsonArray().add(guid),result ->{
          connection.close();
          if (result.succeeded()){
            int numRows = result.result().getNumRows();
            if(numRows>0){
              System.out.println("这个用户存在token");
              List<JsonArray> results = result.result().getResults();
              System.out.println("results:"+results);
              JsonArray jsonArray = results.get(0);
              System.out.println("jsonArray"+jsonArray);
              String string = jsonArray.getString(0);
              System.out.println("string"+string);
              System.out.println("这个token是"+result.result().getResults().get(0).getString(0));
              response.put("token",result.result().getResults().get(0).getString(0));
            }else {
              System.out.println("这个用户不存在token");
              response.put("isExist",false);
            }
          }else {
            System.out.println("插入token时操作数据库失败");
          }
          resultHandler.handle(Future.succeededFuture(response));
        });

      }else {
        resultHandler.handle(Future.failedFuture(res.cause()));
        System.out.println("连接数据库失败");
      }
    });
    return this;
  }

  @Override
  public WikiDatabaseService UPDATE_TOKEN(String guid, String token, String device, Handler<AsyncResult<JsonObject>> resultHandler) {

    JsonObject response = new JsonObject();
    client.getConnection(res->{
      if (res.succeeded()){
        SQLConnection connection = res.result();
        connection.updateWithParams(sqlQueries.get(SqlQuery.UPDATE_TOKEN),new JsonArray().add(token).add(device).add(guid),result ->{
          connection.close();
          if (result.succeeded()){
            int updated = result.result().getUpdated();
            if(updated>0){
              System.out.println("token更新成功");
              response.put("isSuccess",true);
            }else {
              System.out.println("token更新失败");
              response.put("isSuccess",false);
            }
          }else {
            System.out.println("更新token时操作数据库失败");
          }
          resultHandler.handle(Future.succeededFuture(response));
        });

      }else {
        resultHandler.handle(Future.failedFuture(res.cause()));
        System.out.println("连接数据库失败");
      }
    });
    return this;
  }

  @Override
  public WikiDatabaseService getUser(String username,String password, Handler<AsyncResult<JsonObject>> resultHandler) {
    System.out.println("getUser run...");
    client.getConnection(car -> {
      if(car.succeeded()){
          System.out.println("get conn success");
          SQLConnection connection = car.result();
          connection.queryWithParams(sqlQueries.get(SqlQuery.SQL_GET_PAGE),new JsonArray().add(username), res -> {
              connection.close();
            JsonObject response = new JsonObject();
              if(res.succeeded()) {
                List<JsonArray> results = res.result().getResults();
                String serverPassword = results.get(0).getString(1);
                String uid = results.get(0).getInteger(2).toString();
                System.out.println("uid是"+uid);
                  //比较密码
                  Boolean md5check = serverPassword.equals(password);
                  response.put("md5check",md5check);
                  response.put("uid",uid);
              }else {
                  System.out.println("db error:"+res.cause());
                LOGGER.error("数据库操作失败");
              }
              //放入全局
            resultHandler.handle(Future.succeededFuture(response));
          });
      }else {
          System.out.println("get conn failed"+car.cause());
          resultHandler.handle(Future.failedFuture(car.cause()));
      }
    });
    return this;
  }


}
