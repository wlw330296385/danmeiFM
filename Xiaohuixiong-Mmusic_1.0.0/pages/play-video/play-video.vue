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
			<view class="bg-img bg-mask flex align-center header" :style="'background-image: url('+ (currAlbumTrack.cover ? currAlbumTrack.cover : info.cover) +');'">
				<view v-if="showBarrage && isCurrAlbum" class="barrage-box">
					<lff-barrage ref="ffb" :max-top="160"></lff-barrage>
				</view>
				<subtitle class="my-subtitle" :style="showControl ? 'bottom: 120rpx' : 'bottom: 30rpx'"></subtitle>
				<view v-if="showControl" class="flex align-center control-bar">
					<view @click="showBarrage = !showBarrage" class="text-xxl padding-xs control-icon-box">
						<text v-if="showBarrage" class="lg text-white cuIcon-edit"></text>
						<text v-else class="lg text-white cuIcon-roundclose"></text>
					</view>
					<view class="text-sm padding-xs flex align-center flex-sub control-icon-box">
						<input v-model="form.barrage" class="text-df text-white flex-sub" placeholder="来点弹幕啊~" />
						<text @click="addSingleBarrage" class="text-white padding-lr-sm text-df" style="border-left: 1rpx solid #666666;">发送</text>
					</view>
					<!-- #ifndef H5 -->
					<view @click="$refs.ds.show()" class="text-xxl padding-xs control-icon-box">
						<view class="text-xs text-white bx-text">倍速</view>
					</view>
					<!-- #endif -->
				</view>
			</view>
			<view v-if="(currPlayInfo.currentPosition || currPlayInfo.duration) && isCurrAlbum" class="slider-bar">
				<view class="text-xs player-currentTime">
					<view class="player-currentTime-num">{{currPlayInfo.currentPosition / 1000 | secondToDate}}</view>
				</view>
				<slider class="video-slider" 
					:min="0" 
					:max="currPlayInfo.duration ? currPlayInfo.duration : 1" 
					:value="changingValue < 0 ? currPlayInfo.currentPosition : changingValue" 
					activeColor="#FF0000" 
					backgroundColor="#dedede" 
					block-size="12" 
					@changing="changingProgress"
					@change="changeProgress" />
				<view class="text-xs player-duration">
					<view class="player-duration-num">{{currPlayInfo.duration / 1000 | secondToDate}}</view>
				</view>
			</view>
			<view class="cu-bar bg-white shadow flex align-center justify-center padding-xs padding-top-lg">
				<view @click="doFavorites" class="flex flex-direction flex-sub bnt-box align-center justify-center">
					<text class="" :class="mf ? 'text-red cuIcon-likefill' : 'cuIcon-like'" style="font-size: 18px;"></text> 
					<view class="" :class="mf ? 'text-red' : ''" style="font-size: 12px;">
						喜欢
					</view>
				</view>
				<!-- #ifdef APP-PLUS -->
				<view @click="downloadFileAlbum" class="flex flex-direction flex-sub bnt-box align-center justify-center">
					<text class="cuIcon-down" style="font-size: 18px;"></text> 
					<view class="" style="font-size: 12px;">
						下载
					</view>
				</view>
				<!-- #endif -->
				<!-- #ifdef H5 -->
				<view class="flex flex-direction flex-sub bnt-box align-center justify-center text-gray">
					<text class="cuIcon-down" style="font-size: 18px;"></text> 
					<view class="" style="font-size: 12px;">
						下载
					</view>
				</view>
				<!-- #endif -->
			</view>
			<view class="bg-white solid-bottom">
				<view class="cu-bar padding flex" style="align-items: flex-start; ">
					<image class="radius" style="width: 100rpx; height: 100rpx; background-color: #eeeeee;" mode="scaleToFill" :src="info.cover"></image>
					<view class="flex flex-sub flex-direction margin-lr">
						<view class="text-black text-bold text-xl margin-bottom-xs">
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
					<view v-if="author.avatar" class="cu-avatar round" :style="'background-image:url(' + author.avatar + ')'"></view>
					<view v-else class="cu-avatar round" :style="'background-image:url(https://ossweb-img.qq.com/images/lol/web201310/skin/big99008.jpg)'"></view>
					<view class="flex flex-sub flex-direction margin-lr">
						<view class="text-black text-bold ">
							上传用户：{{info.uploadUserName ? info.uploadUserName : 'Uu.rtyu928'}}
						</view>
					</view>
				</view>
			</view>
			<view v-if="currAlbumTrack" class="bg-white solid-bottom">
				<view class="cu-bar padding flex">
					<view class="ext-black text-bold">
						本集介绍
					</view>
					<view class="text-content text-gray text-xs">
						{{currAlbumTrack.name}}
					</view>
				</view>
				<view v-if="currAlbumTrack.description" class="padding" style="min-height: 300px; padding-bottom: 130rpx;">
					<pre style="white-space: pre-wrap; word-wrap: break-word; line-height: 44rpx;">{{currAlbumTrack.description}}</pre>
				</view>
			</view>
		</view>
		<!-- BottomBar -->
		<BottomBar style='position: fixed;background-color: #fff;'></BottomBar>
		<double-speed ref="ds"></double-speed>
	</view>
</template>

