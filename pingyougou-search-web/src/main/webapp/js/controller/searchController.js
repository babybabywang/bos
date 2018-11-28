app.controller('searchController', function($scope,$location,searchService) {

	$scope.searchMap = {
		'keywords' : '',
		'category' : '',
		'brand' : '',
		'spec' : {},
		'price' : '',
		'pageNo' : 1,
		'pageSize' : 40,
		'sort':'',
		'sortField':''
	};// 搜索对象

	$scope.search = function() {
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
		searchService.itemSearch($scope.searchMap).success(function(response) {
			$scope.resultMap = response;// 搜索返回的结果
			buildLable();
		})
	}

	// 构建分页栏
	buildLable = function() {
		$scope.pageLable = [];
		var firstPage = 1;// 起始页
		var lastPage = $scope.resultMap.totalPages;// 最后页
		$scope.firstDot=true;//前面有点
		$scope.lastDot=true;//后面有点
		if ($scope.resultMap.totalPages > 5) {// 如果总页数大于5
			if ($scope.searchMap.pageNo <= 3) {// 如果当前页小于等于3
				lastPage = 5;
				$scope.firstDot=false;//前面没点
			} else if ($scope.searchMap.pageNo >= lastPage - 2) {// 如果当前页大于等于最大页码-2
				firstPage = lastPage - 4; // 后5页
				$scope.lastDot=false;//后面没点
			} else { // 显示当前页为中心的5页
				firstPage = $scope.searchMap.pageNo - 2;
				lastPage = $scope.searchMap.pageNo + 2;
			}

		}else{
			$scope.firstDot=false;//前面无点
			$scope.lastDot=false;//后边无点
		}
		for (var i = firstPage; i <= lastPage; i++) {
			$scope.pageLable.push(i);
		}
	}
	// 添加搜索项
	$scope.addSearchItem = function(key, value) {
		if (key == 'category' || key == 'brand' || key == 'price') {// 如果点击的是分类或者是品牌
			$scope.searchMap[key] = value;
		} else {
			$scope.searchMap.spec[key] = value;
		}
		$scope.search();// 查询
	}
	// 撤销搜索项
	$scope.removeSearchItem = function(key, value) {
		if (key == 'category' || key == 'brand' || key == 'price') {// 如果点击的是分类或者是品牌
			$scope.searchMap[key] = "";
		} else {
			delete $scope.searchMap.spec[key];
		}
		$scope.search();//查询
	}
	//根据页码查询
	$scope.queryByPage=function(pageNo){
		if(pageNo<1||pageNo>$scope.resultMap.totalPages){
			return ;
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();//查询
	}
	//判断是否当前页是第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	//判断是否当前页是最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}
	}
	
	//排序查询
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sortField=sortField;
		$scope.searchMap.sort=sort;
		$scope.search();//查询
	}
	//判断关键字是不是品牌
	$scope.keywordsIsBrand=function(){
		for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
				return true;
			}
		}
		return false;
	}
	//接收加载搜索参数
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords=$location.search()['keywords'];
		$scope.search();//查询
	}
})