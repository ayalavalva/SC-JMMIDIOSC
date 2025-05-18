JMMIDIElement {
    var <>controller, <>deviceFullName, <>deviceShortName, <>deviceNumb, <>elementFullName, <>elementShortName, <>elementNumber, <>midiChannel, <>deviceOSCpath, <>postMIDIOSC;
    var <>midiReceiver;
    var <>midiValue; // MIDI value received from the device (7-bit from 0 to 127, or 14-bit from 0 to 16383)
    var <>controlBus; // Control bus to send the value to the device
    var <>elementOSCpath;
    var <>label1OSCpath;
    var <>label2OSCpath;

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
        this.controlBus = JMControlBus.new;
    }

    prReceiveMidiValue {
        this.midiReceiver.midiCallback = { |val|
            this.midiValue = val;
            this.prProcessMidi;
        };
    }

    prProcessMidi {
        this.controlBus.setCtrlBusValue(this.elementShortName, this.midiValue); // Normalize the MIDI value to the control bus range

        this.prTriggerCallback(this.controlBus.ctrlBusValue);

        if (this.postMIDIOSC) { this.midiReceiver.postMIDIElementDetails; }; // calls method to post element details to the post window
        this.prSendCtrlBusValuetoOSCElement;    // always fire OSC
        this.prSendCtrlBusValuetoOSClabel2;
    }

    prSendCtrlBusValuetoOSCElement {
        JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.elementOSCpath, this.controlBus.ctrlBusValue); // Send busValue via OSC
    }

    prSendCtrlBusValuetoOSClabel2 {
        "Must override".error;
    }

    // Patch public method to send the value to OSC label
    sendtoOSClabel1 { |message|
        JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label1OSCpath, message);
    }

    // Called by the subclasses to update control bus values from OSC messages
    receiveOSCValuetoControlBus {
        OSCdef(("%%".format(if(this.deviceShortName == "PBF4") {this.deviceShortName.toLower ++ "_" ++ this.deviceNumb} {this.deviceShortName.toLower}, this.elementShortName.toLower) ++ this.elementNumber).asSymbol, { |msg|
            var oscValue = if(this.elementShortName == "BU") {msg[1].asInteger} {msg[1].asFloat}; // forces the value to be an integer for buttons
            this.controlBus.controlBus.set(oscValue);
            this.prTriggerCallback(oscValue); // Calls a method that triggers the callback for the element to get the value in patch code ('controller' is a reference to the JMIntechControllers instance managing this element)
            if (this.postMIDIOSC) { this.prPostOSCElementDetails(oscValue); }; // Calls method to post OSC element details to the post window
        }, "/%/%".format(if(this.deviceShortName == "PBF4") {this.deviceShortName ++ "_" ++ this.deviceNumb} {this.deviceShortName}, this.elementShortName).toLower ++ this.elementNumber;);
    }

    prPostOSCElementDetails { |oscValue|
        (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + this.elementFullName + this.elementNumber + "OSC:" + oscValue).postln;
    }

    // Patch public method to set initial value of the element, the element control bus and send an OSC message with that initial value.
    setElementValue { |initCtrlBusValue|
        this.controlBus.initCtrlBusValue = initCtrlBusValue;
        if (this.controlBus.initCtrlBusValue.notNil) { this.controlBus.controlBus.set(initCtrlBusValue); };
        this.prSendInitCtrlBusValuetoOSC;
    }

    // Send initial trigger value to OSC element and label
    prSendInitCtrlBusValuetoOSC {
        if (this.controlBus.initCtrlBusValue.notNil) {
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.elementOSCpath, this.controlBus.initCtrlBusValue); 
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label2OSCpath, (this.controlBus.initCtrlBusValue * 100).asInteger);// Send the value to OSC label};
        };
    }

    // Patch public method to register a function observing the control bus value
    observeCtrlBusValue { |func|
        this.callbackFunc = func;
    }

    prTriggerCallback { |ctrlBusValue|
        // this.controller.triggerCallback((this.elementShortName ++ this.elementNumber).asSymbol, busValue); // Trigger the callback for the element to get the value in patch code ('controller' is a reference to the JMIntechControllers instance managing this element)
        if (this.callbackFunc.notNil) { this.callbackFunc.value(ctrlBusValue) };
    }

    cb {
        ^this.controlBus.controlBus.index;
    }

    inCB {
        ^In.kr(this.controlBus.controlBus.index, 1);
    }

    lagInCB { |lag = 0.3|
        ^Lag.kr(this.inCB, lag); 
    }
}