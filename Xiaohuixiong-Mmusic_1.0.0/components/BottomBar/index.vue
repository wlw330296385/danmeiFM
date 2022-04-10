<template name="BottomBar">
	<view class="BottomBar">
		<view class="uni-flex uni-row justify-between">
			<view class="flex-item left">
				<view class="">
					<view class="">
						<image src="../../static/images/home.png" mode="aspectFit" style="width: 50rpx;height: 50rpx;" >
							
						</image>
					</view>
					
					<text class="">推荐</text>
				</view>
			</view>
			<view class="flex-item center" >
				<view class="uni-flex uni-row">
					<view class="flex-item changpian" style="">
						<image src="../../static/icon/changpian.png" mode="aspectFit" :class="{'xuanzhuan': isPlaying}"
						 style="width: 60rpx;height: 60rpx;" >
						</image>
					</view>
					<view class="flex-item txt" @tap="goPlay()">
						<text>{{currTrack.name}}
						<!-- {{currTrack.author ? currTrack.author : (currTrack.description ? currTrack.description : '作者·未知~')}} -->
						</text>
					</view>
					<view class="flex-item play-icon"  @tap="isPlaying ? doStop() : doPlay()"  >
						<image :src=" isPlaying ? play_icon_active : play_icon" mode="aspectFit"
						 style="width: 30rpx;height: 30rpx;" >
						</image>
					</view>
				</view>
			</view>
			<view class="flex-item right">
				<view class="" >
						<image src="../../static/icon/mine.png" mode="aspectFit"  
						style="width: 50rpx; height: 50rpx; "
						 >
							
						</image></view>
				<text class="">我的</text>
			</view>
			
			
			<bottem-play-list ref="playlistModal"></bottem-play-list>
		</view>
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
				play_icon: '../../static/icon/sanjiaoxing.png',
				play_icon_active: '../../static/icon/pause.png',
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
				console.log('doStop');
				this.$store.dispatch('doStop');
			},
			doPlay() {
				console.log('doPlay');
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

<style lang="scss" scoped>
	
@import '@/pages/index/common.css';
@import '@/pages/index/index.rpx.css';

.BottomBar{
	z-index:100;
	height: 120rpx;
	position: fixed;
	bottom: 0;
	width: 750rpx;
	background-color: #FFFFFF;
	padding-top: 20rpx;
	.left, .right{
		flex: 3;
		text-align: center;
	}
	.center{
		margin-top: 10rpx;
		height: 80rpx;
		flex:8;
		background-color: #626AFF;
		border-radius: 60rpx;
		.changpian{
			padding: 10rpx 0 0 10rpx; flex: 1;
		};
		.txt{
			width: 300rpx;
		   flex: 4; 
		   text-align: left;
		   padding: 25rpx 5rpx 0 10rpx;
		    overflow: hidden; /*隐藏*/
			white-space: nowrap;  /*不换行*/
			text-overflow: ellipsis;  /* 超出部分省略号 */	
		   color: #FFFDEF;
		};
		.play-icon{
			padding: 25rpx 50rpx 0 0;
		}			
		.xuanzhuan{
			animation:rotate 3s linear infinite;
		}
		
		@keyframes rotate{
			to{
				transform: rotate(360deg);
			}
		}
	}
	
};

</style>
