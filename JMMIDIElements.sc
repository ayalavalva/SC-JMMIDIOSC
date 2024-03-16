JMMIDIElements {
    var <>controller, <>deviceFullName, <>deviceShortName, <>deviceNumb, <>elementFullName, <>elementShortName, <>elementNumber, <>midiChannel, <>deviceOSCpath;
    var <>lowValue = 0, <>highValue = 1;
    var <>triggerValue = nil, <>triggered = false;

    *new { |controller, deviceFullName, deviceShortName, deviceNumb, elementFullName, elementShortName, elementNumber, midiChannel, deviceOSCpath|
        ^super.new(controller, deviceFullName, deviceShortName, deviceNumb, elementFullName, elementShortName, elementNumber, midiChannel, deviceOSCpath)
    }

    init { |controller, deviceFullName, deviceShortName, deviceNumb, elementFullName, elementShortName, elementNumber, midiChannel, deviceOSCpath|
        this.controller = controller;
        this.deviceFullName = deviceFullName;
        this.deviceShortName = deviceShortName;
        this.deviceNumb = deviceNumb;
        this.elementFullName = elementFullName;
        this.elementShortName = elementShortName;
        this.elementNumber = elementNumber;
        this.midiChannel = midiChannel;
        this.deviceOSCpath = deviceOSCpath;
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

        switch(this.elementShortName) 
            { "EN" } {
                var incrementMidiValue;
                // Function to handle the cumulative logic for midiValue
                incrementMidiValue = { |ccValue|
                    var change = (ccValue - 64) * (this.velocityFactor / 1000);
                    this.cumulativeEncoderValue = (this.cumulativeEncoderValue + change).clip(this.lowValue, this.highValue); // Ensure the value stays within the specified range
                    // var addedValue = (ccValue - 64).sign * ((ccValue - 64).abs.pow(3)).asInteger;
                    // this.cumulativeEncoderValue = this.cumulativeEncoderValue + addedValue; // Cumulatively update the value
                    this.cumulativeEncoderValue; // Return the updated cumulative value
                };
                midiValue = incrementMidiValue.value(this.ccValue);
            }
            { "BU" } {midiValue = this.ccValue.linlin(0, 127, 0, 1)};
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
            var busValue;
            var midiValue = ((this.msbCCValue << 7) + this.lsbCCValue).linlin(0, 16383, this.lowValue, this.highValue);
            
            if (this.triggerValue.isNil) 
            {busValue = midiValue;} // If triggerValue is not set, use midiValue directly
            {
                // Check if midiValue is within the 1% range of triggerValue
                if ((midiValue - this.triggerValue).abs < 0.01) // Check if midiValue is within the 1% range of triggerValue
                {
                    if (this.triggered.not) // If entering the 1% range for the first time, set the triggered flag and use midiValue
                    {this.triggered = true; busValue = midiValue;} // Set flag on first entry into the range
                    {busValue = midiValue;} // Keep busValue as the last midiValue within the range after the flag is set
                } 
                {
                    if (this.triggered.not) // If outside the 1% range and the triggered flag has not been set, use triggerValue
                    {busValue = this.triggerValue;} 
                    {busValue = midiValue;} // If triggered flag is set, continue using the last midiValue within the 1% range
                }
            };

            this.controlBus.set(busValue);

            (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + this.elementFullName + this.elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + this.msbCC + "lsbCC" + this.lsbCC ++ ":" + busValue).postln;

            this.midiToOSCValue = busValue; // Update the midiToOSCValue with the new MIDI value

            this.controller.triggerCallback((this.elementShortName ++ this.elementNumber).asSymbol, midiValue); // Trigger the callback for the element to get the value in patch code

            this.msbUpdated = false; // Reset the updated flags
            this.lsbUpdated = false;

            this.sendMIDIValuetoOSC;
            
        }
    }

    sendMIDIValuetoOSC { // Send the OSC message only if oscSendEnabled is true
        if (this.oscSendEnabled) {
            
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.elementOSCpath, this.midiToOSCValue); // Send the value via OSC
            if (this.elementShortName != "BU") {
                JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label2OSCpath, (this.midiToOSCValue * 100).asInteger);
            }; // Send the value via OSC
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

    // Methods called by JMIntechControllers setTriggerValue method to send initial trigger value to OSC element and label
    sendTriggerValuetoOSC {
        if (this.triggerValue.notNil) {
            this.controlBus.set(triggerValue);
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.elementOSCpath, this.triggerValue); 
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label2OSCpath, (this.triggerValue * 100).asInteger);// Send the value to OSC label};
        };
    }

    sendOSCLabel { |message|
        JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label1OSCpath, message);
    }
}