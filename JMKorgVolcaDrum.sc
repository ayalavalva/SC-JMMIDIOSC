JMKorgVolcaDrum {
    var <>mode;  // Single or Split mode
    var <>midiOut;
    var <>channel;  // Default MIDI channel for single mode.
    var <>channels;  // MIDI channels for split mode.
    var <>controller; // Modulating device (ex: Intech Studio device)
    var <>midiNotes;

    var <>splitPartPresetCCs;
    var <>splitGlobalPresetCCs;
    var <>splitPartPreset;
    var <>splitGlobalPreset;
    var <>splitDrumkitPreset;

    // Constructor
    *new { |midiPort, mode = 'split', channel = 11, controller|
        ^super.new.init(midiPort, mode, channel, controller)
    }

    // Initialize
    init { |midiPort, mode, channel, controller|
        this.midiOut = MIDIOut.newByName(midiPort, midiPort);
        this.channel = channel;
        this.channels = (1..6);  // Channels 1-6 for split mode, corresponding to Parts 1-6.
        this.controller = controller;
        this.mode = mode;
        this.midiNotes = [
            1: 60,  // Part 1 - MIDI Note 60 (C4)
            2: 62,  // Part 2 - MIDI Note 62 (D4)
            3: 64,  // Part 3 - MIDI Note 64 (E4)
            4: 65,  // Part 4 - MIDI Note 65 (F4)
            5: 67,  // Part 5 - MIDI Note 67 (G4)
            6: 69   // Part 6 - MIDI Note 69 (A4)
        ];
        this.splitPartPresetCCs = Dictionary.new;
        this.splitGlobalPresetCCs = Dictionary.new;
        this.splitPartPreset = Dictionary.new;
        this.splitGlobalPreset = Dictionary.new;
        this.splitDrumkitPreset = Dictionary.new;

        this.initSplitPartPresetCCs;
        this.initSplitGlobalPresetCCs;

        // this.initPartPreset;
        // this.initGlobalPreset;

        this.midiOut.connect;
    }

    initSplitPartPresetCCs {
        splitPartPresetCCs.put("SELECT1", 14); // https://www.reddit.com/r/volcas/comments/hicyej/volca_drum_cc_values_for_select/
        splitPartPresetCCs.put("SELECT2", 15); // https://www.reddit.com/r/volcas/comments/hicyej/volca_drum_cc_values_for_select/
        splitPartPresetCCs.put("SELECT1-2", 16); // https://www.reddit.com/r/volcas/comments/hicyej/volca_drum_cc_values_for_select/
        
        splitPartPresetCCs.put("LEVEL1", 17); // 0-255 (default: 255)
        splitPartPresetCCs.put("LEVEL2", 18); // 0-255 (default: 255)
        splitPartPresetCCs.put("LEVEL1-2", 19); // 0-255 (default: 255)
        splitPartPresetCCs.put("PITCH1", 26); // 0-255 (default: 24)
        splitPartPresetCCs.put("PITCH2", 27); // 0-255 (default: 24)
        splitPartPresetCCs.put("PITCH1-2", 28); // 0-255 (default: 24)
        splitPartPresetCCs.put("EGATT1", 20); // 0-255 (default: 0)
        splitPartPresetCCs.put("EGATT2", 21); // 0-255 (default: 0)
        splitPartPresetCCs.put("EGATT1-2", 22); // 0-255 (default: 0)
        splitPartPresetCCs.put("EGREL1", 23); // 0-255 (default: 255)
        splitPartPresetCCs.put("EGREL2", 24); // 0-255 (default: 255)
        splitPartPresetCCs.put("EGREL1-2", 25); // 0-255 (default: 255)
        splitPartPresetCCs.put("MODAMT1", 29); // -100-100 (default: 0)
        splitPartPresetCCs.put("MODAMT2", 30); // -100-100 (default: 0)
        splitPartPresetCCs.put("MODAMT1-2", 31); // -100-100 (default: 0)
        splitPartPresetCCs.put("MODRATE1", 46); // 0-255 (default: 0)
        splitPartPresetCCs.put("MODRATE2", 47); // 0-255 (default: 0)
        splitPartPresetCCs.put("MODRATE1-2", 48); // 0-255 (default: 0)

        splitPartPresetCCs.put("PAN", 10); // -100-100 (default: 0)
        splitPartPresetCCs.put("BIT RED", 49); // 0-255 (default: 0)
        splitPartPresetCCs.put("FOLD", 50); // 0-255 (default: 0)
        splitPartPresetCCs.put("DRIVE", 51); // 0-255 (default: 0)
        splitPartPresetCCs.put("DRY GAIN", 52); // -100-100 (default: 0)
        splitPartPresetCCs.put("WAVEGUIDE SEND", 103); // 0-255 (default: 0)
        // post splitPartPresetCCs values
        splitPartPresetCCs.postln;
    }

    initSplitGlobalPresetCCs {
        splitGlobalPresetCCs.put("WAVEGUIDE MODEL", 116); // 0 tube 64 String
        splitGlobalPresetCCs.put("DECAY", 117); // 0-255 (default: 0)
        splitGlobalPresetCCs.put("BODY", 118); // 0-255 (default: 0)
        splitGlobalPresetCCs.put("TUNE", 119); // 0-255 (default: 30)
    }

    // Method to put the argument pair into splitPartPreset dictionary and send the corresponding CC message
    initSplitPartPreset { |part, argsArray|
        argsArray.pairsDo({ |paramName, elementKey|
            /*
            if(["SELECT1", "SELECT2", "SELECT1-2"].includes(paramName)) {
                value = 0.to(126, 3).do({ |ccValue|
                    this.midiOut.control(part - 1, splitPartPresetCCs.at(paramName), ccValue);
                });
            } {
                value = value;
            }
            */
            if(splitPartPresetCCs.includes(paramName).not) {"Parameter % not found in splitPartPresetCCs".format(paramName).warn}; // checks if paramName exists in splitPartPresetCCs
            this.splitPartPreset.put(paramName, this.controller.busValue(elementKey)); // adds paramName and value to splitPartPreset dictionary
            this.splitPartPreset.postln;
            // Sends the corresponding CC message depending on the type of parameter
            this.midiOut.control(part - 1, splitPartPresetCCs.at(paramName), this.controller.busValue(elementKey).linlin(0, 1, 0, 127).asInteger);
        });
    }

    // triggers a note from midiNote array (takes part number and MIDI channel as arguments)
    triggerPart { |part = 1, cc, velocity = 127|
        // if(note.isNil) {"Part % not found or no MIDI note assigned.".format(part).warn} {^nil;};
        midiOut.noteOn(part - 1, cc, velocity);
    }

    /*
    if(["SELECT1", "SELECT2", "SELECT1-2"].includes(paramName)) {
        0.to(126, 3).do({ |ccValue|
            this.midiOut.control(part - 1, splitPartPresetCCs.at(paramName), ccValue);
        });
    } {
        this.midiOut.control(part - 1, splitPartPresetCCs.at(paramName), value);
    };



    initPartPreset {
        var partParams = [ |initValue = 64|
            "LEVEL1", "LEVEL2", "LEVEL1-2",
            "EGATT1", "EGATT2", "EGATT1-2",
            "EGREL1", "EGREL2", "EGREL1-2",
            "PITCH1", "PITCH2", "PITCH1-2",
            "MODAMT1", "MODAMT2", "MODAMT1-2",
            "MODRATE1", "MODRATE2", "MODRATE1-2"
            "PAN", "BIT RED", "FOLD", "DRIVE", "DRY GAIN",
            "SEND", "WAVEGUIDE MODEL", "DECAY", "BODY", "TUNE"
        ];

        partParams.do { |param|
            var fullParamName = "%-PART%".format(param, partIdx + 1);
            var ccNum = this.ccNumberForSplit(fullParamName);
            
            // Check if the parameter CC number exists
            if(ccNum.notNil, {
                // Send MIDI control message to initialize the parameter
                this.midiOut.control(chan, ccNum, initValue);
                
                // Store the initial value in the IdentityDictionary
                partValues.put(fullParamName, initValue);
            });
        };
    }

    // Set Mode (not very useful)
    setMode { |newMode|
        this.mode = newMode;
        "Mode set to: %".format(newMode).postln;
    }

    // Set parameter for a part
    setParameter { |part, param, value|
        var ccNum, targetChannel;

        // Determine the target channel and CC number based on mode and part
        if(this.mode == 'single', {
            targetChannel = this.channel;
            ccNum = this.ccNumberForSingle(param, part);
        }, {
            targetChannel = this.channels[part - 1];  // Adjust for 0-indexed array
            ccNum = this.ccNumberForSplit(param);
        });

        // Send CC message
        this.midiOut.control(targetChannel, ccNum, value);
        "Parameter % set for part % on channel % with value %".format(param, part, targetChannel, value).postln;
    }

    // CC numbers for single channel mode
    ccNumberForSingle { |param, part|
        var ccMapSingle = [
            "SELECT1-2": [14, 23, 46, 55, 80, 89],
            "LEVEL1-2": [15, 24, 47, 56, 81, 90],
            "MODAMT1-2": [16, 25, 48, 57, 82, 96],
            "MODRATE1-2": [17, 26, 49, 58, 83, 97],
            "PITCH1-2": [18, 27, 50, 59, 84, 98],
            "EGATT1-2": [19, 28, 51, 60, 85, 99],
            "EGREL1-2": [20, 29, 52, 61, 86, 100],
            "SEND": [103, 104, 105, 106, 107, 108],
            "PAN": [109, 110, 110, 112, 113, 114],
            // Global parameters - same CC number for all parts
            "SEND": 103, "PAN": 109, "WAVEGUIDE MODEL": 116, "DECAY": 117, "BODY": 118, "TUNE": 119
        ];
        // For global parameters, return the same CC number regardless of the part
        if(["SEND", "PAN", "WAVEGUIDE MODEL", "DECAY", "BODY", "TUNE"].includes(param)) {
            ^ccMapSingle[param];
        } {
            // For part-specific parameters, return the CC number based on the part
            ^ccMapSingle[param].at(part - 1);
        };
    }

    // CC numbers for split channel mode
    ccNumberForSplit { |param|
        var ccMapSplit = [
            // Part-specific parameters (based on MIDI channel 1-6)
            // "SELECT1": 14, "SELECT2": 15, "SELECT1-2": 16,
            "LEVEL1": 17, "LEVEL2": 18, "LEVEL1-2": 19,
            "EGATT1": 20, "EGATT2": 21, "EGATT1-2": 22,
            "EGREL1": 23, "EGREL2": 24, "EGREL1-2": 25,
            "PITCH1": 26, "PITCH2": 27, "PITCH1-2": 28,
            "MODAMT1": 29, "MODAMT2": 30, "MODAMT1-2": 31,
            "MODRATE1": 46, "MODRATE2": 47, "MODRATE1-2": 48

            // Global parameters
            "PAN": 10,
            "BIT RED": 49, "FOLD": 50, "DRIVE": 51, "DRY GAIN": 52,
            "SEND": 103, "WAVEGUIDE MODEL": 116, "DECAY": 117, "BODY": 118, "TUNE": 119,
        ];
        ^ccMapSplit.at(param);
    }

    // Save the current settings as a preset
    savePreset { |presetName, presetData|
        this.presets.put(presetName, presetData);
        "Preset '%'.postln".format(presetName).postln;
    }

    // Recall a saved preset
    recallPreset { |presetName|
        var presetData = this.presets.at(presetName);
        if(presetData.isNil, {
            "Preset '%'.postln not found".format(presetName).warn;
            ^nil;
        });

        presetData.do { |data|
            this.setParameter(data.part, data.param, data.value);
        };
        "Preset '%'.postln recalled".format(presetName).postln;
    }

    // Method to trigger a part with a MIDI note
    triggerPart { |part, velocity = 64|
        var note = midiNotes[part];
        var targetChannel;

        // Determine the target channel based on mode and part
        if(mode == 'single') {
            targetChannel = channel;
        } {
            targetChannel = channels[part - 1];  // Adjust for 0-indexed array
        };

        if(note.notNil, {
            midiOut.noteOn(targetChannel, note, velocity);
        }, {
            "Part % not found or no MIDI note assigned.".format(part).warn;
        });
    }

        // Method for real-time parameter modulation
    modulateParameter { |part, param, startValue, endValue, duration|
        var stepSize, currentValue, numSteps, modTask;
        
        stepSize = (endValue - startValue) / duration;
        currentValue = startValue;
        numSteps = duration.abs;

        modTask = Task({
            numSteps.do({ |i|
                currentValue = currentValue + stepSize;
                this.setParameter(part, param, currentValue);
                (1/numSteps).wait; // Wait time based on number of steps for smooth modulation
            });
        }).play;

        modTask.start;
    }

        // Method for rhythm variation
    varyRhythm { |part, variationPattern|
        variationPattern.do({ |variation, i|
            this.setParameter(part, "DECAY", variation.decay);
            this.setParameter(part, "PITCH1-2", variation.pitch);
            variation.duration.wait; // Wait for the duration of this variation before applying the next
        });
    }

    // Method for morphing between presets
    morphPresets { |presetNameA, presetNameB, duration|
        var presetA = presets[presetNameA];
        var presetB = presets[presetNameB];
        var morphTask;

        if(presetA.isNil || presetB.isNil) {
            "One or both presets not found".warn;
            ^nil;
        };

        morphTask = Task({
            (0..1).step(1/duration).do({ |morphFactor|
                presetA.size.do({ |i|
                    var part = presetA[i].part;
                    var param = presetA[i].param;
                    var valueA = presetA[i].value;
                    var valueB = presetB[i].value;
                    var morphedValue = valueA.linlin(0, 1, valueB, morphFactor);
                    this.setParameter(part, param, morphedValue);
                });
                0.1.wait;  // Adjust for desired morphing speed
            });
        });

        morphTask.start;
    }
*/
}