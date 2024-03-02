JMIntechControllers {
    var <>deviceFullName, <>deviceShortName, <>midiChannel, <>oscServAddr, <>oscServPort;
    var <>potCount = 0, <>encCount = 0, <>fadCount = 0, <>butCount = 0;
    var <>controlBusDict;
    var <>elementDict;
    var <>midiValueDict;
    var <>deviceNumb;

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

        this.controlBusDict = IdentityDictionary.new(n: potCount + encCount + fadCount + butCount);
        this.elementDict = IdentityDictionary.new(n: potCount + encCount + fadCount + butCount);
        this.midiValueDict = IdentityDictionary.new(n: potCount + encCount + fadCount + butCount);
    }
    
    initializeMIDIElements {
        
        potCount.do { |i|
            var elementNumber = i + 1;
            var msbCC = this.startCC + i;
            var lsbCC = msbCC + this.potCount + this.fadCount + this.butCount;
            var elementKey = "PO%".format(elementNumber).asSymbol;
            var element = JMElementPotentiometer.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, elementNumber, this.midiChannel, msbCC, lsbCC);
            this.controlBusDict.put(elementKey, element.controlBus);
            this.elementDict.put(elementKey, element);
            (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Potentiometer" + elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + msbCC + "lsbCC" + lsbCC).postln;
        };

        encCount.do { |i|
            var elementNumber = i + 1;
            var cc = potCount + this.startCC + i;
            var elementKey = "EN%".format(elementNumber).asSymbol;
            var element = JMElementEncoder.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, elementNumber, this.midiChannel, cc);
            this.controlBusDict.put(elementKey, element.controlBus);
            this.elementDict.put(elementKey, element);
            (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Encoder" + elementNumber + "MIDI Channel" + this.midiChannel + "CC" + cc).postln;
        };

        fadCount.do { |i|
            var elementNumber = i + 1;
            var msbCC = potCount + this.startCC + i;
            var lsbCC = msbCC + this.fadCount + this.potCount + this.butCount;
            var elementKey = "FA%".format(elementNumber).asSymbol;
            var element = JMElementFader.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, elementNumber, this.midiChannel, msbCC, lsbCC);
            this.controlBusDict.put(elementKey, element.controlBus);
            this.elementDict.put(elementKey, element);
            (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Fader" + elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + msbCC + "lsbCC" + lsbCC).postln;
        };

        butCount.do { |i|
            var elementNumber = i + 1;
            var cc = potCount + fadCount + encCount + this.startCC + i;
            var elementKey = "BU%".format(elementNumber).asSymbol;
            var element = JMElementButton.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, elementNumber, this.midiChannel, cc);
            this.controlBusDict.put(elementKey, element.controlBus);
            this.elementDict.put(elementKey, element);
            (this.deviceFullName ++ (if (this.deviceShortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Button" + elementNumber + "MIDI Channel" + this.midiChannel + "CC" + cc).postln;
        };
    }

    // Method to register a callback for a specific element
    getMIDIValue { |elementKey, callbackFunc|
        this.midiValueDict.put(elementKey, callbackFunc);
    }

     // Method to trigger a callback for a specific element
     triggerCallback { |elementKey, value|
        var midiValue = this.midiValueDict.at(elementKey);
        if (midiValue.notNil) {
            midiValue.value(value);
        }
    }   

    cb { |key|
        ^this.controlBusDict.at(key);
    }

    inCB { |key|
        ^In.kr(this.cb(key), 1);
    }

    lagInCB { |key, lag = 0.3|
        ^Lag.kr(this.inCB(key), lag); 
    }

    midiOSC { |elementKeys, enableFlag = true|
        elementKeys.do { |key|
            var element = this.elementDict.at(key);
            element.oscSendEnabled = enableFlag;
        };
    }

    free {
        deviceNumb = deviceNumb - 1;
        this.controlBusDict.do { |key, value|
            value.free;
        };
    }
}