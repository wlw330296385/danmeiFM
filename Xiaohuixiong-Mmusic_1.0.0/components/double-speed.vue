<template>
	<view class="double-speed">
		<view @click="isShow = false" class="cu-modal bottom-modal" :class="isShow ? 'show' : ''">
			<view @click.stop class="cu-dialog">
				<view class="cu-bar bg-white">
					<view class="action text-green"></view>
					<view class="action text-blue" @tap="isShow = false">取消</view>
				</view>
				<view class="bg-white">
					<view class="padding speed-item" :class="currSpeed == item ? 'text-red' : ''" v-for="(item, index) in list" @click="speed(item)" :key="index">{{item}}x</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: "double-speed",
		data() {
			return {
				isShow: false,
				list: ['0.50', '1.00', '1.25', '1.50', '1.75', '2.00']
			};
		},
		computed: {
			currSpeed() {
				return this.$store.state.player.currPlayInfo.speed;
			}
		},
		methods: {
			show() {
				this.isShow = true;
			},
			hide() {
				this.isShow = false;
			},
			speed(val) {
				this.isShow = false;
				this.$store.dispatch('changePlayerSpeed', Number(val));
			}
		}
	}
</script>

<style lang="scss" scoped>
	.double-speed {
		.speed-item {
			border-bottom: 1rpx solid #eeeeee;
		}
		.speed-item:last-child {
			border-bottom: none;
		}
	}
</style>
