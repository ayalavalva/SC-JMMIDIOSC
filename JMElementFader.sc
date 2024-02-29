JMElementFader : JMMIDIElements {
    var <>msbCC, <>lsbCC;
    var <>msbCCValue, <>lsbCCValue;
    var <>msbUpdated = false, <>lsbUpdated = false;
    var <>midiToOSCValue;
    var <>controlBus;
    var <>oscSendEnabled = false;

    *new { |deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, msbCC, lsbCC|
        ^super.new.init(deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel).initFader(msbCC, lsbCC)
    }

    initFader { |msbCC, lsbCC|
        this.msbCC = msbCC;
        this.lsbCC = lsbCC;
        this.controlBus = Bus.control(Server.default, 1);
        this.midi14bitReceivers;
    }

    midi14bitReceivers {
        MIDIdef.cc("%_FA%_msb".format(this.deviceFullName, this.elementNumber), { |val|
            this.msbCCValue = val;
            this.msbUpdated = true;
            this.midiValuetoControlBus;
        }, ccNum: this.msbCC, chan: this.midiChannel);

        MIDIdef.cc("%_FA%_lsb".format(this.deviceFullName, this.elementNumber), { |val|
            this.lsbCCValue = val;
            this.lsbUpdated = true;
            this.midiValuetoControlBus;
        }, ccNum: this.lsbCC, chan: this.midiChannel);
    }

    midiValuetoControlBus {
        
        if (this.msbUpdated && this.lsbUpdated) {
            var midiValue = ((this.msbCCValue << 7) + this.lsbCCValue).linlin(0, 16383, 0, 1);
            this.controlBus.set(midiValue);
            (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + "Fader" + this.elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + this.msbCC + "lsbCC" + this.lsbCC ++ ":" + midiValue).postln;
            this.midiToOSCValue = midiValue; // Update the midiToOSCValue with the new MIDI value
            
            this.msbUpdated = false;
            this.lsbUpdated = false;
            
            // Send the OSC message only if oscSendEnabled is true
            if (this.oscSendEnabled) {
                var oscPath = "/%/fa".format(this.deviceShortName.toLower) ++ this.elementNumber.asString; // Construct the OSC path based on the element number
                JMOSCManager.getSharedInstance.send(oscPath, this.midiToOSCValue); // Send the value via OSC
            }
        };
    }
}