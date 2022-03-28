<template>
	<view class="container bg-white">
		<view class="qrimg flex justify-center align-center">
			<view class="qrimg-i">
				<tki-qrcode cid="qrcode1" ref="qrcode" :size="300" :val="val" :onval="onval" :loadMake="loadMake" :usingComponents="true" @result="qrR" />
			</view>
		</view>
		<view class="uni-padding-wrap padding">
			<view class="btns">
				<button type="primary" @tap="saveQrcode">保存二维码</button>
			</view>
		</view>
	</view>
</template>

<script>
import tkiQrcode from '@/components/tki-qrcode/tki-qrcode.vue';
import config from '@/utils/config'
const serverUrl = process.env.NODE_ENV === 'development' ? config.serverUrl.dev : config.serverUrl.pro;
	
export default {
	data() {
		return {
			onval: false, // val值变化时自动重新生成二维码
			loadMake: true, // 组件加载完成后自动生成二维码
			src: '' // 二维码生成后的图片地址或base64
		}
	},
	onLoad: function () {
		
	},
	computed: {
		user() {
			if (this.$store.state.user.user) {
				return this.$store.state.user.user;
			} else {
				return {};
			}
		},
		token() {
			return this.$store.getters.userToken;
		},
		val() {
			return serverUrl + '/#/pages/login/register2?inviter=' + this.user.userId
		}
	},
	methods: {
		saveQrcode() {
			this.$refs.qrcode._saveCode()
		},
		qrR(res) {
			console.log(res)
			this.src = res
		}
	},
	components: {
		tkiQrcode
	}
}
</script>

<style lang="scss" scoped>
	
</style>
