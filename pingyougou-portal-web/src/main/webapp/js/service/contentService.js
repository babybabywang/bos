app.service('contentService',function($http){

	this.findListByCateId=function(categordId){
	 return	$http.get("content/findByCategoryId/"+categordId+".do");
	}
})
