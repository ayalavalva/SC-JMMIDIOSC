JRIntechControllers {
    var <>name, <>midiChannel, <>oscServAddr, <>oscServPort;

    *new { |name, midiChannel, oscServAddr, oscServPort|
        ^super.new.init(name, midiChannel, oscServAddr, oscServPort)
    }

    init { |name, midiChannel, oscServAddr, oscServPort|
        this.name = name;
        this.midiChannel = midiChannel;
        this.oscServAddr = oscServAddr;
        this.oscServPort = oscServPort;
    }
}

JRIntechPO16 : JMMIntechControllers {
    var <>startmsbCC, <>lsbCCoffset;
    var <>controlBusDict; // Dictionary to hold control buses

    *new { |midiChannel=0, startmsbCC=0, lsbCCoffset=16, oscServAddr="127.0.0.1", oscServPort=9000|
        ^super.new.init(name, midiChannel, oscServAddr, oscServPort).initPO16(startmsbCC, lsbCCoffset)
    }

    initPO16 { |startmsbCC, lsbCCoffset|
        this.name="Intech Studio PO16";
        this.startmsbCC = startmsbCC;
        this.lsbCCoffset = lsbCCoffset;
        this.controlBusDict = Dictionary.new(n: 16);
        this.elementsDictionary;
    }

    elementsDictionary {
        16.do { |i|
            var elementNumber = i + 1;
            var msbCC = this.startmsbCC + i;
            var lsbCC = msbCC + this.lsbCCoffset;
            var elementKey = "PO16_PO%".format(elementNumber); // Key for the dictionary
            var element = JRElementPotentiometer.new(this.name, elementNumber, this.midiChannel, msbCC, lsbCC);

            this.controlBusDict.put(elementKey, element.controlBus);

            // Print diagnostic information for each potentiometer
            ("Intech Studio PO16 Potentiometer" + elementNumber + "MIDI Channel" + this.midiChannel + "MSBCC" + msbCC + "LSBCC" + lsbCC).postln;
        };
    }

    // Method to access control bus by key
    controlBus { |key|
        ^this.controlBusDict[key];
    }

    size {
        ^this.controlBusDict.size;
    }
}

JRIntechEN16 : JMMIntechControllers {
    var <>startCC;
    var <>controlBusDict; // Dictionary to hold control buses

    *new { |midiChannel=0, startCC=32, oscServAddr="127.0.0.1", oscServPort=9000|
        ^super.new.init(name, midiChannel, oscServAddr, oscServPort).initEN16(startCC)
    }

    initEN16 { |startCC|
        this.name="Intech Studio EN16";
        this.startCC = startCC;
        this.controlBusDict = Dictionary.new(n: 16);
        this.elementsDictionary;
    }

    elementsDictionary {
        var encoderNumber = 16;
        var buttonNumber = 16;
        
        encoderNumber.do { |i|
            var elementNumber = i + 1;
            var cc = this.startCC + i;
            var elementKey = "EN16_EN%".format(elementNumber); // Key for the dictionary
            var element = JRElementEncoder.new(this.name, elementNumber, this.midiChannel, cc);

            this.controlBusDict.put(elementKey, element.controlBus);

            // Print diagnostic information for each potentiometer
            ("Intech Studio EN16 Potentiometer" + elementNumber + "MIDI Channel" + this.midiChannel + "CC" + cc).postln;
        };

        buttonNumber.do { |i|
            var elementNumber = i + 1;
            var cc = encoderNumber + this.startCC + i;
            var elementKey = "EN16_BU%".format(elementNumber); // Key for the dictionary
            var element = JRElementButton.new(this.name, elementNumber, this.midiChannel, cc);

            this.controlBusDict.put(elementKey, element.controlBus);

            // Print diagnostic information for each potentiometer
            ("Intech Studio EN16 Button" + elementNumber + "MIDI Channel" + this.midiChannel + "CC" + cc).postln;
        };
    }

    // Method to access control bus by key
    controlBus { |key|
        ^this.controlBusDict[key];
    }

    size {
        ^this.controlBusDict.size;
    }
}



JRMIDIElements {
    var <>name, <>elementNumber, <>midiChannel;

    *new { |name, elementNumber, midiChannel|
        ^super.new(name, elementNumber, midiChannel)
    }
}

JRElementPotentiometer {
    var <>name, <>elementNumber, <>midiChannel, <>msbCC, <>lsbCC;
    var <>msbCCValue, <>lsbCCValue;
    var <>msbUpdated = false, <>lsbUpdated = false;
    var <>controlBus; // Removed busVarName, using controlBus directly

    *new { |name, elementNumber, midiChannel, msbCC, lsbCC|
        ^super.new.init(name, elementNumber, midiChannel, msbCC, lsbCC)
    }

    init { |name, elementNumber, midiChannel, msbCC, lsbCC|
        this.name = name;
        this.elementNumber = elementNumber;
        this.midiChannel = midiChannel;
        this.msbCC = msbCC;
        this.lsbCC = lsbCC;
        this.controlBus = Bus.control(Server.default, 1); // Control bus is now directly accessible

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
            this.controlBus.set(combinedMappedValue); // Send a smoothen combined value to the control bus
            "% Potentiometer % MIDI channel % msbCC % lsbCC %: %".format(this.name, this.elementNumber, this.midiChannel, this.msbCC, this.lsbCC, combinedMappedValue).postln;
            // ~sendToTouchOSC.value("/po16/po" ++ (elementNumber).asString, combinedValue.linlin(0, 16383, 0, 1));
            this.msbUpdated = false;
            this.lsbUpdated = false;
        };
    }
}

JRElementEncoder {
    var <>name, <>elementNumber, <>midiChannel, <>cc;
    var <>ccValue;
    var <>controlBus; // Removed busVarName, using controlBus directly

    *new { |name, elementNumber, midiChannel, cc|
        ^super.new.init(name, elementNumber, midiChannel, cc)
    }

    init { |name, elementNumber, midiChannel, cc|
        this.name = name;
        this.elementNumber = elementNumber;
        this.midiChannel = midiChannel;
        this.cc = cc;
        this.controlBus = Bus.control(Server.default, 1); // Control bus is now directly accessible

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
        "% Encoder % MIDI channel % CC %: %".format(this.name, this.elementNumber, this.midiChannel, this.cc, mappedValue).postln
    }
}

JRElementButton {
    var <>name, <>elementNumber, <>midiChannel, <>cc;
    var <>ccValue;
    var <>controlBus; // Removed busVarName, using controlBus directly

    *new { |name, elementNumber, midiChannel, cc|
        ^super.new.init(name, elementNumber, midiChannel, cc)
    }

    init { |name, elementNumber, midiChannel, cc|
        this.name = name;
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
        "% Button % MIDI channel % CC %: %".format(this.name, this.elementNumber, this.midiChannel, this.cc, mappedValue).postln
    }
}

JROSCManager {
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