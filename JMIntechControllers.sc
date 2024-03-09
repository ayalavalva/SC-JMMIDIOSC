JMIntechControllers {
    var <>deviceFullName, <>deviceShortName, <>midiChannel, <>oscServAddr, <>oscServPort; // Device identifiers
    var <>potCount = 0, <>encCount = 0, <>fadCount = 0, <>butCount = 0; // Counts of different control elements
    var <>elementDict; // Dictionary to store element objects
    var <>controlBusDict; // Dictionary to store control bus for each element
    var <>oscPathDict; // Dictionary to store OSC path for each element
    var <>midiValueDict; // Dictionary to store callback functions for each element
    var <>deviceNumb; // Device number (number of instances of PO16, EN16 and PBF4 devices created)

    *new { |deviceFullName, deviceShortName, midiChannel, oscServAddr, oscServPort|
        ^super.new.init(deviceFullName, deviceShortName, midiChannel, oscServAddr, oscServPort)
    }

    init { |deviceFullName, deviceShortName, midiChannel, oscServAddr, oscServPort|
        this.deviceFullName = deviceFullName;
        this.deviceShortName = deviceShortName;
        this.deviceNumb = deviceNumb;
        this.midiChannel = midiChannel;
        this.oscServAddr = oscServAddr;
        this.oscServPort = oscServPort;

        // Initialize dictionaries based on the counts of different MIDI control elements.
        this.elementDict = IdentityDictionary.new(n: potCount + encCount + fadCount + butCount); // Initialize element dictionary
        this.controlBusDict = IdentityDictionary.new(n: potCount + encCount + fadCount + butCount); // Initialize control bus dictionary
        this.oscPathDict = IdentityDictionary.new(n: potCount + encCount + fadCount + butCount); // Initialize MIDI value dictionary

        this.midiValueDict = IdentityDictionary.new(n: potCount + encCount + fadCount + butCount); // Initialize MIDI value dictionary
    }
    
    //  Initializes MIDI elements (potentiometers, encoders, faders, buttons) by creating instances of their respective classes, updating the dictionaries, and printing initialization details to the console. It uses formatted strings to generate unique keys for each element and stores related information in the dictionaries.
    initializeMIDIElements {
        
        potCount.do { |i|
            var elementNumber = i + 1;
            var msbCC = this.startCC + i; // Most significant byte CC is starting from the startCC number
            var lsbCC = msbCC + this.potCount + this.fadCount + this.butCount;
            var elementKey = "PO%".format(elementNumber).asSymbol;
            var element = JMElementPotentiometer.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, elementNumber, this.midiChannel, this.deviceOSCpath, msbCC, lsbCC);
            this.elementDict.put(elementKey, element);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Potentiometer" + elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + msbCC + "lsbCC" + lsbCC).postln;
        };

        encCount.do { |i|
            var elementNumber = i + 1;
            var cc = potCount + this.startCC + i;
            var elementKey = "EN%".format(elementNumber).asSymbol;
            var element = JMElementEncoder.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, elementNumber, this.midiChannel, this.deviceOSCpath, cc);
            this.elementDict.put(elementKey, element);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Encoder" + elementNumber + "MIDI Channel" + this.midiChannel + "CC" + cc).postln;
        };

        fadCount.do { |i|
            var elementNumber = i + 1;
            var msbCC = potCount + this.startCC + i;
            var lsbCC = msbCC + this.fadCount + this.potCount + this.butCount;
            var elementKey = "FA%".format(elementNumber).asSymbol;
            var element = JMElementFader.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, elementNumber, this.midiChannel, this.deviceOSCpath, msbCC, lsbCC);
            this.elementDict.put(elementKey, element);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Fader" + elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + msbCC + "lsbCC" + lsbCC).postln;
        };

        butCount.do { |i|
            var elementNumber = i + 1;
            var cc = potCount + fadCount + encCount + this.startCC + i;
            var elementKey = "BU%".format(elementNumber).asSymbol;
            var element = JMElementButton.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, elementNumber, this.midiChannel, this.deviceOSCpath, cc);
            this.elementDict.put(elementKey, element);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Button" + elementNumber + "MIDI Channel" + this.midiChannel + "CC" + cc).postln;
        };
    }

    // Allows registering callback functions for specific elements, which are stored in the midiValueDict. These callbacks can be triggered upon receiving MIDI messages
    getMIDIValue { |elementKey, callbackFunc|
        this.midiValueDict.put(elementKey, callbackFunc);
    }

    // Method that looks up and executes the callback function associated with a given element key, passing the provided value to the function.
    triggerCallback { |elementKey, value|
        var midiValue = this.midiValueDict.at(elementKey);
        if (midiValue.notNil) {
            midiValue.value(value);
        }
    }   
    
    // Utility method to retrieve a control bus for a specific element key.
    cb { |elementKey|
        ^this.controlBusDict.at(elementKey);
    }

    // Utility method to create an In.kr UGen instance for a specific control bus, allowing integration into SynthDefs
    inCB { |elementKey|
        ^In.kr(this.cb(elementKey), 1);
    }

    // Utility method wrapping the inCB method with Lag.kr, providing smoothed control change handling (useful for control buses modulating frequencies or amplitudes of audio signals))
    lagInCB { |elementKey, lag = 0.3|
        ^Lag.kr(this.inCB(elementKey), lag); 
    }

    // Enables or disables OSC sending for specified element keys, controlling whether MIDI values are forwarded as OSC messages.
    midiOSC { |elementKeys, enableFlag = true|
        elementKeys.do { |key|
            var element = this.elementDict.at(key);
            element.oscSendEnabled = enableFlag;
        };
    }

    setTriggerValue { |elementKey, value|
        var element = this.elementDict.at(elementKey);
        element.triggerValue = value;
    }

    // Sends OSC messages for specified element keys, using the provided OSC path and value.
    sendOSCLabel { |args|
        args.keysValuesDo({ |elementKey, value|
            var element = this.elementDict.at(elementKey);
            element.sendOSCLabel(value);
        });
    }

    // Frees resources and decreases the device count when a device instance is no longer needed.
    free {
        deviceNumb = deviceNumb - 1;
        this.controlBusDict.do { |key, value|
            value.free;
        };
    }
}