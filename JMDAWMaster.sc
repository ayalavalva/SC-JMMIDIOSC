JMDAWMaster : JMDAW {
    var <>name;
    var <>fxGroup;
    var <>mixer2x2;
    var <>faderControlBus;

    *new { |name, fxGroup, mixer2x2, faderControlBus|
        ^super.new.init(name, fxGroup, mixer2x2, faderControlBus)
    }

    init { |name, fxGroup, mixer2x2, faderControlBus|
        this.name = name;
        this.fxGroup = fxGroup;
        this.mixer2x2 = mixer2x2;
        this.faderControlBus = faderControlBus;
    }

    sendAmplitudeToOsCvisualizer {
        OSCdef(\pbf41meter1, {|msg|
            var amplitude = msg[3].linlin(0,0.1, 0, 1);
            ~osc.send("/pbf4_1/meter_1", amplitude, debug: false); // not posting the sent OSC messages in the post window
        }, '/amplitude');
    }
}
