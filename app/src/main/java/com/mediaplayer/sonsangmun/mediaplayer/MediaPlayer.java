package com.mediaplayer.sonsangmun.mediaplayer;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;

public class MediaPlayer extends AppCompatActivity implements View.OnClickListener, android.media.MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {
    private static final int REQUEST_CODE_AUDIO = 0;
    private static final int REQUEST_CODE_VIDEO = 1;
    private static final String TAG = MediaPlayer.class.getSimpleName();
    private static final int AUDIO_PLAY = 1;
    private static final int AUDIO_PAUSE = 0;
    private static final int VIDEO_PLAY = 1;
    private static final int VIDEO_PAUSE = 0;

    private ImageButton mBtnAudioFilePick;    // 파일 선택
    private ImageButton mBtnVideoFilePick;    // 파일 선택
    private ImageButton mBtnPlayer;       // 시작 / 정지
    private int mStat = 0;
    private int mAudioStat = 0;
    private int mVideoStat = 0;
    private TextView mFileName;
    private SeekBar mPlayProgressBar;

    private int mMaxAudioPoint;
    private int mMaxVideoPoint;

    // 플레이어
    private android.media.MediaPlayer mMediaPlayer;
    private VideoView mVideoView;
    private ImageView mImageView;

    private ImageView introMusicImageView;
    private ImageView introVideoImageView;

    private LinearLayout windowOne;
    private LinearLayout windowTwo;
    private LinearLayout viewTypeMusic;
    private LinearLayout viewTypeVideo;

