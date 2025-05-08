JMMIDI14bitReceiver : JMMIDIReceiver {
var <>msbCCValue = 0, <>lsbCCValue = 0; 
var <>midiCallback;

receiveMidiValue {
        MIDIdef.cc(
            "%_%%_msb".format(this.element.deviceShortName, this.element.elementShortName, this.element.elementNumber),
            { |val| 
                this.msbCCValue = val;
                this.createMidiCallback;
            },
            ccNum: this.element.msbCC, chan: this.element.midiChannel
        );
        MIDIdef.cc(
            "%_%%_lsb".format(this.element.deviceShortName, this.element.elementShortName, this.element.elementNumber),
            { |val| 
                this.lsbCCValue = val;
                this.createMidiCallback; 
            },
            ccNum: this.element.lsbCC, chan: this.element.midiChannel
        );
    }

    createMidiCallback {
        var midiValue = (this.msbCCValue << 7) + this.lsbCCValue;
        this.midiCallback.value(midiValue);  // notify listener
    }
}