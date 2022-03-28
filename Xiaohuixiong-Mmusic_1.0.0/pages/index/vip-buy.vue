<template>
	<view>
		<view class="padding">
			<view v-for="(item, index) in prices" :key="index" @click="showQrCode = true" :class="list[index].bg" class="padding radius flex margin-bottom">
				<view class="flex-sub text-bold">
					<text :class="list[index].icon" class="margin-right-sm"></text>
					{{item.name}}
				</view>
				<view class="text-price">{{item.price / 100}}</view>
			</view>
		</view>
		<view @tap="showQrCode = false" class="cu-modal" :class="showQrCode ? 'show' : ''">
			<view @tap.stop class="cu-dialog padding">
				<view class="bg-img bg-white padding-top-lg">
					<view class="flex align-center justify-center" style="min-height: 460rpx;">
						<image mode="aspectFit" :src="qrCodeUrl"></image>
					</view>
				</view>
				<view class="cu-bar bg-white">
					<view class="action margin-0 flex-sub text-red solid-left">
						保存本收款码并通过微信扫码转账，转账时请一定要备注您的用户名，转账完成微信联系客服开通会员。
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				showQrCode: false,
				list: [
					{
						icon: 'cuIcon-selection',
						bg: 'bg-gradual-green'
					},
					{
						icon: 'cuIcon-upstagefill',
						bg: 'bg-gradual-blue'
					},
					{
						icon: 'cuIcon-crownfill',
						bg: 'bg-gradual-pink'
					},
					{
						icon: 'cuIcon-choicenessfill',
						bg: 'bg-gradual-purple'
					},
					{
						icon: 'cuIcon-selection',
						bg: 'bg-gradual-green'
					},
					{
						icon: 'cuIcon-selection',
						bg: 'bg-gradual-green'
					},
					{
						icon: 'cuIcon-selection',
						bg: 'bg-gradual-green'
					}
				]
			}
		},
		computed: {
			config() {
				return this.$store.state.app.config;
			},
			qrCodeUrl() {
				if (this.config && this.config.qrcode && this.config.qrcode.length) {
					return this.config.qrcode[0].url;
				} else {
					return '';
				}
			},
			prices() {
				if (this.config && this.config.prices && this.config.prices.length) {
					return this.config.prices;
				} else {
					return [];
				}
			}
		},
		onShow() {
			this.$store.dispatch('getAppConfig');
		},
		methods: {
			
		}
	}
</script>

<style lang="scss" scoped>
	page {
		background-color: #ffffff;
	}
</style>
