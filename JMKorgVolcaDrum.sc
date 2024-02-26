JMKorgVolcaDrum : JMMIDIDevices {
    var <jmIntechEN16, <jmIntechPBF4;
    var <parameters; // Collection of parameters for each part
    var <currentPart = 1; // Default to part 1

    // Constructor
    *new { |midiOutDevice, en16, pbf4, deviceName=nil, midiChannel=0|
        ^super.new(midiOutDevice, deviceName, midiChannel).init(en16, pbf4);
    }

    // Initialize the Volca Drum controller
    init { |en16, pbf4|
        jmIntechEN16 = en16;
        jmIntechPBF4 = pbf4;
        parameters = Array.fill(7, { IdentityDictionary.new }); // 6 parts + 1 for common parameters
        this.initParameters();
        this.setupCallbacks();
    }

    // Initialize parameters with their CC numbers
    initParameters {
        // Initialize parameters for each part (MIDI channels 1-6)
        parameters.do { |partParams, i|
            partParams.put('selectLayer1', 14);
            partParams.put('selectLayer2', 15);
            partParams.put('levelLayer1', 17);
            partParams.put('levelLayer2', 18);
            partParams.put('egAttLayer1', 20);
            partParams.put('egAttLayer2', 21);
            partParams.put('egRelLayer1', 23);
            partParams.put('egRelLayer2', 24);
            partParams.put('pitchLayer1', 26);
            partParams.put('pitchLayer2', 27);
            partParams.put('modAmtLayer1', 29);
            partParams.put('modAmtLayer2', 30);
            partParams.put('modRateLayer1', 46);
            partParams.put('modRateLayer2', 47);
        };
        // Initialize parameters for both parts (MIDI channel 7)
        // Note: These could be stored in a separate collection if needed
        parameters[6] = IdentityDictionary[
            'pan' -> 10,
            'bitRed' -> 49,
            'fold' -> 50,
            'drive' -> 51,
            'dryGain' -> 52,
            'send' -> 103,
            'waveguideModel' -> 116,
            'decay' -> 117,
            'body' -> 118,
            'tune' -> 119
        ];
    }

    // Setup callbacks for EN16 encoders and PBF4 buttons
    setupCallbacks {
        // EN16 Encoders
        jmIntechEN16.elements.do { |element, i|
            element.setOnChange { |value|
                this.controlChange(i, value);
            };
        };
        // PBF4 Buttons to select parts
        jmIntechPBF4.elements.do { |element, i|
            element.setOnChange { |value|
                if(value > 0) { // Button pressed
                    this.currentPart = i + 1;
                    "Part selected: %".format(this.currentPart).postln;
                }
            };
        };
    }

    // Handle control changes from encoders
    controlChange { |encoderIndex, value|
        var ccNum = this.parameters[currentPart - 1].at(encoderIndex); // -1 because parts are 1-indexed
        if(ccNum.isNil) {
            "CC Number not found for encoder %".format(encoderIndex).postln;
        // No need for 'return;' here, just end the block
        } {
            midiOut.controlChange(currentPart, ccNum, value.asInteger); // Send MIDI control change message
            "Sending CC#% with value % to part %".format(ccNum, value, currentPart).postln;
        }
    }

    // Preset management (saving and loading) could be added here
}