<template name="Video">
	<scroll-view scroll-y="true" @scrolltolower="scrolltolower" style="background-color: #f5f5f5; height: 100%;">
		<view class="video">
			<view class="cu-bar bg-white solid-bottom">
				<view class="action">
					全部动漫
				</view>
				<view class="margin-right-sm action" @tap="search(3)">
					<text class="cuIcon-search margin-lr-sm"></text>
					搜索
				</view>
			</view>
			<view class="padding bg-white solid-bottom">
				<view class="cu-bar justify-start">
					<view class="select-item" @click="changeQueryStatus(0)" :class="query.status == 0 ? 'active' : ''">
						全部
					</view>
					<view class="select-item" @click="changeQueryStatus(2)" :class="query.status == 2 ? 'active' : ''">
						完结
					</view>
					<view class="select-item" @click="changeQueryStatus(1)" :class="query.status == 1 ? 'active' : ''">
						未完结
					</view>
				</view>
				<view class="cu-bar justify-start">
					<view class="select-item" @click="changeVipStatus(-1)" :class="query.vip == -1 ? 'active' : ''">
						全部
					</view>
					<view class="select-item" @click="changeVipStatus(0)" :class="query.vip == 0 ? 'active' : ''">
						免费
					</view>
					<view class="select-item" @click="changeVipStatus(1)" :class="query.vip == 1 ? 'active' : ''">
						VIP
					</view>
				</view>
			</view>
			<view class="list-box" style="margin-top: -10rpx;">
				<view v-if="listStyle == 1" class="flex list-style1">
					<view class="list-style1-item" v-for="(item, index) in list" :key="index" @tap="goPlay(item)">
						<view class="radius shadow-warp bg-white list-style1-item-in">
							<view class="">
								<image class="list-style1-cover" :src="item.cover ? item.cover : '../../static/images/videodisc.png'" mode="aspectFill"></image>
							</view>
							<view class="style-title text-lg padding-tb-sm text-center text-cut">
								<text>{{item.name}}</text>
							</view>
							<view class="flex text-gray padding-bottom-sm padding-lr-xs">
								<view class="flex-sub">
									<text class="cuIcon-video" style="margin-right: 5rpx;"></text>
									{{item.times}}
								</view>
								<view class="margin-left">
									共{{item.totalCount}}集
								</view>
							</view>
							<view class="status-tag" :class="item.status == 1 ? 'bg-pink' : 'bg-brown'">{{item.status == 1 ? '连载中' : '已完结'}}</view>
							<view v-if="item.vip" class="bg-orange padding-lr-xl padding-top-xl style1-vip-icon">VIP</view>
						</view>
					</view>
				</view>
				<view v-else class="find-style-list">
					<view class="style-item solid-bottom padding-tb-xs" v-for="(item, index) in list" :key="index" @tap="goPlay(item)">
						<image :src="item.cover ? item.cover : '../../static/images/videodisc.png'" mode="aspectFill"></image>
						<view class="style-info">
							<view class="style-title padding-tb-xs flex align-center">
								<text v-if="item.vip" class="bg-orange cu-tag round sm margin-right-xs">VIP</text>
								<text>{{item.name}}</text>
							</view>
							<view class="style-msg flex align-center">
								<view class="">
									播放：{{item.times}}
								</view>
								<view class="margin-left">
									共{{item.totalCount}}集
								</view>
								<view class="margin-left">
									{{item.status == 1 ? '连载中' : '已完结'}}
								</view>
							</view>
						</view>
						<view class="style-icon">
							<text class="text-red cuIcon-video"></text>
						</view>
					</view>
				</view>
			</view>
			<!-- last -->
			<view class="last" style="margin-top: 50rpx;">
				<view v-if="loading" class="cu-load loading"></view>
				<view v-else-if="!list.length">空空如也~</view>
				<view @click="loadMove" v-else-if="list.length < total">点击加载更多</view>
				<view v-else-if="list.length == total">没有更多了</view>
			</view>
		</view>
	</scroll-view>
</template>

<script>
	import {
		getAnimeList
	} from '@/api/works.js';

	export default {
		name: "Video",
		props: {},
		data() {
			return {
				listStyle: 1,
				list: [],
				total: 0,
				loading: false,
				query: {
					status: 0,
					vip: -1,
					pageNum: 1,
					pageSize: 20
				}
			}
		},
		created() {
			this.loadAnimeList();
		},
		methods: {
			scrolltolower() {
				if (this.list.length < this.total) {
					this.loadMove();
				}
			},
			changeVipStatus(vip) {
				this.query.vip = vip;
				this.list = [];
				this.total = 0;
				this.query.pageNum = 1;
				this.loadAnimeList();
			},
			changeQueryStatus(status) {
				this.query.status = status;
				this.list = [];
				this.total = 0;
				this.query.pageNum = 1;
				this.loadAnimeList();
			},
			goPlay(item) {
				uni.navigateTo({
					url: '/pages/play-video/play-anime?id=' + item.id
				})
			},
			loadMove() {
				if (this.loading) {
					return ;
				}
				this.query.pageNum++;
				this.loadAnimeList();
			},
			loadAnimeList() {
				if (this.loading) {
					return ;
				}
				this.loading = true;
				getAnimeList(this.query).then(res => {
					this.loading = false;
					if (res.data.code == 200) {
						this.list = this.list.concat(res.data.rows);
						this.total = res.data.total;
					} else {
						this.$toast.fail(res.data.msg);
					}
				}).catch(e => {
					this.loading = false;
				})
			},
			search(type) {
				uni.navigateTo({
					url: '/pages/search/search?type=' + type,
					events: {
						// 为指定事件添加一个监听器，获取被打开页面传送到当前页面的数据
						acceptDataFromOpenedPage: function(data) {
							console.log(data)
						},
						someEvent: function(data) {
							console.log(data)
						}
					},
					success: function(res) {
						// 通过eventChannel向被打开页面传送数据
						res.eventChannel.emit('acceptDataFromOpenerPage', { data: 'test' })
					}
				})
			},
		}
	}
</script>

<style>
	@import url("./index.css");
	.list-style1 {
		margin: 10rpx 10rpx 0 10rpx;
		padding-bottom: 10rpx;
		flex-wrap: wrap;
	}
	.list-style1-item {
		width: 50%;
		padding: 18rpx;
	}
	.list-style1-cover {
		width: 100%;
		height: 360rpx;
	}
	.list-style1-item-in {
		position: relative;
		overflow: hidden;
	}
	.style1-vip-icon {
		transform:rotate(45deg);
		position: absolute;
		top: -30rpx;
		right: -64rpx;
	}
	.status-tag {
		position: absolute;
		top: 0;
		left: 0;
		padding: 5rpx;
		font-size: 24rpx;
	}
</style>
