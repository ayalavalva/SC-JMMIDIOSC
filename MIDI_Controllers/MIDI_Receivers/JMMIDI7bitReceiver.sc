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
}