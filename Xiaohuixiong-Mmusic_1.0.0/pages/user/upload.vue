<template>
	<view class="upload">
		<view class="cu-form-group">
			<view class="title">作品类型</view>
			<picker @change="pickerChange" :value="form.type" :range="picker">
				<view class="picker">
					{{form.type > -1 ? picker[form.type] : '请选择作品类型'}}
				</view>
			</picker>
		</view>
		<view v-if="form.type == 0 || form.type == 2" class="cu-form-group">
			<view class="title">所属专辑</view>
			<view @click="showAlbum = true" class="flex-sub text-right add-album">
				<text class="margin-right-xs">{{albumName}}</text>
				<text class="cuIcon-right text-lg text-grey"></text>
			</view>
		</view>
		<view class="cu-form-group margin-top-sm">
			<view class="title">资源文件</view>
			<view @tap="chooseImage(2)" class="flex-sub text-right text-orange padding-tb-xs" style="width: 100rpx; word-break: break-word;">
				<text v-if="form.url" class="">{{form.url}}</text>
				<text v-else>上传文件</text>
			</view>
		</view>
		<view class="cu-form-group">
			<view class="title">作品名称</view>
			<input v-model="form.name" placeholder="输入作品名称" name="input"></input>
		</view>
		<view class="cu-form-group align-start">
			<view class="title">作品简介</view>
			<textarea v-model="form.description" maxlength="-1" placeholder="输入作品简介"></textarea>
		</view>
		<view class="padding margin-top">
			<view @click="doAddSingle" class="padding text-center bg-gradual-orange text-lg radius">立即提交</view>
		</view>
		
		<view class="cu-modal" :class="showModal ? 'show' : ''" style="z-index: 1999;">
			<view class="cu-dialog">
				<view class="cu-bar bg-white justify-end">
					<view class="content">添加专辑</view>
					<view class="action" @tap="showModal = false">
						<text class="cuIcon-close text-red"></text>
					</view>
				</view>
				<view class="padding-bottom bg-white text-left">
					<view class="cu-bar bg-white">
						<view class="action">
							封面图片
						</view>
						<view class="action">
							{{albumForm.cover ? '1' : '0'}}/1
						</view>
					</view>
					<view class="cu-form-group">
						<view class="grid col-4 grid-square flex-sub">
							<view v-if="albumForm.cover" class="bg-img">
								<image :src="albumForm.cover" mode="aspectFill"></image>
								<view class="cu-tag bg-red" @tap.stop="delImg">
									<text class='cuIcon-close'></text>
								</view>
							</view>
							<view class="solids" @tap="chooseImage(1)" v-if="!albumForm.cover">
								<text class='cuIcon-cameraadd'></text>
							</view>
						</view>
					</view>
					<view class="cu-form-group">
						<view class="title">专辑名称</view>
						<input v-model="albumForm.name" placeholder="输入专辑名称" name="input"></input>
					</view>
					<view class="cu-form-group align-start">
						<view class="title">专辑简介</view>
						<textarea v-model="albumForm.description" maxlength="-1" placeholder="输入专辑简介"></textarea>
					</view>
				</view>
				<view class="cu-bar bg-white">
					<view class="action margin-0 flex-sub solid-left" @tap="showModal = false">取消</view>
					<view class="action margin-0 flex-sub text-green solid-left" @tap="doAddAlbum">确定</view>
				</view>
			</view>
		</view>
		<view @click="showAlbum = false" class="cu-modal bottom-modal" :class="showAlbum ? 'show' : ''">
			<view @click.stop class="cu-dialog">
				<view class="cu-bar bg-white">
					<view @click="addAlbum" class="action text-green">添加专辑</view>
					<view class="action text-blue" @tap="showAlbum = false">取消</view>
				</view>
				<view class="padding bg-white">
					<view v-for="(itm, index) in myAlbum" :key="index" class="padding-lr-sm text-left flex my-album-item">
						<view @click="clickMyAlbumItem(itm)" class="padding-tb-sm flex-sub">{{itm.name}}</view>
						<view @click.stop="delMyAlbum(itm)" class="flex align-center justify-center text-red">
							<text>删除</text>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import {addMemberAlbum, getMyAlbum, addMemberSingle, delMyAlbum} from '@/api/works.js';
	import config from '@/utils/config.js';
	const serverUrl = process.env.NODE_ENV === 'development' ? config.serverUrl.dev : config.serverUrl.pro;
	
	export default {
		data() {
			return {
				showAlbum: false,
				showModal: false,
				form: {
					type: -1,
					albumId: -1
				},
				albumForm: {name: ''},
				picker: ['广播剧', '音乐', '动漫'],
				albumList: ['钢铁之翼'],
				myAlbum: []
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
			},
			albumName() {
				if (this.form.albumId) {
					let ev = this.myAlbum.filter(i => i.id == this.form.albumId);
					if (ev && ev.length) {
						return ev[0].name;
					}
				}
				return '选择专辑';
			}
		},
		onLoad() {
			this.loadMyAlbum();
		},
		methods: {
			clickMyAlbumItem(item) {
				this.$set(this.form, 'albumId', item.id);
				this.showAlbum = false;
			},
			addAlbum() {
				this.showAlbum = true;
				this.showModal = true;
			},
			pickerChange(e) {
				this.form.type = e.detail.value;
			},
			albumChange(e) {
				this.form.albumId = e.detail.value;
			},
			delImg(e) {
				this.$set(this.albumForm, 'cover', null);
			},
			loadMyAlbum() {
				getMyAlbum().then(res => {
					if (res.data.code === 200) {
						this.myAlbum = res.data.data;
					} else {
						this.$toast.fail(res.data.msg);
					}
				})
			},
			chooseImage(type) {
				uni.chooseImage({
					count: 1, //默认9
					sizeType: ['original', 'compressed'], //可以指定是原图还是压缩图，默认二者都有
					sourceType: ['album'], //从相册选择
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
										if (type == 1) {
											this.$set(this.albumForm, 'cover', data.url);
										} else {
											this.$set(this.form, 'url', data.url);
										}
									} else {
										this.$toast.fail(data.msg);
									}
								}
							});
						}
					}
				});
			},
			doAddAlbum() {
				if (this.form.type === 0) {
					this.albumForm.type = 1;
				} else if (this.form.type === 1) {
					this.albumForm.type = 2;
				} else if (this.form.type === 2) {
					this.albumForm.type = 3;
				}
				addMemberAlbum(this.albumForm).then(res => {
					if (res.data.code === 200) {
						this.showModal = false;
						this.albumForm = {};
						this.$toast.success('操作成功');
					} else {
						this.$toast.fail(res.data.msg);
					}
				})
			},
			doAddSingle() {
				let data = {...this.form};
				if (this.form.type === 0) {
					data.type = 1;
				} else if (this.form.type === 1) {
					data.type = 2;
				} else if (this.form.type === 2) {
					data.type = 3;
				}
				
				addMemberSingle(data).then(res => {
					if (res.data.code === 200) {
						this.$toast.success('操作成功');
						setTimeout(() => {
							uni.navigateBack({
								delta: 1
							})
						}, 2500)
					} else {
						this.$toast.fail(res.data.msg);
					}
				})
			},
			delMyAlbum(item) {
				let query = {id: item.id};
				delMyAlbum(query).then(res => {
					if (res.data.code === 200) {
						this.$toast.success('操作成功');
						this.loadMyAlbum();
					} else {
						this.$toast.fail(res.data.msg);
					}
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.upload {
		.add-album {
			margin-right: -8rpx;
		}
		.album-select {
			flex: none;
		}
		.my-album-item {
			border-bottom: 1rpx solid #eeeeee;
		}
	}
</style>
