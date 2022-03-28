<template>
	<view class="login-box bg-white padding-xs margin-top radius register" style="min-height: 300rpx;">
		<view v-if="!status">
			<form>
				<view class="cu-form-group">
					<view class="title">用户名</view>
					<input class="solid-bottom" style="height: 40px;" placeholder="注册时的邮箱账号" type="text" v-model="form.username"></input>
				</view>
			</form>
			<view class="cu-bar bg-white  flex padding-lr">
				<button @click="toResetPassword" class="cu-btn block line-orange margin-tb-sm lg" style="width: 100%;">下一步</button>
			</view>
		</view>
		<view v-else class="">
			<view class="flex justify-center  margin-tb-sm text-green title-msg text-lg">
				密码重置成功！
			</view>
			<view class="flex justify-center text-center padding">
				重置密码的邮件已经发送到您到邮箱，请登陆邮箱查看重置密码链接。
			</view>
			<view class="cu-bar bg-white  flex padding-lr">
				<button @click="toLogin" class="cu-btn block line-orange margin-tb-sm lg" style="width: 100%;">返回登陆</button>
			</view>
		</view>
		<view class="cu-bar  flex align-center justify-center foot" style="box-shadow: 0px 0px 0px #888888;">
			耽听FM @ 2021
		</view>
	</view>
</template>

<script>
	import {toResetPassword} from '@/api/user.js';
	
	export default {
		data() {
			return {
				form: {
					username:''
				},
				status:false,
				loading: false
			}
		},
		onLoad(query) {
		},
		computed: {
			
		},
		methods: {
			toLogin() {
				uni.navigateTo({
					url: '/pages/login/login'
				})
			},
			toResetPassword() {
				if (this.loading) {
					return ;
				}
				if (this.form.username == '') {
					return this.$toast.fail('请输入注册时的邮箱地址');
				}
				let reg = new RegExp("^[a-z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$"); //正则表达式
				if (!reg.test(this.form.username)) {
					return this.$toast.fail('邮箱格式不正确');
				}
				
				
				this.loading = true;
				toResetPassword(this.form).then(res => {
					this.loading = false;
					if (res.data.code === 200) {
						this.status = true
					} else {
						this.$toast.fail(res.data.msg);
					}
				}).catch(e => {
					this.loading = false;
					this.status = true
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
