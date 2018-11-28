app
		.controller(
				'seckillController',
				function(seckillService, $scope, $location, $interval) {
					// 查询秒杀的商品列表
					$scope.findList = function() {
						seckillService.findList().success(function(response) {
							$scope.seckillList = response;
						})
					}
					// 接受参数id
					$scope.findOneFromRedis = function() {
						seckillService
								.findOneFromRedis($location.search()['id'])
								.success(
										function(response) {
											$scope.seckillGoods = response;
											// 倒计时开始
											// 获取从结束时间到当前日期的秒数
											allsecond = Math
													.floor((new Date(
															$scope.seckillGoods.endTime)
															.getTime() - new Date()
															.getTime()) / 1000);
											
											time = $interval(
													function() {
														allsecond = allsecond - 1;
													$scope.timeString=convertTimeString(allsecond);
														$scope.timeString = convertTimeString(allsecond);// 转换时间字符串
														if (allsecond <= 0) {
															$interval
																	.cancel(time);
														}
														
													}, 1000);
										})
					}
					// 转换秒为 天小时分钟格式 XXX天10：20：33
					convertTimeString = function(alllsecond) {
						var days = Math.floor(allsecond / (60 * 60 * 24));// 天数
						var hours = Math
								.floor((allsecond - days * 60 * 60 * 24)
										/ (60 * 60));// 小数数
						var minutes = Math.floor((allsecond - days * 60 * 60
								* 24 - hours * 60 * 60) / 60);// 分钟数
						var seconds = allsecond - days * 60 * 60 * 24 - hours
								* 60 * 60 - minutes * 60; //秒数
						var timeString = "";
						if (days > 0) {
							timeString = days + "天 ";
						}
						return timeString + hours + ":" + minutes + ":"
								+ seconds;
					}

					// 倒计时
					/*
					 * time=$interval(function(){ $scope.second=$scope.second-1;
					 * if($scope.second<=0){ $interval.cancel(time); } },1000);
					 */
				//提交订单
				$scope.sumbitOrder=function(){
					seckillService.sumbitOrder($scope.seckillGoods.id).success(
							function(response){
								if(response.success){
									alert("抢购成功,请在5分钟完成支付!");
									location.href="pay.html";//跳转支付页面
								}else{
									alert(response.message);
								}
							})
				}
				})