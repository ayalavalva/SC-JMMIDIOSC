JMControlBus {
    var <>controlBus;
    var <>lowCtrlBusValue = 0, <>highCtrlBusValue = 1;
    var <>initCtrlBusValue = nil, <>initCtrlBusTriggered = false;
    var <>velocityFactor = 10;
    var <>ctrlBusValue = 0;

    *new {
        ^super.new.init;
    }
    
    init {
        this.controlBus = Bus.control(Server.default, 1);
        this.lowCtrlBusValue = lowCtrlBusValue;
        this.highCtrlBusValue = highCtrlBusValue;
        this.initCtrlBusValue = initCtrlBusValue;
        this.initCtrlBusTriggered = initCtrlBusTriggered;
        this.velocityFactor = velocityFactor;
        this.ctrlBusValue = ctrlBusValue;
    }

    setCtrlBusValue { |elementShortName, midiValue|
        switch (elementShortName) 
            {"PO"} { this.prSetCtrlValuePotentiometerFader(midiValue); }
            {"EN"} { this.prSetCtrlValueEncoder(midiValue); }
            {"FA"} { this.prSetCtrlValuePotentiometerFader(midiValue); }
            {"BU"} { this.prSetCtrlValueButton(midiValue); };
    }

    prSetCtrlValuePotentiometerFader { |midiValue|
        var normMidiValue = midiValue.linlin(0, 16383, this.lowCtrlBusValue, this.highCtrlBusValue); // Bitwise left shift by 7 positions of the MSB value (same as * 128) and add the LSB value to get the 14-bit MIDI value, then linearly map it to the control bus range

        if (this.initCtrlBusValue.isNil) 
        {this.ctrlBusValue = normMidiValue;} // If initCtrlBusValue is not set, use normMidiValue directly
        {
            if ((normMidiValue - this.initCtrlBusValue).abs < 0.01)
            // If normMidiValue is within the 1% range of initCtrlBusValue
            {
                if (this.initCtrlBusTriggered.not) // If entering the 1% range for the first time, set the initCtrlBusTriggered flag and use normMidiValue
                {this.initCtrlBusTriggered = true; this.ctrlBusValue = normMidiValue;} // Set flag on first entry into the range
                {this.ctrlBusValue = normMidiValue;} // Keep this.ctrlBusValue as the last normMidiValue within the range after the flag is set
            }
            // if normMidiValue is outside the 1% range of initCtrlBusValue:
            {
                if (this.initCtrlBusTriggered.not) // If outside the 1% range and the initCtrlBusTriggered flag has not been set, use initCtrlBusValue
                {this.ctrlBusValue = this.initCtrlBusValue;} 
                {this.ctrlBusValue = normMidiValue;} // If initCtrlBusTriggered flag is set, continue using the last normMidiValue within the 1% range
            }
        };

        this.controlBus.set(this.ctrlBusValue); // Set the control bus value
    }

    prSetCtrlValueEncoder { |midiValue|
        var normMidiValue = (midiValue - 64) * (this.velocityFactor / 1000);
        var incrementMidiValue = { |normMidiValue|
            // Check if both 'this.lowCtrlBusValue' and 'this.highCtrlBusValue' are non-nil
            if (this.lowCtrlBusValue.notNil and: { this.highCtrlBusValue.notNil }) {
                // If both are non-nil, apply clipping to ensure the value stays within the specified range
                this.initCtrlBusValue = (this.initCtrlBusValue + normMidiValue).clip(this.lowCtrlBusValue, this.highCtrlBusValue);
            } {
                // If either is nil, just add the 'normMidiValue' without clipping
                this.initCtrlBusValue = this.initCtrlBusValue + normMidiValue;
            };

            this.initCtrlBusValue; // Return the updated cumulative value
        };

        if (this.initCtrlBusValue.isNil) { this.initCtrlBusValue = 0; };
        this.ctrlBusValue = incrementMidiValue.value(normMidiValue);
        this.controlBus.set(this.ctrlBusValue);
    }

    prSetCtrlValueButton { |midiValue|
        var normMidiValue = midiValue.linlin(0, 127, this.lowCtrlBusValue, this.highCtrlBusValue); // Bitwise left shift by 7 positions of the MSB value (same as * 128) and add the LSB value to get the 14-bit MIDI value, then linearly map it to the control bus range
        this.ctrlBusValue = normMidiValue;

        this.controlBus.set(this.ctrlBusValue); // Set the control bus value
    }
}


