JMMIDIElements {
    var <>controller, <>deviceFullName, <>deviceShortName, <>deviceNumb, <>elementFullName, <>elementShortName, <>elementNumber, <>midiChannel;

    *new { |controller, deviceFullName, deviceShortName, deviceNumb, elementFullName, elementShortName, elementNumber, midiChannel|
        ^super.new(controller, deviceFullName, deviceShortName, deviceNumb, elementFullName, elementShortName, elementNumber, midiChannel)
    }

    init { |controller, deviceFullName, deviceShortName, deviceNumb, elementFullName, elementShortName, elementNumber, midiChannel|
        this.controller = controller;
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

        this.receiveOSCValuetoControlBus; // also receive values from OSC, updates the control bus and allows to get the OSC value in patch code
    }

    midiValuetoControlBus7bit {
        var midiValue;
        switch(this.elementShortName) { "EN" } {midiValue = (this.ccValue - 64).sign * ((this.ccValue - 64).abs.pow(3)).asInteger} { "BU" } {midiValue = this.ccValue.linlin(0, 127, 0, 1)};
        this.controlBus.set(midiValue);
        (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + (switch(this.elementShortName) {"EN"} {"Encoder"} {"BU"} {"Button"}) + this.elementNumber + "MIDI Channel" + this.midiChannel + "CC" + this.cc ++ ":" + midiValue).postln;
        this.midiToOSCValue = midiValue; // Update the midiToOSCValue with the new MIDI value

        this.controller.triggerCallback((this.elementShortName ++ this.elementNumber).asSymbol, midiValue); // Trigger the callback for the element to get the value in patch code ('controller' is a reference to the JMIntechControllers instance managing this element)

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

        this.receiveOSCValuetoControlBus; // also receive values from OSC, updates the control bus and allows to get the OSC value in patch code
    }

    midiValuetoControlBus14bit {
        if (this.msbUpdated && this.lsbUpdated) {
            var midiValue = ((this.msbCCValue << 7) + this.lsbCCValue).linlin(0, 16383, 0, 1);
            this.controlBus.set(midiValue);
            (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + this.elementFullName + this.elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + this.msbCC + "lsbCC" + this.lsbCC ++ ":" + midiValue).postln;
            this.midiToOSCValue = midiValue; // Update the midiToOSCValue with the new MIDI value

            this.controller.triggerCallback((this.elementShortName ++ this.elementNumber).asSymbol, midiValue); // Trigger the callback for the element to get the value in patch code

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

    receiveOSCValuetoControlBus {
        OSCdef(("%%".format(if(this.deviceShortName == "PBF4") {this.deviceShortName.toLower ++ "_" ++ this.deviceNumb} {this.deviceShortName.toLower}, this.elementShortName.toLower) ++ this.elementNumber).asSymbol, { |msg|
            var oscValue = if(this.elementShortName == "BU") {msg[1].asInteger} {msg[1].asFloat}; // forces the value to be an integer for buttons
            this.controlBus.set(oscValue);
            this.controller.triggerCallback((this.elementShortName ++ this.elementNumber).asSymbol, oscValue); // Trigger the callback for the element to get the value in patch code
            (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + this.elementFullName + this.elementNumber + "OSC:" + oscValue).postln;
        }, "/%/%".format(if(this.deviceShortName == "PBF4") {this.deviceShortName.toLower ++ "_" ++ this.deviceNumb} {this.deviceShortName.toLower}, this.elementShortName.toLower) ++ this.elementNumber.asString;);
    }
}