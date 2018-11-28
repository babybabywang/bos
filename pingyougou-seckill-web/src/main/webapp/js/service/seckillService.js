app.service('seckillService',function($http){
	//读取列表数据绑定到表单中
	this.findList=function(){
		return $http.get('seckillGoods/findList.do');
	}
	//根据id读取秒杀商品信息
	this.findOneFromRedis=function(id){
		return $http.get('seckillGoods/findOneFromRedis.do?id='+id);
	}
	//提交订单
	this.sumbitOrder=function(seckillId){
		return $http.get('seckillOrder/submitOrder.do?seckillId='+seckillId);
	}
})