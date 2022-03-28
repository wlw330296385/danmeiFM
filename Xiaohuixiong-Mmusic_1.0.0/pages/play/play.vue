<template>
	<view class="content">
		<view class="status-bar"></view>
		<!-- header -->
		<view class="player-header">
			<text class="player-header-icon text-white cuIcon-back" @tap="goBack"></text>
			<view class="player-header-title text-lg text-cut flex align-center">
				<text class="title-name">{{currTrack.name}}</text>
				<text class="margin-lr-sm">-</text>
				<text class="title-msg">{{currTrack.author ? currTrack.author : '作者·未知~'}}</text>
			</view>
			<text class="player-header-share text-white cuIcon-share"></text>
		</view>
		<!-- videodisc -->
		<view class="player-videodisc">
			<image :src="currTrack.cover ? currTrack.cover : '../../static/images/videodisc.png'" 
			:style="isPlaying ? 'animation-play-state: running' : 'animation-play-state: paused;'" 
			class="round turn cover-img" mode="aspectFill"></image>
		</view>
		<!-- slider -->
		<view class="flex align-center player-slider">
			<view class="player-currentTime">
				<view class="player-currentTime-num">{{currPlayInfo.currentPosition / 1000 | secondToDate}}</view>
			</view>
			<slider class="slider" min="0" :max="currPlayInfo.duration ? currPlayInfo.duration : 1"
				:value="changingValue < 0 ? currPlayInfo.currentPosition : changingValue" activeColor="#b6b6b6"
				backgroundColor="#dedede" block-size="12" @changing="changingProgress" @change="changeProgress" />
			<view class="player-duration">
				<view class="player-duration-num">{{currPlayInfo.duration / 1000 | secondToDate}}</view>
			</view>
		</view>
		<!-- playbar -->
		<view class="play-bar">
			<view @click="doFavorites" class="">
				<text v-if="currTrack.follow" class="text-red cuIcon-likefill"></text>
				<text v-else class="text-white cuIcon-like"></text>
			</view>
			<view @tap="plaryPrevious" class="">
				<text v-if="hasPrevious" class="text-white cuIcon-backwardfill"></text>
				<text v-else class="text-grey cuIcon-backwardfill"></text>
			</view>
			<view class="play-menu">
				<text class="text-white cuIcon-playfill" :class="isPlaying ? 'cuIcon-stop' : 'cuIcon-playfill'" @tap="playMusic"></text>
			</view>
			<view @tap="playNext" class="">
				<text v-if="hasNext" class="text-white cuIcon-play_forward_fill"></text>
				<text v-else class="text-grey cuIcon-play_forward_fill"></text>
			</view>
			<view @click="$refs.playlistModal.show()" class="">
				<text class="text-white cuIcon-list"></text>
			</view>
		</view>
		<bottem-play-list ref="playlistModal"></bottem-play-list>
	</view>
</template>

<script>
	import {doFavorites} from '@/api/user.js';
	import BottemPlayList from '@/components/bottem-play-list.vue';
	import {
		secondToDate
	} from '@/utils/util.js';

	export default {
		components: {
			BottemPlayList
		},
		data() {
			return {
				optList: [{
						'id': 0,
						'icon': 'cuIcon-like'
					},
					{
						'id': 1,
						'icon': 'cuIcon-down'
					},
					{
						'id': 2,
						'icon': 'cuIcon-notice'
					},
					{
						'id': 3,
						'icon': 'cuIcon-message'
					},
					{
						'id': 4,
						'icon': 'cuIcon-moreandroid'
					}
				],
				changingValue: -1
			}
		},
		computed: {
			isPlaying() {
				return this.$store.state.player.currPlayInfo.isPlaying;
			},
			hasPrevious() {
				return this.$store.getters.hasPrevious;
			},
			hasNext() {
				return this.$store.getters.hasNext;
			},
			currTrack() {
				if (this.$store.getters.currTrack) {
					return this.$store.getters.currTrack
				} else {
					return {}
				}
			},
			currPlayInfo() {
				if (this.$store.state.player.currPlayInfo) {
					return this.$store.state.player.currPlayInfo
				} else {
					return {}
				}
			}
		},
		onLoad() {

		},
		filters: {
			secondToDate(val) {
				return secondToDate(val);
			}
		},
		methods: {
			goBack() {
				uni.navigateBack({
					delta: 1
				})
			},
			playMusic() {
				if (this.isPlaying) {
					this.$store.dispatch('doStop');
				} else {
					this.$store.dispatch('doPlay');
				}
			},
			changingProgress(e) {
				this.changingValue = e.detail.value;
			},
			changeProgress(e) {
				this.$store.dispatch('seekTo', e.detail.value);
				setTimeout(() => {
					this.changingValue = -1;
				}, 110)
			},
			playNext() {
				this.$store.dispatch('playNext');
			},
			plaryPrevious() {
				this.$store.dispatch('plaryPrevious');
			},
			doFavorites() {
				let data = {works_id: this.currTrack.id};
				doFavorites(data).then(res => {
					if (res.data.code === 200) {
						this.currTrack.follow = this.currTrack.follow == 1 ? 0 : 1;
					} else if (res.data.code === 401) {
						uni.navigateTo({
							url: '/pages/login/login'
						})
					} else {
						this.$toast.fail(res.data.msg);
					}
				})
			},
			gn(index) {
				console.log('~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~');
				if (index == 0) {
					this.$store.dispatch('isServiceRunning');
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import url("./index.css");
	
	.cover-img {
		border: 9rpx solid #6f7173;
	}
	.play-stop {
		animation-play-state: paused;
	}
	.play-running {
		animation-play-state: running;
	}
	.turn {
		width: 100px;
		height: 100px;
		background: aqua;
		animation: turn 5s linear infinite;
		margin: 100px auto;
	}

	/*
		turn : 定义的动画名称
		1s : 动画时间
		linear : 动画以何种运行轨迹完成一个周期
		infinite :规定动画应该无限次播放
	*/
	@keyframes turn {
		0% {
			transform: rotate(0deg);
		}

		25% {
			transform: rotate(90deg);
		}

		50% {
			transform: rotate(180deg);
		}

		75% {
			transform: rotate(270deg);
		}

		100% {
			transform: rotate(360deg);
		}
	}
</style>
