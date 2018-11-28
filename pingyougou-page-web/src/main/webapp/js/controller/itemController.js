app
		.controller(
				'itemController',
				function($scope,$http) {
					$scope.num = 1;
					// 数量加减
					$scope.addNum = function(x) {
						$scope.num += x;
						if ($scope.num < 1) {
							$scope.num = 1;
						}
					}
					// 定义规格数据存储规格数据
					$scope.specificationItems = {};
					// 选择规则数据
					$scope.selectSpecification = function(key, value) {
						$scope.specificationItems[key] = value;
						searchSku();
					}
					// 判断规格数据是否被选中
					$scope.isSelect = function(key, value) {
						if ($scope.specificationItems[key] == value) {
							return true;
						} else {
							return false;
						}
					}

					$scope.sku = {};// 当前选择的sku

					// 加载默认的sku
					$scope.loadSku = function() {
						$scope.sku = skuList[0];
						$scope.specificationItems = JSON.parse(JSON
								.stringify($scope.sku.spec));

					}

					// 匹配specificationItems和spec是否相同
					matchObject = function(map1, map2) {
						for ( var k in map1) {
							if (map1[k] != map2[k]) {
								return false;
							}
						}
						for ( var k in map2) {
							if (map2[k] != map1[k]) {
								return false;
							}
						}
						return true;
					}
					// 根据规格查找sku
					searchSku = function() {
						for (var i = 0; i < skuList.length; i++) {
							if (matchObject(skuList[i].spec,
									$scope.specificationItems)) {
								$scope.sku = skuList[i];
								return;
							}
						}
						$scope.sku = {
							id : 0,
							title : '--------',
							price : 0
						};// 如果没有匹配的
					}

					// 添加商品到购物车
					$scope.addToCart = function() {

						$http
								.get(
										'http://localhost:9107/cart/addGoodsToCartList.do?itemId='
												+ $scope.sku.id + '&num='
												+ $scope.num,{'withCredentials':true})
								.success(
										function(response) {
											if (response.success) {
												location.href = 'http://localhost:9107/cart.html';//跳转到购物车页面
											} else {
												alert(response.message);
											}

										})
					}

				})