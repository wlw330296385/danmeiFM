
const JsPlayer = function() {

	this.player = uni.createInnerAudioContext();
	this.list = [];
	this.currInfo = {};
	this.eventHandler = {};
	
	// 播放信息推送
	this._timer = setInterval(() => {
		let data = {
			is_playing: !this.player.paused,
			player_state: 1,
			has_previous: this.hasPrevious(),
			has_next: this.hasNext(),
			speed: 1
		};
		
		data.current_position = this.player.currentTime ? Math.floor(this.player.currentTime * 1000) : 0;
		data.duration = this.player.duration ? Math.floor(this.player.duration * 1000) : 0;
		
		if (this.currInfo.currentSong) {
			data.current_song = this.currInfo.currentSong;
		} else {
			data.current_song = {id: 0};
		}
		
		if (this.eventHandler.onPlayerInfo) {
			let list = this.eventHandler.onPlayerInfo;
			for(let i = 0; i < list.length; i++) {
				let item = list[i];
				item(data);
			}
		}
	}, 100)

	this.isServiceRunning = function () {
		return true;
    };
	
	this.listPlay = function (list, currInfo, playNow) {
		this.list = list;
		if (playNow) {
			this.player.src = currInfo.currentSong.url;
			if (this.currInfo.currentSong && currInfo.currentSong.url == this.currInfo.currentSong.url) {
				this.player.seek(currInfo.currentPosition > 0 ? currInfo.currentPosition / 1000 : 0);
			}
			this.player.play();
		}
		this.currInfo = currInfo;
    };
	
	this.changePlayerSpeed = function (speed) {
		console.log('~~~~~~~~~~~~~~~changePlayerSpeed')
    };
	
	this.playListSong = function (id) {
		let ll = this.list.filter(i => i.id == id);
		if (ll) {
			this.currInfo.currentSong = ll[0];
			this.player.src = this.currInfo.currentSong.url;
			this.player.play();
		}
    };
	
	this.setCurrentInfo = function (currentInfo) {
		console.log('~~~~~~~~~~~~~~~setCurrentInfo')
    };
	
	this.setPlayList = function (list) {
		this.list = list;
		if (!this.currInfo.currentSong && list.length) {
			this.currInfo.currentSong = list[0];
		}
    };
	
	this.follow = function () {
		console.log('~~~~~~~~~~~~~~~follow')
    };
	
	this.seekTo = function (position) {
		this.currInfo.currentPosition = position;
		this.player.seek(position > 0 ? position / 1000 : 0);
    };
	
	this.play = function () {
		this.player.play()
    };
	
	this.pause = function () {
		this.player.pause()
    };
	
	this.previousSong = function () {
		if (this.hasPrevious()) {
			let index = this.getCurrentSongIndex();
			this.currInfo.currentSong = this.list[index - 1];
			this.player.src = this.currInfo.currentSong.url;
			this.player.play();
		}
    };
	
	this.nextSong = function () {
		if (this.hasNext()) {
			let index = this.getCurrentSongIndex();
			this.currInfo.currentSong = this.list[index + 1];
			this.player.src = this.currInfo.currentSong.url;
			this.player.play();
		}
    };
	
	this.onPlay = function (fn) {
		if (!this.eventHandler.onPlay) {
			this.eventHandler.onPlay = [];
		}
		this.eventHandler.onPlay.push(fn)
    };
	
	this.onPause = function (fn) {
		if (!this.eventHandler.onPause) {
			this.eventHandler.onPause = [];
		}
		this.eventHandler.onPause.push(fn)
    };
	
	this.onPreviousSong = function (fn) {
		if (!this.eventHandler.onPreviousSong) {
			this.eventHandler.onPreviousSong = [];
		}
		this.eventHandler.onPreviousSong.push(fn)
    };
	
	this.onNextSong = function (fn) {
		if (!this.eventHandler.onNextSong) {
			this.eventHandler.onNextSong = [];
		}
		this.eventHandler.onNextSong.push(fn)
    };
	
	this.onFollow = function (fn) {
		if (!this.eventHandler.onFollow) {
			this.eventHandler.onFollow = [];
		}
		this.eventHandler.onFollow.push(fn)
    };

    this.onPlayerInfo = function (fn) {
		if (!this.eventHandler.onPlayerInfo) {
			this.eventHandler.onPlayerInfo = [];
		}
		this.eventHandler.onPlayerInfo.push(fn)
    };

	this.onClose = function (fn) {
		if (!this.eventHandler.onClose) {
			this.eventHandler.onClose = [];
		}
		this.eventHandler.onClose.push(fn)
	}
	
	this.setServerConfig = function () {
		
	}
	
	this.setUserToken = function () {
		
	}
	
	this.hasPrevious = function() {
		let index = this.getCurrentSongIndex();
		return index > 0 && this.list.length > 0;
	}
	
	this.hasNext = function() {
		let index = this.getCurrentSongIndex();
		return index < this.list.length - 1;
	}
	
	this.getCurrentSongIndex = function() {
		let currIndex = -1;
		if (this.currInfo && this.currInfo.currentSong) {
		   for (let i = 0; i < this.list.length; i++) {
				let item = this.list[i];
				if (item.id == this.currInfo.currentSong.id) {
					currIndex = i;
					break;
				}
			}
		}
		return currIndex;
	}
};

export default JsPlayer;
