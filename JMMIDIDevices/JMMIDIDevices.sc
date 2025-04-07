JMMIDIDevices {
    var <midiOut; // MIDIOut object for sending MIDI messages
    var <deviceName, <midiChannel;

    // Constructor
    *new { |midiOutDevice, deviceName=nil, midiChannel=0|
        ^super.new().init(midiOutDevice, deviceName, midiChannel);
    }

    // Initialize the MIDI device
    init { |midiOutDevice, deviceName, midiChannel|
        this.deviceName = deviceName;
        this.midiChannel = midiChannel;
        midiOut = MIDIOut.new(midiOutDevice);
        midiOut.latency = 0;
        midiOut.connect;
    }

    // Basic MIDI message sending method
    sendMIDI { |type, data1, data2|
        /*
        switch(type)
        {
            'noteOn': { midiOut.noteOn(midiChannel, data1, data2) };
            'noteOff': { midiOut.noteOff(midiChannel, data1, data2) };
            'cc': { midiOut.controlChange(midiChannel, data1, data2) };
            // Add more cases as needed
        }
        */
    }

    // Method to change the MIDI channel
    setMIDIChannel { |channel|
        midiChannel = channel;
    }

    // Cleanup method to close MIDI connection
    free {
        midiOut.disconnect;
    }
}