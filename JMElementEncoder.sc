JMElementEncoder : JMMIDIElements {
    var <>cc;
    var <>ccValue;
    var <>velocityFactor = 10;

    *new { |controller, deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, deviceOSCpath, cc|
        ^super.new.init(controller, deviceFullName, deviceShortName, deviceNumb, "Encoder", "EN", elementNumber, midiChannel, deviceOSCpath).initEncoder(cc)
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
            this.initValue = (this.initValue + midiValue).clip(this.lowValue, this.highValue); // Ensure the value stays within the specified range
            this.initValue; // Return the updated cumulative value
        }; 

        if (this.initValue.isNil)
        { this.initValue = 0; this.busValue = incrementMidiValue.value(this.ccValue); }
        { this.busValue = incrementMidiValue.value(this.ccValue); }
    }
}