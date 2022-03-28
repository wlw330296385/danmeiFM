<template name="Music">
	<scroll-view scroll-y="true" @scrolltolower="scrolltolower" style="height: 100%;">
		<view class="music">
			<view class="cu-bar bg-white solid-bottom">
				<view class="action">
					全部音乐
				</view>
				<view class="margin-right-sm action" @tap="search(2)">
					<text class="cuIcon-search margin-lr-sm"></text>
					搜索
				</view>
			</view>
			<view class="padding bg-white solid-bottom">
				<view class="cu-bar justify-start">
					<view class="select-item" @click="changeCat(0)" :class="query.categoryId == 0 ? 'active' : ''">
						全部
					</view>
					<view v-for="(item, index) in categorys" :key="index" class="select-item" @click="changeCat(item.id)" :class="query.categoryId == item.id ? 'active' : ''">
						{{item.name}}
					</view>
				</view>
			</view>
			<view class="list-box bg-white">
				<view class="find-style-list">
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
								<view class="margin-lr-xl">
									{{item.duration}}
								</view>
							</view>
						</view>
						<view class="style-icon">
							<uniIcons type="headphones" size="20" color='fff'></uniIcons>
						</view>
					</view>
				</view>
			</view>
			<!-- last -->
			<view class="last">
				<view v-if="loading" class="cu-load loading"></view>
				<view v-else-if="!list.length">空空如也~</view>
				<view @click="loadMove" v-else-if="list.length < total">点击加载更多</view>
				<view v-else-if="list.length == total">没有更多了</view>
			</view>
		</view>
	</scroll-view>
</template>

<script>
	import {getMusicList} from '@/api/works.js';
	import {uniIcons} from '@dcloudio/uni-ui'
	export default {
		components: {uniIcons},
		name: "music",
		props: {},
		data() {
			return {
				list: [],
				total: 0,
				loading: false,
				query: {
					categoryId: 0,
					pageNum: 1,
					pageSize: 20
				}
			}
		},
		computed: {
			user() {
				return this.$store.state.user.user;
			},
			categorys() {
				let list = this.$store.state.app.config.categorys;
				if (list) {
					return list.filter(i => i.type == 2);
				} else {
					return [];
				}
			}
		},
		created() {
			this.localMusicList();
		},
		methods: {
			scrolltolower() {
				if (this.list.length < this.total) {
					this.loadMove();
				}
			},
			goPlay(item) {
				if (this.list.length) {
					if (item.vip > 0 && !this.user) {
						uni.navigateTo({
							url: '/pages/login/login'
						})
						return ;
					}
					if (item.vip > 0 && this.user && this.user.vip <= 0) {
						this.$toast.fail('当前剧集只有vip可以播放');
						setTimeout(() => {
							uni.navigateTo({
								url: '/pages/index/vip-buy'
							})
						}, 2500);
						return ;
					}
					
					this.$store.commit('addToPlayList', [item]);
					this.$store.commit('setPlayId', item.id);
					this.$store.dispatch('doPlay');
					uni.navigateTo({
						url: '/pages/play/play'
					})
				}
			},
			changeCat(catId) {
				this.query.categoryId = catId;
				this.list = [];
				this.total = 0;
				this.query.pageNum = 1;
				this.localMusicList();
			},
			loadMove() {
				if (this.loading) {
					return ;
				}
				this.query.pageNum++;
				this.localMusicList();
			},
			localMusicList() {
				if (this.loading) {
					return ;
				}
				this.loading = true;
				getMusicList(this.query).then(res => {
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
			}
		}
	}
</script>

<style>
	@import url("./index.css");
</style>
