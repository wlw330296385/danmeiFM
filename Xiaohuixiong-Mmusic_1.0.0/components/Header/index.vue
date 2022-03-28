<template name="Header">
	<view>
		<view class="status-bar"></view>
		<view class="header">
			<view class="header-menu">
				<view :class="current==0?'selected':''" @tap="childChangeView(0)">
					我的
				</view>
				<view :class="current==1?'selected':''" @tap="childChangeView(1)">
					广播剧
				</view>
				<view :class="current==2?'selected':''" @tap="childChangeView(2)">
					动漫
				</view>
				<view :class="current==3?'selected':''" @tap="childChangeView(3)">
					音乐
				</view>
			</view>
		</view>		
	</view>
</template>

<script>
	export default {
		name: "Header",
		props:{
			current:{
				type: Number,
				default: 1
			}
		},
		data() {
			return{
				
			}
		},
		methods:{
			childChangeView(id){
				this.$emit('changeView', id);
			},
			goSearch(){
				uni.navigateTo({
					url: '../../pages/search/search'
				})
			},
			showDrawer(){
				this.$emit('showDrawer', true);
			}
		}
	}
</script>

<style>
	@import url("./index.css");
</style>
