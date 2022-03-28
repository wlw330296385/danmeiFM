<template>
	<view v-if="!info.id" class="text-center padding">
		<text class="cu-load loading"></text>
	</view>
	<view v-else style="position: relative;" >
		<view class="nav">
			<view @click="goBack" class="flex align-center justify-center go-back">
				<view class="cuIcon-back"></view>
			</view>
		</view>
		<view class="">
			<view class="bg-img bg-mask flex align-center header" :key="currSingle.id">
				<video class="video-obj" 
					id="myVideo" :src="currSingle.url" 
					@error="videoErrorCallback" 
					@play="videoPlayCallback"
					:autoplay="true" 
					:danmu-list="danmuList" 
					enable-danmu 
					danmu-btn 
					controls></video>
				<view v-if="showControl" class="flex align-center control-bar">
					<view @click="showBarrage = !showBarrage" class="text-xxl padding-xs control-icon-box">
						<text v-if="showBarrage" class="lg text-white cuIcon-edit"></text>
						<text v-else class="lg text-white cuIcon-roundclose"></text>
					</view>
					<view class="text-sm padding-xs flex align-center flex-sub control-icon-box">
						<input v-model="form.barrage" class="text-df text-white flex-sub" placeholder="来点弹幕啊~" />
						<text @click="addSingleBarrage" class="text-white padding-lr-sm text-df" style="border-left: 1rpx solid #666666;">发送</text>
					</view>
					<view @click="$refs.ds.show()" class="text-xxl padding-xs control-icon-box">
						<view class="text-xs text-white bx-text">倍速</view>
					</view>
				</view>
			</view>
			<view class="cu-bar bg-white shadow flex align-center justify-center padding-xs padding-top-lg">
				<view @click="doFavorites" class="flex flex-direction flex-sub bnt-box align-center justify-center">
					<text class="" :class="mf ? 'text-red cuIcon-likefill' : 'cuIcon-like'" style="font-size: 18px;"></text> 
					<view class="" :class="mf ? 'text-red' : ''" style="font-size: 12px;">
						喜欢
					</view>
				</view>
				<view class="flex flex-direction flex-sub bnt-box align-center justify-center">
					<text class="cuIcon-down" style="font-size: 18px;"></text> 
					<view class="" style="font-size: 12px;">
						下载
					</view>
				</view>
			</view>
			<view class="bg-white solid-bottom">
				<view class="cu-bar padding flex">
					<image class="radius" style="width: 100rpx; height: 100rpx; background-color: #eeeeee;" mode="scaleToFill" :src="info.cover"></image>
					<view class="flex flex-sub flex-direction margin-lr">
						<view class="text-black text-bold text-xl">
							{{info.name}}
						</view>
						<view class="text-content text-gray text-xs">
							{{info.times}}次播放 {{info.categoryName}} {{info.status === 1 ? '连载中' : '已完结'}}
						</view>
					</view>
				</view>
				<view v-if="info.description" class="padding">
					<text v-if="!showDescription">
						{{info.description.substring(0, 46)}}
						<text v-if="info.description.length > 45" class="text-blue margin-left-xs">
							<text @click="showDescription = true" v-if="!showDescription">展开 <text class="cuIcon-unfold"></text></text>
						</text>
					</text>
					<pre style="white-space: pre-wrap; word-wrap: break-word; line-height: 44rpx;" v-if="showDescription">{{info.description}}</pre>
					<text @click="showDescription = false" v-if="showDescription" class="text-blue">收起 <text class="cuIcon-fold"></text></text>
				</view>
			</view>
			<view class="bg-white solid-bottom">
				<view class="cu-bar padding flex">
					<view class="ext-black text-bold">选集</view>
					<view class="text-content text-gray text-xs">
						共{{info.totalCount}}集
					</view>
				</view>
				<view class="bg-white padding">
					<view class="grid margin-bottom text-center col-3">
						<view v-for="(item, index) in list" :key="index" class="flex justify-center margin-bottom single-box">
							<view @click="clickSingle(item)" class="shadow-warp padding-sm single-box-in" :class="currId == item.id ? 'active' : ''">
								<view class="text-black text-cut margin-bottom-xs">
									{{item.name}}
								</view>
								<view class="text-xs text-grey text">
									{{item.duration}}
								</view>
								<view v-if="item.vip" class="vip-icon bg-orange padding-lr padding-top">VIP</view>
							</view>
						</view>
					</view>
				</view>
			</view>
			<view class="bg-white solid-bottom">
				<view class="padding flex align-center">
					<view class="cu-avatar round" style="background-image:url(https://ossweb-img.qq.com/images/lol/web201310/skin/big10001.jpg)"></view>
					<view class="flex flex-sub flex-direction margin-lr">
						<view class="text-black text-bold ">
							上传用户：{{info.uploadUserName}}
						</view>
					</view>
				</view>
			</view>
			<view v-if="currSingle" class="bg-white solid-bottom">
				<view class="cu-bar padding flex">
					<view class="ext-black text-bold">
						本集介绍
					</view>
					<view class="text-content text-gray text-xs">
						{{currSingle.name}}
					</view>
				</view>
				<view v-if="currSingle.description" class="padding" style="min-height: 300px; padding-bottom: 130rpx;">
					<pre style="white-space: pre-wrap; word-wrap: break-word; line-height: 44rpx;">{{currSingle.description}}</pre>
				</view>
			</view>
		</view>
		<double-speed ref="ds"></double-speed>
	</view>
