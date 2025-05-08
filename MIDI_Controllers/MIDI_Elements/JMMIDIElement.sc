JMMIDIElement {
    var <>controller, <>deviceFullName, <>deviceShortName, <>deviceNumb, <>elementFullName, <>elementShortName, <>elementNumber, <>midiChannel, <>deviceOSCpath;
    var <>midiReceiver, <>controlBus;
    var <>midiValue; // MIDI value received from the device (7-bit from 0 to 127, or 14-bit from 0 to 16383)
    var <>lowNormMidiValue = 0, <>highNormMidiValue = 1; // Could be renamed to minControlBusValue and maxControlBusValue for more clarity
    var <>normMidiValue; // Normalized MIDI value (lo to 1.0)
    var <>initNormMidiValue = nil, <>initNormMidiTriggered = false;
    var <>busValue = 0;
    var <>elementOSCpath;
    var <>label1OSCpath;
    var <>label2OSCpath;
    var <>postMIDIOSC; // Flag to control whether to post MIDI and OSC messages to the post window

    var <>callbackFunc;

    *new { |controller, deviceFullName, deviceShortName, deviceNumb, elementFullName, elementShortName, elementNumber, midiChannel, deviceOSCpath, postMIDIOSC|
        ^super.new(controller, deviceFullName, deviceShortName, deviceNumb, elementFullName, elementShortName, elementNumber, midiChannel, deviceOSCpath, postMIDIOSC)
    }

    init { |controller, deviceFullName, deviceShortName, deviceNumb, elementFullName, elementShortName, elementNumber, midiChannel, deviceOSCpath, postMIDIOSC|
        this.controller = controller;
        this.deviceFullName = deviceFullName;
        this.deviceShortName = deviceShortName;
        this.deviceNumb = deviceNumb;
        this.elementFullName = elementFullName;
        this.elementShortName = elementShortName;
        this.elementNumber = elementNumber;
        this.midiChannel = midiChannel;
        this.deviceOSCpath = deviceOSCpath;
        this.postMIDIOSC = postMIDIOSC; // Set the flag to control whether to post MIDI and OSC messages to the post window

        this.midiValue = 0;
        this.controlBus = Bus.control(Server.default, 1);
    }

    updateMidiValue {
        this.midiReceiver.midiCallback = { |val|
            this.midiValue = val;
            this.processMidi;
        };
    }

    processMidi {
        this.normalizeMidiValue;
        this.calculateBusValue;
        this.controlBus.set(this.busValue);
        this.triggerCallback(this.busValue);
        if (this.postMIDIOSC) { this.postMIDIElementDetails; }; // calls method to post element details to the post window
        this.sendBusValuetoOSCElement;    // always fire OSC
        this.sendBusValuetoOSClabel2;
    }

    // Calculate normMidiValue based on the element type (7-bit or 14-bit)
    normalizeMidiValue {
        if (this.elementShortName == "BU") { this.normMidiValue = this.midiValue.linlin(0, 127, this.lowNormMidiValue, this.highNormMidiValue); } { this.normMidiValue = this.midiValue.linlin(0, 16383, this.lowNormMidiValue, this.highNormMidiValue) }; // Bitwise left shift by 7 positions of the MSB value (same as * 128) and add the LSB value to get the 14-bit MIDI value, then linearly map it to the control bus range
    }
    
    // Handles the conversion of MIDI values to control bus values for 14-bit MIDI elements
    calculateBusValue{
            if (this.initNormMidiValue.isNil) 
            {this.busValue = this.normMidiValue;} // If initNormMidiValue is not set, use normMidiValue directly
            {
                if ((this.normMidiValue - this.initNormMidiValue).abs < 0.01)
                // If normMidiValue is within the 1% range of initNormMidiValue
                {
                    if (this.initNormMidiTriggered.not) // If entering the 1% range for the first time, set the initNormMidiTriggered flag and use normMidiValue
                    {this.initNormMidiTriggered = true; this.busValue = this.normMidiValue;} // Set flag on first entry into the range
                    {this.busValue = this.normMidiValue;} // Keep this.busValue as the last normMidiValue within the range after the flag is set
                }
                // if normMidiValue is outside the 1% range of initNormMidiValue:
                {
                    if (this.initNormMidiTriggered.not) // If outside the 1% range and the initNormMidiTriggered flag has not been set, use initNormMidiValue
                    {this.busValue = this.initNormMidiValue;} 
                    {this.busValue = this.normMidiValue;} // If initNormMidiTriggered flag is set, continue using the last normMidiValue within the 1% range
                }
            };
    }

    postMIDIElementDetails {
        var commonDetails = (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + this.elementFullName + this.elementNumber + "MIDI Channel" + this.midiChannel);
        if (this.elementShortName == "BU" or: {this.elementShortName == "EN"}) 
        { (commonDetails + "CC" + this.cc ++ ":" + this.busValue).postln; }
        { (commonDetails + "msbCC" + this.msbCC + "lsbCC" + this.lsbCC ++ ":" + this.busValue).postln; };
    }

    sendBusValuetoOSCElement {
        JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.elementOSCpath, this.busValue); // Send busValue via OSC
    }

    sendBusValuetoOSClabel2 {
        "Must override".error;
    }

    // Patch method to send the value to OSC label
    sendtoOSClabel1 { |message|
        JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label1OSCpath, message);
    }

    // Called by the subclasses to update control bus values from OSC messages
    receiveOSCValuetoControlBus {
        OSCdef(("%%".format(if(this.deviceShortName == "PBF4") {this.deviceShortName.toLower ++ "_" ++ this.deviceNumb} {this.deviceShortName.toLower}, this.elementShortName.toLower) ++ this.elementNumber).asSymbol, { |msg|
            var oscValue = if(this.elementShortName == "BU") {msg[1].asInteger} {msg[1].asFloat}; // forces the value to be an integer for buttons
            this.controlBus.set(oscValue);
            this.triggerCallback(oscValue); // Calls a method that triggers the callback for the element to get the value in patch code ('controller' is a reference to the JMIntechControllers instance managing this element)
            if (this.postMIDIOSC) { this.postOSCElementDetails(oscValue); }; // Calls method to post OSC element details to the post window
        }, "/%/%".format(if(this.deviceShortName == "PBF4") {this.deviceShortName ++ "_" ++ this.deviceNumb} {this.deviceShortName}, this.elementShortName).toLower ++ this.elementNumber;);
    }

    postOSCElementDetails { |oscValue|
        (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + this.elementFullName + this.elementNumber + "OSC:" + oscValue).postln;
    }

    // Methods called by JMIntechControllers setElementValue method to set the initial value of the control bus
    setBusValueToInitNormMidiValue {
        if (this.initNormMidiValue.notNil) {
            this.controlBus.set(this.initNormMidiValue);
        }
    }

    // Methods called by JMIntechControllers setElementValue method to send initial trigger value to OSC element and label
    prSendInitNormMidiValuetoOSC {
        if (this.initNormMidiValue.notNil) {
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.elementOSCpath, this.initNormMidiValue); 
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label2OSCpath, (this.initNormMidiValue * 100).asInteger);// Send the value to OSC label};
        };
    }

    observeBusValue { |func|
        this.callbackFunc = func;
    }

    triggerCallback { |busValue|
        // this.controller.triggerCallback((this.elementShortName ++ this.elementNumber).asSymbol, busValue); // Trigger the callback for the element to get the value in patch code ('controller' is a reference to the JMIntechControllers instance managing this element)
        if (callbackFunc.notNil) { this.callbackFunc.value(busValue) };
    }

    // Sets initial value of the element, sets the element control bus and sends OSC message with that initial value.
    setElementValue { |initNormMidiValue|
        this.initNormMidiValue = initNormMidiValue;
        this.setBusValueToInitNormMidiValue;
        this.prSendInitNormMidiValuetoOSC;
    }

    cb {
        ^this.controlBus;
    }

    inCB {
        ^In.kr(this.controlBus, 1);
    }

    lagInCB { |lag = 0.3|
        ^Lag.kr(this.inCB, lag); 
    }
}