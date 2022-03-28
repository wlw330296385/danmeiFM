<template>
	<view class="subtitle text-center text-orange">
		<view v-for="(item, index) in currSubtitleLine" :key="index">
			{{item}}
		</view>
	</view>
</template>

<script>
	import {getAlbumSingleDetails} from '@/api/works.js';
	import {timeStrToMillisecond} from '@/utils/util.js';
	
	export default {
		name:"subtitle",
		data() {
			return {
				info: {},
				subtitle: {}
			};
		},
		props: {
			url: {
				type: String
			}
		},
		computed: {
			currPlayInfo() {
				if (this.$store.state.player.currPlayInfo) {
					return this.$store.state.player.currPlayInfo
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
			currTrack() {
				return this.$store.getters.currTrack;
			},
			currSubtitleLine() {
				let keys = Object.keys(this.subtitle).sort((x, y) => {
					return Number(x) < Number(y) ? 1 : -1;
				});
				for(let i = 0; i < keys.length; i++) {
					if (this.currentPosition >= Number(keys[i])) {
						return this.subtitle[keys[i]];
					}
				}
				return [];
			}
		},
		watch: {
			currId(val) {
				this.loadSubtitle(val);
			}
		},
		created() {
			this.loadSubtitle(this.currId);
		},
		methods: {
			timeStrToMillisecond,
			loadSubtitle(id) {
				if (id && id != 0) {
					this.subtitle = {};
					this.info = {};
					getAlbumSingleDetails({id}).then(res => {
						if (res.data.code == 200) {
							this.info = res.data.data;
							if (this.info && this.info.zhCnSubtitle && this.currId == id) {
								this.parseSubtitle(this.info.zhCnSubtitle)
							}
						} else {
							this.$toast.fail(res.data.msg);
						}
					})
				}
			},
			parseSubtitle(subtitle) {
				let list = subtitle.split("\n");
				let lastTimeArr = [];
				for(let i = 0; i < list.length; i++) {
					let line = list[i].trim();
					if (line.indexOf('[') === 0) {
						let reg = new RegExp('^(\\[(([0-9]{2}:)?[0-9]{2}:[0-9]{2}(\.[0-9]{1,4})?)\\]\s*)*', 'gm');
						let arr = line.match(reg);
						if (arr && arr[0]) {
							let newTime = [];
							let text = line.replace(arr[0], '');
							reg = new RegExp('(([0-9]{2}:)?[0-9]{2}:[0-9]{2}(\.[0-9]{1,4})?)', 'g');
							arr = arr[0].match(reg);
							if (arr.length) {
								for (let j = 0; j < arr.length; j++) {
									let time = this.timeStrToMillisecond(arr[j]);
									if (time) {
										newTime.push(time);
									}
								}
							}
							if (newTime.length) {
								lastTimeArr = newTime;
								this.addToList(newTime, text);
							}
						} else if (lastTimeArr.length) {
							this.addToList(lastTimeArr, line);
						}
					} else if (lastTimeArr.length) {
						this.addToList(lastTimeArr, line);
					}
				}
			},
			addToList(timeArr, text) {
				if (timeArr && timeArr.length) {
					for (let i = 0; i < timeArr.length; i++) {
						if (!this.subtitle[timeArr[i]]) {
							this.$set(this.subtitle, timeArr[i], []);
						}
						this.subtitle[timeArr[i]].push(text);
					}
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.subtitle {
		padding: 0 15rpx;
		word-break: break-all;
		font-size: 36rpx;
		font-weight: bold;
		text-shadow: 2rpx 2rpx 2rpx #ffffff;
	}
</style>
