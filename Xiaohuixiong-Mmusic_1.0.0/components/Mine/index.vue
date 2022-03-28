<template name="Mine">
	<view class="mine bg-white">
		<!-- user info -->
		<view v-if="user" class="mine-user" @click="clickUser">
			<view class="flex align-center">
				<view v-if="!user.avatar" class="user-ico">
					<view class="cu-avatar lg round"><text class="cuIcon-people"></text></view>
				</view>
				<view v-else class="user-ico">
					<view class="cu-avatar lg round" :style="'background-image: url(' + user.avatar + ');'"></view>
				</view>
				<view class="flex-sub user-msg">
					<view class="margin-bottom-xs text-lg flex align-center">
						<text>{{user.nickName}}</text>
						<text :class="user.vip ? 'bg-orange' : ''" class="cu-tag round sm margin-left-xs">{{user.vip ? 'VIP' : '非VIP'}}</text>
					</view>
					<view class="text-gray">{{user.userName}}</view>
				</view>
				<view class="text-xxl text-gray cuIcon-right"></view>
			</view>
		</view>
		<view v-else class="mine-user" @click="clickUser">
			<view class="flex align-center">
				<view class="user-ico">
					<view class="cu-avatar lg round"><text class="cuIcon-people"></text></view>
				</view>
				<view class="flex-sub user-msg">
					游客
				</view>
				<view class="user-login">
					账号登录
				</view>
			</view>
		</view>
		<view @click="gotoVipBuy" class="vip-box">
			<image mode="widthFix" src="../../static/images/vip-bar.jpg"></image>
		</view>
		<view @click="clickService" class="cu-bar padding-lr solid-bottom" style="width: 100%;">
			<view class="">
				加入VIP加客服
			</view>
			
			<view class="flex align-center">
				<view class="margin-lr-sm text-gray">
					{{config.member_service}}
				</view>
				<view class="cuIcon-right"></view>
			</view>
		</view>
		<!-- #ifdef APP-PLUS -->
		<view class="cu-bar padding-lr solid-bottom" style="width: 100%;">
			<view class="">当前APP版本</view>
			<view class="flex align-center">
				<view class="margin-lr-sm text-gray">
					{{version()}}
				</view>
				<view class="cuIcon-right"></view>
			</view>
		</view>
		<!-- #endif -->
		<!-- user category -->
		<view class="user-category">
			<view class="cate-class" v-for="(item, index) in cateList" :key="index" @tap="changeView(item.id)">
				<template v-if="current_id==item.id">
					<image class="user-fun-icon" mode="aspectFit" :src="item.url"></image>
					<view class="cate-name active">
						{{item.title}}
					</view>
				</template>
				<template v-else>
					<image class="user-fun-icon" mode="aspectFit" :src="item.url"></image>
					<text v-if="false" class="cate-icon text-orange" :class="'cuIcon-'+ item.icon"></text>
					<view class="cate-name">
						{{item.title}}
					</view>
				</template>
			</view>
		</view>

		<view class="user-favorite padding-tb-sm" style="width: 100%;">
			<template v-if="current_id==0">
				<view v-if="followList.length" class="favorite-style-list">
					<view class="style-item  solid-bottom padding-tb-xs" v-for="(item, index) in followList" :key="index" @tap="goPlay(item)">
						<image :src="item.cover ? item.cover + '?imageMogr2/thumbnail/!260x260r' : '../../static/images/videodisc.png'" mode="aspectFill"></image>
						<view class="style-info">
							<view class="style-title padding-tb-xs">
								{{item.name}}
							</view>
							<view class="style-msg">
								{{item.type == 1 ? '广播剧' : (item.type == 2 ? '音乐' : '视频')}} | 
								<template v-if="item.type == 2">{{item.duration}}</template>
								<template v-else>共{{item.count}}集</template>
							</view>
						</view>
						<view class="style-icon">
							<text class="text-red cuIcon-playfill"></text>
						</view>
					</view>
				</view>
				<view class="text-center text-gray padding">
					<view v-if="fLoading" class="cu-load loading"></view>
					<view v-else-if="!followList.length">空空如也~</view>
					<view @click="loadMove" v-else-if="followList.length < fTotal">点击加载更多</view>
					<view v-else-if="followList.length == fTotal">没有更多了</view>
				</view>
			</template>
			<template v-if="current_id==1">
				<view v-if="historyList.length" class="favorite-style-list">
					<view class="style-item  solid-bottom padding-tb-xs" v-for="(item, index) in historyList" :key="index" @tap="goPlay(item)">
						<image :src="item.cover ? item.cover + '?imageMogr2/thumbnail/!260x260r' : '../../static/images/videodisc.png'" mode="aspectFill"></image>
						<view class="style-info">
							<view class="style-title padding-tb-xs">
								{{item.name}}
							</view>
							<view class="style-msg">
								{{item.type == 1 ? '广播剧' : (item.type == 2 ? '音乐' : '视频')}} |
								<template v-if="item.type == 2">{{item.duration}}</template>
								<template v-else>共{{item.count}}集</template>
							</view>
						</view>
						<view class="style-icon">
							<text class="text-red cuIcon-add"></text>
						</view>
					</view>
				</view>
				<view class="text-center text-gray padding">
					<view v-if="hLoading" class="cu-load loading"></view>
					<view v-else-if="!historyList.length">空空如也~</view>
					<view @click="loadMove" v-else-if="historyList.length < hTotal">点击加载更多</view>
					<view v-else-if="historyList.length == hTotal">没有更多了</view>
				</view>
			</template>
			<template v-if="current_id==2">
				<view v-if="myWorksList.length" class="favorite-style-list">
					<view class="style-item solid-bottom padding-tb-xs" v-for="(item, index) in myWorksList" :key="index">
						<image :src="item.cover ? item.cover : '../../static/images/videodisc.png'" mode="aspectFill"></image>
						<view class="style-info">
							<view class="style-title padding-tb-xs">
								{{item.name}}
							</view>
							<view class="style-msg">
								{{item.type == 1 ? '广播剧' : (item.type == 2 ? '音乐' : '视频')}} 
								<template v-if="item.duration"> | {{item.duration}}</template>
							</view>
						</view>
						<view class="style-icon">
							<text class="text-red cuIcon-right"></text>
						</view>
					</view>
					<view class="padding"></view>
				</view>
				<view v-else class="text-center text-gray padding">暂无记录</view>
				<view @click="gotoUpload" class="flex align-center justify-center bg-orange round padding-xs add-video-box">
					<text class="cuIcon-add add-video-icon"></text>
				</view>
			</template>
			<template v-if="current_id==3">
				<view v-if="downloadList.length" class="favorite-style-list">
					<view v-for="(item, index) in downloadList" :key="index" style="width: 100%;">
						<view class="style-item  solid-bottom padding-tb-xs" @tap="goPlay({type: 1, albumId: item.a_id})">
							<image :src="item.cover ? item.cover : '../../static/images/videodisc.png'" mode="aspectFill"></image>
							<view class="style-info">
								<view class="style-title padding-tb-xs" style="width: 100%;">
									{{item.a_name}}
								</view>
								<view class="style-msg">
									广播剧 | 共{{item.items ? item.items.length: 0}}集
								</view>
							</view>
							<view v-if="openIndex == index" @click.stop="deleteDownload(item)" class="text-xl margin-right-sm padding-lr-sm">
								<text class="cuIcon-delete"></text>
							</view>
							<view @click.stop="openIndex = openIndex == index ? -1 : index" class="style-icon flex align-center" style="height: 70rpx;">
								<text v-if="openIndex == index" class="text-red cuIcon-fold"></text>
								<text v-else class="text-red cuIcon-unfold"></text>
							</view>
						</view>
						<view v-if="openIndex == index" class="padding-lr padding-tb-sm">
							<view v-for="(itm, inx) in item.items" class="flex align-center margin-bottom-sm">
								<view class="flex-sub">
									{{itm.s_name}}
								</view>
								<view class="text-sm">
									<text v-if="itm.id == downloadingId" class="text-yellow">下载中</text>
									<text v-else-if="itm.a_status == 1" class="text-green">已下载</text>
									<text v-else class="text-gray">未下载</text>
								</view>
							</view>
						</view>
					</view>
				</view>
				<view v-else class="text-center text-gray padding">暂无记录</view>
			</template>
		</view>
	</view>
