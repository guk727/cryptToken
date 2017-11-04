package io.vertx.database;


import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.HashMap;

@ProxyGen
public interface WikiDatabaseService {

  @Fluent
  WikiDatabaseService getUser(String username, String password, Handler<AsyncResult<JsonObject>> resultHandler);
  @Fluent
  WikiDatabaseService INSERT_TOKEN(String guid, String token, String device, Handler<AsyncResult<JsonObject>> resultHandler);
  @Fluent
  WikiDatabaseService QUERY_TOKEN(String guid, Handler<AsyncResult<JsonObject>> resultHandler);
  @Fluent
  WikiDatabaseService UPDATE_TOKEN(String guid, String token, String device, Handler<AsyncResult<JsonObject>> resultHandler);

  static WikiDatabaseService create(AsyncSQLClient client, HashMap<SqlQuery, String> sqlQueries, Handler<AsyncResult<WikiDatabaseService>> readyHandler) {
    return new WikiDatabaseServiceImpl(client, sqlQueries, readyHandler);
  }
  // end::create[]
  static WikiDatabaseService createProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(WikiDatabaseService.class,vertx,address);
  }

}
