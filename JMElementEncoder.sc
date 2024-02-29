JMElementEncoder : JMMIDIElements {
    var <>cc;
    var <>ccValue;
    var <>controlBus;
    var <>oscSendEnabled = false;

    *new { |deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, cc|
        ^super.new.init(deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel).initEncoder(cc)
    }

    initEncoder { |cc|
        this.cc = cc;
        this.controlBus = Bus.control(Server.default, 1);
        this.midiReceiver;
    }

    midiReceiver {
        MIDIdef.cc("%_EN%".format(this.deviceFullName, this.elementNumber), { |val|
            this.ccValue = val;
            this.midiValuetoControlBus;
        }, ccNum: this.cc, chan: this.midiChannel);
    }

    midiValuetoControlBus {
        var midiValue = (this.ccValue - 64).sign * ((this.ccValue - 64).abs.pow(3)).asInteger;
        this.controlBus.set(midiValue);
        (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + "Encoder" + this.elementNumber + "MIDI Channel" + this.midiChannel + "CC" + this.cc ++ ":" + midiValue).postln;
        this.midiToOSCValue = midiValue; // Update the midiToOSCValue with the new MIDI value

        // Send the OSC message only if oscSendEnabled is true
        if (this.oscSendEnabled) {
            var oscPath = "/%/en".format(this.deviceShortName.toLower) ++ this.elementNumber.asString; // Construct the OSC path based on the element number
            JMOSCManager.getSharedInstance.send(oscPath, this.midiToOSCValue); // Send the value via OSC
        }
    }
}