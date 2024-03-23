JMIntechControllers {
    var <>deviceFullName, <>deviceShortName, <>deviceNumb, <>midiChannel, <>oscServAddr, <>oscServPort; // Device identifiers
    var <>elementDict; // Dictionary to store element objects
    var <>busValueDict; // Dictionary to store bus values for some elements 

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
        this.elementDict = IdentityDictionary.new; // Initialize element dictionary
        this.busValueDict = IdentityDictionary.new; // Initialize MIDI value dictionary                                 /// NOT SURE I NEED A DICTIONARY FOR ALL OF THIS. ELEMENT DICTIONARY SHOULD BE ENOUGH, THEN ACCESS DIRECTLY THE VARIABLE
    }

    //  Initializes MIDI elements (potentiometers, encoders, faders, buttons) by creating instances of their respective classes, updating the dictionaries, and printing initialization details to the console. It uses formatted strings to generate unique keys for each element and stores related information in the dictionaries.
    initializeMIDIElements {
        var nbElements = this.elementGroupOrder.collect { |elements| elements[1] }.sum; // Calculate the total number of elements of the device

        this.elementGroupOrder.do { |elementKeyValueOuter|
            var nbElementsBefore = 0, nbElementsAfter = 0;  // Initialize the number of elements before and after a group of elements
            var elementType = elementKeyValueOuter[0];
            var elementCount = elementKeyValueOuter[1];
            var keyIndex = this.elementGroupOrder.detectIndex { |elementKeyValueInner| elementKeyValueInner[0] == elementType };

            if(keyIndex > 0) { this.elementGroupOrder[0..keyIndex - 1].do { |elementBefore| nbElementsBefore = nbElementsBefore + elementBefore[1];}; };
            this.elementGroupOrder[(keyIndex + 1)..].do { |elementAfter| nbElementsAfter = nbElementsAfter + elementAfter[1];};
    
            elementCount.do { |i|
                var elementNumber = i + 1;
                var cc = this.startCC + nbElementsBefore + i;
                var msbCC = this.startCC + nbElementsBefore + i;
                var lsbCC = msbCC + nbElements;

                // Creates an instance of the appropriate element class based on the element type
                var element = switch(elementType)
                {'PO'} { JMElementPotentiometer.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, elementNumber, this.midiChannel, this.deviceOSCpath, msbCC, lsbCC); }
                {'EN'} { JMElementEncoder.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, elementNumber, this.midiChannel, this.deviceOSCpath, cc); }
                {'FA'} { JMElementFader.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, elementNumber, this.midiChannel, this.deviceOSCpath, msbCC, lsbCC); }
                {'BU'} { JMElementButton.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, elementNumber, this.midiChannel, this.deviceOSCpath, cc); };
                
                var elementKey = (element.elementShortName ++ elementNumber).asSymbol;

                // Prints initialization details to the console
                switch(element.elementShortName.asSymbol)
                {'PO'} { element.postMIDIElementDetails; }
                {'EN'} { element.postMIDIElementDetails; }
                {'FA'} { element.postMIDIElementDetails; }
                {'BU'} { element.postMIDIElementDetails; };
                
                // Initializes element instance dictionary
                this.elementDict.put(elementKey, element);
            };
        };
    }

    busValue { |elementKey|
        ^this.elementDict.at(elementKey).busValue;
    }

    // Allows registering callback functions for specific elements, which are stored in the busValueDict. These callbacks can be triggered upon receiving MIDI messages
    getBusValue { |elementKey, callbackFunc|
        this.busValueDict.put(elementKey, callbackFunc);
    }

    // Method that looks up and executes the callback function associated with a given element key, passing the provided value to the function.
    triggerCallback { |elementKey, busValue|
        var midiValue = this.busValueDict.at(elementKey);
        if (midiValue.notNil) {
            midiValue.value(busValue);
        }
    }   
    
    // Utility method to retrieve a control bus for a specific element key.
    cb { |elementKey|
        ^this.elementDict.at(elementKey).controlBus;
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

    // Sets initial value of the element, sets the element control bus and sends OSC message with that initial value.
    setElementValue { |elementKey, initValue|
        var element = this.elementDict.at(elementKey);
        element.initValue = initValue;
        element.setBusValueToInitValue;
        element.sendInitValuetoOSC;
    }

    // Sets the low, high, initial and velocity factor value of the element, sets the element control bus and sends OSC message with that initial value.
    setEncoderValues { |elementKey, lowValue, initValue, highValue, velocityFactor = 10|
        var element = this.elementDict.at(elementKey);
        element.lowValue = lowValue;
        element.initValue = initValue;
        element.highValue = highValue;
        element.velocityFactor = velocityFactor;
        element.sendInitValuetoOSC;
    }

    // Sends OSC messages for specified element keys, using the provided OSC path and value.
    sendtoOSClabel1 { |args|
        args.keysValuesDo({ |elementKey, value|
            var element = this.elementDict.at(elementKey);
            element.sendtoOSClabel1(value);
        });
    }
}