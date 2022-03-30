<template name="BottomBar">
	<view class="fixed bottom" style="z-index: 999;">
		<view class="bottom-left">
			<image :src="currTrack.cover ? currTrack.cover : '../../static/images/home.png'" class="round" mode="aspectFill" @tap="goPlay"></image>
			<view class="aideo-info" @tap="goPlay">
				<view class="audio-name">
					{{currTrack.name}}
				</view>
				<view class="audio-singer">
					{{currTrack.author ? currTrack.author : (currTrack.description ? currTrack.description : '作者·未知~')}}
				</view>
			</view>
		</view>
		<view class="bottom-right">
			<view @tap="plaryPrevious" class="margin-right">
				<text v-if="hasPrevious" class="text-black cuIcon-backwardfill"></text>
				<text v-else class="text-gray cuIcon-backwardfill"></text>
			</view>
			<view class="btn-player margin-right">
				<text v-if="!isPlaying" :class="playList.length ? 'text-black' : 'text-gray'" class=" cuIcon-playfill" @tap="doPlay"></text>
				<text v-else class="text-black cuIcon-stop" @tap="doStop"></text>
			</view>
			<view @tap="playNext" class="margin-right">
				<text v-if="hasNext" class="text-black cuIcon-play_forward_fill"></text>
				<text v-else class="text-gray cuIcon-play_forward_fill"></text>
			</view>
			<view @tap.stop="showPlaylist" class="btn-info">
				<text class="text-black cuIcon-list"></text>
			</view>
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
	@import url("./index.css");
</style>
