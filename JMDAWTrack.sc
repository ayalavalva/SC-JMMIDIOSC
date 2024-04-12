JMDAWTrack : JMDAW {
    var <>name, <>number;
    var <>audioBus;
    var <>group, <>synthGroup, <>fxGroup;
    var <>mixer2x2, <>send2x2;
    var <>faderControlBus, <>sendControlBus;

    *new { |name, number, audioBus, group, synthGroup, fxGroup, mixer2x2, send2x2, faderControlBus, sendControlBus|
        ^super.new.init(name, number, audioBus, group, synthGroup, fxGroup, mixer2x2, send2x2, faderControlBus, sendControlBus)
    }

    init { |name, number, audioBus, group, synthGroup, fxGroup, mixer2x2, send2x2, faderControlBus, sendControlBus|
        this.name = name;
        this.number = number;
        this.audioBus = audioBus;
        this.group = group;
        this.synthGroup = synthGroup;
        this.fxGroup = fxGroup;
        this.mixer2x2 = mixer2x2;
        this.send2x2 = send2x2;
        this.faderControlBus = faderControlBus;
        this.sendControlBus = sendControlBus;
    }

}