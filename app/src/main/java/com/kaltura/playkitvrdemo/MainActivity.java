package com.kaltura.playkitvrdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.kaltura.netkit.connect.response.ResultElement;
import com.kaltura.playkit.PKMediaConfig;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKMediaFormat;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.playkit.PlayKitManager;
import com.kaltura.playkit.Player;
import com.kaltura.playkit.ads.AdController;
import com.kaltura.playkit.api.ovp.SimpleOvpSessionProvider;
import com.kaltura.playkit.mediaproviders.base.OnMediaLoadCompletion;
import com.kaltura.playkit.mediaproviders.ovp.KalturaOvpMediaProvider;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    //The url of the first source to play
    private static final String FIRST_SOURCE_URL = "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8";
    //The url of the second source to play
    private static final String SECOND_SOURCE_URL = "https://cdnapisec.kaltura.com/p/2215841/sp/221584100/playManifest/entryId/1_w9zx2eti/protocol/https/format/url/falvorIds/1_1obpcggb,1_yyuvftfz,1_1xdbzoa6,1_k16ccgto,1_djdf6bk8/a.mp4";

    //id of the first entry
    private static final String FIRST_ENTRY_ID = "entry_id_1";
    //id of the second entry
    private static final String SECOND_ENTRY_ID = "entry_id_2";
    //id of the first media source.
    private static final String FIRST_MEDIA_SOURCE_ID = "source_id_1";
    //id of the second media source.
    private static final String SECOND_MEDIA_SOURCE_ID = "source_id_2";

    private Player player;
    private PKMediaConfig mediaConfig;
    private Button playPauseButton;

    //Put here your provider base url
    private static final String PROVIDER_BASE_URL = "https://cdnapisec.kaltura.com";
    //Put here your partner id.
    private static final int PARTNER_ID = 2196781;
    //Put here your KS.
    private static final String KS = "";
    //Put here your entry id.
    private static final String PROVIDER_ENTRY_ID = "1_afvj3z0u";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //First. Create PKMediaConfig object.
        mediaConfig = new PKMediaConfig();

        //Create instance of the player.
        player = PlayKitManager.loadPlayer(this, null);

        //Add player to the view hierarchy.
        addPlayerToView();

        //Add simple play/pause button.
        addPlayPauseButton();

        //Init change media button which will switch between entries.
        initChangeMediaButton();

        //Prepare the first entry.
       // prepareFirstEntry();

        //Create media provider and request media.
        createMediaProvider();
    }

    /**
     * Create ovp media provider, that will request for media entry.
     */
    private void createMediaProvider() {

        //Initialize provider.
        KalturaOvpMediaProvider mediaProvider = new KalturaOvpMediaProvider();

        //Initialize ovp session provider.
        SimpleOvpSessionProvider sessionProvider = new SimpleOvpSessionProvider(PROVIDER_BASE_URL, PARTNER_ID, KS);

        //Set entry id for the session provider.
        mediaProvider.setEntryId(PROVIDER_ENTRY_ID);

        //Set session provider to media provider.
        mediaProvider.setSessionProvider(sessionProvider);

        //Load media from media provider.
        mediaProvider.load(new OnMediaLoadCompletion() {
            @Override
            public void onComplete(ResultElement<PKMediaEntry> response) {
                //When response received check if it was successful.
                if (response.isSuccess()) {
                    //If so, prepare player with received PKMediaEntry.
                    preparePlayer(response.getResponse());
                } else {
                    //If response was not successful print it to console with error message.
                    String error = "failed to fetch media data: " + (response.getError() != null ? response.getError().getMessage() : "");
                    Log.e(TAG, error);
                }
            }
        });
    }

    /**
     * Prepare player and start playback.
     *
     * @param mediaEntry - media entry we received from media provider.
     */
    private void preparePlayer(final PKMediaEntry mediaEntry) {
        //The preparePlayer is called from another thread. So first be shure
        //that we are running on ui thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //Initialize media config object.
                createMediaConfig(mediaEntry);
            }
        });

    }

    private void createMediaConfig(final PKMediaEntry mediaEntry) {
        //Initialize empty mediaConfig object.
        mediaConfig = new PKMediaConfig();

        //Set media entry we received from provider.
        mediaConfig.setMediaEntry(mediaEntry);

        //Prepare player with media configurations.
        player.prepare(mediaConfig);
    }

    /**
     * Initialize the changeMedia button. On click it will change media.
     */
    private void initChangeMediaButton() {
        //Get reference to the button.
        Button changeMediaButton = (Button) this.findViewById(R.id.change_media_button);
        //Set click listener.
        changeMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Change media.
                changeMedia();
            }
        });
    }

    /**
     * Will switch between entries. If the first entry is currently active it will
     * prepare the second one. Otherwise it will prepare the first one.
     */
    private void changeMedia() {

        //Before changing media we must call stop on the player.
        player.stop();

        //Check if id of the media entry that is set in mediaConfig.
        if (mediaConfig.getMediaEntry().getId().equals(FIRST_ENTRY_ID)) {

            //If first one is active, prepare second one.
            prepareSecondEntry();
        } else if(mediaConfig.getMediaEntry().getId().equals(SECOND_ENTRY_ID)){
            //If the second one is active, prepare the first one.
            createMediaProvider();
        } else if(mediaConfig.getMediaEntry().getId().equals(PROVIDER_ENTRY_ID)){
            prepareFirstEntry();
        }

        //Just reset the playPauseButton text to "Play".
        resetPlayPauseButtonToPlayText();
    }



    /**
     * Prepare the first entry.
     */
    private void prepareFirstEntry() {
        //Second. Create PKMediaEntry object.
        PKMediaEntry mediaEntry = createFirstMediaEntry();

        //Add it to the mediaConfig.
        mediaConfig.setMediaEntry(mediaEntry);

        //Prepare player with media configuration.
        player.prepare(mediaConfig);
    }

    /**
     * Prepare the second entry.
     */
    private void prepareSecondEntry() {
        //Second. Create PKMediaEntry object.
        PKMediaEntry mediaEntry = createSecondMediaEntry();

        //Add it to the mediaConfig.
        mediaConfig.setMediaEntry(mediaEntry);

        //Prepare player with media configuration.
        player.prepare(mediaConfig);
    }

    /**
     * Create {@link PKMediaEntry} with minimum necessary data.
     *
     * @return - the {@link PKMediaEntry} object.
     */
    private PKMediaEntry createFirstMediaEntry() {
        //Create media entry.
        PKMediaEntry mediaEntry = new PKMediaEntry();

        //Set id for the entry.
        mediaEntry.setId(FIRST_ENTRY_ID);

        //Set media entry type. It could be Live,Vod or Unknown.
        //For now we will use Unknown.
        mediaEntry.setMediaType(PKMediaEntry.MediaEntryType.Unknown);

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        List<PKMediaSource> mediaSources = createFirstMediaSources();

        //Set media sources to the entry.
        mediaEntry.setSources(mediaSources);

        return mediaEntry;
    }

    /**
     * Create {@link PKMediaEntry} with minimum necessary data.
     *
     * @return - the {@link PKMediaEntry} object.
     */
    private PKMediaEntry createSecondMediaEntry() {
        //Create media entry.
        PKMediaEntry mediaEntry = new PKMediaEntry();

        //Set id for the entry.
        mediaEntry.setId(SECOND_ENTRY_ID);

        //Set media entry type. It could be Live,Vod or Unknown.
        //For now we will use Unknown.
        mediaEntry.setMediaType(PKMediaEntry.MediaEntryType.Unknown);

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        List<PKMediaSource> mediaSources = createSecondMediaSources();

        //Set media sources to the entry.
        mediaEntry.setSources(mediaSources);

        return mediaEntry;
    }

    /**
     * Create list of {@link PKMediaSource}.
     *
     * @return - the list of sources.
     */
    private List<PKMediaSource> createFirstMediaSources() {
        //Init list which will hold the PKMediaSources.
        List<PKMediaSource> mediaSources = new ArrayList<>();

        //Create new PKMediaSource instance.
        PKMediaSource mediaSource = new PKMediaSource();

        //Set the id.
        mediaSource.setId(FIRST_MEDIA_SOURCE_ID);

        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.setUrl(FIRST_SOURCE_URL);

        //Set the format of the source. In our case it will be hls.
        mediaSource.setMediaFormat(PKMediaFormat.hls);

        //Add media source to the list.
        mediaSources.add(mediaSource);

        return mediaSources;
    }

    /**
     * Create list of {@link PKMediaSource}.
     *
     * @return - the list of sources.
     */
    private List<PKMediaSource> createSecondMediaSources() {
        //Init list which will hold the PKMediaSources.
        List<PKMediaSource> mediaSources = new ArrayList<>();

        //Create new PKMediaSource instance.
        PKMediaSource mediaSource = new PKMediaSource();

        //Set the id.
        mediaSource.setId(SECOND_MEDIA_SOURCE_ID);

        //Set the content url. In our case it will be link to mp4 source(.mp4).
        mediaSource.setUrl(SECOND_SOURCE_URL);

        //Set the format of the source. In our case it will be mp4.
        mediaSource.setMediaFormat(PKMediaFormat.mp4);

        //Add media source to the list.
        mediaSources.add(mediaSource);

        return mediaSources;
    }

    /**
     * Will add player to the view.
     */
    private void addPlayerToView() {
        //Get the layout, where the player view will be placed.
        LinearLayout layout = (LinearLayout) findViewById(R.id.player_root);
        //Add player view to the layout.
        layout.addView(player.getView());
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private void addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = (Button) this.findViewById(R.id.play_pause_button);
        //Add clickListener.
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    //If player is playing, change text of the button and pause.
                    playPauseButton.setText(R.string.play_text);
                    player.pause();
                } else {
                    //If player is not playing, change text of the button and play.
                    playPauseButton.setText(R.string.pause_text);
                    player.play();
                }
            }
        });
    }

    /**
     * Just reset the play/pause button text to "Play".
     */
    private void resetPlayPauseButtonToPlayText() {
        playPauseButton.setText(R.string.play_text);
    }
}
