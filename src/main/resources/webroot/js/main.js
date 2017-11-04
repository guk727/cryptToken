
var guid="";
var token="";

var hashList=[[0,5,9,15,22,28],[2,8,19,25,30,31],[20,25,31,3,4,8],[25,31,0,9,13,17]
,[29,2,11,17,21,26],[10,15,18,29,2,3],[5,10,15,17,18,22],[8,20,22,27,19,21]];

$(function(){
   $("#loginBtn").click(function(){
        $.post(
            "http://localhost:8083/login/submit",
            {"username":"guk","password":"123","model":"iphoneX"},
            function(data){
                dataj=JSON.parse(data);
                guid=dataj.uid;
                token=dataj.token;
                alert("post login submit success. uid:"+guid+";token:"+token);
            }
        );
   });
   $("#showDataBtn").click(function(){
        if(guid==""||token==""){
            alert("首先需要登录获取token guid等信息");
            return;
        }

        var timestamp=new Date().getTime();
        var path="of/index";
        var param={"pid":123};
        var randomStr=token[2]+token[5]+token[9];
        var hashIndex=parseInt(randomStr,16)%8;
        var hash=hashList[hashIndex];
        var cryptToken="";
        for(var i=0;i<hash.length;i++){
            cryptToken=token[hash[i]];
        }
        var sign=md5(path+timestamp+guid+param+cryptToken)
        console.log("js md5code:"+path+timestamp+guid+param+cryptToken);
        alert("md5:"+sign);
        console.log("md5:"+sign);
        var dataj={"guid":guid,"sign":sign,"timestamp":timestamp,"param":param,"path":path};
        console.log(JSON.stringify(dataj))
        $.post(
            "http://localhost:8083/of/index",
            JSON.stringify(dataj),
            function(data){
                alert("get of index success");
            }
        );
   });
});