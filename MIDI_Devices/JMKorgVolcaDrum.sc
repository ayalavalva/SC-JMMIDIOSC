JMKorgVolcaDrum {
    var <>mode;  // Single or Split mode
    var <>midiOut;
    var <>channel = 11;  // Default MIDI channel for single mode.
    var <>channels;  // MIDI channels 1-6 for split channel mode, corresponding to Parts 1-6.
    var <>midiNotes;

    var <>elementPart, <>elementLayer, <>elementParam, <>elementValue;
    var <>partValue, <>layerValue, <>paramValue, <>valueValue;
    
    var <>sp_ParamCCDict;
    var <>sp_PartDict;
    var <>sp_ValuesDict;
    var <>sp_GlobalDict;

    // Constructor (SP = split channels, SI = single channel)
    *newSP { |midiPort|
        MIDIClient.destinations.detect { |endpoint| endpoint.name == midiPort }.notNil.if 
        { ^super.new.initSP(midiPort) } 
        { midiPort + " not found".warn; }
    }

    // Constructor (SP = split channels, SI = single channel)
    *newSI { |midiPort, channel|
        ^super.new.initSI(midiPort, channel)
    }

    initSI { |midiPort, channel|
        this.channel = channel;
        this.initAll(midiPort);
    }

    initSP { |midiPort|
        this.channels = [1, 2, 3, 4, 5, 6];
        this.midiNotes = [
            1: 60,  // Part 1 - MIDI Note 60 (C4)
            2: 62,  // Part 2 - MIDI Note 62 (D4)
            3: 64,  // Part 3 - MIDI Note 64 (E4)
            4: 65,  // Part 4 - MIDI Note 65 (F4)
            5: 67,  // Part 5 - MIDI Note 67 (G4)
            6: 69   // Part 6 - MIDI Note 69 (A4)
        ];

        this.elementPart = elementPart;
        this.elementLayer = elementLayer;
        this.elementParam = elementParam;
        this.elementValue = elementValue;
        this.partValue = partValue;
        this.layerValue = layerValue;
        this.paramValue = paramValue;
        this.valueValue = valueValue;

        this.init_sp_ParamCCDict;
        this.init_sp_PartDict;
        this.init_sp_ValuesDict;
        this.init_sp_GlobalDict;

        // this.initEncoderCallbacks;
        
        this.initAll(midiPort);
    }

    initAll { |midiPort|
        this.midiOut = MIDIOut.newByName(midiPort, midiPort);
        this.midiOut.connect;
    }

    init_sp_ParamCCDict {
        this.sp_ParamCCDict = IdentityDictionary.new;
        this.sp_ParamCCDict = IdentityDictionary.with(*[
            \SELECT1 -> 14, // https://www.reddit.com/r/volcas/comments/hicyej/volca_drum_cc_values_for_select/
            \SELECT2 -> 15, // https://www.reddit.com/r/volcas/comments/hicyej/volca_drum_cc_values_for_select/
            \SELECT1_2 -> 16, // https://www.reddit.com/r/volcas/comments/hicyej/volca_drum_cc_values_for_select/

            \LEVEL1 -> 17, // 0-255 (default: 255)
            \LEVEL2 -> 18, // 0-255 (default: 255)
            \LEVEL1_2 -> 19, // 0-255 (default: 255)
            \PITCH1 -> 26, // 0-255 (default: 24)
            \PITCH2 -> 27, // 0-255 (default: 24)
            \PITCH1_2 -> 28, // 0-255 (default: 24)
            \EGATT1 -> 20, // 0-255 (default: 0)
            \EGATT2 -> 21, // 0-255 (default: 0)
            \EGATT1_2 -> 22, // 0-255 (default: 0)
            \EGREL1 -> 23, // 0-255 (default: 255)
            \EGREL2 -> 24, // 0-255 (default: 255)
            \EGREL1_2 -> 25, // 0-255 (default: 255)
            \MODAMT1 -> 29, // -100-100 (default: 0)
            \MODAMT2 -> 30, // -100-100 (default: 0)
            \MODAMT1_2 -> 31, // -100-100 (default: 0)
            \MODRATE1 -> 46, // 0-255 (default: 0)
            \MODRATE2 -> 47, // 0-255 (default: 0)
            \MODRATE1_2 -> 48, // 0-255 (default: 0)

            \PAN -> 10, // -100-100 (default: 0)
            \BIT_RED -> 49, // 0-255 (default: 0)
            \FOLD -> 50, // 0-255 (default: 0)
            \DRIVE -> 51, // 0-255 (default: 0)
            \DRY_GAIN -> 52, // -100-100 (default: 0)
            \WAVEGUIDE_SEND -> 103,  // 0-255 (default: 0)

            \WAVEGUIDE_MODEL -> 116, // 0 tube 64 String
            \DECAY -> 117, // 0-255 (default: 0)
            \BODY -> 118, // 0-255 (default: 0)
            \TUNE -> 119 // 0-255 (default: 30)
        ]);
    }

    init_sp_PartDict {
        this.sp_PartDict = IdentityDictionary.new;
        [
            \SELECT1, \SELECT2, \SELECT1_2,
            \LEVEL1, \LEVEL2, \LEVEL1_2,
            \PITCH1, \PITCH2, \PITCH1_2,
            \EGATT1, \EGATT2, \EGATT1_2,
            \EGREL1, \EGREL2, \EGREL1_2,
            \MODAMT1, \MODAMT2, \MODAMT1_2,
            \MODRATE1, \MODRATE2, \MODRATE1_2,
            \PAN, \BIT_RED, \FOLD, \DRIVE, \DRY_GAIN, \WAVEGUIDE_SEND
        ].do { |paramName|
            // Initialize each parameter with part-specific settings
            this.sp_PartDict[paramName] = IdentityDictionary.with(*[
                1 -> (controller: nil, element: nil, defaultValue: 0, currentValue: nil),
                2 -> (controller: nil, element: nil, defaultValue: 0, currentValue: nil),
                3 -> (controller: nil, element: nil, defaultValue: 0, currentValue: nil),
                4 -> (controller: nil, element: nil, defaultValue: 0, currentValue: nil),
                5 -> (controller: nil, element: nil, defaultValue: 0, currentValue: nil),
                6 -> (controller: nil, element: nil, defaultValue: 0, currentValue: nil)
            ]);
        };
    }

    init_sp_GlobalDict {
        this.sp_GlobalDict = IdentityDictionary.new;
            // Initialize each parameter with global settings
        this.sp_GlobalDict = IdentityDictionary.with(*[
            \WAVEGUIDE_MODEL -> (controller: nil, element: nil, defaultValue: 0, currentValue: nil),
            \DECAY -> (controller: nil, element: nil, defaultValue: 0, currentValue: nil),
            \BODY -> (controller: nil, element: nil, defaultValue: 0, currentValue: nil),
            \TUNE -> (controller: nil, element: nil, defaultValue: 0, currentValue: nil)
        ]);
    }

    init_sp_ValuesDict {
        var parts = ['Part 1', 'Part 2', 'Part 3', 'Part 4', 'Part 5', 'Part 6', 'Global'];
        var layerParams = ['SELECT', 'LEVEL', 'PITCH', 'EGATT', 'EGREL', 'MODAMT', 'MODRATE'];
        var layer1_2Params = ['PAN', 'BIT_RED', 'FOLD', 'DRIVE', 'DRY_GAIN', 'WAVEGUIDE_SEND'];
        var globalParams = ['WAVEGUIDE_MODEL', 'DECAY', 'BODY', 'TUNE'];
    
        // Initialize the main dictionary
        sp_ValuesDict = IdentityDictionary.new;
    
        parts.do { |part|
            var partDict = IdentityDictionary.new;
    
            // Handle global part separately
            if(part == 'Global') {
                globalParams.do { |param|
                    // Use paramDict for each parameter
                    var paramDict = IdentityDictionary.new;
                    paramDict['Value'] = 0;
                    partDict[param] = paramDict;
                };
            } {
                // For non-global parts, handle each layer
                ['Layer 1', 'Layer 2', 'Layer 1-2'].do { |layer|
                    var layerDict = IdentityDictionary.new;
                    var params;

                    // Decide which parameters to use based on the layer
                    if(layer == 'Layer 1-2', {
                        params = layer1_2Params;
                    }, {
                        params = layerParams;
                    });

                    // Populate the layer dictionary with parameters
                    params.do { |param|
                        // Use paramDict for each parameter
                        var paramDict = IdentityDictionary.new;
                        paramDict['Value'] = 0;
                        layerDict[param] = paramDict;
                    };

                    // Add the populated layer dictionary to the part dictionary
                    partDict[layer] = layerDict;
                };
            };
    
            // Add the populated part dictionary to the main dictionary
            sp_ValuesDict[part] = partDict;
        };
    
        // Debugging: Post the structure for verification
        sp_ValuesDict.postln;
    }

    ///////////////////////////// BEGINNING OF ENCODER CONTROLS /////////////////////////////

    // Publid method called in patch code to set the encoder controls (parts, layers, params, value) and get their values
    encodercontrols { |controller, encoderParts, encoderLayers, encoderParams, encoderValues|
        controller.setEncoderValues(encoderParts, nil, 0, nil, 1000);
        controller.setEncoderValues(encoderLayers, nil, 0, nil, 1000);
        controller.setEncoderValues(encoderParams, nil, 0, nil, 1000);
        controller.setEncoderValues(encoderParams, nil, 0, nil, 1000);
        controller.getBusValue(encoderParts, { |val| this.handleEncoderParts(val); });
        controller.getBusValue(encoderLayers, { |val| this.handleEncoderLayers(val); });
        controller.getBusValue(encoderParams, { |val| this.handleEncoderParams(val); });
        controller.getBusValue(encoderValues, { |val| this.handleEncoderValues(val); });
    }
    
    handleEncoderParts { |val|
        var parts = ['Part 1', 'Part 2', 'Part 3', 'Part 4', 'Part 5', 'Part 6', 'Global'];
        var index = (val.asInteger % parts.size);  // Scale and cycle through parts
        this.partValue = parts[index];
        JMOSCManager.getSharedInstance.send("/en16/en9_lb2", this.partValue);
        if (this.partValue == 'Global') {JMOSCManager.getSharedInstance.send("/en16/en10_lb2", "");};
    }

    handleEncoderLayers { |val|
        var layers = ['Layer 1', 'Layer 2', 'Layer 1-2', 'Common'];
        var index = (val.asInteger % layers.size);  // Scale and cycle through layers
        if (this.partValue == 'Global') {
            this.layerValue = nil;
            JMOSCManager.getSharedInstance.send("/en16/en10_lb2", "");
        } {
            this.layerValue = layers[index];
            JMOSCManager.getSharedInstance.send("/en16/en10_lb2", this.layerValue);
        };
    }

    handleEncoderParams { |val|
        var layerParams = ['SELECT', 'LEVEL', 'PITCH', 'EGATT', 'EGREL', 'MODAMT', 'MODRATE'];
        var layer1_2Params = ['PAN', 'BIT_RED', 'FOLD', 'DRIVE', 'DRY_GAIN', 'WAVEGUIDE_SEND'];
        var globalParams = ['WAVEGUIDE_MODEL', 'DECAY', 'BODY', 'TUNE'];
        var params;
        var index;
        if (this.partValue == 'Global') { 
            params = globalParams;
        } { 
            if (this.layerValue == 'Layer 1-2') {
                params = layer1_2Params;
            } {
                params = layerParams;
            }; 
        };
        index = (val.asInteger % params.size);  // Scale and cycle through params
        this.paramValue = params[index];
        JMOSCManager.getSharedInstance.send("/en16/en11_lb2", this.paramValue);
    }

    handleEncoderValues { |val|
        // Assuming the value is to be scaled for the MIDI message
        var currentValue = if (this.partValue == 'Global') {
            this.sp_ValuesDict[this.partValue][this.paramValue]['Value'];
        } {
            this.sp_ValuesDict[this.partValue][this.layerValue][this.paramValue]['Value'];
        };

        var newValue = (currentValue + (val * 127).floor.asInteger).clip(0, 127);
        // Update the sp_ValuesDict
        if (this.partValue == 'Global') {
            this.sp_ValuesDict[this.partValue][this.paramValue]['Value'] = newValue;
        } {
            this.sp_ValuesDict[this.partValue][this.layerValue][this.paramValue]['Value'] = newValue;
        };
        // Send MIDI message (example, adjust as necessary)
        this.sendMIDI(this.partValue, this.layerValue, this.paramValue, newValue);
        JMOSCManager.getSharedInstance.send("/en16/en12_lb2", newValue);
    }

    // Example MIDI sending method, adjust as needed
    sendMIDI { |part, layer, param, value|
        var channel = if(part == 'Global') {0;} { (part.asString.replace("Part ", "").asInteger - 1); };
        var layerNum = layer.asString.replace("Layer ", "");
        var parameter = if (part == 'Global' or: {layer == 'Layer 1-2'}) { param } { (param ++ layerNum).asSymbol; };
        var ccNum = this.sp_ParamCCDict[parameter];
        this.midiOut.control(channel, ccNum, value);
    }

    ///////////////////////////// END OF ENCODER CONTROLS /////////////////////////////

    pr_sendPartDefaultValues { // TO BE VERIFIED (COPILOT)
        // sends for each part number, the defaults values of each part parameters contained in sp_PartDict (except when parameter name contains '1_2') via MIDI to the Korg Volca Drum
        (1..6).do { |part|
            this.sp_PartDict.do { |paramName|
                if(paramName.contains("_").not) {
                    var ccNum = this.sp_ParamCCDict[paramName];
                    var value = sp_PartDict[part][paramName].defaultValue;
                    var controller = sp_PartDict[part][paramName].controller;
                    var element = sp_PartDict[part][paramName].element;
                    this.midiOut.control(part - 1, ccNum, value);
                    "% % is sending to Korg Volca Drum Part % Param % value: %".format(controller, element, part, paramName, value).postln;
                };
            };
        };
    }

    pr_sendGlobalDefaultValues { // TO BE VERIFIED (COPILOT)
        // sends the defaults values for the global parameters (sp_GlobalDict) via MIDI to the Korg Volca Drum
        this.sp_GlobalDict.do { |paramName|
            var ccNum = this.sp_ParamCCDict[paramName];
            var value = sp_GlobalDict[paramName].defaultValue;
            var controller = sp_GlobalDict[paramName].controller;
            this.midiOut.control(0, ccNum, value);
            "% is sending to Korg Volca Drum Global Param % value: %".format(controller, paramName, value).postln;
        };
    }

    // Method to put the argument pair into splitPartDict dictionary and send the corresponding CC message
    sp_PartPreset { |part, paramName, controller, elementKey|
            /*
            if(["SELECT1", "SELECT2", "SELECT1-2"].includes(paramName)) {
                value = 0.to(126, 3).do({ |ccValue|
                    this.midiOut.control(part - 1, ssp_CCDict.at(paramName), ccValue);
                });
            } {
                value = value;
            }
            */
            var busValue;
            var ccNum = this.sp_ParamCCDict[paramName];
            if(this.sp_ParamCCDict.includesKey(paramName).not) {"Parameter % not found in sp_ParamCCDict".format(paramName).warn}; // checks if paramName exists in sp_ParamCCDict
            // Sends the corresponding CC message depending on the type of parameter
            busValue = controller.getBusValue(elementKey, {|val| 
                var midiChannel = part - 1;
                var midiValue = val.linlin(0, 1, 0, 127).asInteger;
                this.sp_PartDict[paramName][part].currentValue = midiValue;
                "% % is sending to Korg Volca Drum Part % Param % value: %".format(controller, elementKey, part, paramName, this.sp_PartDict[paramName][part].currentValue).postln;
                this.midiOut.control(midiChannel, ccNum, midiValue);
            });
    }

    elementControls { |elementPart, elementLayer, elementParam, elementValue|
        // allocate a controller element to a parameter
        var controller = elementPart;

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