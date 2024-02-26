JMIntechPO16 : JMIntechControllers {
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