import {getUserInfo} from '@/api/user.js';

export default {
	state: {
		user: uni.getStorageSync("user_info"),
		token: uni.getStorageSync("user_token")
	},
	getters: {
		userToken(state) {
			return state.token;
		},
		userInfo(state) {
			return state.user;
		}
	},
	mutations: {
		setUser(state, user) {
			if (user == null) {
				uni.removeStorageSync("user_info");
			} else {
				uni.setStorageSync("user_info", user);
			}
			state.user = user;
		},
		setToken(state, token) {
			if (token == null) {
				uni.removeStorageSync("user_token");
			} else {
				uni.setStorageSync("user_token", token);
			}
			state.token = token;
		}
	},
	actions: {
		getUserInfo(context) {
			return new Promise((resolve, reject) => {
				getUserInfo().then(res => {
					if (res.data.code == 200) {
						context.commit('setUser', res.data.user);
					} else if (res.data.code == 401) {
						context.commit('setUser', null);
						context.commit('setToken', null);
						// this.$toast.fail('登录信息已过期');
					}
				})
			});
		}
	}
}
