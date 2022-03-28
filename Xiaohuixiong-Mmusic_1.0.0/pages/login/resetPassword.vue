<template>
	<view class="reset-password">
		<view class="padding flex align-center">
			<view class="text-xl" style="width: 180rpx; ">新 密 码：</view>
			<view class="flex-sub text-xl"><input class=" bg-gray padding-sm radius text-xl" style="height: 64rpx;" type="password" v-model="form.password" /></view>
		</view>
		<view class="padding flex align-center">
			<view class="text-xl" style="width: 180rpx;">确认密码：</view>
			<view class="flex-sub"><input class=" bg-gray padding-sm radius text-xl" style="height: 64rpx;" type="password" v-model="form.password2" /></view>
		</view>
		<view class="padding">
			<view @click="toResetPassword" class="bg-orange padding-sm radius text-center text-xl">确定</view>
		</view>
	</view>
</template>

<script>
	import {toResetPassword2} from '@/api/user.js';
	
	export default {
		data() {
			return {
				form: {},
				query: {}
			}
		},
		onLoad(query) {
			this.query = query;
		},
		methods: {
			toResetPassword() {
				if (!this.form.password) {
					return this.$toast.fail('请填写新密码');
				}
				if (this.form.password != this.form.password2) {
					return this.$toast.fail('输入的两次密码不一致');
				}
				
				this.loading = true;
				toResetPassword2({...this.form, key: this.query.t}).then(res => {
					this.loading = false;
					if (res.data.code === 200) {
						this.$toast.success('重置成功');
						setTimeout(() => {
							uni.navigateTo({
								url: '/pages/login/login'
							})
						}, 2000)
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
	page {
		background-color: #ffffff;
	}
	.reset-password {
		max-width: 1000rpx;
	}
</style>
