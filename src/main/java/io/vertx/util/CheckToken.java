package io.vertx.util;


import io.vertx.util.Secret.NewMd5;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//检查签名
public class CheckToken {

  private static Map<Integer, List<Integer>> hashList = new HashMap<>();

  static {
    Integer[][] hash = {{0,5,9,15,22,28},{2,8,19,25,30,31},
            {20,25,31,3,4,8},{25,31,0,9,13,17},
            {29,2,11,17,21,26},{10,15,18,29,2,3},
            {5,10,15,17,18,22},{8,20,22,27,19,21}};
    for (int i = 0; i <= 7; i++) {
      hashList.put(i, Arrays.asList(hash[i]));
    }
  }

  public static String check(String path, String sign, String token, String guid,String param, Long timestamp){

    long millis = System.currentTimeMillis();
    Long l = millis -  timestamp;
    if (l>30000){
      return "SN002";
      //超过30分钟 时间异常
    }
    String cryptToken = CheckToken.getcryptToken(token);
    String signChecked = NewMd5.GetMD5Code(path+timestamp+guid+param+cryptToken);
    System.out.println("md5code:"+path+timestamp+guid+param+cryptToken);
    System.out.println("check signChecked:"+signChecked);
    if (signChecked.equalsIgnoreCase(sign)){
      System.out.println("sn000");
      return "SN000";
      //签名认证成功
    }else {
      return "SN005";
      //签名错误
    }
  }

  public static String getcryptToken(String token){
    //此处省略,大家可以根据自己的算法实现
    //各种算法  返回加密后的cryptToken
    StringBuilder randomStr = new StringBuilder();
    randomStr.append(token.charAt(2));
    randomStr.append(token.charAt(5));
    randomStr.append(token.charAt(9));

    List<Integer> hash = hashList.get(Integer.parseInt(randomStr.toString(),16) % 8);

    StringBuilder cryptToken = new StringBuilder();
    for (Integer i : hash) {
      cryptToken.append(token.charAt(i));
    }
    System.out.println(cryptToken.toString());
    return cryptToken.toString() ;

  }


}
