JMDAWTrackManager {
    var <>countMasterTrack = 1, <>countAudioTrack, <>countSendTrack;
    var <>midiControllers; // [TODO] this variable is maybe not needed in this class
    var <>faderControlBusArray, <>sendControlBusArray; // Array of control busses for faders and sends
    var <>tracksArray; // Array of all tracks
    var <>tracksDict; // Identity dictionary for all tracks

    *new { |countAudioTrack, countSendTrack, midiControllers|
        ^super.new.init(countAudioTrack, countSendTrack, midiControllers);
    }

    init { |countAudioTrack, countSendTrack, midiControllers|
        this.countAudioTrack = countAudioTrack;
        this.countSendTrack = countSendTrack;
        this.midiControllers = midiControllers;

        // store the control bus index, not its current value
        this.faderControlBusArray = [
            this.midiControllers.pbf41.fa1.cb,
            this.midiControllers.pbf41.fa2.cb,
            this.midiControllers.pbf41.fa3.cb,
            this.midiControllers.pbf41.fa4.cb,
            this.midiControllers.pbf42.fa1.cb,
            this.midiControllers.pbf42.fa2.cb,
            this.midiControllers.pbf42.fa3.cb,
            this.midiControllers.pbf42.fa4.cb
        ];

        // likewise for send buses
        this.sendControlBusArray = [
            this.midiControllers.pbf41.po1.cb,
            this.midiControllers.pbf41.po2.cb,
            this.midiControllers.pbf41.po3.cb,
            this.midiControllers.pbf41.po4.cb,
            this.midiControllers.pbf42.po1.cb,
            this.midiControllers.pbf42.po2.cb,
            this.midiControllers.pbf42.po3.cb,
            this.midiControllers.pbf42.po4.cb
        ];

        this.prAddMixer2x2; // Add the mixer SynthDef
        this.prAddSend2x2; // Add the send SynthDef

        this.tracksArray = Array.new;
        this.createAudioBusArrays; // Create audio busses for tracks and sends

        this.tracksDict = IdentityDictionary.new(n: countMasterTrack + countAudioTrack + countSendTrack);
        this.addTrackstoTracksDictionary;
    }

    prAddMixer2x2 {
        SynthDef(\mixer2x2, { |in, out, pan = 0, controlBus|
            var sig = In.ar(in, 2);
            var level = In.kr(controlBus, 1);
            sig = Balance2.ar(sig[0], sig[1], pan, level);
            // SendReply.kr(Impulse.kr(20), '/amplitude', Amplitude.ar(sig, attackTime: 0.01, releaseTime: 0.01)); // Send amplitude to OsC visualizer
            ReplaceOut.ar(in, sig);  // "maintains on own bus"
            Out.ar(out, sig);  // "copies to an output target"
        }).add;

        Server.local.sync; // Ensure SynthDefs are compiled before proceeding
    }
    
    prAddSend2x2 {
        SynthDef(\send2x2, { |in, out, controlBus|
            var sig = In.ar(in, 2);
            var level = In.kr(controlBus, 1);
            sig = sig * level;  // DAW sends do have a level control
            Out.ar(out, sig);
        }).add;

        Server.local.sync; // Ensure SynthDefs are compiled before proceeding
    }

    createAudioBusArrays {
        var trackAudioBusArray = Array.fill(this.countAudioTrack, { Bus.audio(Server.local, 2) });
        var sendAudioBusArray = Array.fill(this.countSendTrack, { Bus.audio(Server.local, 2) });

        this.addAudioTracksToArray(trackAudioBusArray, sendAudioBusArray); // [TODO] Problem when more than 1 send track !!!!!
        this.addSendTracksToArray(sendAudioBusArray);
        this.addMasterTrackToArray;
    }

    addAudioTracksToArray { |trackAudioBusArray, sendAudioBusArray|
        this.countAudioTrack.do { |i| 
            var number = i + 1;
            var trackAudioBus = trackAudioBusArray[i];
            var sendAudioBus = sendAudioBusArray[i];
            var faderControlBus = this.faderControlBusArray[this.countMasterTrack + i];
            var sendControlBus = this.sendControlBusArray[this.countMasterTrack + i];
            var audioTrack = JMDAWAudioTrack.new(number: number, trackAudioBus: trackAudioBus, sendAudioBus: sendAudioBus, faderControlBus: faderControlBus, sendControlBus: sendControlBus);
            this.tracksArray = this.tracksArray.add(audioTrack); // Read https://doc.sccode.org/Classes/Array.html .add(item)
        };
    }

    // Add send tracks
    addSendTracksToArray { |sendAudioBusArray|
        this.countSendTrack.do { |i| 
            var number = i + 1;
            var sendAudioBus = sendAudioBusArray[i];
            var faderControlBus = this.faderControlBusArray[this.countMasterTrack + this.countAudioTrack + i];
            var sendTrack = JMDAWSendTrack.new(number: number, sendAudioBus: sendAudioBus, faderControlBus: faderControlBus);
            this.tracksArray = this.tracksArray.add(sendTrack);  // Read https://doc.sccode.org/Classes/Array.html .add(item)
        };
    }

    // Add master tracks to the existing array
    addMasterTrackToArray {
        this.countMasterTrack.do { |i| 
            var faderControlBus = this.faderControlBusArray[i];
            var masterTrack = JMDAWMasterTrack.new(faderControlBus: faderControlBus);
            this.tracksArray = this.tracksArray.add(masterTrack); // Read https://doc.sccode.org/Classes/Array.html .add(item)
        };
    }
    
    addTrackstoTracksDictionary {
        this.tracksArray.do { |track|
            var trackKey = track.name.toLower.asSymbol;
            this.tracksDict.put(trackKey, track);
        };
    }
}