<script>
	import BottomBar from '@/components/BottomBar/index.vue';
	import {getAlbumDetails, addSingleBarrage, getAlbumSingleBarrage, addPlayTimes, addMemberPlayHistory} from '@/api/works.js';
	import {doFavorites} from '@/api/user.js';
	import {secondToDate} from '@/utils/util.js'; 
	import Subtitle from '@/components/subtitle.vue';
	import DoubleSpeed from '@/components/double-speed.vue';
	import Sqllite from '@/utils/sqllite.js';
	
	export default {
		components: {BottomBar, Subtitle, DoubleSpeed},
		data() {
			return {
				info: {},
				list: [],
				author: {},
				mf: null,
				changingValue: -1,
				showControl: true,
				showBarrage: true,
				showDescription: false,
				query: {id: 1},
				form: {},
				barrages: [],
				currBarrages: []
			};
		},
		computed: {
			user() {
				return this.$store.state.user.user;
			},
			currPlayInfo() {
				if (this.$store.state.player.currPlayInfo) {
					return this.$store.state.player.currPlayInfo;
				} else {
					return {}
				}
			},
			currentPosition() {
				if (this.currPlayInfo && this.currPlayInfo.currentPosition) {
					return this.currPlayInfo.currentPosition;
				} else {
					return 0;
				}
			},
			currId() {
				return this.$store.state.player.currPlayInfo.playId;
			},
			isCurrAlbum() {
				let ee = this.list.filter(i => i.id == this.currId);
				return ee && ee.length;
			},
			currAlbumTrack() {
				let ee = this.list.filter(i => i.id == this.currId);
				if (ee && ee.length) {
					return ee[0];
				} else if (this.list.length) {
					return this.list[0];
				}
			}
		},
		filters: {
			secondToDate(val) {
				return secondToDate(val);
			}
		},
		onLoad(query) {
			this.query = {...this.query, ...query};
			this.loadAlbumSingleBarrage(this.currId);
			this.loadAlbumDetails();
		},
		watch: {
			currentPosition(val) {
				if (this.currBarrages && this.currBarrages.length && this.showBarrage) {
					let list = this.currBarrages.filter(i => i.time >= val - 300 && i.time < val + 300);
					if (list && list.length) {
						list = list.sort((x, y) => {
							return x.time > y.time ? 1 : -1;
						})
						this.currBarrages = this.currBarrages.filter(i => i.time >= val + 300);
						list.forEach(item => {
							this.$refs.ffb.add({item: item.text});
						})
					}
				}
			},
			currId(val) {
				if (this.isCurrAlbum) {
					this.loadAlbumSingleBarrage(val);
				}
			}
		},
		methods: {
			downloadFileAlbum() {
				if (!this.user) {
					uni.navigateTo({
						url: '/pages/login/login'
					})
					return ;
				}
				if (this.user && this.user.vip <= 0) {
					this.$toast.fail('当前剧集只有vip可以播放');
					setTimeout(() => {
						uni.navigateTo({
							url: '/pages/index/vip-buy'
						})
					}, 2500);
					return ;
				}
				if (this.list && this.list.length) {
					let sqllite = new Sqllite('app');
					for (let i = 0; i < this.list.length; i++) {
						let item = this.list[i];
						if (item.url && sqllite.isOpenDatabase()) {
							let sql = `INSERT INTO download (id, a_id, a_name, s_name, cover, a_url, a_path, a_status) VALUES (${item.id}, ${item.albumId}, '${item.albumName}', '${item.name}', '${this.info.cover}', '${item.url}', '', 0)`;
							sqllite.executeSql(sql, e => {
								console.log('INSERT INTO success~~~~~~~~~~~~~~~~~~~~!')
							}, e => {
								console.log('INSERT INTO error!')
							})
						}
					}
					sqllite.closeDatabase();
					this.$toast.success('已加入下载列表');
				} else {
					this.$toast.fail('资源未加载');
				}
			},
			goBack() {
				uni.navigateBack({
					delta: 1
				})
			},
			changingProgress(e) {
				this.changingValue = e.detail.value;
			},
			changeProgress(e) {
				this.$store.dispatch('seekTo', e.detail.value);
				this.currBarrages = [...this.barrages];
				setTimeout(() => {
					this.changingValue = -1;
				}, 110)
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
							this.author = res.data.author;
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
			addMemberPlayHistory(id) {
				let data = {singleId: id};
				addMemberPlayHistory(data).then(res => {
					if (res.data.code === 200) {
						
					} else {
						
					}
				}).catch(e => {
					this.$toast.fail(e);
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
				
				let list = [];
				for (let i = 0; i < this.list.length; i++) {
					let item = {...this.list[i]};
					if (item.url) {
						// item.name = this.info.name + ' ' + item.name;
						if (!item.cover) {
							item.cover = this.info.cover;
						}
						list.push(item);
					}
				}
				
				this.$store.commit('addToPlayList', list);
				this.$store.commit('setPlayId', item.id);
				this.$store.dispatch('doPlay');
				
				addPlayTimes({singleId: item.id}).then(res => {
					
				})
				if (this.user) {
					this.addMemberPlayHistory(item.id);
				}
			},
			doFavorites() {
				if (this.list.length <= 0) {
					return this.$toast.fail('当前专辑没有任何单曲');
				}
				let data = {works_id: this.list[0].id};
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
		height: 600rpx;
		margin-top: 0rpx;
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
	.active{
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
