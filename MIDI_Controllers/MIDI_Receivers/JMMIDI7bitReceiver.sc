JMMIDI7bitReceiver : JMMIDIReceiver {
    var <>ccValue; 
    var <>midiCallback;
    
    receiveMidiValue {
        MIDIdef.cc(
            "%_%%".format(this.element.deviceFullName, this.element.elementShortName, this.element.elementNumber),
            { |val| 
                this.ccValue = val;
                this.createMidiCallback;
            },
            ccNum: this.element.cc, chan: this.element.midiChannel
        );
    }

    createMidiCallback {
        this.midiCallback.value(this.ccValue);  // notify listener
    }

    postMIDIElementDetails {
        var commonDetails = (this.element.deviceFullName ++ (if (this.element.deviceShortName == "PBF4") {" (" ++ this.element.deviceNumb ++ ")"} {""}) + this.element.elementFullName + this.element.elementNumber + "MIDI Channel" + this.element.midiChannel);

        (commonDetails + "CC" + this.element.cc ++ ":" + this.element.controlBus.ctrlBusValue).postln;
    }
}