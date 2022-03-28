<template>
	<view class="user-info">
		<view @click="chooseImage" class="cu-form-group margin-top-xs padding-tb-sm">
			<view class="title">头像</view>
			<view class="cu-avatar xl round bg-gray avatar-box">
				<image v-if="user.avatar" class="avatar-img" mode="aspectFill" :src="user.avatar"></image>
				<text v-else class="cuIcon-people"></text>
			</view>
		</view>
		<view v-if="user.vip" class="cu-form-group text-orange">
			<view class="title">VIP</view>
			<view class="flex-sub text-right">
				{{user.vipTime > '2200-01-01' ? '终身会员' : '到期时间 ' + user.vipTime}}
			</view>
			<view class="text-xl lg cuIcon-right margin-left-sm"></view>
		</view>
		<view v-else class="cu-form-group" @click="buy_vip()">
			<view class="title">VIP</view>
			<view class="flex-sub text-right text-bold text-yellow">
				购买VIP
			</view>
			<view class="text-xl lg cuIcon-right margin-left-sm"></view>
		</view>
		<view class="cu-form-group">
			<view class="title">邮箱</view>
			<view class="flex-sub text-right text-gray">
				{{user.userName}}
			</view>
			<view class="text-xl lg text-gray cuIcon-right margin-left-sm"></view>
		</view>
		<view @click="showEditBox(1)" class="cu-form-group">
			<view class="title">昵称</view>
			<view class="flex-sub text-right text-gray">
				{{user.nickName}}
			</view>
			<view class="text-xl lg text-gray cuIcon-right margin-left-sm"></view>
		</view>
		<view class="cu-form-group">
			<view class="title">上传作品</view>
			<view class="flex-sub text-right text-gray">
				
			</view>
			<view class="text-xl lg text-gray cuIcon-right margin-left-sm"></view>
		</view>
		<view class="cu-form-group" @click="goto('/pages/user/qrcode')">
			<view class="title">分享二维码</view>
			<view class="flex-sub text-right text-gray">
				
			</view>
			<view class="text-xl lg text-gray cuIcon-right margin-left-sm"></view>
		</view>
		<view class="cu-form-group"  @click="goto('/pages/user/inviteruser')">
			<view class="title">渠道用户管理</view>
			<view class="flex-sub text-right text-gray">
			</view>
			<view class="text-xl lg text-gray cuIcon-right margin-left-sm"></view>
		</view>
		<!--
		<view class="cu-form-group margin-top-sm">
			<view class="title">性别</view>
			<picker mode="selector" :value="user.sex" :range="['男', '女', '保密']" @change="sexChange">
				<view class="picker text-gray">
					{{user.sex == 0 ? '男' : (user.sex == 1 ? '女' : '保密')}}
				</view>
			</picker>
			<view class="text-xl lg text-gray cuIcon-right margin-left-sm"></view>
		</view>
		<view class="cu-form-group">
			<view class="title">出生年月</view>
			<picker mode="date" :value="user.birthday" start="1900-01-01" end="2020-12-30" @change="birthdayChange">
				<view class="picker text-gray">
					{{user.birthday ? user.birthday : '保密'}}
				</view>
			</picker>
			<view class="text-xl lg text-gray cuIcon-right margin-left-sm"></view>
		</view>
		<view @click="showEditBox(2)" class="cu-form-group">
			<view class="title">个性签名</view>
			<view class="flex-sub text-right text-gray">
				{{user.signature ? user.signature : '介绍一下自己吧ε≡٩(๑>₃<)۶ '}}
			</view>
			<view class="text-xl lg text-gray cuIcon-right margin-left-sm"></view>
		</view>
		-->
		<view class="padding-xl">
			<view @click="logout" class="padding-lr padding-tb-sm radius text-center text-xl bg-gradual-blue">退出登录</view>
		</view>
		<view class="cu-modal" :class="showBox ? 'show' : ''">
			<view class="cu-dialog">
				<view class="cu-bar bg-white justify-end">
					<view class="content">{{showBox == 1 ? '修改昵称' : '修改签名'}}</view>
					<view class="action" @tap="showBox = 0">
						<text class="cuIcon-close text-red"></text>
					</view>
				</view>
				<view class="padding-xl">
					<input v-model="form.value" class="input-box" type="textarea" />
				</view>
				<view class="cu-bar bg-white justify-end">
					<view class="action">
						<button class="cu-btn line-green text-green" @tap="showBox = 0">取消</button>
						<button class="cu-btn bg-green margin-left" @tap="editInfo">确定</button>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import {logout, updateMember} from '@/api/user.js';
	import config from '@/utils/config.js'
	const serverUrl = process.env.NODE_ENV === 'development' ? config.serverUrl.dev : config.serverUrl.pro;
	
	export default {
		data() {
			return {
				form: {},
				showBox: 0
			}
		},
		computed: {
			user() {
				if (this.$store.state.user.user) {
					return this.$store.state.user.user;
				} else {
					return {};
				}
			},
			token() {
				return this.$store.getters.userToken;
			}
		},
		onShow() {
			this.$store.dispatch('getUserInfo');
			this.form = {...this.user};
		},
		methods: {
			editInfo() {
				let data = {};
				if (this.showBox == 1) {
					data = {nickName: this.form.value};
				} else if (this.showBox == 2) {
					data = {signature: this.form.value};
				}
				this.updateMember(data);
				this.showBox = 0;
			},
			showEditBox(item) {
				if (item == 1) {
					this.showBox = 1;
					this.form.value = this.user.nickName;
				} else if (item == 2) {
					this.showBox = 2;
					this.form.value = this.user.signature;
				}
			},
			sexChange(e) {
				let data = {sex: e.detail.value};
				this.updateMember(data);
			},
			birthdayChange(e) {
				let data = {birthday: e.detail.value};
				this.updateMember(data);
			},
			logout() {
				logout().then(res => {
					if (res.data.code == 200) {
						this.$store.commit('setUser', null);
						this.$store.commit('setToken', null);
						this.$toast.success('退出成功');
						setTimeout(() => {
							uni.navigateTo({
								url: '/pages/index/index'
							})
						}, 2000);
					} else {
						this.$toast.fail(res.data.msg);
					}
				})
			},
			chooseImage() {
				uni.chooseImage({
					count: 1, //默认9
					sizeType: ['compressed'], 		//可以指定是原图还是压缩图，默认二者都有
					success: (res) => {
						if (res.tempFilePaths && res.tempFilePaths.length) {
							let url = serverUrl + '/common/upload';
							uni.uploadFile({
								url: url, 			//仅为示例，非真实的接口地址
								filePath: res.tempFilePaths[0],
								name: 'file',
								header: {
									'Authorization': 'Bearer ' + this.token
								},
								success: (uploadFileRes) => {
									let data = JSON.parse(uploadFileRes.data);
									if (data.code === 200) {
										this.updateMember({avatar: data.url})
									} else {
										this.$toast.fail(data.msg);
									}
								}
							});
						}
					}
				});
			},
			updateMember(data) {
				updateMember(data).then(res => {
					if (res.data.code === 200) {
						this.$store.commit('setUser', {...this.user, ...data});
						this.$toast.success('更新成功');
					} else {
						this.$toast.fail(res.data.msg);
					}
				})
			},
			buy_vip(){
				uni.navigateTo({
				  url: '/pages/index/vip-buy',
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
			goto(url){
				uni.navigateTo({
					url: url
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.user-info {
		.avatar-box {
			overflow: hidden;
		}
		.avatar-img {
			height: 100%;
			width: 100%;
		}
		.cu-form-group uni-picker {
			padding-right: 0;
		}
		.cu-form-group uni-picker::after {
			content: none;
		}
		.input-box {
			border: 1rpx solid #eaedf8;
			padding: 15rpx;
			border-radius: 6rpx;
			height: 70rpx;
			background-color: #ffffff;
		}
	}
</style>
