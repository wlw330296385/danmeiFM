<template>
	<view class="login-box padding-xs margin-top radius register">
		<view class="header bg-white cu-bar flex align-center justify-center text-bold text-black solid-bottom radius">
			1、账号注册 -> 2、下载app
		</view>
		<view >
			<form>
				<view class="cu-form-group">
					<view class="title">用户名</view>
					<input v-if="isreg"  placeholder="请填写邮箱作为用户名" type="text" v-model="form.username" disabled></input>
					<input v-else placeholder="请填写邮箱作为用户名" type="text" v-model="form.username"></input>
				</view>
				<view class="cu-form-group solid-bottom">
					<view class="title">密 码</view>
					<input  v-if="isreg"  placeholder="6个以上字符" type="password" v-model="form.password" disabled></input>
					<input v-else  placeholder="6个以上字符" type="password" v-model="form.password"></input>
				</view>
				<view class="cu-form-group solid-bottom">
					<view class="title">确认密码</view>
					<input  v-if="isreg"  placeholder="6个以上字符" type="password" v-model="form.confirmPassword" disabled></input>
					<input v-else placeholder="6个以上字符" type="password" v-model="form.confirmPassword"></input>
				</view>
				<view class="cu-form-group solid-bottom">
					<view class="title">邀请码</view>
					<view class="">
						{{form.inviter}}
					</view>
				</view>
			</form>
			<view class="cu-bar bg-white  flex padding-lr" style="padding-top: 20px;">
				<button  v-if="isreg"  class="cu-btn bg-red margin-tb-sm lg" style="width: 100%;" @click="downloan()" >↓注册成功，请下载app吧↓</button>
				<button  v-else class="cu-btn bg-red margin-tb-sm lg" style="width: 100%;" @tap="doRegister()">注 册</button>
			</view>
			<view class="cu-bar bg-white  flex padding-lr">
				<a v-if="allow" href="/danting.apk" class="cu-btn block bg-orange margin-tb-sm lg radius" style="width: 100%;">下载耽听app</a>
				<view v-else class="cu-btn block line-gray margin-tb-sm lg radius" style="width: 100%;">注册完成方可下载app</view>
			</view>
			<view class="padding bg-white" style="height: 100px;">
				注意事项：请使用手机浏览器打开本页面，如果你是通过微信扫二维码进入到本页面，请点击微信右上角，选择“在浏览器打开”。
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
				loading: false,
				allow:false,
				isreg:false
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
			downloan() {
				uni.downloadFile({
				    url: '/danting.apk', //仅为示例，并非真实的资源
				    success: (res) => {
				        if (res.statusCode === 200) {
				            console.log('下载成功');
				        }
				    }
				});
			},
			doRegister() {
				if (this.loading) {
					return ;
				}
				let reg = new RegExp("^[a-z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$"); //正则表达式
				if (!reg.test(this.form.username)) {
					return this.$toast.fail('邮箱格式不正确');
				}
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
						this.allow = true
						this.isreg = true
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
