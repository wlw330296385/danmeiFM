<template>
	<view class="login-box padding-xs margin-top radius register">
		<view class="header bg-white cu-bar flex align-center justify-center text-bold text-black solid-bottom radius">
			账号注册
		</view>
		<view >
			<form>
				<view class="cu-form-group">
					<view class="title">用户名</view>
					<input placeholder="个人常用邮箱" type="text" v-model="form.username"></input>
				</view>
				<view class="cu-form-group solid-bottom">
					<view class="title">密 码</view>
					<input placeholder="6个以上字符" type="password" v-model="form.password"></input>
				</view>
				<view class="cu-form-group solid-bottom">
					<view class="title">确认密码</view>
					<input placeholder="6个以上字符" type="password" v-model="form.confirmPassword"></input>
				</view>
				<view class="cu-form-group solid-bottom">
					<view class="title">邀请码</view>
					<input placeholder="填写邀请码" type="text" v-model="form.inviter"></input>
				</view>
			</form>
			<view class="cu-bar bg-white  flex padding-lr" style="padding-top: 20px;">
				<template v-if="allowRegister">
					<button class="cu-btn bg-red margin-tb-sm lg" style="width: 100%;" @tap="doRegister()">注 册</button>
				</template>
				<template v-else>
					<button disabled class="cu-btn bg-red margin-tb-sm lg" style="width: 100%;">注 册</button>
				</template>
			</view>
			<view class="cu-bar bg-white  flex padding-lr">
				<button @click="toLogin" class="cu-btn block line-orange margin-tb-sm lg" style="width: 100%;">已有账户，去登录</button>
			</view>
		</view>
		<view class="cu-bar  flex align-center justify-center foot" style="box-shadow: 0px 0px 0px #888888;">
			耽听FM @ 2021
		</view>
	</view>
</template>

<script>
	import {register} from '@/api/user.js';
	
	export default {
		data() {
			return {
				form: {},
				loading: false
			}
		},
		onLoad(query) {
					console.log(query)
					if(query.inviter)this.form.inviter = query.inviter
				},
		computed: {
			allowRegister() {
				return this.form.username && this.form.password && this.form.username.length > 3 && this.form.password.length >= 6;
			}
		},
		methods: {
			toLogin() {
				uni.navigateTo({
					url: '/pages/login/login'
				})
			},
			doRegister() {
				if (this.loading) {
					return ;
				}
				/*let reg = new RegExp("^[a-z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$"); //正则表达式
				if (!reg.test(this.form.username)) {
					return this.$toast.fail('邮箱格式不正确');
				}*/
				if (this.form.password != this.form.confirmPassword) {
					return this.$toast.fail('两次输入的密码不一致');
				}
				
				this.loading = true;
				register(this.form).then(res => {
					this.loading = false;
					if (res.data.code === 200) {
						this.$store.commit('setUser', res.data.user);
						this.$store.commit('setToken', res.data.token);
						this.$toast.success('注册成功');
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

<style lang="scss" scoped>
	.register {
		.cu-form-group .title {
			min-width: calc(4em + 15px);
		}
	}
</style>
