//分页需要导入的模块
var app = angular.module("pingyougou", []);
/*$sce服务写成过滤器*/
app.filter('trustHtml',['$sce',function($sce){
    return function(data){// 传入参数是被过滤的内容
        return $sce.trustAsHtml(data); // 返回过滤后的内容(信任html的转换
    }
}]);