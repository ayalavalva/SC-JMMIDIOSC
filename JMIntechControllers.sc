JMIntechControllers {
    var <>fullName, <>shortName, <>midiChannel, <>oscServAddr, <>oscServPort;
    var <>potCount = 0, <>encCount = 0, <>fadCount = 0, <>butCount = 0;
    var <>controlBusDict;
    var <>deviceNumb;

    *new { |fullName, shortName, midiChannel, oscServAddr, oscServPort|
        ^super.new.init(fullName, shortName, midiChannel, oscServAddr, oscServPort)
    }

    init { |fullName, shortName, midiChannel, oscServAddr, oscServPort|
        this.fullName = fullName;
        this.shortName = shortName;
        this.deviceNumb = deviceNumb;
        this.midiChannel = midiChannel;
        this.oscServAddr = oscServAddr;
        this.oscServPort = oscServPort
    }
    
    buildElementsDict {
        
        this.controlBusDict = IdentityDictionary.new(n: potCount + encCount + fadCount + butCount);

        potCount.do { |i|
            var elementNumber = i + 1;
            var msbCC = this.startCC + i;
            var lsbCC = msbCC + this.potCount + this.fadCount + this.butCount;
            var elementKey = "PO%".format(elementNumber).asSymbol;
            var element = JMElementPotentiometer.new(this.fullName, this.deviceNumb, elementNumber, this.midiChannel, msbCC, lsbCC);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.fullName ++ (if (this.shortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Potentiometer" + elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + msbCC + "lsbCC" + lsbCC).postln;
        };

        encCount.do { |i|
            var elementNumber = i + 1;
            var cc = potCount + this.startCC + i;
            var elementKey = "EN%".format(elementNumber).asSymbol;
            var element = JMElementEncoder.new(this.fullName, this.deviceNumb, elementNumber, this.midiChannel, cc);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.fullName ++ (if (this.shortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Encoder" + elementNumber + "MIDI Channel" + this.midiChannel + "CC" + cc).postln;
        };

        fadCount.do { |i|
            var elementNumber = i + 1;
            var msbCC = potCount + this.startCC + i;
            var lsbCC = msbCC + this.fadCount + this.potCount + this.butCount;
            var elementKey = "FA%".format(elementNumber).asSymbol;
            var element = JMElementFader.new(this.fullName, this.deviceNumb, elementNumber, this.midiChannel, msbCC, lsbCC);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.fullName ++ (if (this.shortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Fader" + elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + msbCC + "lsbCC" + lsbCC).postln;
        };

        butCount.do { |i|
            var elementNumber = i + 1;
            var cc = potCount + fadCount + encCount + this.startCC + i;
            var elementKey = "BU%".format(elementNumber).asSymbol;
            var element = JMElementButton.new(this.fullName, this.deviceNumb, elementNumber, this.midiChannel, cc);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.fullName ++ (if (this.shortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Button" + elementNumber + "MIDI Channel" + this.midiChannel + "CC" + cc).postln;
        };
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

    sendBusOSC { |oscPath, key|
        JMOSCManager.getSharedInstance.send(oscPath, this.controlBusDict.at(key));
    }

    dictSize {
        ^this.controlBusDict.size; 
    }

    free {
        deviceNumb = deviceNumb - 1;
        this.controlBusDict.do { |key, value|
            value.free;
        };
    }
}