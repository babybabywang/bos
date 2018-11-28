 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	
	//注册
	$scope.reg=function(){
		//比较俩次输入的密码是否一致
		if($scope.entity.password!=$scope.repassword){
			alert("两次输入的密码不一致，请重新输入");		   
			$scope.entity.password="";//清空
			return;
		}
		userService.add($scope.entity,$scope.redis_code).success(function(response){
			alert(response.message);
		})
	}
	
	//发送验证码
	$scope.sendCode=function(){
		if($scope.entity.phone==null||$scope.entity.phone==""){
			alert("请输入手机号码！");
			return ;
		}
		userService.sendCode($scope.entity.phone).success(function(response){
			alert(response.message);
		})
	}
    
});	
