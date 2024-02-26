JMElementButton : JMMIDIElements {
    var <>cc;
    var <>ccValue;
    var <>controlBus;

    *new { |name, deviceNumb, elementNumber, midiChannel, cc|
        ^super.new.init(name, deviceNumb, elementNumber, midiChannel).initButton(cc)
    }

    initButton { |cc|
        this.cc = cc;
        this.controlBus = Bus.control(Server.default, 1); // Control bus is now directly accessible

        this.midiReceiver;
    }

    midiReceiver {
        MIDIdef.cc("%_But%".format(this.name, this.elementNumber), { |val|
            this.ccValue = val;
            this.mappedMIDIValuetoControlBus;
        }, ccNum: this.cc, chan: this.midiChannel);
    }

    mappedMIDIValuetoControlBus {
        var mappedValue = this.ccValue.linlin(0, 127, 0, 1);
        this.controlBus.set(mappedValue);
        (this.name ++ (if (this.name == "Intech Studio PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + "Button" + this.elementNumber + "MIDI Channel" + this.midiChannel + "CC" + this.cc ++ ":" + mappedValue).postln;
    }
}