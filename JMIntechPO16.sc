JMIntechPO16 : JMIntechControllers {
    classvar <>classDeviceNumb = 0;
    var <>startCC;
    var <>deviceNumb;

    *new { |midiChannel=0, startCC=0, oscServAddr="127.0.0.1", oscServPort=9000|
        this.classDeviceNumb = this.classDeviceNumb + 1;
        ^super.new.init("Intech Studio PO16", "PO16", midiChannel, oscServAddr, oscServPort).initPO16(startCC)
    }

    initPO16 { |startCC|
        this.deviceNumb = classDeviceNumb;
        this.startCC = startCC;
        this.potCount = 16;
        super.buildElementsDict;
    }
}