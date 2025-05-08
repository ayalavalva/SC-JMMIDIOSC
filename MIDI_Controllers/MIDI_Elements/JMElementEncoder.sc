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
        this.updateMidiValue;

        super.receiveOSCValuetoControlBus; // also receive values from OSC, updates the control bus and allows to get the OSC value in patch code
    }

    // Handles the conversion of MIDI values to control bus values (bypasses the super method in JMMIDIElements)
    calculateBusValue {
        var incrementMidiValue;

        // Function to handle the cumulative logic for midiValue
        incrementMidiValue = { |ccValue|
            var midiValue = (ccValue - 64) * (this.velocityFactor / 1000);
            // Check if both 'this.lowNormMidiValue' and 'this.highValue' are non-nil
            if(this.lowNormMidiValue.notNil and: { this.highNormMidiValue.notNil }) {
                // If both are non-nil, apply clipping to ensure the value stays within the specified range
                this.initNormMidiValue = (this.initNormMidiValue + midiValue).clip(this.lowNormMidiValue, this.highNormMidiValue);
            } {
                // If either is nil, just add the 'midiValue' without clipping
                this.initNormMidiValue = this.initNormMidiValue + midiValue;
            };
            this.initNormMidiValue; // Return the updated cumulative value
        }; 

        if (this.initNormMidiValue.isNil)
        { this.initNormMidiValue = 0; this.busValue = incrementMidiValue.value(this.midiValue); }
        { this.busValue = incrementMidiValue.value(this.midiValue); }
    }

    // Sets the low, high, initial and velocity factor value of the element, sets the element control bus and sends OSC message with that initial value.
    setEncoderValues { |lowNormMidiValue, initNormMidiValue, highNormMidiValue, velocityFactor = 10|
        this.lowNormMidiValue = lowNormMidiValue;
        this.initNormMidiValue = initNormMidiValue;
        this.highNormMidiValue = highNormMidiValue;
        this.velocityFactor = velocityFactor;
        this.prSendInitNormMidiValuetoOSC;
    }

    // Methods called by JMIntechControllers setElementValue method to send initial trigger value to OSC element and label
    prSendInitNormMidiValuetoOSC {
        if (this.initNormMidiValue.notNil) {
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.elementOSCpath, this.initNormMidiValue); 
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label2OSCpath, (this.initNormMidiValue).asInteger);// Send the value to OSC label};
        };
    }

    sendBusValuetoOSClabel2 {
        if(this.lowNormMidiValue.notNil and: { this.highNormMidiValue.notNil }) {
            // If both are non-nil, apply clipping to ensure the value stays within the specified range
            JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label2OSCpath, (this.busValue).asInteger);
        };
    }
}