</template>

<script>
	import {getAlbumDetails, addSingleBarrage, getAlbumSingleBarrage, addMemberPlayHistory} from '@/api/works.js';
	import {doFavorites} from '@/api/user.js';
	import DoubleSpeed from '@/components/double-speed.vue';
	
	export default {
		components: {DoubleSpeed},
		data() {
			return {
				info: {},
				list: [],
				mf: null,
				showControl: false,
				showBarrage: true,
				showDescription: false,
				query: {id: 3},
				form: {},
				barrages: [],
				currBarrages: [],
				danmuList: [],
				currId: 0
			};
		},
		computed: {
			user() {
				return this.$store.state.user.user;
			},
			currSingle() {
				let ev = this.list.filter(i => i.id == this.currId);
				if (ev && ev.length) {
					return ev[0];
				} else if (this.list && this.list.length) {
					return this.list[0];
				} else {
					return null;
				}
			}
		},
		onLoad(query) {
			this.query = {...this.query, ...query};
			this.loadAlbumSingleBarrage(this.currId);
			this.loadAlbumDetails();
		},
		methods: {
			goBack() {
				uni.navigateBack({
					delta: 1
				})
			},
			loadAlbumSingleBarrage(id) {
				if (id && id != 0) {
					getAlbumSingleBarrage({id}).then(res => {
						if (res.data.code == 200) {
							if (this.currId == id) {
								this.barrages = [...res.data.data];
								this.currBarrages = [...res.data.data];
							}
						} else {
							this.$toast.fail(res.data.msg);
						}
					})
				}
			},
			loadAlbumDetails() {
				if (this.query.id && this.query.id != 0) {
					getAlbumDetails({id: this.query.id}).then(res => {
						if (res.data.code == 200) {
							this.info = res.data.details;
							this.list = res.data.list;
							if (this.list && this.list.length) {
								this.currId = this.list[0].id;
							}
							this.mf = res.data.member_follow;
						} else {
							this.$toast.fail(res.data.msg);
						}
					})
				}
			},
			addSingleBarrage() {
				if (!this.currId) {
					return this.$toast.fail('缺少id');
				}
				if (!this.form.barrage) {
					return this.$toast.fail('请填写弹幕内容');
				}
				let data = {
					id: this.currId,
					text: this.form.barrage,
					time: this.currentPosition
				};
				
				addSingleBarrage(data).then(res => {
					if (res.data.code == 200) {
						if (this.showBarrage) {
							this.$refs.ffb.add({item: this.form.barrage});
						}
						this.barrages.push(data);
						this.form.barrage = '';
					} else {
						this.$toast.fail(res.data.msg);
					}
				})
			},
			clickSingle(item) {
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
				if (!item.url) {
					return this.$toast.fail('资源地址错误');
				}
				this.currId = item.id;
			},
			videoPlayCallback() {
				let data = {singleId: this.currId};
				addMemberPlayHistory(data).then(res => {
					if (res.data.code === 200) {
						
					} else {
						
					}
				}).catch(e => {
					this.$toast.fail(e);
				})
				
				this.$store.dispatch('doStop');
			},
			videoErrorCallback() {
				
			},
			doFavorites() {
				if (this.list.length <= 0) {
					return this.$toast.fail('当前专辑没有任何单曲');
				}
				let data = {status: 1, works_id: this.list[0].id};
				doFavorites(data).then(res => {
					if (res.data.code === 200) {
						this.mf = !this.mf;
					} else if (res.data.code === 401) {
						uni.navigateTo({
							url: '/pages/login/login'
						})
					} else {
						this.$toast.fail(res.data.msg);
					}
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.header {
		padding-top: var(--status-bar-height);
		background-color: #000000;
		position: relative;
	}
	.my-subtitle {
		position: absolute;
		left: 0;
		width: 100%;
	}
	.barrage-box {
		position: absolute;
		width: 100%;
		height: 400rpx;
		left: 0;
		top: 0;
	}
	.video-obj {
		width: 100%;
		bottom: 0;
	}
	.control-bar {
		position: absolute;
		height: 100rpx;
		background-color: rgba(231, 231, 231, 0.2);
		width: 100%;
		bottom: 0;
		left: 0;
		padding: 0 10rpx 10rpx 10rpx;
		
		
		.control-icon-box {
			background-color: rgba(0,0,0, 0.6);
			border-radius: 10rpx;
			margin: 0 10rpx;
			height: 60rpx;
			display: flex;
			align-items: center;
		}
		.bx-text {
			border: 1rpx solid #eeeeee;
			padding: 3rpx 5rpx;
			border-radius: 6rpx;
		}
	}
	.bnt-box{
		height: 90rpx;
		width: 180rpx;
	}
	.shadow{
	    box-shadow:0rpx 0rpx 10rpx #ddd;
	}
	.nav-li {
	    border-radius: 6px;
	    width: 90%;
		height: 120rpx;
	}
	.active {
		border: 1rpx solid #ff9b05;
	}
	.slider-bar {
		position: relative;
		z-index: 999;
		height: 20rpx;
		margin-top: -18rpx;
		
		.video-slider {
			margin: 0;
		}
		.player-currentTime {
			position: absolute;
			left: 10rpx;
			top: 26rpx;
		}
		.player-duration {
			position: absolute;
			right: 10rpx;
			top: 26rpx;
		}
	}
	.single-box-in {
		width: 90%;
		height: 120rpx;
		position: relative;
		overflow: hidden;
		
	}
	.single-box {
		.vip-icon {
			font-size: 24rpx;
			transform:rotate(45deg);
			top: -20rpx;
			right: -42rpx;
			position: absolute;
		}
	}
	.nav {
		position: absolute;
		width: 100%;
		height: 100rpx;
		top: var(--status-bar-height);
		z-index: 99999;
	
		.go-back {
			z-index: 99999;
			background-color: rgba($color: #000000, $alpha: 0.6);
			width: 62rpx;
			height: 62rpx;
			position: absolute;
			top: 16rpx;
			left: 16rpx;
			
			font-size: 42rpx;
			color: #ffffff;
			border-radius: 1000rpx;
		}
	}
</style>
