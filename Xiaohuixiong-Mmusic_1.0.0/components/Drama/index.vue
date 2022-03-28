<template name="Drama">
	<view class="drama">
		<!-- swiper -->
		<swiper class="drama-swiper" :indicator-dots="false" :autoplay="true" :interval="3000" :duration="1000" circular="true" vertical="true">
			<swiper-item v-for="(item, index) in swpierList" :key="index">
				<image :src="item.cover" mode="" @tap="goBannerUrl(item)"></image>
			</swiper-item>
		</swiper>
		<!-- category -->
		<view class="drama-category">
			<view class="type">
				<view class="type-icon">
					<text class="text-white cuIcon-wefill"></text>
				</view>
				<view class="type-name">
					热门推荐
				</view>
			</view>
			<view class="type">
				<view class="type-icon">
					<text class="text-white cuIcon-pullup"></text>
				</view>
				<view class="type-name">
					剧集更新
				</view>
			</view>
			<view class="type">
				<view class="type-icon">
					<text class="text-white cuIcon-rank"></text>
				</view>
				<view class="type-name">
					排行榜
				</view>
			</view>
			<view class="type" @tap="childChangeView(4)">
				<view class="type-icon">
					<text class="text-white cuIcon-sort"></text>
				</view>
				<view class="type-name">
					全部
				</view>
			</view>
		</view>
		<!-- more choise -->
		<view class="drama-choise margin-top">
			<view class="choise-left">
				精选推荐
			</view>
			<view @click="childChangeView(4)" class="choise-right">
				查看更多
			</view>
		</view>
		<!-- choise list -->
		<view class="drama-choise-list">
			<view class="choise-item" v-for="(item, index) in boutique" :key="index" @tap="goBoutiquePlay(item)">
				<image :src="item.cover" mode=""></image>
				<view class="choise-title">
					{{item.name}}
				</view>
			</view>
		</view>

		<!-- everyone listen -->
		<view class="drama-choise">
			<view class="choise-left">
				热门TOP10
			</view>
			<view @click="childChangeView(4)" class="choise-right">
				查看更多
			</view>
		</view>
		<view class="drama-style-list">
			<view class="style-item" v-for="(item, index) in hot" :key="index" @tap="goHotPlay(item)">
				<image :src="item.cover" mode=""></image>
				<view class="style-info">
					<view class="style-title padding-tb-xs">
						{{item.name}}
					</view>
					<view class="style-msg flex align-center">
						<view class="">
							播放：{{item.times}}
						</view>
						<view class="margin-left">
							共{{item.count}}集
						</view>
						<view class="margin-left">
							{{item.status == 1 ? '连载中' : '已完结'}}
						</view>
					</view>
				</view>
				<view class="style-icon">
					<text class="text-red cuIcon-playfill"></text>
				</view>
			</view>
		</view>
		<!-- last -->
		<view class="last">

		</view>
	</view>
</template>

<script>
	import {getRadioDramaRecommendList} from '@/api/works.js';
	
	export default {
		name: "drama",
		props: {
			current: {
				type: Number,
				default: 1
			}
		},
		data() {
			return {
				swpierList: [],
				boutique: [],
				hot: []
			}
		},
		created() {
			this.loadRadioDramaRecommendList();
		},
		methods: {
			goBoutiquePlay(item) {
				uni.navigateTo({
					url: '/pages/play-video/play-video?id=' + item.worksId
				})
			},
			goHotPlay(item) {
				uni.navigateTo({
					url: '/pages/play-video/play-video?id=' + item.worksId
				})
			},
			goBannerUrl(item) {
				uni.navigateTo({
					url: '/pages/play-video/play-video?id=' + item.worksId
				})
			},
			loadRadioDramaRecommendList() {
				getRadioDramaRecommendList().then(res => {
					if (res.data.code == 200) {
						this.swpierList = res.data.banner;
						this.boutique = res.data.boutique;
						this.hot = res.data.hot;
					} else {
						this.$toast.fail(res.data.msg);
					}
				})
			},
			childChangeView(id) {
				this.$emit('changeView', id);
			},
			goPlay() {
				uni.navigateTo({
					url: '/pages/play-video/play-video'
				})
			}
		}
	}
</script>

<style>
	@import url("./index.css");
</style>
