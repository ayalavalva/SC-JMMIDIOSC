JMDAWMasterTrack {
    var <>name;
    var <>group;
    var <>mixer2x2;
    var <>faderControlBus;

    *new { |faderControlBus|
        ^super.new.init(faderControlBus)
    }

    init { |faderControlBus|
        this.name = "Master";
        this.group = Group.tail;
        this.faderControlBus = faderControlBus;
        this.mixer2x2 = Synth(\mixer2x2, [in: 0, out: 0, controlBus: faderControlBus], addAction: \addToTail);
    }

    sendAmplitudeToOsCvisualizer {
        OSCdef(\pbf41meter1, {|msg|
            var amplitude = msg[3].linlin(0,0.1, 0, 1);
            ~osc.send("/pbf4_1/meter_1", amplitude, debug: false); // not posting the sent OSC messages in the post window
        }, '/amplitude');
    }
}