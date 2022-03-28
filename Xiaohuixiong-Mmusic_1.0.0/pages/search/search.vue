<template>
	<view class="content padding-top-lg">
		<view class="status-bar"></view>
		<!-- serchBar -->
		<view class="search-box">
			<text class="search-box-icon text-black cuIcon-back" @tap="goBack"></text>
			<view class="search-input">
				<input v-model="query.name"
					class="input"
					type="text" 
					value=""
					focus="true"
					placeholder="大家都在搜 "
					placeholder-style="color:#ababab;"/>
			</view>
			<text @click="loadList()" class="search-box-icon text-black cuIcon-search"></text>
		</view>
		<!-- 热搜title -->
		<view class="padding-tb-sm padding-lr hot-search-title">热搜榜</view>
		<!-- 热搜列表 -->
		<view class="flex-sub hot-search-list-box">
			<scroll-view scroll-y class="hot-search-list">
				<view class="search-list" v-for="(item, index) in searchList" :key="index" @click="goPlay(item)">
					<view class="search-list-left">
						<view class="search-rank">
							{{index + 1}}
						</view>
						<view class="search-list-title">
							<view :class="item.vip ? 'text-orange' : ''" class="search-list-name flex align-center">
								<text>{{item.name}}</text>
								<text v-if="item.vip" class="bg-orange cu-tag round sm margin-left-xs">VIP</text>
							</view>
						</view>
					</view>
					<view class="search-list-right">
						{{item.times}}
					</view>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script>
	import { getAnimeList, getRadioDramaList, getMusicList } from '@/api/works.js';
	
	export default {
		data() {
			return {
				loading: false,
				searchList:[],
				total: 0,
				query: {
					pageNum: 1,
					pageSize: 100
				}
			}
		},
		computed: {
			loadList() {
				let type = this.query.type ? this.query.type : 1;
				if (type == 1) {
					return this.localRadioDramaList;
				} else if (type == 2) {
					return this.localMusicList;
				} else {
					return this.loadAnimeList;
				}
			}
		},
		onLoad(query) {
			this.query = {...this.query, ...query};
		},
		onShow() {
			this.loadList()
		},
		methods: {
			goPlay(item) {
				console.log(item)
				if (item.type == 1) {
					uni.navigateTo({
						url: '/pages/play-video/play-video?id=' + item.id
					})
				} else if (item.type == 2) {
					uni.navigateTo({
						url: '/pages/play/play'
					})
				} else {
					uni.navigateTo({
						url: '/pages/play-video/play-anime?id=' + item.id
					})
				}
			},
			goBack(){
				uni.navigateBack({
					delta: 1
				})
			},
			loadAnimeList() {
				if (this.loading) {
					return ;
				}
				this.loading = true;
				getAnimeList(this.query).then(res => {
					this.loading = false;
					if (res.data.code == 200) {
						this.searchList = res.data.rows;
						this.total = res.data.total;
					} else {
						this.$toast.fail(res.data.msg);
					}
				}).catch(e => {
					this.loading = false;
				})
			},
			localRadioDramaList() {
				if (this.loading) {
					return ;
				}
				this.loading = true;
				getRadioDramaList(this.query).then(res => {
					this.loading = false;
					if (res.data.code == 200) {
						this.searchList = res.data.rows;
						this.total = res.data.total;
					} else {
						this.$toast.fail(res.data.msg);
					}
				}).catch(e => {
					this.loading = false;
				})
			},
			localMusicList() {
				if (this.loading) {
					return ;
				}
				this.loading = true;
				getMusicList(this.query).then(res => {
					this.loading = false;
					if (res.data.code == 200) {
						this.searchList = res.data.rows;
						this.total = res.data.total;
					} else {
						this.$toast.fail(res.data.msg);
					}
				}).catch(e => {
					this.loading = false;
				})
			}
		}
	}
</script>

<style>
	@import url("./search.css");
	
	.hot-search-list-box {
		position: relative;
	}
</style>
