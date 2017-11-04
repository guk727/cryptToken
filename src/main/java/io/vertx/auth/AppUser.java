package io.vertx.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

/**
 * Created by IronResolve on 2017/10/15.
 */
public class AppUser extends AbstractUser {


    private String guid;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public AppUser() {
        // default constructor
    }

    public AppUser( String guid) {
       this.guid = guid;
    }


    @Override
    protected void doIsPermitted(String permission, Handler<AsyncResult<Boolean>> resultHandler) {

    }

    @Override
    public JsonObject principal() {

        return null;
    }

    @Override
    public User isAuthorised(String authority, Handler<AsyncResult<Boolean>> resultHandler) {

        return super.isAuthorised(authority, resultHandler);
    }

    @Override
    public void setAuthProvider(AuthProvider authProvider) {
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "guid='" + guid + '\'' +
                '}';
    }
}
