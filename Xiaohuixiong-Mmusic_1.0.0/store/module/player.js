let playlist = uni.getStorageSync('playlist');
import JsPlayer from '@/utils/js-player.js';

export default {
	state: {
		playlist: playlist ? playlist : [],
		player: null,
		currPlayInfo: {
			playId: "0",		// 当前播放曲目id
			isPlaying: false,	// 是否播放中
			playType: 1,		// 播放类型 1列表播放
			speed: 1,			// 播放速度
			duration: 0,		// 音频时长
			currentPosition: 0	// 当前播放位置
		}
	},
	getters: {
		currTrack(state) {
			if (state.playlist && state.playlist.length && state.currPlayInfo.playId) {
				for (let i = 0; i < state.playlist.length; i++) {
					if (state.currPlayInfo.playId == state.playlist[i].id) {
						return state.playlist[i]
					}
				}
			}
		},
		nextTrackId(state) {
			if (state.playlist && state.currPlayInfo.playId) {
				for(let i = 0; i < state.playlist.length; i++) {
					if (state.playlist[i].id == state.currPlayInfo.playId && i < state.playlist.length - 1) {
						return state.playlist[i + 1].id;
					}
				}
			}
			return 0;
		},
		previousTrackId(state) {
			if (state.playlist && state.currPlayInfo.playId) {
				for(let i = 0; i < state.playlist.length; i++) {
					if (state.playlist[i].id == state.currPlayInfo.playId) {
						if (i === 0) {
							return 0;
						} else {
							return state.playlist[i - 1].id;
						}
					}
				}
			}
			return 0;
		},
		hasPrevious(state) {
			if (!state.playlist || !state.playlist.length || state.currPlayInfo.playId == 0) {
				return false;
			}
			if (state.playlist && state.currPlayInfo.playId) { 
				for(let i = 0; i < state.playlist.length; i++) {
					if (state.playlist[i].id == state.currPlayInfo.playId && i === 0) {
						return false;
					}
				}
			}
			return true;
		},
		hasNext(state) {
			if (state.playlist && state.currPlayInfo.playId) { 
				for(let i = 0; i < state.playlist.length; i++) {
					if (state.playlist[i].id == state.currPlayInfo.playId && i < state.playlist.length - 1) {
						return true;
					}
				}
			}
			return false;
		}
	},
	mutations: {
		changeFollowStatus(state, status) {
			if (state.playlist && state.playlist.length) {
				let ev = state.playlist.filter(i => i.id == state.currPlayInfo.playId);
				if (ev && ev.length) {
					ev[0] = {...ev[0], follow: status};
				}
			}
		},
		setPlayer(state, player) {
			state.player = player
		},
		setPlayId(state, playId) {
			state.currPlayInfo.playId = playId
		},
		setPlayerStatus(state, status) {
			state.currPlayInfo.isPlaying = status;
		},
		setCurrPlayInfo(state, info) {
			state.currPlayInfo = {...state.currPlayInfo, ...info};
		},
		setPlaySpeed(state, speed) {
			state.currPlayInfo.speed = speed;
		},
		setPlayList(state, list) {
			state.playlist = list;
			uni.setStorage({
				key: 'playlist',
				data: state.playlist
			})
		},
		addToPlayList(state, list) {
			// 删除播放列表已存在的项目
			let ids = list.map(i => i.id);
			let l = state.playlist.filter(i => ids.indexOf(i.id) === -1);

			for (let i = 0; i < list.length; i++) {
				l.push(list[i]);
			}
			state.playlist = l;
			uni.setStorage({
				key: 'playlist',
				data: state.playlist
			})
		}
	},
	actions: {
		resetPlayer(context) {
			context.commit('setPlayList', []);
			if (context.state.player) {
				context.state.player.close();
			}
		},
		initPlayer(context) {
			return new Promise((resolve, reject) => {
				if (context.state.player) {
					resolve();
				} else {
					// #ifdef H5
					context.commit('setPlayer', new JsPlayer());
					// #endif 
					// #ifndef H5
					context.commit('setPlayer', uni.requireNativePlugin("WDMediaPlayer"));
					// #endif
					
					if (context.state.player) {
						context.state.player.onPlayerInfo(function(d) {
							if (d) {
								context.commit('setCurrPlayInfo', {
									playId: d.current_song.id,
									isPlaying: d.is_playing,
									duration: d.duration,
									currentPosition: d.current_position,
									speed: d.speed
								});
							}
						})
						context.state.player.onClose(function() {
							context.commit('setPlayerStatus', false);
						})
						
						// 发送用户信息配置给后台服务
						context.state.player.setServerConfig(context.getters.serverConfig);
						context.state.player.setUserToken({token: context.getters.userToken});
					}
					resolve();
				}
			});
		},
		doPlay(context) {
			const state = context.state;
			if (!state.playlist || !state.playlist.length) {
				return ;
			}
			let fn = function() {
				if (state.player) {
					let currInfo = {};
					if (state.currPlayInfo && state.currPlayInfo.playId && state.currPlayInfo.playId != 0) {
						currInfo = {...state.currPlayInfo, currentSong: context.getters.currTrack};
					} else {
						currInfo = {...state.currPlayInfo, currentSong: state.playlist[0]};
					}
					state.player.listPlay(state.playlist, currInfo, true);
				}
			}
			if (!context.state.player || !context.state.player.isServiceRunning()) {
				context.dispatch('initPlayer').then(fn);
			} else {
				fn();
			}
		},
		doStop(context) {
			if (context.state.player) {
				context.state.player.pause();
			}
		},
		doClose(context) {
			if (context.state.player) {
				context.state.player.close();
			}
		},
		playListItem(context, itemId) {
			if (context.state.playlist && context.state.player) {
				context.state.player.playListSong(itemId);
			}
		},
		playNext(context) {
			if (context.state.player) {
				context.state.player.nextSong();
			}
		},
		plaryPrevious(context) {
			if (context.state.player) {
				context.state.player.previousSong();
			}
		},
		plaryFollow(context) {
			if (context.state.player) {
				context.state.player.follow();
			}
		},
		seekTo(context, position) {
			if (context.state.player) {
				context.state.player.seekTo(position);
			}
		},
		changePlayerSpeed(context, speed) {
			context.commit('setPlaySpeed', speed);
			if (context.state.player) {
				context.state.player.changePlayerSpeed(speed);
			}
		},
		isServiceRunning(context) {
			context.state.player.isServiceRunning();
		}
		
	}
}
