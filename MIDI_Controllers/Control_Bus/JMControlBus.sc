JMControlBus {
    var <>controlBus;
    var <>lowCtrlBusValue = 0, <>highCtrlBusValue = 1;
    var <>initCtrlBusValue = nil, <>initCtrlBusTriggered = false;
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
        this.ctrlBusValue = ctrlBusValue;
    }

    prSetCtrlBusValue {|elementShortName, midiValue|
        var normMidiValue;
        if (elementShortName == "BU") 
        { normMidiValue = midiValue.linlin(0, 127, this.lowCtrlBusValue, this.highCtrlBusValue); } 
        { normMidiValue = midiValue.linlin(0, 16383, this.lowCtrlBusValue, this.highCtrlBusValue) }; // Bitwise left shift by 7 positions of the MSB value (same as * 128) and add the LSB value to get the 14-bit MIDI value, then linearly map it to the control bus range

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
}


