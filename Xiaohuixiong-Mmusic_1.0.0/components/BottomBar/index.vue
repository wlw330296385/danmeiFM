<template name="BottomBar">
	<view class="uni-flex uni-row">
		<view class="flex-item">
			<view class="outer17 flex-col justify-end align-end">
				<view class="group6 flex-col"></view>
			</view>
			<text class="word31">推荐</text>
		</view>
		<view class="flex-item">
			<view class="outer11 flex-col justify-end align-start">
			<view class="section11 flex-row justify-between">
				<view class="mod2 flex-col">
					<image class="icon14" referrerpolicy="no-referrer"
						src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPng747c57dbd2b81b2b3b312a4d64d16f58273076c4374582624ed7d424e575b0c8" />
				</view>
				<view class="mod3 flex-col">
					<image class="img4" referrerpolicy="no-referrer"
						src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPnga711b8dac11bfbd23f1acd9b062b555842bbc902a6d00567b4cd26eded055451" />
					<text class="txt27">窈窕珍馐第一集</text>
				</view>
			</view>
			<image class="label11" referrerpolicy="no-referrer"
				src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPng60581bc039befefe91341df04e90479c532051e037349fe8160c84e848b9b951" />
		</view>
		</view>
		<view class="flex-item">
			<view class="outer18 flex-col"></view>
			<text class="word32">我的</text>
		</view>
		
		
		<bottem-play-list ref="playlistModal"></bottem-play-list>
	</view>
</template>

<script>
	import BottemPlayList from '@/components/bottem-play-list.vue';

	export default {
		name: "BottomBar",
		components: {
			BottemPlayList
		},
		props: {},
		data() {
			return {

			}
		},
		computed: {
			hasPrevious() {
				return this.$store.getters.hasPrevious;
			},
			hasNext() {
				return this.$store.getters.hasNext;
			},
			isPlaying() {
				return this.$store.state.player.currPlayInfo.isPlaying;
			},
			currTrack() {
				if (this.$store.getters.currTrack) {
					return this.$store.getters.currTrack
				} else {
					return {}
				}
			},
			playList() {
				return this.$store.state.player.playlist;
			}
		},
		methods: {
			playNext() {
				this.$store.dispatch('playNext');
			},
			plaryPrevious() {
				this.$store.dispatch('plaryPrevious');
			},
			doStop() {
				this.$store.dispatch('doStop');
			},
			doPlay() {
				this.$store.dispatch('doPlay');
			},
			goPlay() {
				console.log(this.currTrack)
				if (this.currTrack && this.currTrack.type) {
					if (this.currTrack.type == 1) {
						uni.navigateTo({
							url: '/pages/play-video/play-video?id=' + this.currTrack.albumId
						})
					} else if (this.currTrack.type == 2) {
						uni.navigateTo({
							url: '/pages/play/play'
						})
					}
				}
			},
			showPlaylist() {
				this.$refs.playlistModal.show();
			}
		}
	}
</script>

<style>
	
@import '@/pages/index/common.css';
@import '@/pages/index/index.rpx.css';

.img3 {
  z-index: 330;
  position: absolute;
  left: 0;
  top: 0;
  width: 714rpx;
  height: 330rpx;
}

</style>
