JMMIDIElements {
    var <>deviceFullName, <>deviceShortName, <>deviceNumb, <>elementFullName, <>elementShortName, <>elementNumber, <>midiChannel;

    *new { |deviceFullName, deviceShortName, deviceNumb, elementFullName, elementShortName, elementNumber, midiChannel|
        ^super.new(deviceFullName, deviceShortName, deviceNumb, elementFullName, elementShortName, elementNumber, midiChannel)
    }

    init { |deviceFullName, deviceShortName, deviceNumb, elementFullName, elementShortName, elementNumber, midiChannel|
        this.deviceFullName = deviceFullName;
        this.deviceShortName = deviceShortName;
        this.deviceNumb = deviceNumb;
        this.elementFullName = elementFullName;
        this.elementShortName = elementShortName;
        this.elementNumber = elementNumber;
        this.midiChannel = midiChannel;
    }

    midi7bitReceiver {
        MIDIdef.cc("%_%%".format(this.deviceFullName, this.elementShortName, this.elementNumber), { |val|
            this.ccValue = val;
            this.midiValuetoControlBus7bit;
        }, ccNum: this.cc, chan: this.midiChannel);
    }

    midiValuetoControlBus7bit {
        var midiValue;
        switch(this.elementShortName) { "EN" } {midiValue = (this.ccValue - 64).sign * ((this.ccValue - 64).abs.pow(3)).asInteger} { "BU" } {midiValue = this.ccValue.linlin(0, 127, 0, 1)};
        this.controlBus.set(midiValue);
        (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + "Encoder" + this.elementNumber + "MIDI Channel" + this.midiChannel + "CC" + this.cc ++ ":" + midiValue).postln;
        this.midiToOSCValue = midiValue; // Update the midiToOSCValue with the new MIDI value

        this.sendMIDIValuetoOSC;
    }

    midi14bitReceivers {
        MIDIdef.cc("%_%%_msb".format(this.deviceShortName, this.elementShortName, this.elementNumber), { |val|
            this.msbCCValue = val;
            this.msbUpdated = true;
            this.midiValuetoControlBus14bit;
        }, ccNum: this.msbCC, chan: this.midiChannel);

        MIDIdef.cc("%_%%_lsb".format(this.deviceShortName, this.elementShortName, this.elementNumber), { |val|
            this.lsbCCValue = val;
            this.lsbUpdated = true;
            this.midiValuetoControlBus14bit;
        }, ccNum: this.lsbCC, chan: this.midiChannel);
    }

    midiValuetoControlBus14bit {
        if (this.msbUpdated && this.lsbUpdated) {
            var midiValue = ((this.msbCCValue << 7) + this.lsbCCValue).linlin(0, 16383, 0, 1);
            this.controlBus.set(midiValue);
            (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + this.elementFullName + this.elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + this.msbCC + "lsbCC" + this.lsbCC ++ ":" + midiValue).postln;
            this.midiToOSCValue = midiValue; // Update the midiToOSCValue with the new MIDI value

            this.msbUpdated = false; // Reset the updated flags
            this.lsbUpdated = false;

            this.sendMIDIValuetoOSC;
        }
    }

    sendMIDIValuetoOSC { // Send the OSC message only if oscSendEnabled is true
        if (this.oscSendEnabled) {
            var oscPath = "/%/%".format(if(this.deviceShortName == "PBF4") {this.deviceShortName.toLower ++ "_" ++ this.deviceNumb} {this.deviceShortName.toLower}, this.elementShortName.toLower) ++ this.elementNumber.asString; // Construct the OSC path
            JMOSCManager.getSharedInstance.send(oscPath, this.midiToOSCValue); // Send the value via OSC
        }
    }
}