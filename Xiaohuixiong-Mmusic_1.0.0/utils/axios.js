import config from './config';

/**
 * HTTP网络请求封装
 */
export default class HttpRequest {
	constructor(serverUrl = serverUrl) {
		this.serverUrl = serverUrl
	}

	handleError(code) {
		let errMsg = null
		switch (code) {
			case 401:
				errMsg = "请先登录"
			case 402:
				errMsg = "登录票据已过期，请重新登录"
			case 403:
				errMsg = "您的票据检测异常，请重新登录"
		}
		return errMsg
	}

	request(opt) {
		if (opt.url) {
			if (opt.url.indexOf('http://') === -1 && opt.url.indexOf('https://') === -1) {
				opt.url = this.serverUrl + opt.url;
			}
		}
		const req = (resolve, reject) => {
			let header = {
				'Accept': 'application/json, text/plain, */*', 
				...opt.header 
			};
			try {
				// 添加用户信息到Header
				const token = uni.getStorageSync("user_token");
				if (token) {
					header['Authorization'] = 'Bearer ' + token;
				}
			} catch (e) {}

			uni.request({
				url: opt.url,
				header: header,
				data: opt.data,
				method: opt.method,
				success: (res) => {
					const {	statusCode, errMsg } = res;
					if (statusCode >= 200 && statusCode < 300) {
						const respCode = res.data.Code
						const _errMsg = this.handleError(respCode)
						if (_errMsg == null) {
							resolve(res)
						} else {
							// 跳转到登陆页
							let routes = getCurrentPages();
							if (routes.length === 0 || routes[routes.length - 1].route !== 'pages/user/login') {
								uni.showToast({
									title: _errMsg,
									icon: 'none'
								})
								setTimeout(() => {
									let routes = getCurrentPages();
									if (routes.length === 0 || routes[routes.length - 1].route !== 'pages/user/login') {
										uni.navigateTo({
											url: '/pages/user/login'
										})
									}
								}, 1000);
							}

							reject({
								errMsg,
								message: `ErrorMessage：${_errMsg}，StatusCode：${respCode}`
							})
							setTimeout(() => uni.$emit('logout', errMsg), 1000);
						}
					} else {
						reject({
							errMsg,
							message: `ErrorMessage：${errMsg}，StatusCode：${statusCode}`
						})
					}
				},
				fail: (err) => {
					err.message = err.errMsg
					reject(err);
				},
				complete: () => {

				},
				dataType: opt.dataType,
				responseType: opt.responseType,
			})
		}

		return new Promise(req);
	}


	get(opt) {
		return this.request({
			method: 'GET',
			...opt
		})
	}

	post(opt) {
		return this.request({
			method: 'POST',
			...opt
		})
	}

	put(opt) {
		return this.request({
			method: 'PUT',
			...opt
		})
	}

	delete(opt) {
		return this.request({
			method: 'DELETE',
			...opt
		})
	}
}
