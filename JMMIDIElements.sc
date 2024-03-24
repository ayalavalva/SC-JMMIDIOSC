JMMIDIElements {
    var <>controller, <>deviceFullName, <>deviceShortName, <>deviceNumb, <>elementFullName, <>elementShortName, <>elementNumber, <>midiChannel, <>deviceOSCpath;
    var <>controlBus;
    var <>lowValue = 0, <>highValue = 1;
    var <>initValue = nil, <>initTriggered = false;
    var <>busValue = 0;
    var <>oscSendEnabled = true; // I honestly don't see the point of keeping this flag (and the method in JMIntechControllers). All elements should always send their busValue to both the widget and the label2
    var <>elementOSCpath;
    var <>label1OSCpath;
    var <>label2OSCpath;

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
        this.controlBus = Bus.control(Server.default, 1);
    }

    midi7bitReceiver {
        MIDIdef.cc("%_%%".format(this.deviceFullName, this.elementShortName, this.elementNumber), { |val|
            this.ccValue = val;
            this.midiValuetoControlBus;
        }, ccNum: this.cc, chan: this.midiChannel);
    }

    midi14bitReceivers {
        MIDIdef.cc("%_%%_msb".format(this.deviceShortName, this.elementShortName, this.elementNumber), { |val|
            this.msbCCValue = val;
            this.midiValuetoControlBus;
        }, ccNum: this.msbCC, chan: this.midiChannel);

        MIDIdef.cc("%_%%_lsb".format(this.deviceShortName, this.elementShortName, this.elementNumber), { |val|
            this.lsbCCValue = val;
            this.midiValuetoControlBus;
        }, ccNum: this.lsbCC, chan: this.midiChannel);
    }

    midiValuetoControlBus {
        this.midiValueToControlBusValue; // calls superclass or subclass overriding methods to convert MIDI value to control bus value
        this.controlBus.set(this.busValue); // sets the control bus to busValue
        this.postMIDIElementDetails; // calls method to post element details to the post window
        this.triggerCallback(this.busValue); // Calls a method that triggers the callback for the element to get the value in patch code ('controller' is a reference to the JMIntechControllers instance managing this element)
        if (this.oscSendEnabled) { this.sendBusValuetoOSCElement; this.sendBusValuetoOSClabel2 }; // Sends the bus value to the OSC element and label2
    }

    // Handles the conversion of MIDI values to control bus values for 14-bit MIDI elements
    midiValueToControlBusValue {
        var midiValue = ((this.msbCCValue << 7) + this.lsbCCValue).linlin(0, 16383, this.lowValue, this.highValue);
            
            if (this.initValue.isNil) 
            {this.busValue = midiValue;} // If initValue is not set, use midiValue directly
            {
                if ((midiValue - this.initValue).abs < 0.01)
                // If midiValue is within the 1% range of initValue
                {
                    if (this.initTriggered.not) // If entering the 1% range for the first time, set the initTriggered flag and use midiValue
                    {this.initTriggered = true; this.busValue = midiValue;} // Set flag on first entry into the range
                    {this.busValue = midiValue;} // Keep this.busValue as the last midiValue within the range after the flag is set
                }
                // if midiValue is outside the 1% range of initValue:
                {
                    if (this.initTriggered.not) // If outside the 1% range and the initTriggered flag has not been set, use initValue
                    {this.busValue = this.initValue;} 
                    {this.busValue = midiValue;} // If initTriggered flag is set, continue using the last midiValue within the 1% range
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

    sendtoOSClabel1 { |message|
        JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label1OSCpath, message);
    }

    sendBusValuetoOSClabel2 {
        if (this.elementShortName != "BU") { JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label2OSCpath, (this.busValue * 100).asInteger); };
    }

    // Listens for OSC messages and sets the control bus value
    receiveOSCValuetoControlBus {
        OSCdef(("%%".format(if(this.deviceShortName == "PBF4") {this.deviceShortName.toLower ++ "_" ++ this.deviceNumb} {this.deviceShortName.toLower}, this.elementShortName.toLower) ++ this.elementNumber).asSymbol, { |msg|
            var oscValue = if(this.elementShortName == "BU") {msg[1].asInteger} {msg[1].asFloat}; // forces the value to be an integer for buttons
            this.controlBus.set(oscValue);
            this.triggerCallback(oscValue); // Calls a method that triggers the callback for the element to get the value in patch code ('controller' is a reference to the JMIntechControllers instance managing this element)
            this.postOSCElementDetails(oscValue);
        }, "/%/%".format(if(this.deviceShortName == "PBF4") {this.deviceShortName ++ "_" ++ this.deviceNumb} {this.deviceShortName}, this.elementShortName).toLower ++ this.elementNumber;);
    }

    postOSCElementDetails { |oscValue|
        (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + this.elementFullName + this.elementNumber + "OSC:" + oscValue).postln;
    }

    // Methods called by JMIntechControllers setElementValue method to set the initial value of the control bus
    setBusValueToInitValue {
        if (this.initValue.notNil) {
            this.controlBus.set(this.initValue);
        }
    }

    // Methods called by JMIntechControllers setElementValue method to send initial trigger value to OSC element and label
    sendInitValuetoOSC {
        if (this.initValue.notNil) {
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.elementOSCpath, this.initValue); 
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label2OSCpath, (this.initValue * 100).asInteger);// Send the value to OSC label};
        };
    }

    triggerCallback { |value|
        this.controller.triggerCallback((this.elementShortName ++ this.elementNumber).asSymbol, value); // Trigger the callback for the element to get the value in patch code ('controller' is a reference to the JMIntechControllers instance managing this element)
    }
}