JMElementButton : JMMIDIElements {
    var <>cc;
    var <>ccValue;
    var <>controlBus;
    var <>oscSendEnabled = false;

    *new { |deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, cc|
        ^super.new.init(deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel).initButton(cc)
    }

    initButton { |cc|
        this.cc = cc;
        this.controlBus = Bus.control(Server.default, 1); // Control bus is now directly accessible

        this.midiReceiver;
    }

    midiReceiver {
        MIDIdef.cc("%_But%".format(this.deviceFullName, this.elementNumber), { |val|
            this.ccValue = val;
            this.midiValuetoControlBus;
        }, ccNum: this.cc, chan: this.midiChannel);
    }

    midiValuetoControlBus {
        var midiValue = this.ccValue.linlin(0, 127, 0, 1);
        this.controlBus.set(midiValue);
        (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + "Button" + this.elementNumber + "MIDI Channel" + this.midiChannel + "CC" + this.cc ++ ":" + midiValue).postln;

        if (this.oscSendEnabled) {
            var oscPath = "/%/bu".format(this.deviceShortName.toLower) ++ this.elementNumber.asString; // Construct the OSC path based on the element number
            JMOSCManager.getSharedInstance.send(oscPath, this.midiToOSCValue); // Send the value via OSC
        }
    }
}