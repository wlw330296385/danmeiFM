<template>
	<view class="login-box padding-xs margin-top radius">
		<view class="header bg-white cu-bar flex align-center justify-center text-bold text-black solid-bottom radius">
			账号登陆
		</view>
		<view>
			<form>
				<view class="cu-form-group">
					<view class="title">用户名</view>
					<input placeholder="邮箱账户" type="text" v-model="form.username"></input>
				</view>
				<view class="cu-form-group solid-bottom">
					<view class="title">密 码</view>
					<input placeholder="6个以上字符" type="password" v-model="form.password"></input>
				</view>
			</form>
			<view class="cu-bar bg-white  flex padding-lr" style="padding-top: 20px;">
				<template v-if="allowLogin">
					<button class="cu-btn bg-red margin-tb-sm lg" style="width: 100%;" @tap="checkLogin()">登 陆</button>
				</template>
				<template v-else>
					<button disabled class="cu-btn bg-red margin-tb-sm lg" style="width: 100%;">登陆</button>
				</template>
			</view>
			<view class="flex justify-around bg-white padding-lr">
				<button @click="toRegister" class="cu-btn block line-orange margin-tb-sm lg">注册账户</button>
				<button @click="toGetPassword" class="cu-btn block line-brown margin-tb-sm lg">找回密码</button>
					
			</view>
		</view>
		<view class="cu-bar  flex align-center justify-center flex-direction margin-bottom foot" style="box-shadow: 0px 0px 0px #888888;">
			<view class="">
				©2021 耽听FM
			</view>
			<view class="text-sm">
				有更新版本，下载更新
			</view>
		</view>
	</view>
</template>

<script>
	import {login} from '@/api/user.js';
	
	export default {
		data() {
			return {
				form: {
					username: 'ok0@qq.com',
					password: '111111'
				},
				loading: false
			};
		},
		computed: {
			allowLogin() {
				return this.form.username && this.form.password &&  this.form.username.length > 3 && this.form.password.length >= 6
			}
		},
		created() {
			this.checkLogin();
		},
		methods: {
			toRegister() {
				uni.navigateTo({
					url: '/pages/login/register'
				})
			},
			toGetPassword() {
				uni.navigateTo({
					url: '/pages/login/getPassword'
				})
			},
			checkLogin() {
				if (this.loading) {
					return ;
				}
				/*let reg = new RegExp("^[a-z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$"); //正则表达式
				if (!reg.test(this.form.username)) {
					return this.$toast.fail('邮箱格式不正确');
				}*/
				
				this.loading = true;
				login(this.form).then(res => {
					this.loading = false;
					if (res.data.code === 200) {
						this.$store.commit('setUser', res.data.user);
						this.$store.commit('setToken', res.data.token);
						this.$toast.success('登录成功');
						setTimeout(() => {
							uni.navigateTo({
								url: '/pages/index/index'
							})
						}, 2000);
					} else {
						this.$toast.fail(res.data.msg);
					}
				}).catch(e => {
					this.loading = false;
					this.$toast.fail('网络异常');
				})
			}
		}
	}
</script>

<style lang="scss">
	.cu-form-group .title {
		min-width: calc(4em + 15px);
	}
</style>
