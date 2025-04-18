JMDAW {
    var <>numMaster = 1, <>numTracks, <>numSends;
    var <>faderControlBusses, <>sendControlBusses;
    var <>channels;
    var <>po16, <>en16, <>pbf41, <>pbf42;
    var <>postMIDIOSC = false;

    *new { |numTracks = 6, numSends = 1, postMIDIOSC|
        if (numTracks + numSends > 7) 
        { "Too many channels and sends. Maximum is 7.".error; }
        { ^super.new.init(numTracks, numSends, postMIDIOSC); }
    }

    init { |numTracks, numSends, postMIDIOSC|
        this.numTracks = numTracks;
        this.numSends = numSends;

        this.po16 = JMIntechPO16(midiChannel: 0, startCC: 0, oscServAddr: "127.0.0.1", oscServPort: 9000, postMIDIOSC: this.postMIDIOSC);
	    this.en16 = JMIntechEN16(midiChannel: 0, startCC: 32, oscServAddr: "127.0.0.1", oscServPort: 9000, postMIDIOSC: this.postMIDIOSC);
	    this.pbf41 = JMIntechPBF4(midiChannel: 0, startCC: 64, oscServAddr: "127.0.0.1", oscServPort: 9000, postMIDIOSC: this.postMIDIOSC);
	    this.pbf42 = JMIntechPBF4(midiChannel: 0, startCC: 88, oscServAddr: "127.0.0.1", oscServPort: 9000, postMIDIOSC: this.postMIDIOSC);

        this.faderControlBusses = [this.pbf41.cb(\FA1), this.pbf41.cb(\FA2), this.pbf41.cb(\FA3), this.pbf41.cb(\FA4), this.pbf42.cb(\FA1), this.pbf42.cb(\FA2), this.pbf42.cb(\FA3), this.pbf42.cb(\FA4)];
        this.sendControlBusses = [this.pbf41.cb(\PO1), this.pbf41.cb(\PO2),this.pbf41.cb(\PO3), this.pbf41.cb(\PO4), 
        this.pbf42.cb(\PO1), this.pbf42.cb(\PO2), this.pbf42.cb(\PO3),this.pbf42.cb(\PO4)];
        
        this.channels = IdentityDictionary.new(n: numMaster + numTracks + numSends);

        this.defineMixer2x2;
        this.defineSend2x2;
        // this.sendAmplitudeToOsCvisualizer;
        Server.local.sync; // Ensure SynthDefs are compiled before proceeding
        this.createAudioBusses;
    }

    defineMixer2x2 {
        SynthDef(\mixer2x2, { |in, out, pan = 0, controlBus|
            var sig = In.ar(in, 2);
            var level = In.kr(controlBus, 1);
            sig = Balance2.ar(sig[0], sig[1], pan, level);
            // SendReply.kr(Impulse.kr(20), '/amplitude', Amplitude.ar(sig, attackTime: 0.01, releaseTime: 0.01)); // Send amplitude to OsC visualizer
            ReplaceOut.ar(in, sig);  // "maintains on own bus"
            Out.ar(out, sig);  // "copies to an output target"
        }).add;
    }
    
    defineSend2x2 {
        SynthDef(\send2x2, { |in, out, controlBus|
            var sig = In.ar(in, 2);
            var level = In.kr(controlBus, 1);
            sig = sig * level;  // DAW sends do have a level control
            Out.ar(out, sig);
        }).add;
    }

    createAudioBusses {
        var trackAudioBusses = Array.fill(this.numTracks, { Bus.audio(Server.local, 2) });
        var sendAudioBusses = Array.fill(this.numSends, { Bus.audio(Server.local, 2) });
        this.createTracks(trackAudioBusses, sendAudioBusses);
        this.createSends(sendAudioBusses);
        this.createMaster;
    }

    createTracks { |trackAudioBusses, sendAudioBusses|
        // var lastGroup;
        numTracks.do { |i|
            var channelKey = ('track' ++ (i + 1)).asSymbol;
            var name = "Track" + (i + 1);
            var number = i + 1;
            var trackAudioBus = trackAudioBusses[i];
            var sendAudioBus = sendAudioBusses[i];
            // var group = if(i == 0, { Group.new }, { Group.after(lastGroup) });
            var group = Group.tail;
            var synthGroup = Group.head(group);
            var fxGroup = Group.after(synthGroup);
            var faderControlBus = this.faderControlBusses[this.numMaster + i];
            var sendControlBus = this.sendControlBusses[this.numMaster + i];
            var mixer2x2 = Synth(\mixer2x2, [in: trackAudioBus, out: 0, pan: 0, controlBus: faderControlBus], target: group, addAction: \addToTail);
            var send2x2 = Synth(\send2x2, [in: trackAudioBus, out: sendAudioBus, controlBus: sendControlBus], target: mixer2x2, addAction: \addAfter);
            var channel = JMDAWTrack.new(name: name, number: number, audioBus: trackAudioBus, group: group, synthGroup: synthGroup, fxGroup: fxGroup, mixer2x2: mixer2x2, send2x2: send2x2, faderControlBus: faderControlBus, sendControlBus: sendControlBus);
            
            this.channels.put(channelKey, channel);
            
            // lastGroup = group; // Update lastGroup to the current group for the next iteration
        };
    }

    createSends { |sendAudioBusses|
        numSends.do { |i|
            var channelKey = ('send' ++ (i + 1)).asSymbol;
            var name = "Send" + (i + 1);
            var number = i + 1;
            var sendAudioBus = sendAudioBusses[i];
            var group = Group.tail;
            var fxGroup = Group.head(group);
            var faderControlBus = this.faderControlBusses[this.faderControlBusses.size - this.numSends + i];
            var mixer2x2 = Synth(\mixer2x2, [in: sendAudioBus, out: 0, pan: 0, controlBus: faderControlBus], target: group, addAction: \addToTail);
            var channel = JMDAWSend.new(name: name, number: number, audioBus: sendAudioBus, group: group, fxGroup: fxGroup, mixer2x2: mixer2x2, faderControlBus: faderControlBus);
            
            this.channels.put(channelKey, channel)
        };
    }

    createMaster {
        var channelKey = 'master';
        var name = "master";
        var fxGroup = Group.tail;
        var faderControlBus = this.faderControlBusses[0];
        var mixer2x2 = Synth(\mixer2x2, [in: 0, out: 0, controlBus: faderControlBus], addAction: \addToTail);
        var channel = JMDAWMaster.new(name: name, fxGroup: fxGroup, mixer2x2: mixer2x2, faderControlBus: faderControlBus);
        
        this.channels.put(channelKey, channel);
    }

    // Method to get a channel audioBus by channelKey
    bus { |channelKey|
        ^this.channels[channelKey].audioBus;
    }

    // Method to get a channel synthGroup by channelKey
    synthGroup { |channelKey|
        ^this.channels[channelKey].synthGroup;
    }

    // Method to get a channel fxGroup by channelKey
    fxGroup { |channelKey|
        ^this.channels[channelKey].fxGroup;
    }

    // mehtod to get a faderControlBus by channelKey
    faderControlBus { |channelKey|
        ^this.channels[channelKey].faderControlBus;
    }

    // Method to get a sendControlBus by channelKey
    sendControlBus { |channelKey|
        ^this.channels[channelKey].sendControlBus;
    }   
}