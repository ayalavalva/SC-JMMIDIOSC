JMDAWSendTrack {
    var <>name, <>number;
    var <>trackAudioBus;
    var <>group, <>fxGroup;
    var <>mixer2x2;
    var <>faderControlBus;

    *new { |number, sendAudioBus, faderControlBus|
        ^super.new.init(number, sendAudioBus, faderControlBus)
    }

    init { |number, sendAudioBus, faderControlBus|
        this.name = "Send" ++ number.asString;
        this.number = number;
        this.trackAudioBus = sendAudioBus;
        this.group = Group.tail;
        this.fxGroup = Group.head(group);
        this.faderControlBus = faderControlBus;
        this.mixer2x2 = Synth(\mixer2x2, [in: sendAudioBus, out: 0, pan: 0, controlBus: faderControlBus], target: group, addAction: \addToTail);
    }
}