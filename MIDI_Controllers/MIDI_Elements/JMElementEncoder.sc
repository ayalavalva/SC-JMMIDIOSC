JMElementEncoder : JMMIDIElement {
    var <>cc;
    var <>ccValue;
    var <>velocityFactor = 10;

    *new { |controller, deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, deviceOSCpath, postMIDIOSC, cc|
        ^super.new.init(controller, deviceFullName, deviceShortName, deviceNumb, "Encoder", "EN", elementNumber, midiChannel, deviceOSCpath, postMIDIOSC).initEncoder(cc)
    }

    initEncoder { |cc|
        this.cc = cc;
        this.velocityFactor = velocityFactor;
        this.elementOSCpath = "/en" ++ this.elementNumber;
        this.label1OSCpath = this.elementOSCpath ++ "_lb1";
        this.label2OSCpath = this.elementOSCpath ++ "_lb2";
        
        this.midiReceiver = JMMIDI7bitReceiver(this);
        this.prReceiveMidiValue;

        super.receiveOSCValuetoControlBus; // also receive values from OSC, updates the control bus and allows to get the OSC value in patch code
    }

    // Handles the conversion of MIDI values to control bus values (bypasses the super method in JMMIDIElements)
    calculateCtrlBusValue {
        var incrementMidiValue;

        // Function to handle the cumulative logic for midiValue
        incrementMidiValue = { |ccValue|
            var midiValue = (ccValue - 64) * (this.velocityFactor / 1000);
            // Check if both 'this.lowNormMidiValue' and 'this.highValue' are non-nil
            if(this.controlBus.lowCtrlBusValue.notNil and: { this.controlBus.highCtrlBusValue.notNil }) {
                // If both are non-nil, apply clipping to ensure the value stays within the specified range
                this.controlBus.initCtrlBusValue = (this.controlBus.initCtrlBusValue + midiValue).clip(this.controlBus.lowCtrlBusValue, this.controlBus.highCtrlBusValue);
            } {
                // If either is nil, just add the 'midiValue' without clipping
                this.controlBus.initCtrlBusValue = this.controlBus.initCtrlBusValue + midiValue;
            };
            this.controlBus.initCtrlBusValue; // Return the updated cumulative value
        }; 

        if (this.controlBus.initCtrlBusValue.isNil)
        { this.controlBus.initCtrlBusValue = 0; this.controlBus.ctrlBusValue = incrementMidiValue.value(this.midiValue); }
        { this.controlBus.ctrlBusValue = incrementMidiValue.value(this.midiValue); }
    }

    // Sets the low, high, initial and velocity factor value of the element, sets the element control bus and sends OSC message with that initial value.
    setEncoderValues { |lowCtrlBusValue, initCtrlBusValue, highCtrlBusValue, velocityFactor = 10|
        this.controlBus.lowCtrlBusValue = lowCtrlBusValue;
        this.controlBus.initCtrlBusValue = initCtrlBusValue;
        this.controlBus.highCtrlBusValue = highCtrlBusValue;
        this.velocityFactor = velocityFactor;
        this.prSendInitCtrlBusValuetoOSC;
    }

    // Methods called by JMIntechControllers setElementValue method to send initial trigger value to OSC element and label
    prSendInitCtrlBusValuetoOSC {
        if (this.controlBus.initCtrlBusValue.notNil) {
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.elementOSCpath, this.controlBus.initCtrlBusValue); 
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label2OSCpath, (this.controlBus.initCtrlBusValue).asInteger);// Send the value to OSC label};
        };
    }

    prSendCtrlBusValuetoOSClabel2 {
        if(this.controlBus.lowCtrlBusValue.notNil and: { this.controlBus.highCtrlBusValue.notNil }) {
            // If both are non-nil, apply clipping to ensure the value stays within the specified range
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label2OSCpath, (this.controlBus.ctrlBusValue).asInteger);
        };
    }
}