JMElementFader : JMMIDIElements {
    var <>msbCC, <>lsbCC;
    var <>msbCCValue, <>lsbCCValue;
    var <>msbUpdated = false, <>lsbUpdated = false;
    var <>controlBus;

    *new { |name, deviceNumb, elementNumber, midiChannel, msbCC, lsbCC|
        ^super.new.init(name, deviceNumb, elementNumber, midiChannel).initFader(msbCC, lsbCC)
    }

    initFader { |msbCC, lsbCC|
        this.msbCC = msbCC;
        this.lsbCC = lsbCC;
        this.controlBus = Bus.control(Server.default, 1);

        this.midi14bitReceivers;
    }

    midi14bitReceivers {
        MIDIdef.cc("%_Fad%_msb".format(this.name, this.elementNumber), { |val|
            this.msbCCValue = val;
            this.msbUpdated = true;
            this.mappedCombinedMIDIValuetoControlBus;
        }, ccNum: this.msbCC, chan: this.midiChannel);

        MIDIdef.cc("%_Fad%_lsb".format(this.name, this.elementNumber), { |val|
            this.lsbCCValue = val;
            this.lsbUpdated = true;
            this.mappedCombinedMIDIValuetoControlBus;
        }, ccNum: this.lsbCC, chan: this.midiChannel);
    }

    mappedCombinedMIDIValuetoControlBus {
        if (this.msbUpdated && this.lsbUpdated) {
            var combinedMappedValue = ((this.msbCCValue << 7) + this.lsbCCValue).linlin(0, 16383, 0, 1);
            this.controlBus.set(combinedMappedValue);
            (this.name ++ (if (this.name == "Intech Studio PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + "Fader" + this.elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + this.msbCC + "lsbCC" + this.lsbCC ++ ":" + combinedMappedValue).postln;
            // ~sendToTouchOSC.value("/po16/po" ++ (elementNumber).asString, combinedValue.linlin(0, 16383, 0, 1));
            this.msbUpdated = false;
            this.lsbUpdated = false;
        };
    }
}