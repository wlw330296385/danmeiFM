import {getAppConfig} from '@/api/app.js';
import config from '@/utils/config.js';
import {timestamp} from '@/utils/util.js';
import Sqllite from '@/utils/sqllite.js';

export default {
	state: {
		config: {},
		downloadingId: 0,
		downloadList: [{
			id: 1,
			a_id: 607,
			a_name: '日落大道',
			s_name: '02',
			cover: 'http://www.bq77.net.cn/upload/2021/07/15/670897cb-a312-4ecf-82d4-c54227aa7500.jpg',
			a_url: 'https://danting-1306371307.file.myqcloud.com/RLDD/01_1.mp3',
			a_path: '',
			a_status: 0,
			items: [
				{
					id: 1,
					a_id: 2,
					a_name: '日落大道',
					s_name: '01',
					cover: 'http://www.bq77.net.cn/upload/2021/07/15/670897cb-a312-4ecf-82d4-c54227aa7500.jpg',
					a_url: 'https://danting-1306371307.file.myqcloud.com/RLDD/01_1.mp3',
					a_path: '',
					a_status: 0
				},
				{
					id: 1,
					a_id: 2,
					a_name: '日落大道',
					s_name: '02',
					cover: 'http://www.bq77.net.cn/upload/2021/07/15/670897cb-a312-4ecf-82d4-c54227aa7500.jpg',
					a_url: 'https://danting-1306371307.file.myqcloud.com/RLDD/01_1.mp3',
					a_path: '',
					a_status: 0
				}
			]
		},
		{
			id: 1,
			a_id: 607,
			a_name: '日落大道',
			s_name: '02',
			cover: 'http://www.bq77.net.cn/upload/2021/07/15/670897cb-a312-4ecf-82d4-c54227aa7500.jpg',
			a_url: 'https://danting-1306371307.file.myqcloud.com/RLDD/01_1.mp3',
			a_path: '',
			a_status: 0,
			items: [
				{
					id: 1,
					a_id: 2,
					a_name: '日落大道',
					s_name: '01',
					cover: 'http://www.bq77.net.cn/upload/2021/07/15/670897cb-a312-4ecf-82d4-c54227aa7500.jpg',
					a_url: 'https://danting-1306371307.file.myqcloud.com/RLDD/01_1.mp3',
					a_path: '',
					a_status: 0
				},
				{
					id: 1,
					a_id: 2,
					a_name: '日落大道',
					s_name: '02',
					cover: 'http://www.bq77.net.cn/upload/2021/07/15/670897cb-a312-4ecf-82d4-c54227aa7500.jpg',
					a_url: 'https://danting-1306371307.file.myqcloud.com/RLDD/01_1.mp3',
					a_path: '',
					a_status: 0
				}
			]
		}]
	},
	getters: {
		serverConfig() {
			let serverUrl = process.env.NODE_ENV === 'development' ? config.serverUrl.dev : config.serverUrl.pro;
			return {
				serverUrl
			}
		}
	},
	mutations: {
		setConfig(state, config) {
			state.config = config;
		},
		setDownloadList(state, list) {
			state.downloadList = list;
		},
		setDownloadingId(state, id) {
			state.downloadingId = id;
		}
	},
	actions: {
		selectDownloadList(context) {
			// #ifdef APP-PLUS
			let sqllite = new Sqllite('app');
			let sql = `select * from download`;
			sqllite.selectSql(sql, res => {
				sqllite.closeDatabase();
				if (!res || !res.length) {
					res = [];
				}
				context.commit('setDownloadList', res);
			}, e => {
				sqllite.closeDatabase();
			})
			// #endif
		},
		initDownload(context) {
			// 每3秒查询一条数据库未下载的记录
			// #ifdef APP-PLUS
			setInterval(() => {
				let sqllite = new Sqllite('app');
				let sql = `select * from download where a_status = 0`;
				sqllite.selectSql(sql, res => {
					if (res && res.length) {
						let item = res[0];
						if (item && item.a_url) {
							// 如果当前曲目正在下载就跳过
							if (context.state.downloadingId == item.id) {
								return ;
							} else {
								context.commit('setDownloadingId', item.id);
							}
							
							const errorFn = function(p) {
								sqllite.closeDatabase();
								console.log('下载失败了~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~' + p)
							}
							
							uni.downloadFile({
								url: item.a_url, 
								success: (r1) => {
									if (r1.statusCode && r1.tempFilePath) {
										uni.saveFile({
											tempFilePath: r1.tempFilePath,
											success: (r3) => {
												if (r3.savedFilePath) {
													let sql2 = `update download set a_path = '${r3.savedFilePath}', a_status = 1 where id = ` + item.id;
													sqllite.executeSql(sql2, res => {
														context.dispatch('selectDownloadList')
														console.log(r3.savedFilePath)
													}, res => {
														uni.removeSavedFile({
															filePath: r3.savedFilePath,
															complete: function (res) {
														        
															}
														});
														errorFn(5)
													})
													sqllite.closeDatabase();
												} else {
													errorFn(4)
												}
											},
											fail: (r4) => {
												errorFn(3)
											}
										})
									} else {
										errorFn(2)
									}
								},
								fail: (r2) => {
									errorFn(1)
								}
							})
							return ;
						}
					} else {
						context.commit('setDownloadingId', 0);
					}
					sqllite.closeDatabase();
				}, e => {
					sqllite.closeDatabase();
				})
			}, 3000)
			// #endif
		},
		getAppConfig(context) {
			getAppConfig().then(res => {
				if (res.data.code == 200) {
					context.commit('setConfig', res.data.data);

					// #ifdef APP-PLUS
					let lur = uni.getStorageSync('last-update-reminder');
					if (res.data.data && res.data.data.latest_version && (!lur || timestamp() - lur > 24 * 60 * 60)) {
						let vData = JSON.parse(res.data.data.latest_version)
						if (vData.version != plus.runtime.version) {
							uni.showModal({ // 提醒用户更新  
								title: "发现新版本",  
								content: "发现新版本，点击确定前往下载更新！",
								success: (r) => {
									uni.setStorage({
										key: 'last-update-reminder',
										data: timestamp()
									})
									if (r.confirm) {
										plus.runtime.openURL(vData.url);
									}
								}
							})  
						}
					}
					// #endif
				}
			})
		}
	}
}
