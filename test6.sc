JOIntechControllers {
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
            var element = JOElementPotentiometer.new(this.fullName, this.deviceNumb, elementNumber, this.midiChannel, msbCC, lsbCC);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.fullName ++ (if (this.shortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Potentiometer" + elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + msbCC + "lsbCC" + lsbCC).postln;
        };

        encCount.do { |i|
            var elementNumber = i + 1;
            var cc = potCount + this.startCC + i;
            var elementKey = "EN%".format(elementNumber).asSymbol;
            var element = JOElementEncoder.new(this.fullName, this.deviceNumb, elementNumber, this.midiChannel, cc);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.fullName ++ (if (this.shortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Encoder" + elementNumber + "MIDI Channel" + this.midiChannel + "CC" + cc).postln;
        };

        fadCount.do { |i|
            var elementNumber = i + 1;
            var msbCC = potCount + this.startCC + i;
            var lsbCC = msbCC + this.fadCount + this.potCount + this.butCount;
            var elementKey = "FA%".format(elementNumber).asSymbol;
            var element = JOElementFader.new(this.fullName, this.deviceNumb, elementNumber, this.midiChannel, msbCC, lsbCC);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.fullName ++ (if (this.shortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Fader" + elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + msbCC + "lsbCC" + lsbCC).postln;
        };

        butCount.do { |i|
            var elementNumber = i + 1;
            var cc = potCount + fadCount + encCount + this.startCC + i;
            var elementKey = "BU%".format(elementNumber).asSymbol;
            var element = JOElementButton.new(this.fullName, this.deviceNumb, elementNumber, this.midiChannel, cc);
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
        JOOSCManager.getSharedInstance.send(oscPath, this.controlBusDict.at(key));
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

JOIntechPO16 : JOIntechControllers {
    var <>startCC;

    *new { |midiChannel=0, startCC=0, oscServAddr="127.0.0.1", oscServPort=9000|
        ^super.new.init("Intech Studio PO16", "PO16", midiChannel, oscServAddr, oscServPort).initPO16(startCC)
    }

    initPO16 { |startCC|
        this.startCC = startCC;
        this.potCount = 16;
        super.buildElementsDict;
    }
}

JOIntechEN16 : JOIntechControllers {
    var <>startCC;

    *new { |midiChannel=0, startCC=32, oscServAddr="127.0.0.1", oscServPort=9000|
        ^super.new.init("Intech Studio EN16", "EN16", midiChannel, oscServAddr, oscServPort).initEN16(startCC)
    }

    initEN16 { |startCC|
        this.startCC = startCC;
        this.encCount = 16;
        this.butCount = 16;
        super.buildElementsDict;
    }
}

JOIntechPBF4 : JOIntechControllers {
    classvar <>classDeviceNumb = 0;
    var <>startCC;
    var <>deviceNumb;

    *new { |midiChannel=0, startCC=64, oscServAddr="127.0.0.1", oscServPort=9000|
        this.classDeviceNumb = this.classDeviceNumb + 1;
        ^super.new.init("Intech Studio PBF4", "PBF4", midiChannel, oscServAddr, oscServPort).initPBF4(startCC)
    }

    initPBF4 { |startCC|
        this.deviceNumb = classDeviceNumb;
        this.startCC = startCC;
        this.potCount = 4;
        this.fadCount = 4;
        this.butCount = 4;
        super.buildElementsDict;
    }
}

JOMIDIElements {
    var <>name, <>deviceNumb, <>elementNumber, <>midiChannel;

    *new { |name, deviceNumb, elementNumber, midiChannel|
        ^super.new(name, deviceNumb, elementNumber, midiChannel)
    }

    init { |name, deviceNumb, elementNumber, midiChannel|
        this.name = name;
        this.deviceNumb = deviceNumb;
        this.elementNumber = elementNumber;
        this.midiChannel = midiChannel;
    }
}

JOElementPotentiometer : JOMIDIElements {
    var <>msbCC, <>lsbCC;
    var <>msbCCValue, <>lsbCCValue;
    var <>msbUpdated = false, <>lsbUpdated = false;
    var <>controlBus;

    *new { |name, deviceNumb, elementNumber, midiChannel, msbCC, lsbCC|
        ^super.new.init(name, deviceNumb, elementNumber, midiChannel).initPotentiometer(msbCC, lsbCC)
    }

    initPotentiometer { |msbCC, lsbCC|
        this.msbCC = msbCC;
        this.lsbCC = lsbCC;
        this.controlBus = Bus.control(Server.default, 1);

        this.midi14bitReceivers;
    }

    midi14bitReceivers {
        MIDIdef.cc("%_PO%_msb".format(this.name, this.elementNumber), { |val|
            this.msbCCValue = val;
            this.msbUpdated = true;
            this.mappedCombinedMIDIValuetoControlBus;
        }, ccNum: this.msbCC, chan: this.midiChannel);

        MIDIdef.cc("%_PO%_lsb".format(this.name, this.elementNumber), { |val|
            this.lsbCCValue = val;
            this.lsbUpdated = true;
            this.mappedCombinedMIDIValuetoControlBus;
        }, ccNum: this.lsbCC, chan: this.midiChannel);
    }

    mappedCombinedMIDIValuetoControlBus {
        if (this.msbUpdated && this.lsbUpdated) {
            var combinedMappedValue = ((this.msbCCValue << 7) + this.lsbCCValue).linlin(0, 16383, 0, 1);
            this.controlBus.set(combinedMappedValue);
            (this.name ++ (if (this.name == "Intech Studio PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + "Potentiometer" + this.elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + this.msbCC + "lsbCC" + this.lsbCC ++ ":" + combinedMappedValue).postln;
            // ~sendToTouchOSC.value("/po16/po" ++ (elementNumber).asString, combinedValue.linlin(0, 16383, 0, 1));
            this.msbUpdated = false;
            this.lsbUpdated = false;
        };
    }
}

JOElementFader : JOMIDIElements {
    var <>msbCC, <>lsbCC;
    var <>msbCCValue, <>lsbCCValue;
    var <>msbUpdated = false, <>lsbUpdated = false;
    var <>controlBus;

    *new { |name, deviceNumb, elementNumber, midiChannel, msbCC, lsbCC|
        ^super.new.init(name, deviceNumb, elementNumber, midiChannel).initFader(msbCC, lsbCC)
    }

    initFader { |msbCC, lsbCC|
        this.msbCC = msbCC;
        this.lsbCC = lsbCC;
        this.controlBus = Bus.control(Server.default, 1);

        this.midi14bitReceivers;
    }

    midi14bitReceivers {
        MIDIdef.cc("%_FA%_msb".format(this.name, this.elementNumber), { |val|
            this.msbCCValue = val;
            this.msbUpdated = true;
            this.mappedCombinedMIDIValuetoControlBus;
        }, ccNum: this.msbCC, chan: this.midiChannel);

        MIDIdef.cc("%_FA%_lsb".format(this.name, this.elementNumber), { |val|
            this.lsbCCValue = val;
            this.lsbUpdated = true;
            this.mappedCombinedMIDIValuetoControlBus;
        }, ccNum: this.lsbCC, chan: this.midiChannel);
    }

    mappedCombinedMIDIValuetoControlBus {
        if (this.msbUpdated && this.lsbUpdated) {
            var combinedMappedValue = ((this.msbCCValue << 7) + this.lsbCCValue).linlin(0, 16383, 0, 1);
            this.controlBus.set(combinedMappedValue);
            (this.name ++ (if (this.name == "Intech Studio PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + "Fader" + this.elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + this.msbCC + "lsbCC" + this.lsbCC ++ ":" + combinedMappedValue).postln;
            // ~sendToTouchOSC.value("/po16/po" ++ (elementNumber).asString, combinedValue.linlin(0, 16383, 0, 1));
            // JOOSCManager.getSharedInstance.send("/pbf4_1/fa%".format(this.elementNumber).asString, combinedMappedValue);
            this.msbUpdated = false;
            this.lsbUpdated = false;
        };
    }
}

JOElementEncoder : JOMIDIElements {
    var <>cc;
    var <>ccValue;
    var <>controlBus;

    *new { |name, deviceNumb, elementNumber, midiChannel, cc|
        ^super.new.init(name, deviceNumb, elementNumber, midiChannel).initEncoder(cc)
    }

    initEncoder { |cc|
        this.cc = cc;
        this.controlBus = Bus.control(Server.default, 1);
        this.midiReceiver;
    }

    midiReceiver {
        MIDIdef.cc("%_EN%".format(this.name, this.elementNumber), { |val|
            this.ccValue = val;
            this.mappedMIDIValuetoControlBus;
        }, ccNum: this.cc, chan: this.midiChannel);
    }

    mappedMIDIValuetoControlBus {
        var mappedValue = (this.ccValue - 64).sign * ((this.ccValue - 64).abs.pow(3)).asInteger;
        this.controlBus.set(mappedValue);
        (this.name ++ (if (this.name == "Intech Studio PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + "Encoder" + this.elementNumber + "MIDI Channel" + this.midiChannel + "CC" + this.cc ++ ":" + mappedValue).postln;
    }
}

JOElementButton : JOMIDIElements {
    var <>cc;
    var <>ccValue;
    var <>controlBus;

    *new { |name, deviceNumb, elementNumber, midiChannel, cc|
        ^super.new.init(name, deviceNumb, elementNumber, midiChannel).initButton(cc)
    }

    initButton { |cc|
        this.cc = cc;
        this.controlBus = Bus.control(Server.default, 1); // Control bus is now directly accessible

        this.midiReceiver;
    }

    midiReceiver {
        MIDIdef.cc("%_But%".format(this.name, this.elementNumber), { |val|
            this.ccValue = val;
            this.mappedMIDIValuetoControlBus;
        }, ccNum: this.cc, chan: this.midiChannel);
    }

    mappedMIDIValuetoControlBus {
        var mappedValue = this.ccValue.linlin(0, 127, 0, 1);
        this.controlBus.set(mappedValue);
        (this.name ++ (if (this.name == "Intech Studio PBF4") {" (" ++ this.deviceNumb ++ ")"} {""}) + "Button" + this.elementNumber + "MIDI Channel" + this.midiChannel + "CC" + this.cc ++ ":" + mappedValue).postln;
    }
}

JOOSCManager {
    var <>oscServAddr, <>oscServPort;
    var <>oscAddr;
    // Declare sharedInstance as a class variable
    classvar <sharedInstance;

    *new { |oscServAddr, oscServPort|
        ^super.new.init(oscServAddr, oscServPort);
    }

    init { |oscServAddr, oscServPort|
        this.oscServAddr = oscServAddr;
        this.oscServPort = oscServPort;
        this.oscAddr = NetAddr.new(oscServAddr, oscServPort);
    }

    // Class method to get or create the shared instance
    *getSharedInstance { |oscServAddr = "127.0.0.1", oscServPort = 9000|
        if(sharedInstance.isNil, {
            sharedInstance = this.new(oscServAddr, oscServPort);
        });
        ^sharedInstance;
    }

    send { |oscPath, args|
    "Sending OSC Message: %, Args: %".format(oscPath, args).postln;
        this.oscAddr.sendMsg(oscPath, *args);
    }
}

/*
JOOSCManager {
    var <>oscServAddr, <>oscServPort;
    var <>oscAddr;

    *new {|oscServAddr, oscServPort|
        ^super.new.init(oscServAddr, oscServPort)
    }

    init { |oscServAddr, oscServPort|
        this.oscServAddr = oscServAddr;
        this.oscServPort = oscServPort;
        this.oscAddr = NetAddr.new(oscServAddr, oscServPort);
    }

    send { |oscPath, args|
        this.oscAddr.sendMsg(oscPath, *args);
    }
}