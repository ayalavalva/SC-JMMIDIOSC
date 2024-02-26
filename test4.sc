JIntechControllers {
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
            var elementKey = "%_PO%".format(this.shortName, elementNumber).asSymbol;
            var element = JElementPotentiometer.new(this.shortName, this.deviceNumb, elementNumber, this.midiChannel, msbCC, lsbCC);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.fullName ++ (if (this.shortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Potentiometer" + elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + msbCC + "lsbCC" + lsbCC).postln;
        };

        encCount.do { |i|
            var elementNumber = i + 1;
            var cc = potCount + this.startCC + i;
            var elementKey = "%_EN%".format(this.shortName, elementNumber).asSymbol;
            var element = JElementEncoder.new(this.shortName, this.deviceNumb, elementNumber, this.midiChannel, cc);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.fullName ++ (if (this.shortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Encoder" + elementNumber + "MIDI Channel" + this.midiChannel + "CC" + cc).postln;
        };

        fadCount.do { |i|
            var elementNumber = i + 1;
            var msbCC = potCount + this.startCC + i;
            var lsbCC = msbCC + this.fadCount + this.potCount + this.butCount;
            var elementKey = "%_FA%".format(this.shortName, elementNumber).asSymbol;
            var element = JElementPotentiometer.new(this.shortName, this.deviceNumb, elementNumber, this.midiChannel, msbCC, lsbCC);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.fullName ++ (if (this.shortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Fader" + elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + msbCC + "lsbCC" + lsbCC).postln;
        };

        butCount.do { |i|
            var elementNumber = i + 1;
            var cc = potCount + fadCount + encCount + this.startCC + i;
            var elementKey = "%_BU%".format(this.shortName, elementNumber).asSymbol;
            var element = JElementButton.new(this.shortName, this.deviceNumb, elementNumber, this.midiChannel, cc);
            this.controlBusDict.put(elementKey, element.controlBus);
            (this.fullName ++ (if (this.shortName == "PBF4") {" (%)".format(this.deviceNumb)} {""}) + "Button" + elementNumber + "MIDI Channel" + this.midiChannel + "CC" + cc).postln;
        };
    }

    controlBus { |key| 
        ^this.controlBusDict[key]; 
    }

    size { 
        ^this.controlBusDict.size; 
    }

    free {
        deviceNumb = deviceNumb - 1;
        this.controlBusDict.do { |key, value|
            value.free;
        };
    }
}

JIntechPO16 : JIntechControllers {
    var <>startCC;

    *new { |midiChannel=0, startCC=0, oscServAddr="127.0.0.1", oscServPort=9000|
        ^super.new.init("Intech Studio PO16", "PO16", midiChannel, oscServAddr, oscServPort).initPO16(startCC)
    }

    initPO16 { |startCC|
        this.startCC = startCC;
        this.potCount = 16;
        this.buildElementsDict;
    }
}

JIntechEN16 : JIntechControllers {
    var <>startCC;

    *new { |midiChannel=0, startCC=32, oscServAddr="127.0.0.1", oscServPort=9000|
        ^super.new.init("Intech Studio EN16", "EN16", midiChannel, oscServAddr, oscServPort).initEN16(startCC)
    }

    initEN16 { |startCC|
        this.startCC = startCC;
        this.encCount = 16;
        this.butCount = 16;
        this.buildElementsDict;
    }
}

JIntechPBF4 : JIntechControllers {
    classvar <>classDeviceNumb = 0;
    var <>startCC;
    var <>deviceNumb;

    *new { |midiChannel=0, startCC=64, oscServAddr="127.0.0.1", oscServPort=9000|
        this.classDeviceNumb = this.classDeviceNumb + 1;
        ^super.new.init("Intech Studio PBF4", "PBF4", midiChannel, oscServAddr, oscServPort).initPO16(startCC)
    }

    initPO16 { |startCC|
        this.deviceNumb = classDeviceNumb;
        this.startCC = startCC;
        this.potCount = 4;
        this.fadCount = 4;
        this.butCount = 4;
        this.buildElementsDict;
    }
}

JMIDIElements {
    var <>name, <>elementNumber, <>midiChannel;

    *new { |name, deviceNumb, elementNumber, midiChannel|
        ^super.new(name, deviceNumb, elementNumber, midiChannel)
    }
}

JElementPotentiometer {
    var <>name, <>elementNumber, <>midiChannel, <>msbCC, <>lsbCC;
    var <>msbCCValue, <>lsbCCValue;
    var <>msbUpdated = false, <>lsbUpdated = false;
    var <>controlBus;

    var <>deviceNumb;

    *new { |name, deviceNumb, elementNumber, midiChannel, msbCC, lsbCC|
        ^super.new.init(name, deviceNumb, elementNumber, midiChannel, msbCC, lsbCC)
    }

    init { |name, deviceNumb, elementNumber, midiChannel, msbCC, lsbCC|
        this.name = name;
        this.deviceNumb = deviceNumb;
        this.elementNumber = elementNumber;
        this.midiChannel = midiChannel;
        this.msbCC = msbCC;
        this.lsbCC = lsbCC;
        this.controlBus = Bus.control(Server.default, 1);

        this.midi14bitReceivers;
    }

    midi14bitReceivers {
        MIDIdef.cc("%_Pot%_msb".format(this.name, this.elementNumber), { |val|
            this.msbCCValue = val;
            this.msbUpdated = true;
            this.mappedCombinedMIDIValuetoControlBus;
        }, ccNum: this.msbCC, chan: this.midiChannel);

        MIDIdef.cc("%_Pot%_lsb".format(this.name, this.elementNumber), { |val|
            this.lsbCCValue = val;
            this.lsbUpdated = true;
            this.mappedCombinedMIDIValuetoControlBus;
        }, ccNum: this.lsbCC, chan: this.midiChannel);
    }

    mappedCombinedMIDIValuetoControlBus {
        if (this.msbUpdated && this.lsbUpdated) {
            var combinedMappedValue = ((this.msbCCValue << 7) + this.lsbCCValue).linlin(0, 16383, 0, 1);
            this.controlBus.set(combinedMappedValue);
            (this.name ++ (if (this.name == "Intech Studio PBF4") {" " ++ (this.deviceNumb)} {""}) + "Potentiometer" + this.elementNumber + "MIDI Channel" + this.midiChannel + "msbCC" + this.msbCC + "lsbCC" + this.lsbCC ++ ":" + combinedMappedValue).postln;
            // ~sendToTouchOSC.value("/po16/po" ++ (elementNumber).asString, combinedValue.linlin(0, 16383, 0, 1));
            this.msbUpdated = false;
            this.lsbUpdated = false;
        };
    }
}

JElementEncoder {
    var <>name, <>elementNumber, <>midiChannel, <>cc;
    var <>ccValue;
    var <>controlBus; // Removed busVarName, using controlBus directly

    var <>deviceNumb;

    *new { |name, deviceNumb, elementNumber, midiChannel, cc|
        ^super.new.init(name, deviceNumb, elementNumber, midiChannel, cc)
    }

    init { |name, deviceNumb, elementNumber, midiChannel, cc|
        this.name = name;
        this.deviceNumb = deviceNumb;
        this.elementNumber = elementNumber;
        this.midiChannel = midiChannel;
        this.cc = cc;
        this.controlBus = Bus.control(Server.default, 1);

        this.midiReceiver;
    }

    midiReceiver {
        MIDIdef.cc("%_Enc%".format(this.name, this.elementNumber), { |val|
            this.ccValue = val;
            this.mappedMIDIValuetoControlBus;
        }, ccNum: this.cc, chan: this.midiChannel);
    }

    mappedMIDIValuetoControlBus {
        var mappedValue = (this.ccValue - 64).sign * ((this.ccValue - 64).abs.pow(3)).asInteger;
        this.controlBus.set(Lag.kr(mappedValue, 0.01));
        (this.name ++ (if (this.name == "Intech Studio PBF4") {" " ++ (this.deviceNumb)} {""}) + "Encoder" + this.elementNumber + "MIDI Channel" + this.midiChannel + "CC" + this.cc ++ ":" + mappedValue).postln;
    }
}

JElementButton {
    var <>name, <>elementNumber, <>midiChannel, <>cc;
    var <>ccValue;
    var <>controlBus; // Removed busVarName, using controlBus directly

    var <>deviceNumb;

    *new { |name, deviceNumb, elementNumber, midiChannel, cc|
        ^super.new.init(name, deviceNumb, elementNumber, midiChannel, cc)
    }

    init { |name, deviceNumb, elementNumber, midiChannel, cc|
        this.name = name;
        this.deviceNumb = deviceNumb;
        this.elementNumber = elementNumber;
        this.midiChannel = midiChannel;
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
        this.controlBus.set(Lag.kr(mappedValue, 0.01));
        (this.name ++ (if (this.name == "Intech Studio PBF4") {" " ++ (this.deviceNumb)} {""}) + "Button" + this.elementNumber + "MIDI Channel" + this.midiChannel + "CC" + this.cc ++ ":" + mappedValue).postln;
    }
}

JOSCManager {
    var <>oscServAddr, <>oscServPort;
    var <>oscAddr;
    var <>oscPath;

    *new {|oscServAddr, oscServPort|
        ^super.new.init(oscServAddr, oscServPort)
    }

    init { |oscServAddr, oscServPort|
        oscServAddr = this.oscServAddr;
        oscServPort = this.oscServPort;
        this.oscAddr = NetAddr.new(oscServAddr, oscServPort);
    }

    sendToOSC { |oscPath, args|
        oscAddr.sendMsg(oscPath, *args);
    }
}