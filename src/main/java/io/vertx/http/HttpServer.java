package io.vertx.http;

import io.vertx.auth.YcAuthProvider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.database.WikiDatabaseService;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import java.util.*;

public class HttpServer extends AbstractVerticle {
  private String address = "server-address";
  private WikiDatabaseService dbService;

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    dbService = WikiDatabaseService.createProxy(vertx, address);
    io.vertx.core.http.HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);
    Set<String> header = new HashSet<>();
    header.add("Access-Control-Allow-Origin:*");
    header.add("Content-Type");
    Set<HttpMethod> methods = new HashSet<>();
    methods.add(HttpMethod.GET);
    methods.add(HttpMethod.POST);
    methods.add(HttpMethod.OPTIONS);
    router.route().handler(CorsHandler.create("*").allowedHeaders(header).allowedMethods(methods));
    router.get("/app/*").handler(StaticHandler.create().setCachingEnabled(false));
    router.post().handler(BodyHandler.create());
    //登录  登录成功生成token存到服务器,Redis mysql 都可以
    router.post("/login/submit").handler(this::getUser);
    //所有post请求以of开头的请求都会经过这个路由
    router.route("/of/*").handler(this::signHandler);
    router.route("/of/otherpath").handler(this::otherHandler);

    server
      .requestHandler(router::accept)
      .listen(8083, ar -> {
        if (ar.succeeded()) {
          System.out.println("Server is now listening!");
        } else {
          System.out.println("Failed to bind!");
        }
      });
  }

  private void otherHandler(RoutingContext routingContext) {

    //执行操作代码
    JsonObject response = new JsonObject();
    //返回的结果集
    response.put("ResultData","需要返回的数据");
//    response.put("SERVERNO", routingContext.get("SERVERNO").toString());
//    response.put("serverTime",routingContext.get("serverTime").toString());
    routingContext.response().end(response.encode());
  }

  //签名验证请求
  private void signHandler(RoutingContext routingContext) {
      System.out.println("signHandler run..."+routingContext.getBodyAsString());
      System.out.println("signHandler run..."+routingContext.request().getParam("guid"));
      System.out.println("signHandler run..."+routingContext.get("guid"));
    JsonObject bodyAsJson = routingContext.getBodyAsJson();
    System.out.println("signHandler run111");
    String guid = bodyAsJson.getString("guid");
      System.out.println("signHandler run222:"+guid
      );
    //得到请求path
    String path = routingContext.request().path();
    String sign = bodyAsJson.getString("sign");
    Long timestamp = bodyAsJson.getLong("timestamp");
    //参数
    JsonObject param = bodyAsJson.getJsonObject("param");
    //把参数转为string类型
    String encode = param.encode();
    System.out.println("guid:"+guid+";sign:"+sign+";timestamp:"+timestamp+";param:"+param);
    JsonObject authInfo = new JsonObject().put("path",path).put("timestamp",timestamp)
            .put("guid",guid).put("param",encode).put("sign",sign);
 //   JsonObject authInfo = new JsonObject().put("guid","guifdsfsafsd");
    YcAuthProvider signAuth = new YcAuthProvider(dbService,vertx);
    signAuth.authenticate(authInfo,res->{
      if (res.succeeded() ){
        User app = res.result();
        System.out.println("app+="+app);
        routingContext.next();
      }else {
          System.out.println("auth error:"+res.cause());
        res.cause().printStackTrace();
      }
    });
  }
  //处理登陆请求
  private void getUser(RoutingContext routingContext) {
    String username = routingContext.request().getParam("username");
    String password = routingContext.request().getParam("password");
    //登录设备
    String device = routingContext.request().getParam("model");
    System.out.println(username + "      " + password);
    JsonObject jsonObject = new JsonObject();
    //执行实现类
    dbService.getUser(username, password, reply -> {
      if (reply.succeeded()) {
        System.out.println("dbService.getUser success");
        JsonObject payload = reply.result();
        Boolean md5Check = payload.getBoolean("md5check");
        String uid = payload.getString("uid");
        System.out.println("uid:"+uid);
        if (md5Check) {
          //检查是否已经存在token
          dbService.QUERY_TOKEN(uid, resu -> {
            if (resu.succeeded()) {
              String token = resu.result().getString("token");
              //重新生成一个token
              String uuid = UUID.randomUUID().toString();
              System.out.println("-----------------" + token);
              if (token != null && token.length() > 0) {
                //token存在就进行更新
                dbService.UPDATE_TOKEN(uid, uuid, device, resul -> {
                  if (resul.succeeded()) {
                    System.out.println("gengxin token");
                    if (resul.result().getBoolean("isSuccess")) {
                      System.out.println("更新token");
                      jsonObject.put("isExist", "1");
                      jsonObject.put("uid", uid);
                      jsonObject.put("token", uuid);
                      routingContext.response().end(jsonObject.encode());
                    }
                  }
                });
              } else {
                //不存在就新增token
                System.out.println("不存在就新增token");
                dbService.INSERT_TOKEN(uid, uuid, device, resul -> {
                  if (resul.succeeded()) {
                    System.out.println("新增token");
                    System.out.println(resul.result().getBoolean("isSuccess"));
                    if (resul.result().getBoolean("isSuccess")) {
                      System.out.println("新增token");
                      jsonObject.put("isExist", "1");
                      jsonObject.put("uid", uid);
                      jsonObject.put("token", uuid);
                      routingContext.response().end(jsonObject.encode());
                    }
                  }
                });
              }
            }
          });
        } else {
          //密码错误
          jsonObject.put("isSuccess", "-2");
          routingContext.response().end(jsonObject.encode());
        }
      } else {
        //查询操作失败
        jsonObject.put("isSuccess", "-1");
        routingContext.response().end(jsonObject.encode());
      }
    });
  }

}
