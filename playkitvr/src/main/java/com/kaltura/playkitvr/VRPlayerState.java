package com.kaltura.playkitvr;

/**
 * Created by anton.afanasiev on 30/08/2017.
 */

class VRPlayerState {

    static final int INVALID = -1;
    static final int IDLE = 0;
    static final int LOADING = 1;
    static final int PAUSE = 2;
    static final int BUFFERING = 3;
    static final int PLAYING = 4;
    static final int END_OF_FILE = 5;
    static final int CONNECTION_ERROR = 6;
    static final int STREAM_ERROR = 7;
}
