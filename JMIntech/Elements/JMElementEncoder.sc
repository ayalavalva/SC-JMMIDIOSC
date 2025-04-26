JMElementEncoder : JMMIDIElements {
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
        
        super.midi7bitReceiver;
        super.receiveOSCValuetoControlBus; // also receive values from OSC, updates the control bus and allows to get the OSC value in patch code
    }

    // Handles the conversion of MIDI values to control bus values (bypasses the super method in JMMIDIElements)
    midiValueToControlBusValue {
        var incrementMidiValue;

        // Function to handle the cumulative logic for midiValue
        incrementMidiValue = { |ccValue|
            var midiValue = (ccValue - 64) * (this.velocityFactor / 1000);
            // Check if both 'this.lowValue' and 'this.highValue' are non-nil
            if(this.lowValue.notNil and: { this.highValue.notNil }) {
                // If both are non-nil, apply clipping to ensure the value stays within the specified range
                this.initValue = (this.initValue + midiValue).clip(this.lowValue, this.highValue);
            } {
                // If either is nil, just add the 'midiValue' without clipping
                this.initValue = this.initValue + midiValue;
            };
            this.initValue; // Return the updated cumulative value
        }; 

        if (this.initValue.isNil)
        { this.initValue = 0; this.busValue = incrementMidiValue.value(this.ccValue); }
        { this.busValue = incrementMidiValue.value(this.ccValue); }
    }

    // Sets the low, high, initial and velocity factor value of the element, sets the element control bus and sends OSC message with that initial value.
    setEncoderValues { |lowValue, initValue, highValue, velocityFactor = 10|
        this.lowValue = lowValue;
        this.initValue = initValue;
        this.highValue = highValue;
        this.velocityFactor = velocityFactor;
        this.prSendInitValuetoOSC;
    }

    // Methods called by JMIntechControllers setElementValue method to send initial trigger value to OSC element and label
    prSendInitValuetoOSC {
        if (this.initValue.notNil) {
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.elementOSCpath, this.initValue); 
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label2OSCpath, (this.initValue).asInteger);// Send the value to OSC label};
        };
    }

    sendBusValuetoOSClabel2 {
        if(this.lowValue.notNil and: { this.highValue.notNil }) {
            // If both are non-nil, apply clipping to ensure the value stays within the specified range
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label2OSCpath, (this.busValue).asInteger);
        };
    }
}