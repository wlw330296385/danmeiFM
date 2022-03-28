<template>
	<view class="inviteruser">
		<view class="cu-bar bg-white padding-lr">
			<view class="">
				成功邀请的用户
			</view>
			<view class="flex">
				<view class="margin-lr">
					总：{{total}}
				</view>
				<view class="text-blue">
					会员：{{memberTotal}}
				</view>
			</view>
		</view>
		<view class="cu-list menu">
			<view class="cu-item" v-for="(item, index) in list">
				<view class="flex align-center">
					<view class="cu-avatar round" :style="'background-image:url(' + item.avatar + ')'">
						
					</view>
					<view class="margin-lr">
						{{item.userName}}
					</view>
				</view>
				<view class="flex align-center">
					<view v-if="item.vip == 1" class="cu-tag bg-gradual-pink radius ">
						{{item.vipTime > '2200-01-01' ? '终身会员' : '到期时间 ' + item.vipTime}}
					</view>
					<view v-else class="radius ">
						注册会员
					</view>
					<view class="cuIcon-right margin-left"></view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import {getMyMember} from '@/api/user.js';

	export default {
		data() {
			return {
				memberTotal: 0,
				total: 0,
				list: [],
				form: {},
				showBox: 0,
				query: {
					pageSize: 1000,
					pageNum: 1
				}
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
			this.form = {...this.user};
		},
		onLoad() {
			this.loadMyMember();
		},
		methods: {
			loadMyMember() {
				getMyMember(this.query).then(res => {
					if (res.data.code === 200) {
						this.list = res.data.data.list;
						this.total = res.data.data.total;
						this.memberTotal = res.data.data.memberTotal;
					} else {
						this.$toast.fail(res.data.msg);
					}
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	
</style>
