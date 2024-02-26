JMIntechEN16 : JIntechControllers {
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