app.service("searchService",function($http){
	this.itemSearch=function(searchMap){
		return $http.post('itemsearch/search.do',searchMap);
	}
})