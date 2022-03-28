export default class Toast {
	
	success(message){
		uni.showToast({
			title: message
		})
	}
	
	message(message){
		uni.showToast({
			icon: 'none',
			title: message
		})
	}
	
	fail(message){
		uni.showToast({
			icon: 'none',
			title: message
		})
	}
	

	loading({message = "请稍后...", mask = true} = {}){
		
		uni.showLoading({
			title: message,
			mask: mask
		})
	} 
	
	hideLoading(){
		uni.hideLoading()
	}
}