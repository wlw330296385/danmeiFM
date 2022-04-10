<script>
	import Sqllite from '@/utils/sqllite.js';
	
	export default {
		onLaunch: function() {
			console.log('App Show');
			
			// #ifdef APP-PLUS
			this.initDababase();
			// #endif
			this.$store.dispatch('resetPlayer');
			this.$store.dispatch('initPlayer');
			this.$store.dispatch('initDownload');
		},
		onShow: function() {
			this.$store.dispatch('getAppConfig');
			this.$store.dispatch('getUserInfo');
			
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		},
		onError() {
			console.log('App Error')
		},
		computed: {
			playlist() {
				return this.$store.state.player.playlist;
			},
			currPlayInfo() {
				return this.$store.state.player.currPlayInfo;
			},
			player() {
				return this.$store.state.player.player;
			},
			userToken() {
				return this.$store.getters.userToken;
			}
		},
		watch: {
			playlist(val) {
				if (this.player && this.player.isServiceRunning()) {
					this.player.setPlayList(val);
				}
			},
			currPlayInfo(val) {
				uni.setStorage({
					key: 'curr_play_info',
					data: val
				})
			},
			userToken(val) {
				if (this.player) {
					this.player.setUserToken({token: val});
				}
			}
		},
		methods: {
			initDababase() {
				let sqllite = new Sqllite('app');
				let sql = 'CREATE TABLE IF NOT EXISTS download(id INT PRIMARY KEY NOT NULL, a_id INT NOT NULL, a_name VARCHAR(1024) NOT NULL, s_name VARCHAR(1024) NOT NULL, cover VARCHAR(1024) NOT NULL, a_url VARCHAR(1024) NOT NULL, a_path VARCHAR(1024) NOT NULL, a_status INT NOT NULL)';
				
				sqllite.executeSql(sql, e => {
					console.log('success');
					console.log(e)
				}, e => {
					console.log('error');
					console.log(e)
				});
				sqllite.closeDatabase();
			}
		}
	}
</script>

<style>
	@import url("./colorui/icon.css");
	@import url("./colorui/main.css");
	
	/* uni.css - 通用组件、模板样式库，可以当作一套ui库应用 */
	@import url('/common/uni.css');
	/*每个页面公共css */
	page {
		width: 100%;
		height: 100%;
	}

	.content {
		width: 100%;
		height: 100%;
		background-color: #ffffff;
		color: #000000;
		box-sizing: border-box;
		position: relative;
		display: flex;
		flex-direction: column;
	}
</style>
