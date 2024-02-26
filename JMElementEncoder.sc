JMElementEncoder : JMMIDIElements {
    var <>cc;
    var <>ccValue;
    var <>controlBus;

    *new { |name, deviceNumb, elementNumber, midiChannel, cc|
        ^super.new.init(name, deviceNumb, elementNumber, midiChannel).initEncoder(cc)
    }

    initEncoder { |cc|
        this.cc = cc;
        this.controlBus = Bus.control(Server.default, 1);
        this.midiReceiver;
    }

    midiReceiver {
        MIDIdef.cc("%_Enc%".format(this.name, this.elementNumber), { |val|
            this.ccValue = val;
            this.mappedMIDIValuetoControlBus;
        }, ccNum: this.cc, chan: this.midiChannel);
    }

    mappedMIDIValuetoControlBus {
        var mappedValue = (this.ccValue - 64).sign * ((this.ccValue - 64).abs.pow(3)).asInteger;
        this.controlBus.set(Lag.kr(mappedValue, 0.01));
        (this.name ++ (if (this.name == "Intech Studio PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + "Encoder" + this.elementNumber + "MIDI Channel" + this.midiChannel + "CC" + this.cc ++ ":" + mappedValue).postln;
    }
}