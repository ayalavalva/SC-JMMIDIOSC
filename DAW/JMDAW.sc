JMDAW {
    var <>countAudioTrack, <>countSendTrack, <>postMIDIOSC;
    var <>midiControllers;
    var <>po16, <>en16, <>pbf41, <>pbf42;
    var <>tracks;

    *new { |countAudioTrack = 6, countSendTrack = 1, postMIDIOSC = false|
        if (countAudioTrack + countSendTrack > 7) 
        { "Too many channels and sends. Maximum is 7.".error; }
        { ^super.new.init(countAudioTrack, countSendTrack, postMIDIOSC); }
    }

    init { |countAudioTrack, countSendTrack, postMIDIOSC|
        this.countAudioTrack = countAudioTrack;
        this.countSendTrack = countSendTrack;
        this.postMIDIOSC = postMIDIOSC;

        this.midiControllers = JMDAWMIDIControllers.new(postMIDIOSC: this.postMIDIOSC);
        this.po16 = this.midiControllers.po16;
        this.en16 = this.midiControllers.en16;
        this.pbf41 = this.midiControllers.pbf41;
        this.pbf42 = this.midiControllers.pbf42;

        this.tracks = JMDAWTrackManager.new(countAudioTrack: countAudioTrack, countSendTrack: countSendTrack, midiControllers: this.midiControllers);
    }

    // Method to get a track audioBus by trackKey
    bus { |trackKey|
        ^this.tracks.tracksDict[trackKey].trackAudioBus;
    }

    // Method to get a track synthGroup by trackKey
    synthGroup { |trackKey|
        ^this.tracks.tracksDict[trackKey].synthGroup;
    }

    // Method to get a track fxGroup by trackKey
    fxGroup { |trackKey|
        ^this.tracks.tracksDict[trackKey].fxGroup;
    }

    // mehtod to get a faderControlBus by trackKey
    faderControlBus { |trackKey|
        ^this.tracks.tracksDict[trackKey].faderControlBus;
    }

    // Method to get a sendControlBus by trackKey
    sendControlBus { |trackKey|
        ^this.tracks.tracksDict[trackKey].sendControlBus;
    }
}