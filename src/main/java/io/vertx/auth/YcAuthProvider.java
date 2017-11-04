package io.vertx.auth;


import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.database.WikiDatabaseService;
import io.vertx.database.WikiDatabaseServiceImpl;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.util.CheckToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IronResolve on 2017/10/15.
 */
public class YcAuthProvider  implements AuthProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(YcAuthProvider.class);

    private Vertx vertx;
    private WikiDatabaseService dbService;
    public YcAuthProvider(WikiDatabaseService dbService ,Vertx vertx){
        this.dbService = dbService;
        this.vertx = vertx;
    }


    @Override
    public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {

        String path = authInfo.getString("path");
        String sign = authInfo.getString("sign");
        String guid = authInfo.getString("guid");
        String param = authInfo.getString("param");
        Long timestamp = authInfo.getLong("timestamp");
        vertx.executeBlocking(res -> {
            dbService.QUERY_TOKEN(guid, resu -> {
                if (resu.succeeded()) {
                    JsonObject result = resu.result();
                    String token = result.getString("token");
                    String SERVERNO = CheckToken.check(path, sign, token, guid, param, timestamp);
                    System.out.println("ycauth  serverno:"+SERVERNO);
                    if ("SN000".equals(SERVERNO)) {
                        System.out.println("snoo0 equal success");
                       User user = new AppUser(guid);
                        user.setAuthProvider(this);
                        res.complete(user);
                    } else {
                        LOGGER.error("签名认证失败");
                    }
                }else {
                    LOGGER.error("查询失败");
                }
            });
        }, resultHandler);

    }
}