    // 재생시 SeekBar 처리
    // http://androiddeveloper.tistory.com/91
    public Handler mProgressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mMediaPlayer == null) return;
            try {
                if (mMediaPlayer.isPlaying()){
                    mPlayProgressBar.setProgress(mMediaPlayer.getCurrentPosition());
                    mProgressHandler.sendEmptyMessageDelayed(0, 100);
                }
            } catch (IllegalStateException e) {

            } catch (Exception e) {

            }
        }
    };

    public Handler mProgressHandler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mVideoView == null) return;
            try{
                if(mVideoView.isPlaying()) {
                    mPlayProgressBar.setProgress(mVideoView.getCurrentPosition());
                    mProgressHandler2.sendEmptyMessageDelayed(0, 100);
                }
            } catch (IllegalStateException e) {

            } catch (Exception e) {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        // 레이아웃 초기화, 이벤트 연결
        init();

        // FileChooser 로 선택되어진 경우
        if (getIntent() != null) {
            Uri uri = getIntent().getData();
            if (uri != null) {
                startMusic(uri);
            }
        }
    }

    private void init() {
        mBtnAudioFilePick = (ImageButton) findViewById(R.id.btn_audioFilePick);
        mBtnVideoFilePick = (ImageButton) findViewById(R.id.btn_videoFilePick);
        mBtnPlayer = (ImageButton) findViewById(R.id.btn_player);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mFileName = (TextView) findViewById(R.id.file_name);
        mPlayProgressBar = (SeekBar) findViewById(R.id.play_seekbard);

        introMusicImageView = (ImageView) findViewById(R.id.intro_music_imageview);
        introVideoImageView = (ImageView) findViewById(R.id.intro_video_imageview);

        windowOne = (LinearLayout) findViewById(R.id.window_one);
        windowTwo = (LinearLayout) findViewById(R.id.window_two);
        viewTypeMusic = (LinearLayout) findViewById(R.id.view_type_music);
        viewTypeVideo = (LinearLayout) findViewById(R.id.view_type_video);

        introMusicImageView.setOnClickListener(this);
        introVideoImageView.setOnClickListener(this);
        mBtnAudioFilePick.setOnClickListener(this);
        mBtnVideoFilePick.setOnClickListener(this);
        mBtnPlayer.setOnClickListener(this);
        mPlayProgressBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i = null;
        switch (v.getId()) {
            case R.id.btn_audioFilePick:
                setPlayMusic();
                break;
            case R.id.btn_videoFilePick:
                setPlayVideo();
                break;
            case R.id.btn_player:
                if(mStat == 0) {
                    // 상태가 음악인 경우 음악관련 처리
                    playerSet("audio");
                    playerAudio();
                } else {
                    // 상태가 동영상인 경우 동영상관련 처리
                    playerSet("video");
                    playerVideo();
                }
                break;
            case R.id.intro_music_imageview:
                introSet("music");
                setPlayMusic();
                break;
            case R.id.intro_video_imageview:
                introSet("video");
                setPlayVideo();
                break;
        }
    }

    private void introSet(String introSw) {
        switch (introSw) {
            case "music":
                introVideoImageView.setImageResource(R.drawable.video_player_img);
                introVideoImageView.setBackgroundColor(Color.parseColor("#FFFFFF"));

                introMusicImageView.setImageResource(R.drawable.music_player_over_img);
                introMusicImageView.setBackgroundColor(Color.parseColor("#000000"));
                break;
            case "video":
                introVideoImageView.setImageResource(R.drawable.video_player_over_img);
                introVideoImageView.setBackgroundColor(Color.parseColor("#000000"));

                introMusicImageView.setImageResource(R.drawable.music_player_img);
                introMusicImageView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                break;
        }
    }

    private void playerSet(String playerSw) {
        switch (playerSw) {
            case "music":
                viewTypeVideo.setVisibility(View.GONE);
                viewTypeMusic.setVisibility(View.VISIBLE);
                break;
            case "video":
                viewTypeMusic.setVisibility(View.GONE);
                viewTypeVideo.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setPlayMusic() {
        Intent i = null;

        // FileChooser 사용
        i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/*");
        startActivityForResult(Intent.createChooser(i, "파일선택..."), REQUEST_CODE_AUDIO);

        // 파일 선택창이 열린후 처리
        // 파일을 선택하면 onActivityResult 가 호출됨.
        mStat = 0;  // 현재 플레이는 음악 플레이

        // 음악 아이콘은 on으로 활성화
        mBtnAudioFilePick.setImageResource(R.drawable.audio_on);
        // 동영상 아이콘은 off로 비활성화
        mBtnVideoFilePick.setImageResource(R.drawable.video);
        // 음악 관련 플레이/멈춤 버튼 처리
        if(mMediaPlayer != null) {
            viewSwitchSet(mAudioStat);
            mPlayProgressBar.setMax(mMaxAudioPoint);
        }


    }

    private void setPlayVideo(){
        Intent i = null;

        // FileChooser 사용
        i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("video/*");
        startActivityForResult(Intent.createChooser(i, "파일선택..."), REQUEST_CODE_VIDEO);

        // 파일 선택창이 열린후 처리
        // 파일을 선택하면 onActivityResult 가 호출됨.
        mStat = 1;  // 현재 플레이는 동영상 플레이

        // 동영상 아이콘은 on으로 활성화
        mBtnVideoFilePick.setImageResource(R.drawable.video_on);
        // 음악 아이콘은 off로 비활성화
        mBtnAudioFilePick.setImageResource(R.drawable.audio);
        // 동영상 관련 플레이/멈춤 버튼 처리
        if (mVideoView != null) {
            viewSwitchSet(mVideoStat);
            mPlayProgressBar.setMax(mMaxVideoPoint);
        }
    }

    private void playerVideo() {
        if (mVideoView != null) {
            if (mVideoStat == 0) {
                // 현 상태가 멈춤이면 플레이를 하고 상태값 플레이로 변경
                mVideoStat = 1;
                statVideoSet(mVideoStat);

                mAudioStat = 0;
                mMediaPlayer.pause();
                mProgressHandler2.sendEmptyMessageDelayed(0, 0);
            } else {
                // 현 상태가 플레이면 멈춤을 하고 상태값 멈춤으로 변경
                mVideoStat = 0;
                statVideoSet(mVideoStat);
            }
            // 화면상 현 상태의 스위치 버튼 표시
            Log.d(TAG, "mVideoStat : " + mVideoStat);
        }
    }

    private void playerAudio() {
        if (mMediaPlayer != null) {
            if (mAudioStat == 0) {
                // 현 상태가 멈춤이면 상태값을 플레이로
                mAudioStat = 1;
                statAudioSet(mAudioStat);

                mVideoStat = 0;
                mVideoView.pause();
                mProgressHandler.sendEmptyMessageDelayed(0, 0);
            } else {
                // 현 상태가 플레이면 멈춤을 하고 상태값 멈춤으로 변경
                mAudioStat = 0;
                statAudioSet(mAudioStat);
            }
            Log.d(TAG, "mAudioStat : " + mAudioStat);
        }
    }

    // 스위치 상태 변경
    private void viewSwitchSet(int sw){
        if(sw == 0) {
            mBtnPlayer.setImageResource(R.drawable.player_play);
        } else {
            mBtnPlayer.setImageResource(R.drawable.player_pause);
        }
    }

    // video 상태 변경
    private void statVideoSet(int stat) {
        if(stat == 0) {
            mVideoView.pause();
            mProgressHandler2.sendEmptyMessageDelayed(0, 0);
            viewSwitchSet(0);
        } else {
            mVideoView.start();
            mProgressHandler2.sendEmptyMessageDelayed(0, 100);
            viewSwitchSet(1);
        }
    }

    // audio 상태 변경
    private void statAudioSet(int stat) {
        if(stat == 0) {
            mMediaPlayer.pause();
            mProgressHandler.sendEmptyMessageDelayed(0, 0);
            viewSwitchSet(0);
        } else {
            mMediaPlayer.start();
            mProgressHandler.sendEmptyMessageDelayed(0, 100);
            viewSwitchSet(1);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            // 선택한 파일이 있는 경우
            Uri fileUri = data.getData();

            // 창전환
            windowOne.setVisibility(View.VISIBLE);
            windowTwo.setVisibility(View.GONE);

            if (requestCode == REQUEST_CODE_AUDIO && resultCode == RESULT_OK) {
                // Audio
                // ImageView 와 viewoView 처리
                playerSet("music");

                startMusic(fileUri);
                mBtnPlayer.setImageResource(R.drawable.player_pause);
                mAudioStat = 1;
                if (mVideoView != null) {
                    // video player pause
//                    mVideoView.pause();
                    mVideoView.stopPlayback();
                    mVideoStat = 0;
                }
//            Intent intent = new Intent(getApplicationContext(), MusicService.class);
//            intent.putExtra("uri", fileUri);
//            startService(intent);

//            mBtnAudioFilePick.setText(fileUri.getPath());

//            mFileName.setText(fileUri.getPath());
            } else if (requestCode == REQUEST_CODE_VIDEO && resultCode == RESULT_OK) {
                // Video
                // ImageView 와 viewoView 처리
                playerSet("video");

                startVideo(fileUri);
                mBtnPlayer.setImageResource(R.drawable.player_pause);
                mVideoStat = 1;
                if (mMediaPlayer != null) {
                    // music player pause
//                    mMediaPlayer.pause();
                    mMediaPlayer.stop();
                    mAudioStat = 0;
                }

//            mVideoView.setVideoURI(fileUri);
//            mVideoView.start();

//            mBtnVideoFilePick.setText(fileUri.getPath());
//            mFileName.setText(fileUri.getPath());
            }

            // 파일명 자르기
            String getFilePath = String.valueOf(fileUri.getPath());
            String[] filePathSplit = getFilePath.split("/");
            int filePathLength = filePathSplit.length;
//        mFileName.setText(filePathSplit[filePathSplit.length]);
            mFileName.setText(filePathSplit[filePathLength - 1]);
        } catch (Exception e) {

        }
    }

    // 음악 재생 시작
    private void startMusic(Uri fileUri) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new android.media.MediaPlayer();
        } else {
            mMediaPlayer.reset();
        }

        mMediaPlayer.setOnCompletionListener(this);
        String fullFilePath = fileUri.toString();

        try {
            mMediaPlayer.setDataSource(getApplicationContext(), fileUri);
            mMediaPlayer.prepare();
            mMaxAudioPoint = mMediaPlayer.getDuration();
            mPlayProgressBar.setMax(mMaxAudioPoint);

            int maxMinPoint = mMaxAudioPoint / 1000 / 60;
            int maxSecPoint = (mMaxAudioPoint / 1000) % 60;
            String maxMinPointStr = "";
            String maxSecPointStr = "";

            if(maxMinPoint < 10) {
                maxMinPointStr = "0" + maxMinPoint + ":";
            } else {
                maxMinPointStr = maxMinPoint + ":";
            }

            if(maxSecPoint < 10) {
                maxSecPointStr = "0" + maxSecPoint;
            } else {
                maxSecPointStr = String.valueOf(maxSecPoint);
            }


            mPlayProgressBar.setProgress(0);

            mMediaPlayer.start();

            mProgressHandler.sendEmptyMessageDelayed(0, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mFileName.setText(fileUri.getPath());
    }

    private void startVideo(Uri fileUri) {
        mVideoView.setVideoURI(fileUri);
        mVideoView.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(android.media.MediaPlayer mp) {
                // 재생 시간 확인
                mMaxVideoPoint = mp.getDuration();

                Log.d(TAG, "video max point : " + mMaxVideoPoint);

                mPlayProgressBar.setMax(mMaxVideoPoint);
                int maxMinPoint = mMaxVideoPoint / 1000 / 60;
                int maxSecPoint = (mMaxVideoPoint / 1000) % 60;
                String maxMinPointStr = "";
                String maxSecPointStr = "";

                if (maxMinPoint < 10) {
                    maxMinPointStr = "0" + maxMinPoint + ":";
                } else {
                    maxMinPointStr = maxMinPoint + ":";
                }

                if (maxSecPoint < 10) {
                    maxSecPointStr = "0" + maxSecPoint;
                } else {
                    maxSecPointStr = String.valueOf(maxSecPoint);
                }


                mPlayProgressBar.setProgress(0);

                mVideoView.start();

                mProgressHandler2.sendEmptyMessageDelayed(0, 100);
            }
        });
        mFileName.setText(fileUri.getPath());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            Toast.makeText(getApplicationContext(), "볼륨 다운", Toast.LENGTH_SHORT).show();
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            Toast.makeText(getApplicationContext(), "볼륨 업", Toast.LENGTH_SHORT).show();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCompletion(android.media.MediaPlayer mp) {
        // 재생이 종료됨

        // 재생이 종료되면 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler.sendEmptyMessageDelayed(0, 0);

    }

    // SeekBar의 드래깅
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        try {
            if (fromUser) {
                if (mStat == 0) {
                    mMediaPlayer.seekTo(mPlayProgressBar.getProgress());
                    mMediaPlayer.start();
                } else {
                    mVideoView.seekTo(mPlayProgressBar.getProgress());
                    mVideoView.start();
                }
            }
//            Toast.makeText(getApplicationContext(), "드래깅", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
        }
    }

    // SeekBar의 터치 시작
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        try {
            if (mStat == 0) {
                mMediaPlayer.pause();
            } else {
                mVideoView.pause();
            }

//            Toast.makeText(getApplicationContext(), "터치시작", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {

        }
    }

    // SeekBar의 터치 종료
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            if(mStat == 0) {
                if(mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                }
            } else {
                if(mVideoView.isPlaying()) {
                    mVideoView.start();
                }
            }

//            Toast.makeText(getApplicationContext(), "터치종료", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
        }
    }
}
