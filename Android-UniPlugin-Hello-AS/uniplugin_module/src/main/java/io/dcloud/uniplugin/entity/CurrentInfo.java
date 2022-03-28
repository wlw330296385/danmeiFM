package io.dcloud.uniplugin.entity;

public class CurrentInfo {

    /**
     * 当前播放位置
     */
    private int currentPosition;

    /**
     * 播放方式
     */
    private int playMode = 1;   // 播放方式 1列表播放 2单曲循环

    /**
     * 当前曲目
     */
    private Song currentSong;

    /**
     * 播放速度
     */
    private float speed = 1;


    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getPlayMode() {
        return playMode;
    }

    public void setPlayMode(int playMode) {
        switch (playMode) {
            case 1:
            case 2:
                this.playMode = playMode;
            default:
                this.playMode = 1;
        }
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
