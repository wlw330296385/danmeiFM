<template>
	<view class="page flex-col justify-start">
			<view class="box1 flex-col">
				<view class="outer2 flex-col">
					<view class="section2 flex-row justify-between" @click="search()">
						<image class="label2" referrerpolicy="no-referrer"
							src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPng9bdf262cda71a077c46329a1597899266155e9c6e388d62e4859c0dbe2b22b4a" />
						<view class="input-search">
							<uni-easyinput
							 style="height: 60rpx; 
							 border-radius: 27rpx; 
							 background-color: #00000080;
							 width: 500rpx;
							 "
							type="text"
							prefixIcon="search" 
							v-model="searchValue"
							 placeholder="请输入内容" 
							 @iconClick="search"
							 :inputBorder="false"
							 confirmType="search"
							 :styles = "{color: '#FFFFFF80'}"
							 ></uni-easyinput>
						</view>	
						<uni-icons custom-prefix="clock" type="icon-clock" size="26" color="#ffffff90"></uni-icons>
						</view>
					
					<view class="box5 flex-col">
						<image class="pic1" referrerpolicy="no-referrer"
							src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPngb8fbfee40703c4c121f076271f393365284415f4e381bb76e1f63b6f05014e6b" />
					</view>
				</view>
				
				
			</view>
			<view class="box3 flex-row justify-between">
				<view class="mod1 flex-col">
					<image class="icon4" referrerpolicy="no-referrer"
						src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPng5b30d3984e83d0d112e65ebd90b419bfccccfcc5d46b6e9a87c169c9f2e73caa"></image>
					<text class="txt2">广播剧</text>	
				</view>
				<view class="mod1 flex-col">
					<image class="icon4" referrerpolicy="no-referrer"
					src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPng00bb43d03c0d57e6a9475dba23663d67139ccc18461b2a14ddd669c43d9588a3"></image>
					<text class="txt2">我的作品</text>
				</view>
				<view class="mod1 flex-col" @click="later()">
				<image class="icon4" referrerpolicy="no-referrer"
					src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPngabe9d6bbecf9fdb22144ed8dbe6beaf5e9655b4c6968a0409f0fc04a2bc68bdd"></image>
				<text class="txt2">漫画</text>
				</view>
				<view class="mod1 flex-col"  @click="later()">
				<image class="icon4" referrerpolicy="no-referrer"
					src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPng32cdfcd5cda1aef40663ba14d1ef9c8136f06b0c8603465c02d40a57b2066a8b"></image>
					<text class="txt2">音乐</text>
				</view>
			</view>
			<view class="flex-col">
				<image class="pic2" referrerpolicy="no-referrer"
					src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPngb18d263e9a21f24922909f6d418b6cbe13c11c8406f7ba6f75b53b36d76b89fe" />
			</view>
			<text class="txt4">猜你喜欢</text>
			<!-- 猜你喜欢的内容 -->
			<view class="box6 flex-row justify-between">
				<view class="" v-for="(i,k) in 6" :key="k">
					<view class="bd2 flex-col justify-end align-start">
						<view class="layer1 flex-row">
							<image class="label4" referrerpolicy="no-referrer"
								src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPng3a638f56550324e8820e6480e539b12500e0ba1d62c6bf6f1b2cd9777fcbb5ad" />
							<text class="info4">8.5万</text>
							<image class="label5" referrerpolicy="no-referrer"
								src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPng9cc145a841560aebbe33b0a851f85e56faed2c24560c4ccdd09413ac536a86c1" />
							<text class="info5">507</text>
						</view>
					</view>
					<text class="txt9">只想触碰你</text>
					<text class="word1">漫画 青春</text>
				</view>
					
			</view>
			<!-- 猜你喜欢的内容end -->	
			
			<view class="box12  flex-row  justify-between ">
				<view class="flex-row">
					<view class="wrap1"></view>
					<text class="info11">更多</text>
				</view>
				<view class="wrap2"></view>
				<view class="flex-row" >
					<view class="wrap3" ></view>
					<text class="info11">换一批</text>
				</view>
			</view>
			
			<image class="pic2" referrerpolicy="no-referrer"
				src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPng2dc34840f9675d38f32648b5473702e0d431e5386d121cbd68e740d52a011af1" />
			
			<view class="box27 flex-row justify-between">
				<text class="word14">精品周更</text>
				<view class="word15">进入频道
				<image class="label10" referrerpolicy="no-referrer"
						src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPng1e47163bc72180e5ad88c4d6e7b33c421c855f6392750ead1a618cddd093e85f" />
				</view>
			</view>
			<!-- 精品周更的内容 -->
			<view class="box18 flex-row justify-between">
				<view class="" v-for="(item, index) in loopData0" :key="index">
				<view class="layer4 flex-col justify-start align-start" :style="{ background: item.lanhuBg0 }"
					>
					<view class="layer5 flex-col"></view>
					<text class="txt14" v-html="item.lanhutext0"></text>
				</view>
				<text class="txt9">修仙就要傍富婆</text>
				<text class="word1">更新至327话</text>
				</view>
			</view>
		
			<!-- 精品周更的内容end -->
			<view class="box27 flex-row justify-between">
				<text class="word14">推荐小说</text>
				<view class="word15">进入频道
				<image class="label10" referrerpolicy="no-referrer"
						src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPng1e47163bc72180e5ad88c4d6e7b33c421c855f6392750ead1a618cddd093e85f" />
				</view>
			</view>
			
			<!-- 推荐小说的内容 -->
			<view class="box18 flex-col justify-center align-start">
				<view  class="my-box flex-row" v-for="(i,k) in 6" :key="k" style="background-color: rgba(249, 249, 249, 1);">
					<view class="outer6 flex-col"></view>
					<view class="layer6 flex-col">
						<view class="main1 flex-row justify-between">
							<text class="word16">霸道总裁爱上我</text>
							<view class="section6 flex-col">
								<text class="word17">小说</text>
							</view>
						</view>
						<text class="info13">会说话的大肘子｜言情&nbsp;甜宠</text>
						<text class="txt16">架空文古早</text>
					</view>
				</view>
				
			</view>
			<!-- 推荐小说的内容end -->
			
			
			<text class="txt4">音乐</text>
			<view class="box18 flex-row justify-between">
				<view class="" v-for="(i,k) in 6" :key="k">
					<image class="pic4" referrerpolicy="no-referrer"
						src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPngf0fa0721532e545629a0c9796418e6abb3c10030a8e97287738f6c2e4996be70" />
					<text class="word23">超然国风你还不来PK</text>
				</view>
				
			</view>
			
			
			<view class="box12  flex-row  justify-between ">
				<view class="flex-row" style="">
					<view class="wrap1"></view>
					<text class="info11">更多</text>
				</view>
				<view class="wrap2"  style=""></view>
				<view class="flex-row" >
					<view class="wrap3"  style=""></view>
					<text class="info11"  style="">换一批</text>
				</view>
			</view>
			
			
			<view class="box27 flex-row justify-between">
				<text class="word14">广播剧推荐</text>
				<text class="word15">进入频道
				<image class="label10" referrerpolicy="no-referrer"
						src="https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPng1e47163bc72180e5ad88c4d6e7b33c421c855f6392750ead1a618cddd093e85f" />
				</text>
			</view>
			<view class="box28 flex-row justify-between">
				<view class="my-box2"  v-for="(i,k) in 6" :key="k">
					
					<view class="main2 flex-col"></view>
					<view class="layer6 flex-col">
						<view class="main1 flex-row justify-between">
							<text class="word16">霸道总裁爱上我</text>
						</view>
						<text class="info13">
							若未成功，帮你自宫——《莲花宝鉴》，这是一个关于童子功的故事，一个可怜的男人，被人强迫…
						</text>
						
						<view class="layer9 flex-row justify-between">
							<text class="word27">1.6万播放</text>
							<text class="word27">已完结</text>
						</view>
					</view>
				</view>
				
			</view>
	
		<!-- BottomBar -->
		<BottomBar></BottomBar>
		
	</view>
</template>
<script>
	import BottomBar from '../../components/BottomBar/index.vue';
	export default {
		components:{
			BottomBar
		},
		data() {
			return {
				loopData0: [{
						lanhuBg0: 'url(https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPng5246ca4c72a295e8e95dec905e8e35374460cad6217036eb20128970aa5d8fc7) 100% no-repeat',
						lanhutext0: '今日上新',
					},
					{
						lanhuBg0: 'url(https://lanhu.oss-cn-beijing.aliyuncs.com/SketchPnga5758bb01bfa6ad822676d176ec594bc7e79ca65d636485f0d0721f11419b85f) 100% no-repeat',
						lanhutext0: '今日上新',
					},
				],
				constants: {},
				searchValue:''
			};
		},
		methods: {
			search(){
				uni.navigateTo({
					url: '../../pages/search/search'
				})
			},
			later(){
				uni.showToast({
					title: '敬请期待',
					icon: 'none'
				})
			}
		},
	};
</script>
<style  lang="scss" scoped>
	@import './common.css';
	@import './index.rpx.css';
	@import "@/static/icon/clock.css";
	/deep/ .uni-easyinput__content{
		min-height: 60rpx;
	}
	.page{
		padding-bottom: 150rpx;
	}
</style>