</template>

<script>
	import { getMyFollowList, getMyHistoryList } from '@/api/user.js';
	import { getMySingle } from '@/api/works.js';
	import { setClipboardData } from '@/uni_modules/u-clipboard/js_sdk';
	import Sqllite from '@/utils/sqllite.js';

	export default {
		name: "Mine",
		props: {},
		data() {
			return {
				openIndex: -1,
				current_app_version:'1.1.14',
				current_id: 0,
				cateList: [
					{
						'id': 0,
						'title': '我的喜欢',
						'icon': 'like',
						'url': '../../static/images/i01.png'
					},
					{
						'id': 1,
						'title': '收听历史',
						'icon': 'service',
						'url': '../../static/images/i02.png'
					},
					{
						'id': 2,
						'title': '我的作品',
						'icon': 'upload',
						'url': '../../static/images/i03.png'
					},
					{
						'id': 3,
						'title': '下载管理',
						'icon': 'pulldown',
						'url': '../../static/images/i04.png'
					}
				],
				followList: [],
				fTotal: 0,
				fQuery: {
					pageSize: 10,
					pageNum: 1
				},
				fLoading: false,
				historyList: [],
				hTotal: 0,
				hQuery: {
					pageSize: 10,
					pageNum: 1
				},
				hLoading: false,
				myWorksList: []
			}
		},
		computed: {
			user() {
				return this.$store.state.user.user;
			},
			config() {
				return this.$store.state.app.config;
			},
			downloadingId() {
				return this.$store.state.app.downloadingId;
			},
			downloadList() {
				let res = this.$store.state.app.downloadList;
				if (res && res.length) {
					let resMap = {};
					for (let i = 0; i < res.length; i++) {
						let item = res[i];
						if (!resMap[item.a_id]) {
							item.items = [];
							resMap[item.a_id] = item;
						}
						resMap[item.a_id].items.push(item);
					}
					return Object.values(resMap);
				} else {
					return [];
				}
			}
		},
		watch: {
			user() {
				this.followList = [];
				this.fTotal = 0;
				this.fQuery = {
					pageSize: 10,
					pageNum: 1
				}
				this.historyList = [];
				this.hTotal = 0;
				this.hQuery = {
					pageSize: 10,
					pageNum: 1
				}
				this.myWorksList = [];
				this.loadList();
			}
		},
		created() {
			this.loadList();
		},
		methods: {
			deleteDownload(item) {
				// #ifdef APP-PLUS
				uni.showModal({
				    title: '确认',
				    content: '确定删除下载记录和对应文件？',
				    success: res => {
				        if (res.confirm) {
							let sqllite = new Sqllite('app');
							// 先查询所有单曲
							let sql2 = `select * from download where a_id = ` + item.a_id;
							sqllite.selectSql(sql2, res => {
								if (res && res.length) {
									// 删除文件
									for (let i = 0; i < res.length; i++) {
										uni.removeSavedFile({
										    filePath: res[0].a_path,
										    complete: function (res) {
												
										    }
										});
									}
									
									let sql = `delete from download where a_id = ` + item.a_id;
									sqllite.executeSql(sql, res => {
										this.$toast.success('删除成功');
										sqllite.closeDatabase();
										this.$store.dispatch('selectDownloadList')
										
									}, res => {
										sqllite.closeDatabase();
									})
									this.$store.dispatch('selectDownloadList')
								}
							}, res => {
								sqllite.closeDatabase();
							})
				        }
				    }
				});
				// #endif
			},
			version() {
				return plus.runtime.version;
			},
			clickService() {
				setClipboardData(this.config.member_service).then(res => {
					this.$toast.message('复制成功');
				})
			},
			loadMove() {
				if (this.current_id == 0) {
					this.fQuery.pageNum++;
					this.loadList();
				} else if (this.current_id == 1) {
					this.hQuery.pageNum++;
					this.loadList();
				} else if (this.current_id == 2) {
					this.oQuery.pageNum++;
					this.loadList();
				} else if (this.current_id == 3) {
					this.loadList();
				}
			},
			gotoVipBuy() {
				if (this.user) {
					uni.navigateTo({
						url: '/pages/index/vip-buy'
					})
				} else {
					uni.navigateTo({
						url: '/pages/login/login'
					})
				}
			},
			loadList() {
				if (this.current_id == 0) {
					this.loadMyFollowList();
				} else if (this.current_id == 1) {
					this.loadMyHistoryList();
				} else if (this.current_id == 2) {
					this.loadMySingleList();
				} else if (this.current_id == 3) {
					this.loadDownloadList();
				}
			},
			loadDownloadList() {
				// #ifdef APP-PLUS
				this.$store.dispatch('selectDownloadList');
				// #endif
			},
			loadMyFollowList() {
				if (this.fLoading || (this.followList.length && this.followList.length >= this.fTotal)) {
					return ;
				}
				this.fLoading = true;
				getMyFollowList(this.fQuery).then(res => {
					this.fLoading = false;
					if (res.data.code === 200) {
						this.followList = this.followList.concat(res.data.rows);
						this.fTotal = res.data.total;
					}
				}).catch(e => {
					this.fLoading = false;
				})
			},
			loadMyHistoryList() {
				if (this.hLoading || (this.historyList.length && this.historyList.length >= this.hTotal)) {
					return ;
				}
				this.hLoading = true;
				getMyHistoryList(this.hQuery).then(res => {
					this.hLoading = false;
					if (res.data.code === 200) {
						this.historyList = this.historyList.concat(res.data.rows);
						this.hTotal = res.data.total;
					}
				}).catch(e => {
					this.hLoading = false;
				})
			},
			loadMySingleList() {
				getMySingle(this.oQuery).then(res => {
					if (res.data.code === 200) {
						this.myWorksList = res.data.data;
					}
				})
			},
			changeView(id) {
				this.current_id = id;
				this.loadList();
			},
			goPlay(item) {
				if (item.type == 1) {
					uni.navigateTo({
						url: '/pages/play-video/play-video?id=' + item.albumId
					})
				} else if (item.type == 2) {
					uni.navigateTo({
						url: '/pages/play/play'
					})
				} else {
					uni.navigateTo({
						url: '/pages/play-video/play-anime?id=' + item.albumId
					})
				}
			},
			clickUser() {
				if (this.user) {
					uni.navigateTo({
						url: '/pages/user/user-info'
					})
				} else {
					uni.navigateTo({
						url: '/pages/login/login'
					})
				}
			},
			gotoUpload() {
				if (!this.user) {
					uni.navigateTo({
						url: '/pages/login/login'
					})
				} else if (this.user.vip) {
					uni.navigateTo({
						url: '/pages/user/upload'
					})
				} else {
					this.$toast.fail('VIP才能上传');
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import url("./index.css");
	
	.user-fun-icon {
		width: 100rpx;
		height: 100rpx;
	}
	.vip-box {
		width: 100%;
		image {
			width: 100%;
		}
	}
	.add-video-box {
		position: fixed;
		bottom: 50rpx;
		opacity: 0.9;
		box-shadow: 0 0 8rpx #787878;
		right: 50rpx;
		width: 88rpx;
		height: 88rpx;
		display: none;
		
		.add-video-icon {
			font-size: 68rpx;
		}
	}
</style>
