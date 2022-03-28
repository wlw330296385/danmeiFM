<template>
	<view class="bottem-play-list">
		<view @tap="isShow = false" class="cu-modal bottom-modal" :class="isShow ? 'show' : ''">
			<view @tap.stop class="cu-dialog text-black">
				<view class="cu-bar bg-white header-bar">
					<view class="action text-lg">当前播放（{{playlist.length}}首）</view>
					<view @click="deleteAll" class="action">
						<text class="lg text-gray cuIcon-delete"></text>
					</view>
				</view>
				<view class="bg-white text-left text-df playlist">
					<scroll-view scroll-y class="playlist-scroll">
						<view v-for="(item, index) in playlist" :key="index" class="margin-left padding-tb-sm playlist-item">
							<view @click="playItem(item)" class="flex align-center justify-center">
								<view v-if="item.id == playId" class="margin-right-sm">
									<text class="lg text-lg text-red cuIcon-notification"></text>
								</view>
								<view :class="item.id == playId ? 'text-red' : ''" class="flex-sub text-cut">{{item.name}}</view>
								<view @click="deleteItem(item)" class="padding-right-sm">
									<text class="lg text-gray cuIcon-close"></text>
								</view>
							</view>
						</view>
					</scroll-view>
				</view>
				<view @tap="isShow = false" class="padding bg-white footer-bar text-lg">取消</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				isShow: false
			};
		},
		computed: {
			playlist() {
				return this.$store.state.player.playlist;
			},
			playId() {
				return this.$store.state.player.currPlayInfo.playId;
			},
			currTrack() {
				return this.$store.getters.currTrack;
			}
		},
		methods: {
			show() {
				this.isShow = true;
			},
			hide() {
				this.isShow = false;
			},
			playItem(item) {
				this.$store.dispatch('playListItem', item.id);
			},
			deleteAll() {
				this.$store.dispatch('doClose');
				this.$store.commit('setPlayList', []);
				this.$store.commit('setPlayId', 0);
			},
			deleteItem(item) {
				if (this.currTrack && this.currTrack.id == item.id) {
					this.$store.dispatch('doClose');
					this.$store.commit('setPlayId', 0);
				}
				let list = this.playlist.filter(i => i.id != item.id);
				this.$store.commit('setPlayList', list);
			}
		}
	}
</script>

<style lang="scss" scoped>
	.bottem-play-list {
		.playlist {
			height: 640rpx;
		}
		.playlist-scroll {
			height: 100%;
		}
		.header-bar {
			border-bottom: 1rpx solid #eaeaea;
		}
		.footer-bar {
			border-top: 1rpx solid #eaeaea;
		}
		.playlist-item {
			border-bottom: 1rpx solid #dadada;
			padding: 25rpx 0 25rpx 0;
		}
		.playlist-item:last-child {
			border-bottom: none;
		}
	}
</style>
