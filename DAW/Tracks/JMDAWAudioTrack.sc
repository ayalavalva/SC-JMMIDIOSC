JMDAWAudioTrack {
    var <>name, <>number;
    var <>trackAudioBus, <>sendAudioBus;
    var <>group, <>synthGroup, <>fxGroup;
    var <>mixer2x2, <>send2x2;
    var <>faderControlBus, <>sendControlBus;

    *new { |number, trackAudioBus, sendAudioBus, faderControlBus, sendControlBus|
        ^super.new.init(number, trackAudioBus, sendAudioBus, faderControlBus, sendControlBus)
    }

    init { |number, trackAudioBus, sendAudioBus, faderControlBus, sendControlBus|
        this.name = "Track" ++ number.asString;
        this.number = number;
        this.trackAudioBus = trackAudioBus;
        this.sendAudioBus = sendAudioBus;
        this.group = Group.tail;
        this.synthGroup = Group.head(group);
        this.fxGroup = Group.after(synthGroup);
        this.faderControlBus = faderControlBus;
        this.sendControlBus = sendControlBus;
        this.mixer2x2 = Synth(\mixer2x2, [in: trackAudioBus, out: 0, pan: 0, controlBus: faderControlBus], target: group, addAction: \addToTail);
        this.send2x2 = Synth(\send2x2, [in: trackAudioBus, out: sendAudioBus, controlBus: sendControlBus], target: mixer2x2, addAction: \addAfter);
    }   
}