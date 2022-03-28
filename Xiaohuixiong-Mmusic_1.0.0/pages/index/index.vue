<template>
	<view class="content">
		<!-- header -->
		<Header :current="swiperCurrent" @changeView="changeView" @showDrawer="showDrawer"></Header>
		<!-- tab选项卡实现 -->
		<swiper class="main-swiper" :indicator-dots="false" :autoplay="false" :current="swiperCurrent" @change="changeCurrent">
			<swiper-item>
				<scroll-view scroll-y="true" style="height: 100%;">
					<Mine></Mine>
				</scroll-view>
			</swiper-item>
			<swiper-item>
				<Yuncun></Yuncun>
			</swiper-item>
			<swiper-item>
				<Video></Video>
			</swiper-item>
			<swiper-item>
				<Music></Music>
			</swiper-item>
		</swiper>

		<!-- BottomBar -->
		<BottomBar></BottomBar>
	</view>
</template>

<script>
	import uniDrawer from '../../components/uni-drawer/uni-drawer.vue'
	import Header from '../../components/Header/index.vue';
	import BottomBar from '../../components/BottomBar/index.vue';
	import Drama from '../../components/Drama/index.vue';
	import Mine from '../../components/Mine/index.vue';
	import Yuncun from '../../components/Yuncun/index.vue';
	import Video from '../../components/Video/index.vue';
	import Music from '../../components/Music/index.vue';

	export default {
		components: {
			uniDrawer,
			Header,
			BottomBar,
			Drama,
			Mine,
			Yuncun,
			Video,
			Music
		},
		data() {
			return {
				ifShowDrawer: false,
				swiperCurrent: 1,
				query: {},
				drawerMenuList: [
					{
						'id': 0,
						'name': '我的消息',
						'icon': 'cuIcon-mail'
					},
					{
						'id': 1,
						'name': '我的好友',
						'icon': 'cuIcon-friend'
					},
					{
						'id': 2,
						'name': '个人主页',
						'icon': 'cuIcon-home'
					},
					{
						'id': 3,
						'name': '个性装扮',
						'icon': 'cuIcon-clothes'
					}
				],
				menuItemList: []
			}
		},
		onLoad(query) {
			this.query = query;
			if (this.query && ['0', '1', '2', '3'].indexOf(this.query.currentTab) !== -1) {
				this.swiperCurrent = Number(this.query.currentTab);
			}
		},
		methods: {
			changeCurrent(e) {
				this.swiperCurrent = e.detail.current;
				if (this.swiperCurrent === 0) {
					this.$store.dispatch('getUserInfo');
				}
			},
			changeView(e) {
				this.swiperCurrent = e;
			},
			showDrawer(e) {
				this.ifShowDrawer = e;
			},
			hidDrawer() {
				this.ifShowDrawer = false;
			},
		}
	}
</script>

<style>
	@import url("./index.css");
</style>
